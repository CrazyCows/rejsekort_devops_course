package devops.rejsekort.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import io.ktor.http.path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckInOutRepository (
    private val endpoint: String = "https://jsonplaceholder.typicode.com"/*"10.0.2.2:5432"*/,


) {
    fun sendEvent(userData: UserData, location: Location): Boolean {
        val jsonLocation = serializeEvent(location)
        var response = false
        CoroutineScope(Dispatchers.Main).launch {
            val status = HttpClient(Android).use { client ->
                client.post {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = endpoint
                        path("location/SignIn")
                        headers{append("token", userData.token)}
                        setBody(jsonLocation)
                    }
                }
            }
            if (status.status.isSuccess()) {
                response = true
            }
            Log.e("BACKEND COMMUNICATION", status.status.description)
        }
        return response
    }
    fun getCheckInStatus() : Boolean {
        var response = false
        CoroutineScope(Dispatchers.Main).launch {
            val status = HttpClient(Android).use { client ->
                client.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = endpoint
                        path("/")
                        //headers{append("token", userData.token)}
                    }
                }
            }
            if (status.status.isSuccess()) {
                response = true
            }
            Log.e("BACKEND COMMUNICATION", status.status.description)
        }
        return response
    }
    private fun serializeEvent(event: Location): String {
        val json =  "{\"Location\":[\"latitude\": " + event.latitude +
                "\",\"longitude\": \"" + event.longitude + "]}"

        return json
    }
}


