package dev.mahasiswa.kelompokone.ui.login

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import dev.mahasiswa.kelompokone.data.FingerprintAuthHelper
import dev.mahasiswa.kelompokone.data.SecureCredentialManager
import dev.mahasiswa.kelompokone.data.TokenManager
import dev.mahasiswa.kelompokone.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val tokenManager: TokenManager,
    private val secureCredentialManager: SecureCredentialManager,
    private val fingerprintAuthHelper: FingerprintAuthHelper,
    private val context: Context
) : ViewModel() {

    private val TAG = "LoginViewModel"

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _biometricState = MutableStateFlow<BiometricState>(BiometricState.Unavailable)
    val biometricState: StateFlow<BiometricState> = _biometricState

    init {
        checkBiometricAvailability()
    }

    fun checkBiometricAvailability() {
        val hasStoredCredentials = secureCredentialManager.hasStoredCredentials()
        val canUseBiometric = fingerprintAuthHelper.canAuthenticate()
        
        Log.d(TAG, "Stored credentials available: $hasStoredCredentials")
        Log.d(TAG, "Biometric authentication available: $canUseBiometric")
        
        _biometricState.value = if (canUseBiometric && hasStoredCredentials) {
            Log.d(TAG, "Setting biometric state to Available")
            BiometricState.Available
        } else {
            Log.d(TAG, "Setting biometric state to Unavailable")
            BiometricState.Unavailable
        }
    }

    fun login(username: String, password: String, shouldSaveCredentials: Boolean = false) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.kcApiService.login(
                    clientId = "setoran-mobile-dev",
                    clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                    grantType = "password",
                    username = username,
                    password = password,
                    scope = "openid profile email"
                )
                if (response.isSuccessful) {
                    response.body()?.let { auth ->
                        tokenManager.saveTokens(auth.access_token, auth.refresh_token, auth.id_token)
                        
                        if (shouldSaveCredentials) {
                            Log.d(TAG, "Saving credentials for future biometric login")
                            secureCredentialManager.saveCredentials(username, password)
                            checkBiometricAvailability()
                        }
                        
                        _loginState.value = LoginState.Success
                    } ?: run {
                        _loginState.value = LoginState.Error("Respons kosong")
                    }
                } else {
                    _loginState.value = LoginState.Error("Login gagal: ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Kesalahan: ${e.message}")
            }
        }
    }

    fun authenticateWithBiometric(activity: FragmentActivity) {
        Log.d(TAG, "Attempting biometric authentication")
        if (_biometricState.value == BiometricState.Available) {
            fingerprintAuthHelper.showBiometricPrompt(
                activity = activity,
                onSuccess = { loginWithSavedCredentials() },
                onFailure = { message -> 
                    Log.e(TAG, "Biometric authentication failed: $message")
                    _loginState.value = LoginState.Error("Biometric authentication failed: $message")
                }
            )
        } else {
            Log.e(TAG, "Attempted biometric authentication but state is: ${_biometricState.value}")
        }
    }

    private fun loginWithSavedCredentials() {
        val email = secureCredentialManager.getEmail()
        val password = secureCredentialManager.getPassword()
        
        Log.d(TAG, "Attempting login with saved credentials. Email exists: ${email != null}, Password exists: ${password != null}")
        
        if (email != null && password != null) {
            login(email, password)
        } else {
            _loginState.value = LoginState.Error("Saved credentials not found")
        }
    }

    fun logout() {
        tokenManager.clearTokens()
        secureCredentialManager.clearCredentials()
        _biometricState.value = BiometricState.Unavailable
        _loginState.value = LoginState.Idle
    }

    companion object {
        fun getFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                        return LoginViewModel(
                            TokenManager(context),
                            SecureCredentialManager(context),
                            FingerprintAuthHelper(context),
                            context
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class BiometricState {
    object Available : BiometricState()
    object Unavailable : BiometricState()
}