package devops.rejsekort

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import devops.rejsekort.data.UserData
import devops.rejsekort.ui.theme.RejsekortTheme
import devops.rejsekort.ui.views.CheckInOutScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RejsekortTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckInOutScreen(
                        changeCheckInStatus = {},
                        getUserData = {
                            UserData(
                                firstName = "Jens",
                                lastName = "Hansen",
                                isCheckedIn = false
                            )
                        }
                    )
                }
            }
        }
    }
}