package edu.fsu.equidistant.fragments

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.fsu.equidistant.R
import edu.fsu.equidistant.data.MeetingAdapter
import edu.fsu.equidistant.data.SharedViewModel
import edu.fsu.equidistant.data.User
import edu.fsu.equidistant.databinding.FragmentMeetingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.CountDownLatch

class MeetingFragment : Fragment(R.layout.fragment_meeting) {

    private val viewModel: SharedViewModel by activityViewModels()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var meetingAdapter: MeetingAdapter
    private lateinit var centerLocation: Location

    private val storage = Firebase.storage
    val storageRef = storage.reference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMeetingBinding.bind(view)

        val usersList: MutableList<User> = mutableListOf()
        meetingAdapter = MeetingAdapter(usersList)

        binding.apply {
            meetingRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
            }

            textViewMeetingId.text = viewModel.meetingID

            buttonStartMeeting.setOnClickListener {
                centerLocation = getCenterPoint(usersList)
                Log.d(TAG, "CenterPoint: $centerLocation")
                val action = MeetingFragmentDirections
                    .actionMeetingFragmentToMapFragment(centerLocation)
                findNavController().navigate(action)
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
                        usersList.clear()
                        if (users != null) {
                            CoroutineScope(Dispatchers.Main).launch {
                            val list: ArrayList<Map<String, Any>> =
                                users as ArrayList<Map<String, Any>>

                                for (user in list) {
                                    var picture: Bitmap? = null
                                    CoroutineScope(Dispatchers.IO).launch{
                                        picture=fetchFirebasePicture(Uri.parse(user["imageUri"].toString()),user["uid"].toString())
                                    }.join()
                                    val userInMeeting = User(
                                        user["username"].toString(),
                                        user["email"].toString(),
                                        user["token"].toString(),
                                        user["longitude"] as Double,
                                        user["latitude"] as Double,
                                        "none",
                                        picture
                                    )
                                    if(usersList.size<list.size) {
                                        usersList.add(userInMeeting)
                                    }
                                }
                                binding.meetingRecyclerView.adapter = meetingAdapter
                            }
                        }

                    }
                }
            }
    }


    private fun getCenterPoint(usersList: MutableList<User>): Location {
        var x = 0.0
        var y = 0.0
        var z = 0.0
        for (user in usersList) {
            Log.d(
                TAG,
                "getCenterPoint: user longitude = ${user.longitude}, latitude = ${user.latitude}"
            )
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
        Log.d(
            TAG,
            "getCenterPoint: longitude = ${centerpoint.longitude}, latitude = ${centerpoint.latitude}"
        )
        return centerpoint
    }

    private suspend fun fetchFirebasePicture(imageUri: Uri, uid:String):Bitmap? = withContext(Dispatchers.IO){
        var bmp: Bitmap? = null
        val done = CountDownLatch(1)
        if (imageUri == null || imageUri.toString().isEmpty()|| imageUri.toString() == "null"){
            done.countDown()
            return@withContext bmp
        }
        val imageRef = storageRef.child("users/"+ uid + "/" +imageUri)
        val ONE_MB : Long = 1024 * 1024
        val task = imageRef.getBytes(ONE_MB)
            .addOnSuccessListener {
                bmp = BitmapFactory.decodeByteArray(it,0,it.size)
                Log.d("TAG","Fetched profile picture")


            }.
            addOnFailureListener{
                Log.d("TAG","Failed to fetch profile picture")
            }
            .addOnCompleteListener{
                done.countDown()
            }
        try{
            done.await()
        }catch(e:InterruptedException){
            Log.d("TAG",e.message.toString())
        }

        bmp= bmp?.let { resizeBitmap(it,250) }
        return@withContext bmp

        //  return@withContext BitmapFactory.decodeByteArray(task.getResult(),0,task.getResult().size)
    }

    private fun resizeBitmap(source:Bitmap,maxLength:Int):Bitmap{
        try {
            if(source.height != maxLength || source.width != maxLength){
                val result = Bitmap.createScaledBitmap(source, maxLength,maxLength,false)
                return result
            }
            return source
        } catch (e: Exception) {
            return source
        }
    }




}