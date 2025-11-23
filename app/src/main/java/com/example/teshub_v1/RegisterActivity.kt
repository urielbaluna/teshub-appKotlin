package com.example.teshub_v1

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.teshub_v1.network.RetrofitClient
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RegisterActivity : AppCompatActivity() {

    // --- Variables para la imagen ---
    private lateinit var ivProfileImage: CircleImageView
    private var selectedImageUri: Uri? = null

    // --- Launcher para el selector de imágenes ---
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Usar Glide para mostrar la imagen seleccionada
            Glide.with(this)
                .load(it)
                .into(ivProfileImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // --- Inicialización de vistas (incluida la imagen) ---
        ivProfileImage = findViewById(R.id.ivProfileImage)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etApellido = findViewById<EditText>(R.id.etApellido)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRegistro)
        val etMatricula = findViewById<EditText>(R.id.etMatricula)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegistro)
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvYaTengoCuenta = findViewById<TextView>(R.id.tvYaTengoCuenta)

        // --- Listener para el botón de seleccionar imagen ---
        btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }


        // --- Listener para el icono de ver/ocultar contraseña (NUEVO) ---
        ivTogglePassword.setOnClickListener {
            // Comprobar el tipo de input actual
            if (etPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Si está oculta -> Mostrarla
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_visibility) // Cambiar al icono de ojo abierto
            } else {
                // Si está visible -> Ocultarla
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_visibility_off) // Cambiar al icono de ojo cerrado
            }
            // Mover el cursor al final del texto después de cambiar el tipo
            etPassword.setSelection(etPassword.text.length)
        }
        // Botón Registrar
        btnRegistrar.setOnClickListener {
            // ... (validaciones existentes)
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val matricula = etMatricula.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() ||
                matricula.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "El formato del correo electrónico no es válido.", Toast.LENGTH_LONG).show()
                etCorreo.error = "Correo no válido" // Opcional: marca el campo con error
                return@setOnClickListener
            }

            // 3. Validar requisitos de la contraseña
            if (!validarContrasena(password)) {
                // Muestra un mensaje de error más específico sobre la contraseña
                Toast.makeText(this, "La contraseña no cumple los requisitos.", Toast.LENGTH_LONG).show()
                // Opcional: Muestra un pop-up o un TextView con los requisitos detallados
                etPassword.error = "Debe tener mayúscula, minúscula, número, símbolo y 4+ caracteres"
                return@setOnClickListener
            }

            // Llamar al registro con Retrofit, pasando el URI de la imagen
            CoroutineScope(Dispatchers.IO).launch {
                registrarUsuario(nombre, apellido, correo, matricula, password, selectedImageUri)
            }
        }

        tvYaTengoCuenta.setOnClickListener {
            finish()
        }
    }

    private fun validarContrasena(contrasena: String): Boolean {
        // ... (sin cambios)
        val tieneMayuscula = contrasena.any { it.isUpperCase() }
        val tieneMinuscula = contrasena.any { it.isLowerCase() }
        val tieneNumero = contrasena.any { it.isDigit() }
        val tieneEspecial = contrasena.any { !it.isLetterOrDigit() }
        val longitudMinima = contrasena.length >= 4

        return tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial && longitudMinima
    }

    // --- Función de registro actualizada para incluir el URI de la imagen ---
    private suspend fun registrarUsuario(
        nombre: String, apellido: String, correo: String,
        matricula: String, password: String, imageUri: Uri?
    ) {
        try {
            val nombreBody = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
            val apellidoBody = apellido.toRequestBody("text/plain".toMediaTypeOrNull())
            val correoBody = correo.toRequestBody("text/plain".toMediaTypeOrNull())
            val matriculaBody = matricula.toRequestBody("text/plain".toMediaTypeOrNull())
            val contrasenaBody = password.toRequestBody("text/plain".toMediaTypeOrNull())

            // --- Crear el MultipartBody.Part para la imagen (si existe) ---
            var imagenPart: MultipartBody.Part? = null
            imageUri?.let { uri ->
                val stream = contentResolver.openInputStream(uri)
                val requestFile = stream?.readBytes()?.toRequestBody(
                    contentResolver.getType(uri)?.toMediaTypeOrNull()
                )
                if (requestFile != null) {
                     imagenPart = MultipartBody.Part.createFormData(
                        "imagen",
                        getFileName(uri), // Función para obtener el nombre del archivo
                        requestFile
                    )
                }
            }

            // --- Llamar a la API con todos los datos, incluida la imagen ---
            val response = RetrofitClient.teshubApi.register(
                nombre = nombreBody,
                apellido = apellidoBody,
                correo = correoBody,
                matricula = matriculaBody,
                contrasena = contrasenaBody,
                imagen = imagenPart // Puede ser null si no se seleccionó imagen
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(this@RegisterActivity, response.mensaje, Toast.LENGTH_LONG).show()
                loginDespuesDeRegistro()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val mensajeDeError: String

                if (e is retrofit2.HttpException) {
                    // Si el error es de tipo HTTP (ej: 400, 404, 409, 500)
                    val errorBody = e.response()?.errorBody()?.string()

                    if (errorBody != null) {
                        // Asumimos que tu API envía un mensaje claro en el cuerpo del error
                        when {
                            // IMPORTANTE: ajusta estas frases para que coincidan con la respuesta REAL de tu API
                            errorBody.contains("El correo ya está registrado", ignoreCase = true) -> {
                                mensajeDeError = "Este correo electrónico ya está en uso."
                                findViewById<EditText>(R.id.etCorreoRegistro).error = "Correo ya existe"
                            }
                            // VALIDACIÓN CORREGIDA PARA LA MATRÍCULA
                            errorBody.contains("La matrícula ya está registrada", ignoreCase = true) -> {
                                mensajeDeError = "Esta matrícula ya ha sido registrada."
                                findViewById<EditText>(R.id.etMatricula).error = "Matrícula ya existe"
                            }
                            else -> {
                                // Para otros errores HTTP que no sean por duplicados
                                mensajeDeError = "Respuesta del servidor: $errorBody"
                                Log.e("RegisterHttpError", "Cuerpo del error: $errorBody")
                            }
                        }
                    } else {
                        // Si el error HTTP no tiene cuerpo, mostramos un mensaje genérico
                        mensajeDeError = "Error del servidor (Código: ${e.code()})."
                    }
                } else {
                    // Para otros tipos de errores (ej. sin conexión a internet)
                    mensajeDeError = "Error de conexión. Revisa tu conexión a internet."
                    Log.e("RegisterException", "Error no HTTP en el registro", e)
                }

                // Mostramos el mensaje de error final al usuario
                Toast.makeText(this@RegisterActivity, mensajeDeError, Toast.LENGTH_LONG).show()
            }
        }
    }
    // --- Función auxiliar para obtener el nombre del archivo a partir de un URI ---
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (cut != null) {
                    result = result.substring(cut + 1)
                }
            }
        }
        return result ?: "unknown_file"
    }

    private fun loginDespuesDeRegistro() {
        Toast.makeText(this, "Registro exitoso. Ahora inicia sesión.", Toast.LENGTH_LONG).show()
        finish()
    }
}
