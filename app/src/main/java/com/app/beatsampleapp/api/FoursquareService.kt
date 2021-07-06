import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service endpoints for interacting with the Foursquare API
 */
interface FoursquareService {
    companion object {
        private const val CLIENT_ID = "K0DAWTLPFX0J3A5L0J31VWEL5IE5ODOLZQBVZ04BFTK1I5D4"
        private const val CLIENT_SECRET = "D2GIYHLONH53W4UZMWYP3YCVLUGOCMEMCIDEN2JWVA4ANABJ"
        private const val VERSION = "20210703"
        private const val COMMON_PARAMS =
            "&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&v=$VERSION"
    }

    @GET("/v2/venues/search?limit=15&radius=1000$COMMON_PARAMS")
    fun getLocationResults(
        @Query("categoryId") query: String,
        @Query("ll") latlng: String
    ): Call<FoursquareResponse>
}