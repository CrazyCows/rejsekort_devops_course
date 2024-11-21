package devops.rejsekort.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.URLProtocol
import io.ktor.http.path

class CheckInOutRepository (
    val endpoint: String,


) {
    suspend fun sendCheckIn(location: Location) {
        val jsonLocation = serialize(location)
        val status = HttpClient(Android).use { client ->
            client.post() {
                url {
                    protocol = URLProtocol.HTTPS
                    host = endpoint
                    path("docs/welcome.html")
                    setBody(jsonLocation)
                }
            }
        }

    }
    suspend fun getCheckInStatus() : Boolean {
        val httpClient: HttpClient = HttpClient(Android)
        val response: HttpResponse = httpClient.get(endpoint)

        Log.i("get check in status", response.status.description)
        httpClient.close()
        return false
    }
    private fun serializeEvent(event: Location): String {
        val json =  "{\"Location\":[\"latitude\": " + event.latitude +
                "\",\"longitude\": \"" + event.longitude + "]}"

        return json
    }
}


