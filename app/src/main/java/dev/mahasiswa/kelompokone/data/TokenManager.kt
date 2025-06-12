package dev.mahasiswa.kelompokone.data

import android.content.Context
import androidx.core.content.edit

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String, idToken: String) {
        val currentTime = System.currentTimeMillis()
        val accessTokenExpiry = currentTime + (4 * 60 * 1000) // 2 minutes
        val refreshTokenExpiry = currentTime + (3 * 60 * 1000) // 1 minute
        
        prefs.edit {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("id_token", idToken)
            putLong("access_token_expiry", accessTokenExpiry)
            putLong("refresh_token_expiry", refreshTokenExpiry)
            putLong("last_activity_time", currentTime)
        }
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    fun getIdToken(): String? = prefs.getString("id_token", null)
    
    fun getAccessTokenExpiryTime(): Long = prefs.getLong("access_token_expiry", 0)
    fun getRefreshTokenExpiryTime(): Long = prefs.getLong("refresh_token_expiry", 0)
    
    fun isAccessTokenExpired(): Boolean {
        val expiryTime = getAccessTokenExpiryTime()
        return expiryTime <= System.currentTimeMillis()
    }
    
    fun isRefreshTokenExpired(): Boolean {
        val expiryTime = getRefreshTokenExpiryTime()
        return expiryTime <= System.currentTimeMillis()
    }
    
    fun updateLastActivityTime() {
        prefs.edit {
            putLong("last_activity_time", System.currentTimeMillis())
        }
    }
    
    fun getLastActivityTime(): Long = prefs.getLong("last_activity_time", 0)
    
    fun isUserInactive(inactivityThreshold: Long = 2 * 60 * 1000): Boolean {
        val lastActivity = getLastActivityTime()
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastActivity) >= inactivityThreshold
    }

    fun clearTokens() {
        prefs.edit {
            clear()
        }
    }
}