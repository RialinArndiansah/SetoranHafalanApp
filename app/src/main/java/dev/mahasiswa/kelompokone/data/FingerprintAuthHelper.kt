package dev.mahasiswa.kelompokone.data

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper class for fingerprint authentication
 */
class FingerprintAuthHelper(private val context: Context) {

    private val TAG = "FingerprintAuthHelper"
    
    // Define authenticator type
    private val authenticators = Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK

    /**
     * Check if biometric authentication is available
     */
    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuthResult = biometricManager.canAuthenticate(authenticators)
        
        // Log result for debugging
        when (canAuthResult) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d(TAG, "Biometric authentication is available")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e(TAG, "No biometric hardware detected")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e(TAG, "Biometric hardware is currently unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.e(TAG, "No fingerprints enrolled on the device")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                Log.e(TAG, "Security update required for biometric")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                Log.e(TAG, "Biometric authentication is unsupported")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                Log.e(TAG, "Biometric status unknown")
            else -> Log.e(TAG, "Unknown biometric status: $canAuthResult")
        }
        
        return canAuthResult == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Show the biometric prompt
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Biometric authentication succeeded")
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(TAG, "Authentication error [$errorCode]: $errString")
                onFailure(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.e(TAG, "Authentication failed")
                onFailure("Authentication failed")
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login dengan Sidik Jari")
            .setSubtitle("Gunakan sidik jari untuk masuk ke akun Anda")
            .setNegativeButtonText("Batal")
            .setAllowedAuthenticators(authenticators)
            .build()

        Log.d(TAG, "Showing biometric prompt")
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }
} 