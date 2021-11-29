package edu.fsu.equidistant.places

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitApi {

    @GET
    suspend fun getNearbyPlaces(@Url url: String) : Response<GoogleResponseModel>

}