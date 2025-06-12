package dev.mahasiswa.kelompokone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.mahasiswa.kelompokone.ui.dashboard.DashboardScreen
import dev.mahasiswa.kelompokone.ui.login.LoginScreen
import dev.mahasiswa.kelompokone.ui.navigation.Screen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    fragmentActivity: FragmentActivity
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                fragmentActivity = fragmentActivity
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
    }
}