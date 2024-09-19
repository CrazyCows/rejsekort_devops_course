package devops.rejsekort.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import devops.rejsekort.data.UserData

@Composable
fun CheckInOutScreen(
    changeCheckInStatus: () -> Unit,
    getUserData: () -> UserData,
) {
    val userData = remember { getUserData() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            Text(
                text = userData.firstName,
                fontSize = 50.sp
            )
            Text(
                text = userData.lastName,
                fontSize = 50.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    changeCheckInStatus()
                },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(75.dp),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text(
                    text = if (userData.isCheckedIn) "Check Out" else "Check In",
                    fontSize = 22.sp
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CheckInOutScreenPreview() {
    CheckInOutScreen(
        changeCheckInStatus = {},
        getUserData = { UserData(
            firstName = "Jens",
            lastName = "Hansen",
            isCheckedIn = false
        ) }
    )
}
