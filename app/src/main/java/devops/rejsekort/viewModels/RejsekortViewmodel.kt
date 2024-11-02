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
import devops.rejsekort.data.UserData

class RejsekortViewmodel : ViewModel() {

    private lateinit var location: Location

    private val _userData = mutableStateOf(UserData(
        firstName = "FN placeholder",
        lastName = "LN placeholder"
        //TODO: Do we remove the checked in from the user data?
        )
    )
    val userData: State<UserData> = _userData

    private var _checkedIn = mutableStateOf(isCheckedin())
    val checkedIn: State<Boolean> = _checkedIn


    private fun isCheckedin(): Boolean{
        //TODO: Ask back end. This is useful if the app is restarted; We could also make it save on the phone but that seems pointless
        return false
    }
    
    fun handleCheckInOut(context: Context){
        if(checkFineLocationAccess(context)){
            CheckInOut(context)
        } else {
            if(checkCoarseLocationAccess(context)){
                Log.e("","Fine location access is required for the app to function")
                Toast.makeText(context, "Fine location access is required for the app to function", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("","Location access is required for the app to function")
                Toast.makeText(context, "Location access is required for the app to function", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun CheckInOut(context: Context){
        try{
            getFineLocation(context)
            _checkedIn.value = !_checkedIn.value
        } catch (e: Exception){
            Toast.makeText(context, "Failed to perform action", Toast.LENGTH_SHORT).show()
        }


    }
    @SuppressLint("MissingPermission")
    private fun getFineLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation //TODO: Check if warning this is an issue; Don't think it is within current logic beyond being an inherent race condition
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    location = loc
                    val toast = Toast.makeText(context, "Location is: " + location.latitude + ", " + location.longitude, Toast.LENGTH_SHORT) //TODO: Should probably be changed
                    toast.show()
                    sendLocationToBackend()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                throw exception
            }

    }

    fun checkFineLocationAccess(context: Context) : Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }

    fun checkCoarseLocationAccess(context: Context) : Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
    }

    private fun sendLocationToBackend(){
        Log.w("RejsekortViewmodel", "Implement model/data layer please")
        //TODO: IMPLEMENT DATA LAYER? Or just send here for simplicity
    }
}