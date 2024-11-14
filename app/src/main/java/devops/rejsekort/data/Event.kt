package devops.rejsekort.data

import android.location.Location
import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("eventType") val eventType: EventType,
    @SerializedName("user") val user: String,
    @SerializedName("timeStamp") val timestamp: Int, //Unix timestamp easiest
    @SerializedName("location") val location: Location
)