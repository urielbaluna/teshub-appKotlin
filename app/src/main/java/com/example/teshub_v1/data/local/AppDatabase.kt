package com.example.teshub_v1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.teshub_v1.data.local.dao.PerfilDao
import com.example.teshub_v1.data.local.entity.PerfilEntity

@Database(entities = [PerfilEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfilDao(): PerfilDao
}