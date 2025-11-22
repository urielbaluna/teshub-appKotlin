package com.example.teshub_v1.network

import com.example.teshub_v1.BuildConfig // 💡 Importante: Accede a la variable de Gradle
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    // 1. Obtiene la URL base de tu archivo gradle.properties
    private const val BASE_URL = BuildConfig.API_BASE_URL

    // 2. Configura Moshi (el motor de JSON)
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // 3. Crea la instancia Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi)) // Conecta Moshi
        .build()

    // 4. Crea el servicio API para que otras clases lo usen
    val teshubApi: TeshubApiService by lazy {
        retrofit.create(TeshubApiService::class.java)
    }
}