package com.example.teshub_v1.network

import com.example.teshub_v1.model.LoginResponse
import com.example.teshub_v1.model.PerfilResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UsuariosService {
    @POST("api/usuarios/login")
    suspend fun login(
        @Body loginRequest: Map<String, String>
    ): LoginResponse

    @GET("api/usuarios/ver-info")
    suspend fun getPerfil(
        @Header("Authorization") token: String
    ): PerfilResponse
}