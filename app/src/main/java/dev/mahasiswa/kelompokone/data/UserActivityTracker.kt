package dev.mahasiswa.kelompokone.data

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.mahasiswa.kelompokone.ui.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class UserActivityTracker(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val lifecycleOwner: LifecycleOwner,
    private val onSessionExpired: () -> Unit,
    private val isOnLoginScreen: () -> Boolean
) : DefaultLifecycleObserver, View.OnTouchListener {

    private var activityRef = WeakReference<Activity>(context as? Activity)
    private var trackerJob: Job? = null
    private val trackerScope = CoroutineScope(Dispatchers.Main)
    
    private val CHECK_INTERVAL = 30000L // Check every 30 seconds
    
    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }
    
    override fun onResume(owner: LifecycleOwner) {
        startTracker()
    }
    
    override fun onPause(owner: LifecycleOwner) {
        stopTracker()
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        stopTracker()
        lifecycleOwner.lifecycle.removeObserver(this)
    }
    
    private fun startTracker() {
        stopTracker()
        
        trackerJob = trackerScope.launch {
            while (isActive) {
                checkTokensAndActivity()
                delay(CHECK_INTERVAL)
            }
        }
    }
    
    private fun stopTracker() {
        trackerJob?.cancel()
        trackerJob = null
    }
    
    private fun checkTokensAndActivity() {
        // Skip checking if already on login screen
        if (isOnLoginScreen()) {
            return
        }
        
        // Check if access token is expired
        if (tokenManager.isAccessTokenExpired()) {
            // If refresh token is also expired or user is inactive, show session expired dialog
            if (tokenManager.isRefreshTokenExpired() || tokenManager.isUserInactive()) {
                showSessionExpiredDialog()
            } else {
                // Normally would refresh the token here
                // For now we'll just update the activity time
                tokenManager.updateLastActivityTime()
            }
        }
    }
    
    private fun showSessionExpiredDialog() {
        // Don't show dialog if already on login screen
        if (isOnLoginScreen()) {
            return
        }
        
        val activity = activityRef.get() ?: return
        
        AlertDialog.Builder(activity)
            .setTitle("Sesi Login Telah Habis")
            .setMessage("Sesi login anda telah habis, harap login ulang.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                // Clear tokens and navigate to login
                tokenManager.clearTokens()
                onSessionExpired()
            }
            .show()
    }
    
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // Update last activity time when user interacts with the screen
        tokenManager.updateLastActivityTime()
        return false // Return false to not consume the event
    }
    
    fun setupTouchListener(view: View) {
        view.setOnTouchListener(this)
    }
} 