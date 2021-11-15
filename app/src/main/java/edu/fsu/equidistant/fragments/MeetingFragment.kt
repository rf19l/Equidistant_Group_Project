package edu.fsu.equidistant.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import edu.fsu.equidistant.R
import edu.fsu.equidistant.data.MeetingAdapter
import edu.fsu.equidistant.data.MeetingID
import edu.fsu.equidistant.data.SharedViewModel
import edu.fsu.equidistant.data.User
import edu.fsu.equidistant.databinding.FragmentMeetingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeetingFragment : Fragment(R.layout.fragment_meeting) {

    private val viewModel: SharedViewModel by viewModels()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var meetingAdapter: MeetingAdapter

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

            textViewMeetingUsers.text = viewModel.meetingID.toString()
        }

        CoroutineScope(Dispatchers.IO).launch {
            getUsersList(meetingAdapter, binding, usersList)
        }
    }

    private fun getUsersList(
        meetingAdapter: MeetingAdapter,
        binding: FragmentMeetingBinding,
        usersList: MutableList<User>
    ) {

        database.collection("meetings").document(MeetingID.meetingID.toString()).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot? = task.result
                    if (document?.exists() == true) {
                        val map: MutableMap<String, Any> = document.data as MutableMap<String, Any>
                        for (entry in map.entries) {
                            if (entry.key == "users") {
                                val list: ArrayList<Map<String, Any>> =
                                    entry.value as ArrayList<Map<String, Any>>

                                for (user in list) {
                                    val user = User(
                                        user["username"].toString(),
                                        user["email"].toString(),
                                        user["token"].toString(),
                                        user["longitude"] as Double,
                                        user["latitude"] as Double
                                    )

                                    usersList.add(user)
                                }
                            }
                        }
                    }

                    Log.d(TAG, usersList.toString())
                    binding.meetingRecyclerView.adapter = meetingAdapter
                }

            }
    }
}