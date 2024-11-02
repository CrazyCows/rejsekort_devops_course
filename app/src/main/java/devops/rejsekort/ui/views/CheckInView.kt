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
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import devops.rejsekort.viewModels.RejsekortViewmodel


@Composable
fun CheckInOutScreen(
    changeCheckInStatus: () -> Unit, //TODO: REMOVE(?)
    viewModel: RejsekortViewmodel = RejsekortViewmodel(),
    getUserData: () -> UserData,
) {

    val context = LocalContext.current
    var location by remember { mutableStateOf("Your location") }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                Log.i("requestPermissionLauncher",
                    if (isGranted) "Permission access granted" else "Permission access not granted")
            })

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
                onClick = { //TODO FIX HERE
                    if(viewModel.hasFineLocationAccess(context)){
                        viewModel.CheckInOut(context)

                    } else {
                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        //nextline()
                        viewModel.CheckInOut(context)
                        if(viewModel.hasFineLocationAccess(context)){
                            Log.e("Check in button", "Unhandled permission denial") //TODO: Handle this. How? Idk
                        }
                    }
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
fun CheckInOutScreenPreview(viewModel: RejsekortViewmodel = RejsekortViewmodel()) {
    CheckInOutScreen(
        changeCheckInStatus = {Log.i("CheckInOutScreenPreview", "changeCheckInStatus clicked")},
        viewModel = viewModel,
        getUserData = { UserData(
            firstName = "Jens",
            lastName = "Hansen",
            isCheckedIn = false
        ) }
    )
}
