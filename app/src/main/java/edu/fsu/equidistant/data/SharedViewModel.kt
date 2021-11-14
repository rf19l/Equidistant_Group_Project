package edu.fsu.equidistant.data

import androidx.lifecycle.ViewModel
import java.util.*

class SharedViewModel : ViewModel() {
    val meetingID: UUID = UUID.randomUUID()
}