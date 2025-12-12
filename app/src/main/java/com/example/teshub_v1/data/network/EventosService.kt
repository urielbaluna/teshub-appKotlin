package com.example.teshub_v1.data.network

import com.example.teshub_v1.data.model.CrearEventoResponse
import com.example.teshub_v1.data.model.EventoDetalleResponse
import com.example.teshub_v1.data.model.EventosResponse
import com.example.teshub_v1.data.model.GeneralResponse
import com.example.teshub_v1.data.model.RegistroEventoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface EventosService {

    @GET("api/eventos")
    suspend fun getEventos(
        @Header("Authorization") token: String,
        @Query("categoria") categoria: String? = null,
        @Query("busqueda") busqueda: String? = null
    ): Response<EventosResponse>

    @GET("api/eventos/{id}")
    suspend fun getEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<EventoDetalleResponse>

    @Multipart
    @POST("api/eventos")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("fecha") fecha: RequestBody,
        @Part("latitud") latitud: RequestBody,
        @Part("longitud") longitud: RequestBody,
        @Part("organizadores_matriculas") organizadores: RequestBody,
        @Part("cupo_maximo") cupoMaximo: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("ubicacion_nombre") ubicacionNombre: RequestBody,
        @Part("tags") tags: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<CrearEventoResponse>

    @DELETE("api/eventos/{id}")
    suspend fun eliminarEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<CrearEventoResponse>

    @Multipart
    @PUT("api/eventos/{id}")
    suspend fun actualizarEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Part("titulo") titulo: RequestBody?,
        @Part("descripcion") descripcion: RequestBody?,
        @Part("fecha") fecha: RequestBody?,
        @Part("latitud") latitud: RequestBody?,
        @Part("longitud") longitud: RequestBody?,
        @Part("organizadores_matriculas") organizadores: RequestBody?,
        @Part("cupo_maximo") cupoMaximo: RequestBody?,
        @Part("categoria") categoria: RequestBody?,
        @Part("ubicacion_nombre") ubicacionNombre: RequestBody?,
        @Part("tags") tags: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): Response<CrearEventoResponse>

    @POST("api/eventos/{id}/registrarse")
    suspend fun registrarseEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<RegistroEventoResponse>

    @DELETE("api/eventos/{id}/cancelar-registro")
    suspend fun cancelarRegistroEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<CrearEventoResponse>
}

interface RevisionesService {

    // (Asesor) Ver qué tesis tengo que revisar
    @GET("api/revisiones/pendientes")
    suspend fun obtenerPendientes(
        @Header("Authorization") token: String
    ): Response<PendientesResponse>

    // (Asesor) Enviar veredicto
    @POST("api/revisiones/revisar")
    suspend fun revisarPublicacion(
        @Header("Authorization") token: String,
        @Body body: RevisionRequest
    ): Response<GeneralResponse>

    // (Ambos) Ver historial de cambios
    @GET("api/revisiones/historial/{id_publi}")
    suspend fun obtenerHistorial(
        @Header("Authorization") token: String,
        @Path("id_publi") idPubli: Int
    ): Response<HistorialResponse>
}