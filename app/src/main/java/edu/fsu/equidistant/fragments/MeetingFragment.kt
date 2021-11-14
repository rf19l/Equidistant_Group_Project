package edu.fsu.equidistant.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import edu.fsu.equidistant.R
import edu.fsu.equidistant.data.SharedViewModel
import edu.fsu.equidistant.databinding.FragmentMeetingBinding

class MeetingFragment : Fragment(R.layout.fragment_meeting) {

    private val viewModel: SharedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMeetingBinding.bind(view)

        binding.apply {
            textViewMeetingId.text = viewModel.meetingID.toString()
        }
    }
}