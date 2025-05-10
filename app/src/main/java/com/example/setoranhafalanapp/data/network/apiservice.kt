package com.example.setoranhafalanapp.data.network
import com.example.setoranhafalan.data.model.AuthResponse
import com.example.setoranhafalan.data.model.SetoranResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("/realms/dev/protocol/openid-connect/token")
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String
    ): Response<AuthResponse>

    @FormUrlEncoded
    @POST("/realms/dev/protocol/openid-connect/token")
    suspend fun refreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Response<AuthResponse>

    //Endpoint setoran-saya dengan parameter apikey
    @GET("mahasiswa/setoran-saya")
    suspend fun getSetoranSaya(
        @Header("Authorization") token: String,
        @Query("apikey") accessToken: String
    ): Response<SetoranResponse>

    // Endpoint setoran-saya tanpa parameter apikey
    @GET("mahasiswa/setoran-saya")
    suspend fun getSetoranSaya(
        @Header("Authorization") token: String
    ): Response<SetoranResponse>
}