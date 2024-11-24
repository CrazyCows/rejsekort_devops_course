package devops.rejsekort.ui.views


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import devops.rejsekort.viewModels.RejsekortViewmodel

@Composable
fun CheckInOutScreen(
    viewModel: RejsekortViewmodel,
) {

    val context = LocalContext.current
    val userData by viewModel.userData
    val isLoading by viewModel.isLoading
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { viewModel.handleCheckInOut(context) }
        )

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
            Spacer(Modifier.height(100.dp))
            Text(
                text = userData.firstName.orEmpty(),
                fontSize = 50.sp
            )
            Text(
                text = userData.lastName.orEmpty(),
                fontSize = 50.sp
            )
        }
        if(isLoading){
            CircularProgressIndicator(
                modifier = Modifier
                    .width(256.dp)
                    .align(Alignment.Center)
                    .padding(10.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 16.dp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {

            Button(
                enabled = !isLoading,
                onClick = {
                    viewModel.isLoading.value = true
                    if(viewModel.checkFineLocationAccess(context)){
                        viewModel.handleCheckInOut(context)
                    } else {
                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) //launches handleCheckInOut
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(75.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(
                    text = if (userData.isCheckedIn) "Check ud" else "Check ind",
                    fontSize = 22.sp
                )
            }
        }
    }
}

