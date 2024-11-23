package devops.rejsekort.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import io.ktor.http.path

class CheckInOutRepository (
    private val endpoint: String = "10.0.2.2:5001",


) {
    suspend fun sendEvent(userData: UserData, location: Location): Boolean {
        val jsonLocation = serializeEvent(location)
        val pathEnd = {if (userData.isCheckedIn) "SignOutOnLocation" else "SignInOnLocation"}
        val status = HttpClient(Android).use { client ->
            client.post {
                url {
                    protocol = URLProtocol.HTTP
                    host = endpoint
                    path("Location/" + pathEnd)
                    bearerAuth(userData.token)
                    setBody(jsonLocation)
                }
            }
        }
        if (status.status.isSuccess()) {
            Log.i("REPO", status.toString())
            return true
        }
        Log.i("REPO", status.toString())
        return false
    }
    suspend fun getCheckInStatus(userData: UserData) : Boolean {
        val status: HttpResponse = HttpClient(Android).get {
            url {
                protocol = URLProtocol.HTTP
                host = endpoint
                path("/Location/LocationSignedIn")
                bearerAuth(userData.token)
            }
        }
        if (status.status.isSuccess()) {
            //TODO: Confirm Data format of response
            return status.bodyAsBytes().toString().contains("true")
        }
        return false
    }
    private fun serializeEvent(loc: Location): String {
        val json =  "{\"Location\":[\"latitude\": " + loc.latitude +
                "\",\"longitude\": \"" + loc.longitude + "]}"
        return json
    }
}


