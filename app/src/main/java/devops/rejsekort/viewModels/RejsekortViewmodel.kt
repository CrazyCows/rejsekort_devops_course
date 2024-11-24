package devops.rejsekort.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import devops.rejsekort.data.CheckInOutRepository
import devops.rejsekort.data.UserData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID


class RejsekortViewmodel: ViewModel() {
    private lateinit var lastLocation: android.location.Location
    private val repo = CheckInOutRepository()
    private val _userData = MutableStateFlow(
        UserData()
    )
    val userData = _userData.asStateFlow()


    private fun isCheckedIn() {
        CoroutineScope(Dispatchers.IO).launch {
            val newCheckInStatus = repo.getCheckInStatus(userData.value)
            _userData.update { c ->
                c.copy(
                    isCheckedIn = newCheckInStatus
                )
            }
        }
    }

    suspend fun handleSignIn(
        context: Context,
        navigation: () -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .setServerClientId("260729048541-br4tr166p0fsj1mhenohfhis2h5870r0.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken
            val backendIdToken = repo.authorizeToken(googleIdToken)
            if (backendIdToken != null) {
                navigation()
                isCheckedIn()
                setUserData(
                    firstName = googleIdTokenCredential.givenName,
                    lastName = googleIdTokenCredential.familyName,
                    userToken = backendIdToken
                )
            }else {
                Toast.makeText(
                    context,
                    "Failed to authenticate",
                    Toast.LENGTH_SHORT
                ).show()
            }


        } catch (e: GetCredentialException) {
            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
            Log.e("GetCredentialException", e.message!!)
        } catch (e: GoogleIdTokenParsingException) {
            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
            Log.e("GoogleIdTokenParsingException", e.message!!)
        }
    }


    private fun setUserData(firstName: String?, lastName: String? ,userToken: String) {
        _userData.update { c ->
            c.copy(
                firstName = firstName,
                lastName = lastName,
                token = userToken,
            )
        }
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

    fun userCheckInOut(context: Context){
        //handleCheckInOut(context)//We have already checked for permissions, so skip this
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
            Toast.makeText(
                context,"Unable to connect to server",Toast.LENGTH_SHORT
            ).show()
            throwable.printStackTrace()
        }
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            sendEventToBackend(context)
        }
    }

    private fun checkInOut(context: Context) {
        try {
            getFineLocation(context)
        } catch (e: Exception) { //TODO: Handle different exceptions differently from Data Layer
            Toast.makeText(context, "Failed to perform action", Toast.LENGTH_SHORT).show()
        }
        Log.i("CheckInOut", "CheckInOut done")
    }

    @SuppressLint("MissingPermission")
    private fun getFineLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation //Don't think it is within current logic beyond being an inherent race condition
            .addOnSuccessListener { loc ->
                Log.i("SUCCESS", loc.toString())
                if (loc != null) {
                    lastLocation = loc
                    Toast.makeText(
                        context,
                        (if (userData.value.isCheckedIn) "Checked out at: " else "Checked out at: ")
                                + lastLocation.latitude + ", " + lastLocation.longitude,
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

    @SuppressLint("MissingPermission")
    suspend fun sendEventToBackend(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = Tasks.await(fusedLocationClient.lastLocation)
        val loc = devops.rejsekort.data.Location(
            latitude = location.latitude,
            longitude = location.longitude,
        )
        val success = repo.sendEvent(userData.value, loc)
        if (success) {
            isCheckedIn()
        }
    }
}