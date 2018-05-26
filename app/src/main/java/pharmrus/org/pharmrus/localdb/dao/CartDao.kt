package pharmrus.org.pharmrus.localdb.dao

import android.app.Application
import android.arch.persistence.room.*
import io.reactivex.Observable
import android.arch.persistence.room.Room
import pharmrus.org.pharmrus.Product
import pharmrus.org.pharmrus.localdb.entity.*
import java.sql.Timestamp
import java.util.*
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration



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

@Database(entities = [Cart::class, CartItem::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CartDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}

class CartRepository (private val application: Application) {

    companion object {
        private var _db:CartDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE cart_item ADD COLUMN price REAL NOT NULL default 0")
                database.execSQL("ALTER TABLE cart_item ADD COLUMN on_stock INTEGER NOT NULL default 0")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE cart_item ADD COLUMN producer TEXT")
            }
        }

        fun getDatabaseInstance (application: Application):CartDatabase {
            if (_db == null) {
                _db = Room
                        .databaseBuilder(application.applicationContext, CartDatabase::class.java, "cart_db")
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .build()
            }

            return _db!!;
        }
    }

    private val db = getDatabaseInstance(application)
    private val dao = db.cartDao()

    @Transaction
    fun getCountInCart(cartId:Long, productId: String):Double = dao.cartItemByCartAndDrug(cartId, productId)?.countInCart ?: 0.0

    @Transaction
    fun cartItems() = dao.currentCart?.itemList

    @Transaction
    private fun upsertItem (cartItem: CartItem): CartItem {
        val foundItem = dao.cartItemByCartAndDrug(cartItem.cartId, cartItem.id)
        foundItem?.let {
            val newItem = cartItem.copy(countInCart = cartItem.countInCart + foundItem.countInCart)
            dao.updateItem(newItem)
            return newItem
        } ?: kotlin.run {
            dao.insertItem(cartItem)
            return cartItem
        }
    }

    @Transaction
    fun addToCart (cartItemToAdd: CartItem): Observable<CartView?> = Observable.fromCallable {
        var cartId = dao.currentCart?.cart?.id
        if (cartId == null) {
            // Create new cart
            val cart = Cart(id = null, dateCreated = Timestamp(Calendar.getInstance().time.time), orderNo = null, status = CartStatus.NEW)
            cartId = dao.insert(cart)
        }

        upsertItem(cartItemToAdd.copy(cartId = cartId, countInCart = 1.0))
        dao.currentCart
    }

    @Transaction
    fun addToCart (product: Product): Observable<CartView?> {
        val cartItem = CartItem(
                id = product.id,
                drugsId = product.drugsId,
                drugsFullName = product.drugsFullName,
                producerShortName = product.producerShortName,
                countInCart = 1.0,
                cartId = -1,
                price = product.retailPrice,
                availableOnStock = if (product.ost > 0) 1 else 0)

        return addToCart(cartItem)
    }

    @Transaction
    fun removeFromCart (productId: String) = Observable.fromCallable {
        var currentCart = dao.currentCart
        currentCart?.let {
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