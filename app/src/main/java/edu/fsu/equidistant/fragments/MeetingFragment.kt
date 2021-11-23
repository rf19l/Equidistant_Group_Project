package edu.fsu.equidistant.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import edu.fsu.equidistant.R
import edu.fsu.equidistant.data.MeetingAdapter
import edu.fsu.equidistant.data.SharedViewModel
import edu.fsu.equidistant.data.User
import edu.fsu.equidistant.databinding.FragmentMeetingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MeetingFragment : Fragment(R.layout.fragment_meeting) {

    private val viewModel: SharedViewModel by activityViewModels()
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
                                    user["token"].toString()
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
}