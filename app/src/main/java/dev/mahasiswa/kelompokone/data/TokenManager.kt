package dev.mahasiswa.kelompokone.data

import android.content.Context
import androidx.core.content.edit

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String, idToken: String) {
        prefs.edit {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("id_token", idToken)
        }
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    fun getIdToken(): String? = prefs.getString("id_token", null)

    fun clearTokens() {
        prefs.edit {
            clear()
        }
    }
}