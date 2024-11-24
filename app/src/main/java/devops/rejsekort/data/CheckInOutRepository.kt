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
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import org.json.JSONObject

class CheckInOutRepository (
    private val endpoint: String = "devops-course-hwhddqemcbgabha5.northeurope-01.azurewebsites.net",//"10.0.2.2:5001",
    private val client: HttpClient = HttpClient(Android)


) {
    suspend fun sendEvent(userData: UserData, location: Location): Boolean {
        val jsonLocation = serializeEvent(location)
        val pathEnd = if (userData.isCheckedIn) {
            "SignOutOnLocation"
        }else {
            "SignInOnLocation"
        }
        val status = client.use { client ->
            client.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = endpoint
                    path("Location/" + pathEnd)
                    bearerAuth(userData.token)
                    contentType(ContentType.Application.Json)
                    setBody(jsonLocation)
                }
            }
        }
        Log.i("SendEvent", status.toString())
        Log.i("SendEvent", userData.token)
        Log.i("SendEvent", status.bodyAsText())
        if (status.status.isSuccess()) {
            return true
        }
        return false
    }
    suspend fun getCheckInStatus(userData: UserData) : Boolean {
        val status: HttpResponse = client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = endpoint
                path("Location/LocationSignedIn")
            }
            bearerAuth(userData.token)
        }
        Log.i("CheckInStatus",status.toString())
        if (status.status.isSuccess()) {
            return true
        }
        return false
    }
    suspend fun authorizeToken(token: String): String? {
        val json = "{\"IdToken\" : \"$token\"}"
        val status: HttpResponse = client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = endpoint
                path("api/GoogleAuth/google-login")
                contentType(ContentType.Application.Json)
                setBody(json)
            }
        }
        Log.i("AuthToken", status.toString())
        if (status.status.isSuccess()) {
            // Extract token from the response
            val responseBody = status.bodyAsText()
            Log.i("AuthTokenBody", responseBody)
            val jsonObject = JSONObject(responseBody)
            return jsonObject.getString("token")
        }
        return null

    }
    private fun serializeEvent(loc: Location): String {
        return """
        {
            "latitude": ${loc.latitude},
            "longitude": ${loc.longitude}
        }
    """.trimIndent()
    }
}


