package com.example.teshub_v1.data.local

import androidx.room.TypeConverter
import com.example.teshub_v1.data.model.Estadisticas
import com.example.teshub_v1.data.model.Interes
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Convertidor para Estadisticas
    @TypeConverter
    fun fromEstadisticas(estadisticas: Estadisticas?): String? {
        return estadisticas?.let { moshi.adapter(Estadisticas::class.java).toJson(it) }
    }

    @TypeConverter
    fun toEstadisticas(json: String?): Estadisticas? {
        return json?.let { moshi.adapter(Estadisticas::class.java).fromJson(it) }
    }

    // Convertidor para List<Interes>
    @TypeConverter
    fun fromInteresesList(intereses: List<Interes>?): String? {
        val type = Types.newParameterizedType(List::class.java, Interes::class.java)
        val adapter = moshi.adapter<List<Interes>>(type)
        return intereses?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toInteresesList(json: String?): List<Interes>? {
        val type = Types.newParameterizedType(List::class.java, Interes::class.java)
        val adapter = moshi.adapter<List<Interes>>(type)
        return json?.let { adapter.fromJson(it) }
    }
}