package devops.rejsekort.viewModels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID


class RejsekortViewmodel: ViewModel() {
    private lateinit var lastLocation: devops.rejsekort.data.Location
    private val repo = CheckInOutRepository()

    val userData2 = mutableStateOf(UserData())


    private fun updateCheckedIn() {
        CoroutineScope(Dispatchers.IO).launch {
            val newCheckInStatus = repo.getCheckInStatus(userData2.value)
            userData2.value = userData2.value.copy(isCheckedIn = newCheckInStatus)
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
                updateCheckedIn()
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
        userData2.value = userData2.value.copy(firstName = firstName, lastName = lastName, token = userToken)
    }

    fun handleCheckInOut(context: Context) {
        if (checkFineLocationAccess(context)) {
            Log.i("handleCheckInOut", "I also need to run once per click with perimissions")
            val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
                Toast.makeText(
                    context,"Unable to connect to server",Toast.LENGTH_SHORT
                ).show()
                throwable.printStackTrace()
            }
            CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                 val result = sendEventToBackend(context)
                withContext(Dispatchers.Main){
                    if (result) { //I should not be legally allowed to concatenate strings like this
                        Toast.makeText(context, "Checked " + if(userData2.value.isCheckedIn) {"out"} else{"in"} + " at: " + lastLocation.latitude + ", " + lastLocation.longitude, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            explainThatLocationIsNeeded(context)
        }
    }

    private fun explainThatLocationIsNeeded(context: Context){  //Error message handling
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

    fun checkFineLocationAccess(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED
    }

    private fun checkCoarseLocationAccess(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PERMISSION_GRANTED
    }

    private suspend fun sendEventToBackend(context: Context) : Boolean{
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try{
            val location = Tasks.await(fusedLocationClient.lastLocation)

            val loc = devops.rejsekort.data.Location(
                latitude = location.latitude,
                longitude = location.longitude,
            )
            lastLocation = loc
            val success = repo.sendEvent(userData2.value, lastLocation)
            if (success) {
                updateCheckedIn()
                return true
            }
        } catch(e: SecurityException){
            //TO-DO: Implement this properly. Possibly remove the try catch and use the suggestion that is given
            Log.e("Error", "User didnt give permission.")
            explainThatLocationIsNeeded(context)
        }
        return false
    }
}