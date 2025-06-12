package dev.mahasiswa.kelompokone.data

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureCredentialManager(context: Context) {

    private val TAG = "SecureCredentialManager"

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "secure_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveCredentials(email: String, password: String) {
        try {
            // Clear any existing credentials before saving
            clearCredentials()
            
            securePrefs.edit().apply {
                putString("email", email)
                putString("password", password)
                putBoolean("has_credentials", true)
                apply()
            }
            
            Log.d(TAG, "Credentials saved successfully")
            
            // Verify credentials were actually saved
            val savedEmail = getEmail()
            val hasCredentialsFlag = hasStoredCredentials()
            
            Log.d(TAG, "Verification - Email saved: ${savedEmail != null}, Has credentials flag: $hasCredentialsFlag")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving credentials: ${e.message}")
        }
    }
    
    fun getEmail(): String? {
        return try {
            securePrefs.getString("email", null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving email: ${e.message}")
            null
        }
    }
    
    fun getPassword(): String? {
        return try {
            securePrefs.getString("password", null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving password: ${e.message}")
            null
        }
    }
    
    fun hasStoredCredentials(): Boolean {
        return try {
            val hasFlag = securePrefs.getBoolean("has_credentials", false)
            val hasEmail = securePrefs.contains("email") && securePrefs.getString("email", null) != null
            val hasPassword = securePrefs.contains("password") && securePrefs.getString("password", null) != null
            
            val result = hasFlag && hasEmail && hasPassword
            Log.d(TAG, "hasStoredCredentials check: flag=$hasFlag, email=${securePrefs.contains("email")}, password=${securePrefs.contains("password")}, result=$result")
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error checking credentials: ${e.message}")
            false
        }
    }
    
    fun clearCredentials() {
        try {
            securePrefs.edit().clear().apply()
            Log.d(TAG, "Credentials cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing credentials: ${e.message}")
        }
    }
} 