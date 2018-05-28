package pharmrus.org.pharmrus

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

data class Product(
    @SerializedName ("id")
    val id: String,
    @SerializedName ("drugsId")
    val drugsId: String,
    @SerializedName ("drugsFullName")
    val drugsFullName: String,
    @SerializedName ("drugsShortName")
    val drugsShortName: String?,
    @SerializedName ("MNN")
    val mnn: String?,
    @SerializedName ("barCode")
    val barCode: String,
    @SerializedName ("producerShortName")
    val producerShortName: String?,
    @SerializedName ("retailPrice")
    val retailPrice: Double,
    @SerializedName ("ost")
    val ost: Double,
    @SerializedName ("drugImage")
    val drugImage: String?
)

data class SearchRow (
    @SerializedName ("countInCart")
    val countInCart: Double,
    @SerializedName ("dp")
    val product: Product
)

data class SearchResult (
    @SerializedName ("hasMore")
    val hasMore: Boolean,
    @SerializedName ("offset")
    val offset: Int,
    @SerializedName ("pageSize")
    val pageSize: Int,
    @SerializedName ("rows")
    val rows: List<SearchRow>
)

data class SearchRequest (
    @SerializedName ("hasImage")
    val hasImage: Int,
    @SerializedName ("offset")
    val offset: Int,
    @SerializedName ("pageSize")
    val pageSize: Int,
    @SerializedName ("text")
    val text: String,
    @SerializedName("sorts")
    val sorts: List<String>?
)

data class RemoteCartItem (
    @SerializedName ("drugId")
    val drugId: String,
    @SerializedName ("drugName")
    val drugName: String,
    @SerializedName ("num")
    val num: Int,
    @SerializedName ("price")
    val price: Double,
    @SerializedName ("availableOnStock")
    val availableOnStock: Int,
    @SerializedName ("producer")
    val producer: String?
)

data class RemoteCartRequest (
    @SerializedName ("userUuid")
    val userUuid: String,
    @SerializedName ("userName")
    val userName: String,
    @SerializedName ("userPhone")
    val userPhone: String,
    @SerializedName ("userMail")
    val userMail: String?,
    @SerializedName ("comment")
    val comment: String?,
    @SerializedName ("items")
    val items: List<RemoteCartItem>
)

data class RemoteCartResponse (
    @SerializedName ("orderNo")
    val orderNo: String
)

interface PharmrusServer {
    @Headers(
        "Accept: application/json",
        "User-Agent: Pharmrus Android App",
        "Content-Language: ru"
    )
    @POST("drugs/fuzzySearch")
    fun search(@Body request: SearchRequest): Observable<SearchResult>

    @Headers(
        "Accept: application/json",
        "User-Agent: Pharmrus Android App",
        "Content-Language: ru"
    )
    @POST("drugs/remoteCart")
    fun sendCard(@Body request: RemoteCartRequest): Observable<RemoteCartResponse>
}

object PharmrusService {
    var retrofit = Retrofit.Builder()
            .baseUrl("http://www.pharmrus24.ru")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    var service = retrofit.create<PharmrusServer>(PharmrusServer::class.java)
}