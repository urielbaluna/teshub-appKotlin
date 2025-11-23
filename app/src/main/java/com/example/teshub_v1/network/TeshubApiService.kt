package com.example.teshub_v1.network

import com.example.teshub_v1.model.CrearPublicacionResponse
import com.example.teshub_v1.model.LoginResponse
import com.example.teshub_v1.model.PerfilResponse
import com.example.teshub_v1.model.PublicacionesListResponse
import com.example.teshub_v1.model.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TeshubApiService {

    @POST("api/usuarios/login")
    suspend fun login(
        @Body loginRequest: Map<String, String>
    ): LoginResponse

    @Multipart
    @POST("api/usuarios/registrar")
    suspend fun register(
        @Part("nombre") nombre: RequestBody,
        @Part("apellido") apellido: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part("matricula") matricula: RequestBody,
        @Part("contrasena") contrasena: RequestBody,
        @Part imagen: MultipartBody.Part? // Campo para la imagen de perfil (opcional)
    ): RegisterResponse

    @GET("api/usuarios/ver-info")
    suspend fun getPerfil(
        @Header("Authorization") token: String
    ): PerfilResponse

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
        @Part archivos: List<MultipartBody.Part>? = null
    ): CrearPublicacionResponse
}