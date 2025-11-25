package com.example.teshub_v1.ui.publicaciones

import android.content.Intent
import android.net.Uri
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
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CrearPublicacionActivity : AppCompatActivity() {

    private var uriPdf: Uri? = null
    private lateinit var etTitulo: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var etColaboradores: TextInputEditText
    private lateinit var etNombreArchivo: TextInputEditText
    private lateinit var btnPublicar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_publicacion)

        etTitulo = findViewById(R.id.et_titulo)
        etDescripcion = findViewById(R.id.et_descripcion)
        etColaboradores = findViewById(R.id.et_colaboradores)
        etNombreArchivo = findViewById(R.id.et_nombre_archivo)
        btnPublicar = findViewById(R.id.btn_publicar)

        val btnBuscarPdf = findViewById<Button>(R.id.btn_buscar_pdf)

        btnBuscarPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, 1001)
        }

        btnPublicar.setOnClickListener {
            val titulo = etTitulo.text.toString()
            val descripcion = etDescripcion.text.toString()
            val colaboradores = etColaboradores.text.toString()

            if (titulo.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Título y descripción son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (uriPdf == null) {
                Toast.makeText(this, "Debes seleccionar un archivo PDF", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔒 Deshabilitar botón para evitar múltiples clics
            btnPublicar.isEnabled = false

            registrarPublicacion(titulo, descripcion, colaboradores)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            uriPdf = data?.data
            val nombreArchivo = uriPdf?.lastPathSegment ?: "Archivo seleccionado"
            etNombreArchivo.setText(nombreArchivo)
            Toast.makeText(this, "Archivo PDF seleccionado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registrarPublicacion(titulo: String, descripcion: String, colaboradores: String) {
        val token = getSharedPreferences("sesion", MODE_PRIVATE).getString("token", null) ?: return

        val tituloPart = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val colabPart = colaboradores.toRequestBody("text/plain".toMediaTypeOrNull())

        val inputStream = contentResolver.openInputStream(uriPdf!!)
        val pdfBytes = inputStream?.readBytes()
        val archivoBody = pdfBytes?.toRequestBody("application/pdf".toMediaTypeOrNull())
        val archivoPart = MultipartBody.Part.createFormData("archivos", "documento.pdf", archivoBody!!)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.publicacionesService.crearPublicacion(
                    "Bearer $token",
                    tituloPart,
                    descPart,
                    colabPart,
                    listOf(archivoPart)
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CrearPublicacionActivity, "¡Proyecto publicado! ID: ${response.id_publi}", Toast.LENGTH_LONG).show()
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CrearPublicacionActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                // 🔓 Rehabilitar botón al terminar (éxito o error)
                withContext(Dispatchers.Main) {
                    btnPublicar.isEnabled = true
                }
            }
        }
    }
}