package pharmrus.org.pharmrus.localdb.dao

import android.app.Application
import android.arch.persistence.room.*
import io.reactivex.Observable
import android.arch.persistence.room.Room
import pharmrus.org.pharmrus.Product
import pharmrus.org.pharmrus.localdb.entity.*
import java.sql.Timestamp
import java.util.*

@Dao
interface CartDao {
    /**
     * Get all carts (orders) wit status not in FINISHED, CANCELLED or REJECTED
     */
    @get:Query("SELECT * from cart WHERE status < 5 ORDER BY cart.dateCreate")
    val allOrders: List<CartView>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cart: Cart): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(cart: Cart): Unit

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(cartItem: CartItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateItem(cartItem: CartItem)

    @get:Query("SELECT * FROM cart WHERE status = 1")
    val currentCart: CartView?

    @Query("SELECT * FROM cart_item WHERE cart_id = :cartId AND id = :id")
    fun cartItemByCartAndDrug (cartId: Long, id: String): CartItem?

    @Query("SELECT * FROM cart WHERE cart.id = :cartId")
    fun cartById(cartId: String): Cart?

    @Query("DELETE FROM cart WHERE cart.id = :cartId")
    fun removeCartById(cartId: Long)

    @Query("DELETE FROM cart_item WHERE cart_id = :cartId and id = :id")
    fun removeItem (cartId: Long, id: String)
}

@Database(entities = [Cart::class, CartItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CartDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}

class CartRepository (private val application: Application) {
    private val db = Room.databaseBuilder(application.applicationContext, CartDatabase::class.java, "cart_db").fallbackToDestructiveMigration().build()
    private val dao = db.cartDao()

    @Transaction
    fun getCountInCart(productId: String):Double = dao.currentCart?.itemList?.find { it.id == productId }?.countInCart ?: 0.0

    @Transaction
    private fun upsertItem (cartItem: CartItem): CartItem {
        val foundItem = dao.cartItemByCartAndDrug(cartItem.cartId, cartItem.id)
        if (foundItem != null) {
            val newItem = cartItem.copy(countInCart = cartItem.countInCart + foundItem.countInCart)
            dao.updateItem(newItem)
            return newItem
        } else {
            dao.insertItem(cartItem)
            return cartItem
        }
    }

    @Transaction
    fun addToCart (product: Product) = Observable.fromCallable {
        var currentCart = dao.currentCart
        if (currentCart == null) {
            // Create new cart
            val cart = Cart(id = null, dateCreated = Timestamp(Calendar.getInstance().time.time), orderNo = null, status = CartStatus.NEW)
            val cartId = dao.insert(cart)
            val cartItem = CartItem(id = product.id, drugsId = product.drugsId, drugsFullName = product.drugsFullName, countInCart = 1.0, cartId = cartId)
            upsertItem (cartItem)
            dao.currentCart
        }
        else {
            val cartItem = CartItem(id = product.id, drugsId = product.drugsId, drugsFullName = product.drugsFullName, countInCart = 1.0, cartId = currentCart.cart.id!!)
            upsertItem (cartItem)
            dao.currentCart
        }
    }

    @Transaction
    fun removeFromCart (productId: String) = Observable.fromCallable {
        var currentCart = dao.currentCart
        if (currentCart != null) {
            val foundItem = dao.cartItemByCartAndDrug(currentCart.cart.id!!, productId)
            if (foundItem != null) {
                val cnt = foundItem.countInCart - 1
                if (cnt <= 0) {
                    dao.removeItem(currentCart.cart.id!!, productId)
                } else {
                    dao.updateItem(foundItem.copy(countInCart = cnt))
                }
            }
        }

        dao.currentCart
    }

    fun findAllOrders () =  Observable.fromCallable { db.cartDao().allOrders }
}