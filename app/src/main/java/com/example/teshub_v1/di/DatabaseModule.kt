package com.example.teshub_v1.di

import android.content.Context
import androidx.room.Room
import com.example.teshub_v1.data.local.AppDatabase
import com.example.teshub_v1.data.local.dao.PerfilDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "teshub_db"
        ).build()
    }

    @Provides
    fun providePerfilDao(database: AppDatabase): PerfilDao {
        return database.perfilDao()
    }
}