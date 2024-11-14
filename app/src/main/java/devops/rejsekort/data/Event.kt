package devops.rejsekort.data

import android.location.Location

data class Event(
    val eventType: EventType,
    val user: UserData,
    val timestamp: Int, //Unix timestamp easiest
    val location: Location
)