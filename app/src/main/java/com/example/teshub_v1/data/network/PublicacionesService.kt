package com.example.teshub_v1.data.network

import com.example.teshub_v1.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

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
        @Part archivos: List<MultipartBody.Part>
    ): CrearPublicacionResponse

    @GET("api/publicaciones/ver/{id_publi}")
    suspend fun verPublicacion(
        @Header("Authorization") token: String,
        @Path("id_publi") idPublicacion: Int
    ): PublicacionDetalleResponse

    @FormUrlEncoded
    @POST("api/publicaciones/comentar")
    suspend fun comentarPublicacion(
        @Header("Authorization") token: String,
        @Field("id_publi") idPublicacion: Int,
        @Field("comentario") comentario: String
    ): CrearComentarioResponse
}
