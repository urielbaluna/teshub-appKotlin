package com.example.teshub_v1.data.network

import com.example.teshub_v1.data.model.CrearPublicacionResponse
import com.example.teshub_v1.data.model.PublicacionesListResponse
import com.example.teshub_v1.data.model.PublicacionesUsuarioResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.DELETE
import retrofit2.http.Path

interface PublicacionesService {
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
    ): CrearPublicacionResponse
    @POST("api/usuarios/ver-info-publicaciones")
    suspend fun obtenerSoloPublicaciones(
        @Header("Authorization") token: String
    ): PublicacionesUsuarioResponse
    @DELETE("api/publicaciones/eliminar/{id_publi}")
    suspend fun eliminarPublicacion(
        @Path("id_publi") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>

}