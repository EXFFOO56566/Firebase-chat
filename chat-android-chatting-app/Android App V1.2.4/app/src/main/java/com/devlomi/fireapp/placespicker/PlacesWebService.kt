package com.devlomi.fireapp.activities.placespicker

import com.devlomi.fireapp.R
import com.devlomi.fireapp.activities.placespicker.model.PlacesResponse
import com.devlomi.fireapp.utils.MyApp
import com.devlomi.fireapp.utils.TimeHelper
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val CLIENT_ID = MyApp.context().getString(R.string.foursquare_client_id)
private val CLIENT_SECRET = MyApp.context().getString(R.string.foursquare_client_secret)
private const val BASE_URL = "https://api.foursquare.com/v2/venues/"


/*
https://api.foursquare.com/v2/venues/search?ll=40.7,-74
&client_id=BPIT0GTVL4I53DA4NTGIP0HTUGDOJ0DXDEBXO3T0Q0LQCLFX
&client_secret=RZJ10X0TMOHDJPGKREFKDHGBJ1RV0BIJ5243LYAQHQABCIUN
&v=20190401
 */
interface PlacesWebService {
    @GET("search/")
    fun getNearbyPlaces(
            @Query("ll") latLng: String
    ): Deferred<PlacesResponse>


    companion object {
        operator fun invoke(
        ): PlacesWebService {
            val requestInterceptor = Interceptor { chain ->

                val url = chain.request()
                        .url()
                        .newBuilder()
                        .addQueryParameter("client_id", CLIENT_ID)
                        .addQueryParameter("client_secret", CLIENT_SECRET)
                        .addQueryParameter("v", TimeHelper.getYYYYMMDD())
                        .build()
                val request = chain.request()
                        .newBuilder()
                        .url(url)
                        .build()

                return@Interceptor chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(requestInterceptor)
                    .build()

            return Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(PlacesWebService::class.java)
        }
    }
}