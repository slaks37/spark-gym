package com.sparkgym.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Open Food Facts backs the barcode scanner and the online food search.
 *
 * It is an open database rather than a licensed commercial one, so results are
 * best-effort — anything it returns is saved locally as a normal food row that
 * the user can correct.
 */
interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}.json")
    suspend fun product(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = FIELDS
    ): OffProductResponse

    @GET("cgi/search.pl?search_simple=1&action=process&json=1&page_size=25")
    suspend fun search(
        @Query("search_terms") terms: String,
        @Query("fields") fields: String = FIELDS
    ): OffSearchResponse

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/"
        private const val FIELDS = "code,product_name,brands,nutriments,serving_size,serving_quantity"

        fun create(userAgent: String): OpenFoodFactsApi {
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    // Open Food Facts asks every client to identify itself.
                    chain.proceed(chain.request().newBuilder().header("User-Agent", userAgent).build())
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(OpenFoodFactsApi::class.java)
        }
    }
}

@Serializable
data class OffProductResponse(
    val status: Int = 0,
    val product: OffProduct? = null
)

@Serializable
data class OffSearchResponse(
    val count: Int = 0,
    val products: List<OffProduct> = emptyList()
)

@Serializable
data class OffProduct(
    val code: String = "",
    @SerialName("product_name") val productName: String = "",
    val brands: String = "",
    @SerialName("serving_size") val servingSize: String = "",
    @SerialName("serving_quantity") val servingQuantity: Double? = null,
    val nutriments: OffNutriments = OffNutriments()
) {
    val isUsable: Boolean get() = productName.isNotBlank() && nutriments.energyKcal100g > 0
}

@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double = 0.0,
    @SerialName("proteins_100g") val proteins100g: Double = 0.0,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double = 0.0,
    @SerialName("fat_100g") val fat100g: Double = 0.0,
    @SerialName("fiber_100g") val fiber100g: Double = 0.0,
    @SerialName("sugars_100g") val sugars100g: Double = 0.0,
    @SerialName("sodium_100g") val sodium100g: Double = 0.0
)
