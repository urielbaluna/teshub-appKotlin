package com.example.teshub_v1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.teshub_v1.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {

    // Insertar o Actualizar el perfil (si ya existe la matrícula, lo reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPerfil(perfil: PerfilEntity)

    // Obtener el perfil (retorna Flow para actualizaciones en tiempo real o directo la entidad)
    @Query("SELECT * FROM perfil_usuario LIMIT 1")
    suspend fun obtenerPerfil(): PerfilEntity?

    // Borrar todo (útil al cerrar sesión)
    @Query("DELETE FROM perfil_usuario")
    suspend fun borrarPerfil()
}