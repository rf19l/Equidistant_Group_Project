package edu.fsu.equidistant.data

import androidx.lifecycle.ViewModel
import java.util.*

class SharedViewModel: ViewModel() {
    var meeting: UUID = UUID.randomUUID()
    var meetingID: String = meeting.toString()
}