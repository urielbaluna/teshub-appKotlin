package com.example.teshub_v1.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PublicacionInfo(
    @Json(name = "id_publi") val id_publi: Int,
    @Json(name = "proyecto_nombre") val proyecto_nombre: String,
    val descripcion: String,
    @Json(name = "imagen_portada") val imagen_portada: String?,
    @Json(name = "hace_cuanto") val hace_cuanto: String,
    val estado: String,
    val vistas: Int = 0,
    val descargas: Int = 0,
    val rating: String = "0.0",
    val tags: List<String>? = emptyList()
)