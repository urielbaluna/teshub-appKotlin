package com.example.teshub_v1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.teshub_v1.data.model.Estadisticas
import com.example.teshub_v1.data.model.Interes
import com.example.teshub_v1.data.model.PerfilResponse

@Entity(tableName = "perfil_usuario")
data class PerfilEntity(
    @PrimaryKey val matricula: String, // La matrícula es única, sirve de ID
    val nombre: String,
    val apellido: String,
    val correo: String,
    val rol: String,
    val imagen: String?,
    val carrera: String?,
    val semestre: String?,
    val biografia: String?,
    val ubicacion: String?,
    val estado: Int,
    // Estos campos complejos necesitan TypeConverters
    val intereses: List<Interes>,
    val estadisticas: Estadisticas?,
    val totalPublicaciones: Int,
    val publicacionDestacada: String?,
    val siguiendo: Boolean
)

// Función de extensión para convertir tu respuesta de API a Entidad de Base de Datos
fun PerfilResponse.toEntity(): PerfilEntity {
    return PerfilEntity(
        matricula = this.matricula,
        nombre = this.nombre,
        apellido = this.apellido,
        correo = this.correo,
        rol = this.rol,
        imagen = this.imagen,
        carrera = this.carrera,
        semestre = this.semestre,
        biografia = this.biografia,
        ubicacion = this.ubicacion,
        estado = this.estado,
        intereses = this.intereses,
        estadisticas = this.estadisticas,
        totalPublicaciones = this.totalPublicaciones,
        publicacionDestacada = this.publicacionDestacada,
        siguiendo = this.siguiendo
    )
}