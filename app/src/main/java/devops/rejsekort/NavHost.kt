package devops.rejsekort

import LoginScreen
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import devops.rejsekort.data.UserData
import devops.rejsekort.ui.views.CheckInOutScreen
import devops.rejsekort.viewModels.RejsekortViewmodel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RejsekortNavHost(
    navController: NavHostController = rememberNavController(),
    context: Context
) {
    val rejsekortViewmodel = remember { RejsekortViewmodel() }
    val credentialManager = CredentialManager.create(context)



    NavHost(
        navController = navController,
        startDestination = if (false) {
            Screens.CHECKIN_SCREEN.name
        } else {
            Screens.LOGIN_SCREEN.name
        }
    ) {
        composable(Screens.CHECKIN_SCREEN.name) {
            CheckInOutScreen(
                viewModel = rejsekortViewmodel,
                getUserData = { /* TODO fix */
                    UserData(
                        firstName = "Jens",
                        lastName = "Hansen",
                        isCheckedIn = false
                    )
                              },
            )
        }
        composable(Screens.LOGIN_SCREEN.name) {
            LoginScreen(
                loginAction = {
                    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId("260729048541-mu6t9f9r9s7jafdl7sqngve5fknncs4n.apps.googleusercontent.com")
                        .build()

                    val request: GetCredentialRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val result = credentialManager.getCredential(
                                request = request,
                                context = context,
                            )
                            rejsekortViewmodel.handleSignIn(result){
                                Toast.makeText(context,"SUCCESS",Toast.LENGTH_LONG).show()
                            }
                            navController.navigate(Screens.CHECKIN_SCREEN.name) {
                                popUpTo(0)
                            }
                        } catch (e: GetCredentialException) {
                            Toast.makeText(context,
                                "Unable to connect",
                                Toast.LENGTH_SHORT
                            ).show()
                            /* TODO Move so it only progresses on success */
                            navController.navigate(Screens.CHECKIN_SCREEN.name) {
                                popUpTo(0)
                            }
                        }
                    }

                },
            )
        }
    }
}