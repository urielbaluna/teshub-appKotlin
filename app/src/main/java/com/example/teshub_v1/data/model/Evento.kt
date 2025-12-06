package com.example.teshub_v1.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Evento(
    @Json(name = "id_evento") val id: Int?,
    @Json(name = "titulo") val titulo: String?,
    @Json(name = "fecha") val fecha: String?,
    @Json(name = "descripcion") val descripcion: String?,
    @Json(name = "url_foto") val urlFoto: String?,
    @Json(name = "latitud") val latitud: String?,
    @Json(name = "longitud") val longitud: String?,
    @Json(name = "cupo_maximo") val cupoMaximo: Int?,
    @Json(name = "es_asistente") val usuarioRegistrado: Boolean?,
    @Json(name = "es_organizador") val esOrganizador: Boolean?,
    @Json(name = "organizadores") val organizadores: List<Organizador>? = null,
    @Json(name = "asistentesRegistrados") val asistentesRegistrados: Int? = 0
) : Parcelable {

    fun organizadoresTexto(): String {
        return organizadores?.joinToString(separator = ", ") { "${it.nombre} ${it.apellido ?: ""}" } ?: ""
    }

    val cupoDisponible: Int
        get() = (cupoMaximo ?: 0) - (asistentesRegistrados ?: 0)

    val hayLugaresDisponibles: Boolean
        get() = cupoDisponible > 0

    fun textoAsistencia(): String {
        val maxCupo = cupoMaximo ?: 0
        val registrados = asistentesRegistrados ?: 0
        if (maxCupo > 0) {
            return "$registrados/$maxCupo asistentes"
        }
        return "$registrados asistentes"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class Ubicacion(
    @Json(name = "latitud") val latitud: Double,
    @Json(name = "longitud") val longitud: Double
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Organizador(
    @Json(name = "matricula") val matricula: String,
    @Json(name = "nombre") val nombre: String,
    @Json(name = "apellido") val apellido: String?
) : Parcelable
