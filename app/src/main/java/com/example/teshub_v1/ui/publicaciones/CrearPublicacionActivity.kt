package com.example.teshub_v1.ui.publicaciones
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

    private val PICK_FILES_REQUEST = 1001
    private var selectedFiles: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_publicacion)

        val etTitulo = findViewById<TextInputEditText>(R.id.et_titulo)
        val etDescripcion = findViewById<TextInputEditText>(R.id.et_descripcion)
        val etColaboradores = findViewById<TextInputEditText>(R.id.et_colaboradores)
        val btnSeleccionarArchivo = findViewById<Button>(R.id.btn_seleccionar_archivo)
        val btnPublicar = findViewById<Button>(R.id.btn_publicar)

        btnSeleccionarArchivo.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Seleccionar archivos"), PICK_FILES_REQUEST)
        }

        btnPublicar.setOnClickListener {
            val titulo = etTitulo.text.toString()
            val descripcion = etDescripcion.text.toString()
            val colaboradores = etColaboradores.text.toString()

            if (titulo.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Título y descripción son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILES_REQUEST && resultCode == RESULT_OK) {
            selectedFiles.clear()

            data?.let {
                if (it.clipData != null) {
                    for (i in 0 until it.clipData!!.itemCount) {
                        selectedFiles.add(it.clipData!!.getItemAt(i).uri)
                    }
                } else if (it.data != null) {
                    selectedFiles.add(it.data!!)
                }
            }

            mostrarArchivosSeleccionados()
        }
    }

    private fun mostrarArchivosSeleccionados() {
        val layout = findViewById<LinearLayout>(R.id.layout_archivos)
        layout.removeAllViews()

        for (uri in selectedFiles) {
            val nombre = obtenerNombreReal(uri)
            val esPDF = nombre.lowercase().endsWith(".pdf")

            agregarArchivo(nombre, esPDF)
        }
    }

    private fun agregarArchivo(nombre: String, esPDF: Boolean) {
        val inflater = LayoutInflater.from(this)
        val card = inflater.inflate(R.layout.item_archivo, null)

        val txtNombre = card.findViewById<TextView>(R.id.txt_nombre_archivo)
        val icono = card.findViewById<ImageView>(R.id.icono_archivo)
        val eliminar = card.findViewById<ImageView>(R.id.btn_eliminar_archivo)
        val layout = findViewById<LinearLayout>(R.id.layout_archivos)

        txtNombre.text = nombre

        icono.setImageResource(
            if (esPDF) R.drawable.ic_pdf else R.drawable.ic_image
        )

        eliminar.setOnClickListener {
            layout.removeView(card)

            // 🔥 También remover del array
            selectedFiles.removeIf { it.toString().contains(nombre) }
        }

        layout.addView(card)
    }

    private fun obtenerNombreReal(uri: Uri): String {
        var fileName = "archivo"
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    fileName = it.getString(index)
                }
            }
        }
        return fileName
    }

    private fun getFileParts(): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()

        for (uri in selectedFiles) {
            val contentResolver = contentResolver
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val fileName = obtenerNombreReal(uri)

            val inputStream = contentResolver.openInputStream(uri) ?: continue
            val bytes = inputStream.readBytes()

            val part = MultipartBody.Part.createFormData(
                "archivos",
                fileName,
                bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )

            parts.add(part)
        }
        return parts
    }

    private fun enviarPublicacion(titulo: String, descripcion: String, colaboradores: String) {
        val sharedPref = getSharedPreferences("sesion", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tituloPart = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val colabPart = colaboradores.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileParts = getFileParts()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.publicacionesService.crearPublicacion(
                    "Bearer $token",
                    tituloPart,
                    descPart,
                    colabPart,
                    if (fileParts.isNotEmpty()) fileParts else null
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CrearPublicacionActivity, "¡Proyecto publicado!", Toast.LENGTH_LONG).show()
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CrearPublicacionActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
