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
    val text: String
)

interface PharmrusServer {
    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Pharmrus Android App"
    )
    @POST("drugs/fuzzySearch")
    fun search(@Body request: SearchRequest): Observable<SearchResult>
}

object SearchService {
    var retrofit = Retrofit.Builder()
            .baseUrl("http://www.pharmrus24.ru")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    var service = retrofit.create<PharmrusServer>(PharmrusServer::class.java)
}