package dev.mahasiswa.kelompokone.data.network

import dev.mahasiswa.kelompokone.data.TokenManager
import dev.mahasiswa.kelompokone.data.network.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://api.tif.uin-suska.ac.id/setoran-dev/v1/"
    private const val KC_URL = "https://id.tif.uin-suska.ac.id"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level
            .BODY
    }

    private var tokenManager: TokenManager? = null
    
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }
    
    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            val localTokenManager = tokenManager ?: return null
            
            // Check if we should attempt to refresh
            if (localTokenManager.isRefreshTokenExpired() || localTokenManager.isUserInactive()) {
                return null // Let the UserActivityTracker handle session expiration
            }
            
            // Get the refresh token
            val refreshToken = localTokenManager.getRefreshToken() ?: return null
            
            // Try to get a new access token
            return runBlocking {
                try {
                    val tokenResponse = kcApiService.refreshToken(
                        clientId = "setoran-mobile-dev",
                        clientSecret = "aqJp3xnXKudgC7RMOshEQP7ZoVKWzoSl",
                        grantType = "refresh_token",
                        refreshToken = refreshToken
                    )
                    
                    if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                        val newAccessToken = tokenResponse.body()?.access_token
                        val newRefreshToken = tokenResponse.body()?.refresh_token
                        val newIdToken = tokenResponse.body()?.id_token
                        
                        if (newAccessToken != null && newRefreshToken != null && newIdToken != null) {
                            // Save the new tokens
                            localTokenManager.saveTokens(newAccessToken, newRefreshToken, newIdToken)
                            
                            // Update the request with the new access token
                            response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccessToken")
                                .build()
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val kcApiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(KC_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}