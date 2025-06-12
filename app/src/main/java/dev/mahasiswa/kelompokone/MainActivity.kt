package dev.mahasiswa.kelompokone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import dev.mahasiswa.kelompokone.ui.navigation.SetupNavGraph
import dev.mahasiswa.kelompokone.ui.theme.SetoranHafalanTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    
    // Use a StateFlow to track activity state
    private val _activityState = MutableStateFlow(false)
    val activityState: StateFlow<Boolean> = _activityState
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                    val navController = rememberNavController()
                    
                    // Call SetupNavGraph directly as a Composable and pass this activity
                    SetupNavGraph(navController = navController, fragmentActivity = this)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        _activityState.value = true
    }
    
    override fun onPause() {
        super.onPause()
        _activityState.value = false
    }
}