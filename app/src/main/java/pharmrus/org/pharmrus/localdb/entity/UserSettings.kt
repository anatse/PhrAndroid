package pharmrus.org.pharmrus.localdb.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "user_info")
data class UserSettings (
    @ColumnInfo(name = "uuid")
    @PrimaryKey
    var uuid: String,

    @ColumnInfo(name = "mail")
    var mail: String? = null,

    @ColumnInfo(name = "phone")
    var phone: String? = null,

    @ColumnInfo(name = "contact")
    var contact: String? = null
)