package com.example.teshub_v1.network

import com.example.teshub_v1.model.LoginResponse
import com.example.teshub_v1.model.PerfilResponse
import com.example.teshub_v1.model.PublicacionesListResponse
import com.example.teshub_v1.model.CrearPublicacionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TeshubApiService {

    @POST("api/usuarios/login")
    suspend fun login(
        @Body loginRequest: Map<String, String>
    ): LoginResponse

    @GET("api/usuarios/ver-info")
    suspend fun getPerfil(
        @Header("Authorization") token: String
    ): PerfilResponse

    // 👇 AQUÍ ESTÁ LA CLAVE DEL ERROR
    @GET("api/publicaciones/listar")
    suspend fun listarPublicaciones(
        @Header("Authorization") token: String
    ): PublicacionesListResponse

    @Multipart
    @POST("api/publicaciones/crear")
    suspend fun crearPublicacion(
        @Header("Authorization") token: String,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("colaboradores") colaboradores: RequestBody,
        // Archivos opcionales
        @Part archivos: List<MultipartBody.Part>? = null
    ): CrearPublicacionResponse
}