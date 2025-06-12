package dev.mahasiswa.kelompokone

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.mahasiswa.kelompokone.data.network.RetrofitClient
import dev.mahasiswa.kelompokone.data.TokenManager
import dev.mahasiswa.kelompokone.data.UserActivityTracker
import dev.mahasiswa.kelompokone.ui.navigation.Screen
import dev.mahasiswa.kelompokone.ui.navigation.SetupNavGraph
import dev.mahasiswa.kelompokone.ui.theme.SetoranHafalanTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    
    // Use a StateFlow to track activity state
    private val _activityState = MutableStateFlow(false)
    val activityState: StateFlow<Boolean> = _activityState
    
    private lateinit var tokenManager: TokenManager
    private lateinit var userActivityTracker: UserActivityTracker
    private lateinit var navController: NavHostController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize token manager
        tokenManager = TokenManager(this)
        
        // Initialize RetrofitClient with token manager
        RetrofitClient.initialize(tokenManager)
        
        // Optimize window insets handling
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Start activity state tracking
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _activityState.value = true
            }
        }
        
        setContent {
            SetoranHafalanTheme {
                // Optimized surface container that takes full size
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use rememberNavController directly
                    navController = rememberNavController()
                    
                    // Call SetupNavGraph directly as a Composable and pass this activity
                    SetupNavGraph(navController = navController, fragmentActivity = this)
                }
            }
        }
        
        // Initialize user activity tracker after setting content
        // to get reference to the navController
        initUserActivityTracker()
        
        // Find the root view and set it up with the activity tracker
        val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        userActivityTracker.setupTouchListener(rootView)
    }
    
    private fun initUserActivityTracker() {
        userActivityTracker = UserActivityTracker(
            context = this,
            tokenManager = tokenManager,
            lifecycleOwner = this,
            onSessionExpired = {
                // Navigate back to login screen when session expires
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Login.route)
                }
            },
            isOnLoginScreen = {
                // Check if current destination is login screen
                navController.currentBackStackEntry?.destination?.route == Screen.Login.route
            }
        )
    }
    
    override fun onResume() {
        super.onResume()
        _activityState.value = true
        tokenManager.updateLastActivityTime()
    }
    
    override fun onPause() {
        super.onPause()
        _activityState.value = false
    }
}