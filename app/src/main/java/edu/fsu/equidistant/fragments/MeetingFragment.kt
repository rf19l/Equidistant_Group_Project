package edu.fsu.equidistant.fragments

import android.content.ContentValues.TAG
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import edu.fsu.equidistant.R
import edu.fsu.equidistant.data.MeetingAdapter
import edu.fsu.equidistant.data.SharedViewModel
import edu.fsu.equidistant.data.User
import edu.fsu.equidistant.databinding.FragmentMeetingBinding
import edu.fsu.equidistant.places.GooglePlaceModel
import edu.fsu.equidistant.places.GoogleResponseModel
import edu.fsu.equidistant.places.LocationViewModel
import edu.fsu.equidistant.places.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.*
import kotlin.collections.ArrayList
import okhttp3.Request.Builder
import java.io.IOException

class MeetingFragment : Fragment(R.layout.fragment_meeting) {

    private val viewModel: SharedViewModel by activityViewModels()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var meetingAdapter: MeetingAdapter
    private lateinit var centerLocation: Location
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var googlePlaceList: ArrayList<GooglePlaceModel>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMeetingBinding.bind(view)

        val usersList: MutableList<User> = mutableListOf()
        meetingAdapter = MeetingAdapter(usersList)
        googlePlaceList = ArrayList()

        binding.apply {
            meetingRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
            }

            textViewMeetingId.text = viewModel.meetingID

            buttonStartMeeting.setOnClickListener {
                centerLocation = getCenterPoint(usersList)
                Log.d(TAG, "CenterPoint: $centerLocation")

//                CoroutineScope(Dispatchers.IO).launch {
//                    getPlaces()
//                }

                getNearbyPlace()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            getUsersList(meetingAdapter, binding, usersList)
        }

        val arguments = arguments
        if (arguments != null) {
            Log.d(TAG, "MeetingArguments: ${arguments.getString("meetingID").toString()}")
            viewModel.meetingID = arguments.getString("meetingID").toString()
            getUsersList(meetingAdapter, binding, usersList)
            binding.textViewMeetingId.text = viewModel.meetingID
        }
    }

    private fun getUsersList(
        meetingAdapter: MeetingAdapter,
        binding: FragmentMeetingBinding,
        usersList: MutableList<User>
    ) {

        database.collection("meetings")
            .document(viewModel.meetingID.toString())
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (document != null) {
                    if (document.exists()) {
                        val users = document.get("users")

                        if (users != null) {
                            val list: ArrayList<Map<String, Any>> =
                                users as ArrayList<Map<String, Any>>

                            usersList.clear()

                            for (user in list) {
                                val userInMeeting = User(
                                    user["username"].toString(),
                                    user["email"].toString(),
                                    user["token"].toString(),
                                    user["longitude"] as Double,
                                    user["latitude"] as Double
                                )

                                usersList.add(userInMeeting)
                            }

                        } else {
                            Log.d(TAG, "users array is null")
                        }
                    }
                }

                binding.meetingRecyclerView.adapter = meetingAdapter
            }
    }


    private fun getCenterPoint(usersList: MutableList<User>): Location {
        var x = 0.0
        var y = 0.0
        var z = 0.0
        for (user in usersList) {
            Log.d(TAG, "getCenterPoint: user longitude = ${user.longitude}, latitude = ${user.latitude}")
            val latitude = user.latitude * Math.PI / 180
            val longitude = user.longitude * Math.PI / 180
            val x1 = Math.cos(latitude) * Math.cos(longitude)
            val y1 = Math.cos(latitude) * Math.sin(longitude)
            val z1 = Math.sin(latitude)
            x += x1
            y += y1
            z += z1
        }
        x /= usersList.size.toDouble()
        y /= usersList.size.toDouble()
        z /= usersList.size.toDouble()
        val centerhyp = Math.sqrt(x * x + y * y)
        val centerlat = Math.atan2(z, centerhyp)
        val centerlon = Math.atan2(y, x)
        val centerpoint = Location("")
        centerpoint.latitude = centerlat * 180 / Math.PI
        centerpoint.longitude = centerlon * 180 / Math.PI
        Log.d(TAG, "getCenterPoint: longitude = ${centerpoint.longitude}, latitude = ${centerpoint.latitude}")
        return centerpoint
    }

    private fun getPlaces() {
        val client: OkHttpClient = OkHttpClient().newBuilder()
            .build()
        val request: Request = Builder()
            .url("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522%2C151.1957362&radius=1500&type=restaurant&keyword=cruise&key=AIzaSyCUNlT9fS_FgParpYQXGkIO_LMdX7jvEHA")
            .method("GET", null)
            .build()
        try {
            val response: Response = client.newCall(request).execute()
            Log.d(TAG, "Response: $response")
        } catch(e: IOException) {
            Log.d(TAG, e.toString())
        }
    }

    private fun getNearbyPlace() {
        val url = ("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + centerLocation.latitude + "," + centerLocation.longitude
                + "&radius=7500&type=restaurant&key=AIzaSyCUNlT9fS_FgParpYQXGkIO_LMdX7jvEHA")

        lifecycleScope.launchWhenStarted {
            locationViewModel.getNearbyPlace(url).collect {
                when (it) {
                    is State.Loading -> {
                        if (it.flag == true) {

                        }
                    }

                    is State.Success -> {
                        val googleResponseModel: GoogleResponseModel =
                            it.data as GoogleResponseModel

                        if (googleResponseModel.googlePlaceModelList != null &&
                                googleResponseModel.googlePlaceModelList.isNotEmpty()) {
                            googlePlaceList.clear()

                            for (i in googleResponseModel.googlePlaceModelList.indices) {
                                googlePlaceList.add(googleResponseModel.googlePlaceModelList[i])
                            }

                            Log.d(TAG, "googlePlaceList array: $googlePlaceList")
                        }
                    }

                    is State.Failed -> {

                    }
                }
            }
        }
    }

}