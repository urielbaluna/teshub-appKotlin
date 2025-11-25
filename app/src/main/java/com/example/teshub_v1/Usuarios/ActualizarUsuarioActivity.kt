package com.example.teshub_v1.Usuarios

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teshub_v1.R
import com.example.teshub_v1.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ActualizarUsuarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizarusuario)

        val edtNombre = findViewById<EditText>(R.id.edtNombre)
        val edtApellido = findViewById<EditText>(R.id.edtApellido)
        val edtCorreo = findViewById<EditText>(R.id.edtCorreo)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)

        btnActualizar.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val apellido = edtApellido.text.toString().trim()
            val correo = edtCorreo.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                actualizarUsuario(nombre, apellido, correo, password)
            }
        }
    }

    private suspend fun actualizarUsuario(nombre: String, apellido: String, correo: String, password: String) {
        try {
            // 🔹 URL de tu API
            val url = URL("${BuildConfig.API_BASE_URL}/api/usuarios/actualizar")

            // 🔹 Obtener token guardado en SharedPreferences
            val sharedPref = getSharedPreferences("sesion", MODE_PRIVATE)
            val token = sharedPref.getString("token", null)
            if (token.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ActualizarUsuarioActivity, "No hay sesión activa", Toast.LENGTH_LONG).show()
                }
                return
            }

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")

            // 🔹 Agregar token en la cabecera
            connection.setRequestProperty("Authorization", "Bearer $token")

            connection.doOutput = true
            connection.doInput = true

            val jsonInput = JSONObject().apply {
                put("nombre", nombre)
                put("apellido", apellido)
                put("correo", correo)
                put("contrasena", password)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonInput.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseText = try {
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                stream.bufferedReader().use(BufferedReader::readText)
            } catch (e: Exception) {
                ""
            }

            Log.d("ActualizarDebug", "Código HTTP: $responseCode")
            Log.d("ActualizarDebug", "Respuesta: $responseText")

            withContext(Dispatchers.Main) {
                if (responseCode in 200..299 && !responseText.startsWith("<!DOCTYPE")) {
                    Toast.makeText(this@ActualizarUsuarioActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val mensajeError = try {
                        JSONObject(responseText).optString("mensaje", responseText)
                    } catch (e: Exception) {
                        responseText
                    }
                    Toast.makeText(this@ActualizarUsuarioActivity, mensajeError, Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ActualizarUsuarioActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ActualizarError", e.stackTraceToString())
            }
        }
    }
}
