package devops.rejsekort.data

import android.content.Context
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CheckInOutRepository (
    val endpoint: String,
) {
    suspend fun sendCheckIn(onSuccess: (Event) -> Unit,
                            onError: (Exception) -> Unit) {
        val url = URL(endpoint)
        val openedConnection = url.openConnection() as HttpURLConnection
        openedConnection.requestMethod = "GET"

        val responseCode = openedConnection.responseCode
        try {
            val reader = BufferedReader(InputStreamReader(openedConnection.inputStream))
            val response = reader.readText()
            val apiResponse = Event(
                parseJson<Event>(response)
            )
            print(response)
            reader.close()
            // Call the success callback on the main thread
            onSuccess(apiResponse)
        } catch (e: Exception) {
            Log.d("Error", e.message.toString())
            // Handle error cases and call the error callback on the main thread
            launch(Dispatchers.Main) {
                onError(Exception("HTTP Request failed with response code $responseCode"))
            }
        } finally {

        }
    }
    private inline fun <reified T>parseJson(text: String): T =
        Gson().fromJson(text, T::class.java)
}


