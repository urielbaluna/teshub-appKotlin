package com.example.teshub_v1

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvMensaje = findViewById<TextView>(R.id.tvMensaje)
        tvMensaje.text = "Cargando perfil..."

        val sharedPref = getSharedPreferences("sesion", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            consultarPerfil(token, tvMensaje)
        }
    }

    private suspend fun consultarPerfil(token: String, tvMensaje: TextView) {
        try {
            val url = URL("https://teshub-api-500177fee003.herokuapp.com/ver-info")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } else {
                connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
            }

            withContext(Dispatchers.Main) {
                if (responseCode == 200) {
                    val json = JSONObject(responseText)
                    val nombre = json.optString("nombre")
                    val apellido = json.optString("apellido")
                    val correo = json.optString("correo")
                    val rol = json.optString("rol")

                    tvMensaje.text = "Bienvenida $nombre $apellido\nCorreo: $correo\nRol: $rol"
                } else {
                    val errorMsg = try {
                        JSONObject(responseText).optString("mensaje", "Error desconocido")
                    } catch (e: Exception) {
                        "Error desconocido"
                    }
                    Toast.makeText(this@HomeActivity, errorMsg, Toast.LENGTH_LONG).show()
                    Log.e("PerfilError", "Código: $responseCode, Mensaje: $errorMsg")
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("PerfilException", e.toString())
            }
        }
    }
}