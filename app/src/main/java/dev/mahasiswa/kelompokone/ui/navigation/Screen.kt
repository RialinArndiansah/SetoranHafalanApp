package dev.mahasiswa.kelompokone.ui.navigation

/**
 * Screen class to define navigation routes
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
} 