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
        firstName = "First name placeholder",
        lastName = "Last name placeholder"
        //TODO: Do we remove the checked in from the user data?
        )
    )
    val userData: State<UserData> = _userData

    private val _permissionHandled = mutableStateOf(false)
    val permissionHandled: State<Boolean> = _permissionHandled //Only keeps track of whether permission is handled in the current session
    private var _checkedIn = mutableStateOf(false)
    val checkedIn: State<Boolean> = _checkedIn

    fun onPermissionHandled(){
        _permissionHandled.value = true
    }
    
    fun handleCheckInOut(context: Context){
        if (permissionHandled.value){ //This prevents the initial run from throwing an exception (Because launchedeffect always runs at app start)
            if(checkFineLocationAccess(context)){
                CheckInOut(context)
            } else {
                //TODO: HANDLE
                throw Exception("GIVE ME ACCESSSSSS")
            }
        } else {
            //Intentionally left blank for being explicit. We do nothing here because its the first call
        }

    }

    private fun CheckInOut(context: Context){
        _checkedIn.value = !_checkedIn.value
        getFineLocation(context)
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
                // TODO: Handle location retrieval failure? I don't know if this ever happens. Still works if flight mode is on (Why does gps still work in flight mode?)
                exception.printStackTrace()
                throw exception
            }

    }

    fun checkFineLocationAccess(context: Context) : Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }

    private fun sendLocationToBackend(){
        Log.w("RejsekortViewmodel", "Implement model/data layer please")
        //TODO: IMPLEMENT DATA LAYER? Or just send here for simplicity
    }
}