package com.example.setoranhafalan.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.auth0.jwt.JWT
import com.example.setoranhafalan.data.model.SetoranResponse
import com.example.setoranhafalan.data.network.RetrofitClient
import com.example.setoranhafalanapp.data.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DashboardViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val dashboardState: StateFlow<DashboardState> = _dashboardState
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName
    private val TAG = "DashboardViewModel"

    init {
        // Menguraikan id_token untuk mendapatkan nama pengguna
        val idToken = tokenManager.getIdToken()
        if (idToken != null) {
            try {
                val decodedJwt = JWT.decode(idToken)
                val name = decodedJwt.getClaim("name").asString() ?: decodedJwt.getClaim("preferred_username").asString()
                _userName.value = name
                Log.d(TAG, "Nama pengguna dari id_token: $name")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menguraikan id_token: ${e.message}")
            }
        }
    }

    fun fetchSetoranSaya() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    // Coba tanpa apikey dulu
                    Log.d(TAG, "data setoran dengan apikey, token: $token")
                    Log.d(TAG, "URL: https://api.tif.uin-suska.ac.id/setoran-dev/v1//v1/mahasiswa/setoran-saya?apikey=$token")
                    val response = RetrofitClient.apiService.getSetoranSaya(
                        token = "Bearer $token",
                        accessToken = token
                    )
                    if (response.isSuccessful) {
                        response.body()?.let { setoran ->
                            Log.d(TAG, "Data setoran berhasil diambil: ${setoran.message}")
                            _dashboardState.value = DashboardState.Success(setoran)
                        } ?: run {
                            Log.e(TAG, "Respons kosong dari server")
                            _dashboardState.value = DashboardState.Error("Respons kosong dari server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Gagal dengan apikey, kode: ${response.code()}, pesan: ${response.message()}, body: $errorBody")
                        if (response.code() == 403) {
                            // Coba tanpa apikey
                            Log.d(TAG, "Mencoba tanpa apikey, https://api.tif.uin-suska.ac.id/setoran-dev//v1/mahasiswa/setoran-saya")
                            val noApiKeyResponse = RetrofitClient.apiService.getSetoranSaya(
                                token = "Bearer $token"
                            )
                            if (noApiKeyResponse.isSuccessful) {
                                noApiKeyResponse.body()?.let { setoran ->
                                    Log.d(TAG, "Data setoran berhasil diambil tanpa apikey: ${setoran.message}")
                                    _dashboardState.value = DashboardState.Success(setoran)
                                } ?: run {
                                    Log.e(TAG, "Respons kosong dari server tanpa apikey")
                                    _dashboardState.value = DashboardState.Error("Respons kosong dari server tanpa apikey")
                                }
                            } else {
                                val noApiKeyErrorBody = noApiKeyResponse.errorBody()?.string()
                                Log.e(TAG, "Gagal tanpa apikey, kode: ${noApiKeyResponse.code()}, pesan: ${noApiKeyResponse.message()}, body: $noApiKeyErrorBody")
                                handleErrorResponse(noApiKeyResponse.code(), noApiKeyErrorBody, noApiKeyResponse.message())
                            }
                        } else {
                            handleErrorResponse(response.code(), errorBody, response.message())
                        }
                    }
                } else {
                    Log.e(TAG, "Access token tidak ditemukan")
                    _dashboardState.value = DashboardState.Error("Token tidak ditemukan")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Pengecualian HTTP: ${e.code()}, pesan: ${e.message()}")
                _dashboardState.value = DashboardState.Error("Kesalahan HTTP: ${e.message()} (Kode: ${e.code()})")
            } catch (e: Exception) {
                Log.e(TAG, "Pengecualian saat mengambil data: ${e.message}", e)
                _dashboardState.value = DashboardState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    private fun handleErrorResponse(code: Int, errorBody: String?, message: String) {
        when (code) {
            401 -> {
                Log.w(TAG, "Token tidak valid, mencoba refresh token")
                viewModelScope.launch {
                    val refreshToken = tokenManager.getRefreshToken()
                    if (refreshToken != null) {
                        val refreshResponse = RetrofitClient.kcApiService.refreshToken(
                            clientId = "setoran-mobile-dev",
                            clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                            grantType = "refresh_token",
                            refreshToken = refreshToken
                        )
                        if (refreshResponse.isSuccessful) {
                            refreshResponse.body()?.let { auth ->
                                Log.d(TAG, "Token berhasil diperbarui")
                                tokenManager.saveTokens(auth.access_token, auth.refresh_token, auth.id_token)
                                val retryResponse = RetrofitClient.apiService.getSetoranSaya(
                                    token = "Bearer ${auth.access_token}",
                                    accessToken = auth.access_token
                                )
                                if (retryResponse.isSuccessful) {
                                    retryResponse.body()?.let { setoran ->
                                        _dashboardState.value = DashboardState.Success(setoran)
                                    } ?: run {
                                        Log.e(TAG, "Respons kosong setelah refresh")
                                        _dashboardState.value = DashboardState.Error("Respons kosong setelah refresh")
                                    }
                                } else {
                                    val retryErrorBody = retryResponse.errorBody()?.string()
                                    Log.e(TAG, "Gagal setelah refresh, kode: ${retryResponse.code()}, pesan: ${retryResponse.message()}, body: $retryErrorBody")
                                    _dashboardState.value = DashboardState.Error("Gagal mengambil data setelah refresh: ${retryResponse.message()} (Kode: ${retryResponse.code()})")
                                }
                            } ?: run {
                                Log.e(TAG, "Respons refresh kosong")
                                _dashboardState.value = DashboardState.Error("Gagal memperbarui token: Respons kosong")
                            }
                        } else {
                            Log.e(TAG, "Gagal refresh token, kode: ${refreshResponse.code()}, pesan: ${refreshResponse.message()}")
                            _dashboardState.value = DashboardState.Error("Gagal memperbarui token: ${refreshResponse.message()} (Kode: ${refreshResponse.code()})")
                        }
                    } else {
                        Log.e(TAG, "Refresh token tidak ditemukan")
                        _dashboardState.value = DashboardState.Error("Refresh token tidak ditemukan")
                    }
                }
            }
            403 -> {
                Log.e(TAG, "Akses ditolak: $errorBody")
                _dashboardState.value = DashboardState.Error("Akses ditolak: Tidak diotorisasi (Kode: 403). Periksa scope/role Keycloak atau nilai apikey.")
            }
            404 -> {
                Log.e(TAG, "Endpoint tidak ditemukan: $message")
                _dashboardState.value = DashboardState.Error("Endpoint tidak ditemukan (Kode: 404)")
            }
            else -> {
                _dashboardState.value = DashboardState.Error("Gagal mengambil data: $message (Kode: $code, Body: $errorBody)")
            }
        }
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        return DashboardViewModel(TokenManager(context)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val data: SetoranResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
}