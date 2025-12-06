package com.example.teshub_v1.data.network

import com.example.teshub_v1.data.model.CrearEventoResponse
import com.example.teshub_v1.data.model.EditarEventoRequest
import com.example.teshub_v1.data.model.Evento
import com.example.teshub_v1.data.model.EventosResponse
import com.example.teshub_v1.data.model.RegistroEventoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface EventosService {

    @GET("api/eventos")
    suspend fun getEventos(@Header("Authorization") token: String): Response<EventosResponse>

    @GET("api/eventos/{id}")
    suspend fun getEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Evento>

    @Multipart
    @POST("api/eventos")
    suspend fun crearEvento(
        @Header("Authorization") token: String,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("fecha") fecha: RequestBody,
        @Part("latitud") latitud: RequestBody,
        @Part("longitud") longitud: RequestBody,
        // --- CORRECCIÓN: Usar el nombre de campo correcto ---
        @Part("organizadores_matriculas") organizadores: RequestBody,
        @Part("cupo_maximo") cupoMaximo: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<CrearEventoResponse>

    @DELETE("api/eventos/{id}")
    suspend fun eliminarEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<CrearEventoResponse>

    @PUT("api/eventos/{id}")
    suspend fun actualizarEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Body evento: EditarEventoRequest
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
    @GET("api/buscar/eventos")
    suspend fun buscarEventos(
        @Header("Authorization") token: String,
        @Query("palabra") palabra: String?,
        @Query("lat") latitud: Double?,
        @Query("lng") longitud: Double?,
        @Query("radioKm") radioKm: Int?,
        @Query("fecha_inicio") fechaInicio: String?,
        @Query("fecha_fin") fechaFin: String?
    ): Response<EventosResponse>


}
