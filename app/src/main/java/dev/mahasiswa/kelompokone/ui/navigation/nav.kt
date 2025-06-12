package dev.mahasiswa.kelompokone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.setoranhafalan.ui.dashboard.DashboardScreen
import dev.mahasiswa.kelompokone.ui.login.LoginScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    fragmentActivity: FragmentActivity
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                fragmentActivity = fragmentActivity
            )
        }
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
    }
}