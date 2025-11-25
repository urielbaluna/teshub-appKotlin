package com.example.teshub_v1.data.model

import com.squareup.moshi.Json

data class Publicacion(
    @Json(name = "id_publi")
    val id: Int?,

    @Json(name = "nombre")
    val titulo: String?,

    @Json(name = "descripcion")
    val descripcion: String?,

    @Json(name = "calificacion_promedio")
    val calificacion: Double?,

    @Json(name = "fecha")
    val fecha: String?,

    @Json(name = "comentarios")
    val comentarios: List<Comentario>?
)