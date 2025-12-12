package com.example.teshub_v1.data.model

data class EventoInfo(
    val id: Int,
    val evento_nombre: String,
    val fecha: String
)

data class EventosUsuarioResponse(
    val eventos: List<EventoInfo>
)


