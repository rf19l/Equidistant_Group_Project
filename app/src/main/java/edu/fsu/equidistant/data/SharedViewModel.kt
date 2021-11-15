package edu.fsu.equidistant.data

import androidx.lifecycle.ViewModel
import java.util.*

class SharedViewModel : ViewModel() {
    var meetingID: UUID = UUID.randomUUID()
}