package com.example.teshub_v1.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Evento(
    val id: Int?,
    val titulo: String?,
    val fecha: String?,
    val descripcion: String?,
    val urlFoto: String?,
    val latitud: String?,
    val longitud: String?,
    val ubicacion: Ubicacion?,
    val cupoMaximo: Int?,
    val usuarioRegistrado: Boolean?,
    val esOrganizador: Boolean?,
    val organizadores: List<Organizador>? = null,
    val asistentesRegistrados: Int? = 0
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
data class Ubicacion(
    val latitud: Double,
    val longitud: Double
) : Parcelable

@Parcelize
data class Organizador(
    val matricula: String,
    val nombre: String,
    val apellido: String?
) : Parcelable
