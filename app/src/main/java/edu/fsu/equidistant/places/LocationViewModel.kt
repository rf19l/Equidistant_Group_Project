package edu.fsu.equidistant.places

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LocationViewModel : ViewModel() {

    fun getNearbyPlace(url: String) = getPlaces(url)

    private fun getPlaces(url: String) = flow<State<Any>> {

        emit(State.Loading(true))

        val response = RetrofitClient.retrofitApi.getNearbyPlaces(url)
        Log.d(TAG,"getPlaces: $response")

        if (response.body()?.googlePlaceModelList?.size!! > 0) {
            emit(State.success(response.body()!!))
        } else {
            emit(State.failed(response.body()!!.error!!))
        }

    }.catch {
//        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)
}