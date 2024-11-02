package devops.rejsekort.viewModels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices

class RejsekortViewmodel : ViewModel() {

    private lateinit var location: Location

    private val _permissionGranted = mutableStateOf(false)
    val permissionGranted: State<Boolean> = _permissionGranted
    private val _permissionHandled = mutableStateOf(false)
    val permissionHandled: State<Boolean> = _permissionHandled

    fun CheckInOut(context: Context){
        getFineLocation(context)


    }
    private fun getFineLocation(context: Context) {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation //TODO: Check if this is an issue; Don't think it is within current logic
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    location = loc
                    val toast = Toast.makeText(context, "Location is: " + location.latitude + ", " + location.longitude, Toast.LENGTH_SHORT) //TODO: Should probably be changed
                    toast.show()
                    //TODO: Do something with lat and long
                }
            }
            .addOnFailureListener { exception ->
                // TODO: Handle location retrieval failure
                exception.printStackTrace()
            }

    }

    fun hasFineLocationAccess(context: Context) : Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }


    private fun sendLocationToBackend(){
        //TODO: IMPLEMENT DATA LAYER? Or just send here for simplicity
    }
}