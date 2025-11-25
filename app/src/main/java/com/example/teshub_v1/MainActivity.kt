package com.example.teshub_v1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                loginUsuario(usuario, password)
            }
        }
    }

    private suspend fun loginUsuario(usuario: String, password: String) {
        try {
            val url = URL("${BuildConfig.API_BASE_URL}/api/usuarios/login")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.doInput = true

            val jsonInput = JSONObject().apply {
                put("correo", usuario)
                put("contrasena", password)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonInput.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseText = try {
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                stream.bufferedReader().use(BufferedReader::readText)
            } catch (e: Exception) {
                ""
            }

            Log.d("LoginDebug", "Código HTTP: $responseCode")
            Log.d("LoginDebug", "Respuesta: $responseText")

            withContext(Dispatchers.Main) {
                if (responseCode in 200..299 && !responseText.startsWith("<!DOCTYPE")) {
                    try {
                        val jsonResponse = JSONObject(responseText)

                        val token = jsonResponse.optString("token", "")
                        val nombre = jsonResponse.optString("nombre", "")
                        val rol = jsonResponse.optString("rol", "")

                        // 🔹 Guardar token en SharedPreferences
                        val sharedPref = getSharedPreferences("sesion", MODE_PRIVATE)
                        sharedPref.edit().putString("token", token).apply()

                        Toast.makeText(
                            this@MainActivity,
                            "Bienvenido $nombre ($rol)",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error al interpretar la respuesta del servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val mensajeError = when {
                        responseText.startsWith("<!DOCTYPE") ->
                            "El servidor devolvió HTML (revisa la ruta o el método POST)."
                        responseText.isEmpty() ->
                            "Sin respuesta del servidor."
                        else -> {
                            try {
                                JSONObject(responseText).optString("mensaje", responseText)
                            } catch (e: Exception) {
                                responseText
                            }
                        }
                    }
                    Toast.makeText(this@MainActivity, mensajeError, Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("LoginError", e.stackTraceToString())
            }
        }
    }
}
