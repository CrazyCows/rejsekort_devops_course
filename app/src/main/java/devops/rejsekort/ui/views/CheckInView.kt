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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import devops.rejsekort.viewModels.RejsekortViewmodel


@Composable
fun CheckInOutScreen(
    viewModel: RejsekortViewmodel = RejsekortViewmodel(),
    getUserData: () -> UserData,
) {

    val context = LocalContext.current
    val permissionHandled by viewModel.permissionHandled //Only keeps track of whether permission is handled in the current session
    val userData by viewModel.userData
    //val userData = remember { getUserData() }

    LaunchedEffect(permissionHandled) { //only runs if the system popup asking for permission runs
        viewModel.handleCheckInOut(context)
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                viewModel.onPermissionHandled()
                Log.i("requestPermissionLauncher",
                    if (isGranted) "Permission access granted" else "Permission access not granted")
            })


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
                    if(viewModel.checkFineLocationAccess(context)){ //we call this here to get the most up to date info, I dont think we can avoid the race condition
                        viewModel.handleCheckInOut(context)
                    } else {
                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        //viewModel.CheckInOut(context) Automatically runs when the permission changes, so commented out here
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(75.dp),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text(
                    //text = if (userData.isCheckedIn) "Check Out" else "Check In",
                    text = if (viewModel.checkedIn.value) "Check Out" else "Check In",
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
        viewModel = viewModel,
        getUserData = { UserData(
            firstName = "Jens",
            lastName = "Hansen",
            isCheckedIn = false
        ) }
    )
}
