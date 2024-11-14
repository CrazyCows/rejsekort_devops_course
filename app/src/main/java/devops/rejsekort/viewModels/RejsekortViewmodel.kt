package devops.rejsekort.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import devops.rejsekort.data.Event
import devops.rejsekort.data.EventType
import devops.rejsekort.data.UserData

class RejsekortViewmodel : ViewModel() {

    private lateinit var lastLocation: Location
    private var _checkedIn = mutableStateOf(isCheckedIn())
    val checkedIn: State<Boolean> = _checkedIn
    private val _userData = mutableStateOf(
        UserData(
            firstName = "FN placeholder",
            lastName = "LN placeholder"
            //TODO: Do we remove the checked in from the user data? Do we remove it entirely?
        )
    )
    val userData: State<UserData> = _userData


    private fun isCheckedIn(): Boolean {
        //TODO: Ask back end. This is useful if the app is restarted; We could also make it save on the phone but that seems pointless
        return false
    }

    fun handleCheckInOut(context: Context) {
        if (checkFineLocationAccess(context)) {

            checkInOut(context)
        } else { //Error message handling
            if (checkCoarseLocationAccess(context)) {
                Log.e("", "Fine location access is required for the app to function")
                Toast.makeText(
                    context,
                    "Fine location access is required for the app to function",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.e("", "Location access is required for the app to function")
                Toast.makeText(
                    context,
                    "Location access is required for the app to function",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun checkInOut(context: Context) {
        try {
            getFineLocation(context)
            _checkedIn.value = !_checkedIn.value
        } catch (e: Exception) { //TODO: Handle different exceptions differently from Data Layer
            Toast.makeText(context, "Failed to perform action", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getFineLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation //Don't think it is within current logic beyond being an inherent race condition
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    lastLocation = loc
                    sendEventToBackend()
                    Toast.makeText(
                        context,
                        (if (isCheckedIn()) "Checked out at: " else "Checked out at: ") + lastLocation.latitude + ", " + lastLocation.longitude,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                throw exception
            }
    }

    fun checkFineLocationAccess(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED
    }

    fun checkCoarseLocationAccess(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PERMISSION_GRANTED
    }

    private fun sendEventToBackend() {
        Log.e("RejsekortViewmodel", "Implement model/data layer please")
        val event = Event(
            eventType = EventType.CHECK_IN,
            user = userData.value,
            timestamp = (System.currentTimeMillis() / 1000).toInt(),
            location = lastLocation
        )

        //dataLayerFunctionOrAPICall(event)
        //TODO: IMPLEMENT DATA LAYER? Or just send here for simplicity
    }
}