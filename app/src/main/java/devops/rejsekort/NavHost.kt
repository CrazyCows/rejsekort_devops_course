package devops.rejsekort

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import devops.rejsekort.ui.views.CheckInOutScreen
import devops.rejsekort.viewModels.RejsekortViewmodel

@Composable
fun RejsekortNavHost(
    navController: NavHostController = rememberNavController()
) {
    val rejsekortViewmodel = remember { RejsekortViewmodel() }



    NavHost(
        navController = navController,
        startDestination = Screens.LOGIN_SCREEN.name
    ) {
        composable(Screens.CHECKIN_SCREEN.name) {
            CheckInOutScreen(
                viewModel = rejsekortViewmodel
            )
        }
        composable(Screens.LOGIN_SCREEN.name) {
            LoginScreen(
                viewModel = rejsekortViewmodel,
                navigation = {
                    navController.navigate(Screens.CHECKIN_SCREEN.name) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}