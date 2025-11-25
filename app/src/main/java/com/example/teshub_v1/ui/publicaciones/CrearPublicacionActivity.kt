package com.example.teshub_v1.ui.publicaciones

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teshub_v1.R
import com.example.teshub_v1.data.network.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class CrearPublicacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_publicacion)

        val etTitulo = findViewById<TextInputEditText>(R.id.et_titulo)
        val etDescripcion = findViewById<TextInputEditText>(R.id.et_descripcion)
        val etColaboradores = findViewById<TextInputEditText>(R.id.et_colaboradores)
        val btnPublicar = findViewById<Button>(R.id.btn_publicar)

        btnPublicar.setOnClickListener {
            val titulo = etTitulo.text.toString()
            val descripcion = etDescripcion.text.toString()
            val colaboradores = etColaboradores.text.toString()

            if (titulo.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Título y descripción son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            enviarPublicacion(titulo, descripcion, colaboradores)
        }
    }

    private fun enviarPublicacion(titulo: String, descripcion: String, colaboradores: String) {
        val sharedPref = getSharedPreferences("sesion", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Convertir Strings a RequestBody (Formato Multipart)
        val tituloPart = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val colabPart = colaboradores.toRequestBody("text/plain".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Nota: Enviamos null en archivos por ahora
                val response = RetrofitClient.publicacionesService.crearPublicacion(
                    "Bearer $token",
                    tituloPart,
                    descPart,
                    colabPart,
                    null
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CrearPublicacionActivity,
                        "¡Proyecto publicado!",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CrearPublicacionActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}