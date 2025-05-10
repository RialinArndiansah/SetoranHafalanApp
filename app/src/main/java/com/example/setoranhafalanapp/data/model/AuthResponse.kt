package com.example.setoranhafalan.data.model

data class AuthResponse(
    val access_token: String,
    val refresh_token: String,
    val id_token: String,
    val expires_in: Int,
    val refresh_expires_in: Int,
    val token_type: String,
    val scope: String
)