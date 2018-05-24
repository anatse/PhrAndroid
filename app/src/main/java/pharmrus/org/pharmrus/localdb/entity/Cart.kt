package pharmrus.org.pharmrus.localdb.entity

import android.arch.persistence.room.*
import java.sql.Timestamp
import java.util.*

object CartStatus {
    val NEW:Int = 1
    val SENT:Int = 2
    val APPROVED:Int = 3
    val PAYED:Int = 4
    val FINISHED:Int = 5
    val REJECTED:Int = 6
    val CANCELLED:Int = 7
}

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromLong (value:Long?):Timestamp? = if (value == null) null else Timestamp(value)

    @TypeConverter
    @JvmStatic
    fun fromTimestamp (value:Timestamp?):Long? = if (value == null) null else value.time
}

@Entity(tableName = "cart")
data class Cart (
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long?,

    @ColumnInfo(name = "orderNo", collate = ColumnInfo.UNICODE)
    var orderNo: String?,

    @ColumnInfo(name = "dateCreate")
    var dateCreated: Timestamp,

    @ColumnInfo(name = "status")
    var status: Int
)

class CartView  {
    @Embedded
    var cart = Cart(id = null, dateCreated = Timestamp(Calendar.getInstance().time.time), orderNo = null, status = CartStatus.NEW)

    @Relation(parentColumn = "id", entityColumn = "cart_id", entity = CartItem::class)
    var itemList: List<CartItem> = listOf()
}

@Entity(tableName = "cart_item",
        //foreignKeys = [ForeignKey(entity = CartItem::class, parentColumns = ["id"], childColumns = ["cart_id"])],
        indices = [Index("cart_id"), Index("id"), Index("drugs_id")],
        primaryKeys = ["cart_id", "id"]
)
data class CartItem (
    @ColumnInfo(name="cart_id")
    val cartId: Long,

    @ColumnInfo(name="id")
    val id: String,

    @ColumnInfo(name="drugs_id")
    val drugsId: String?,

    @ColumnInfo(name="drugs_full_name")
    val drugsFullName: String,

    @ColumnInfo(name="count_in_cart")
    val countInCart: Double
)
