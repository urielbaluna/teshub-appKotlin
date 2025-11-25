// src/main/java/com/example/teshub_v1/MainActivity.kt
package com.example.teshub_v1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 💡 Importaciones para el manejo de la nueva capa de red
import com.example.teshub_v1.network.RetrofitClient
import retrofit2.HttpException
import org.json.JSONObject // Mantenido para parsear mensajes de error del servidor

// 💡 Debes cambiar el destino de navegación
// import com.example.teshub_v1.HomeActivity // OLD

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsuario = findViewById<EditText>(R.id.etUsuario) // Asume R.id.etUsuario existe
        val etPassword = findViewById<EditText>(R.id.etPassword) // Asume R.id.etPassword existe
        val btnLogin = findViewById<Button>(R.id.btnLogin) // Asume R.id.btnLogin existe

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
        // Dentro de onCreate(), después de configurar btnLogin:

        val tvRegistrarse = findViewById<TextView>(R.id.tvRegistrarse)
        tvRegistrarse.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun loginUsuario(usuario: String, password: String) {
        try {
            // 1. Preparar la petición JSON (correo y contrasena)
            val loginRequest = mapOf("correo" to usuario, "contrasena" to password)

            // 2. Llamar al servicio Retrofit. Retrofit y Moshi manejan la conexión HTTP
            // y el mapeo de JSON a la Data Class LoginResponse.
            val response = RetrofitClient.teshubApi.login(loginRequest)

            // 3. Éxito: Navegar y guardar el token (en el hilo principal)
            withContext(Dispatchers.Main) {
                val token = response.token
                val nombre = response.nombre
                val rol = response.rol

                // Guardar el token en SharedPreferences
                val sharedPref = getSharedPreferences("sesion", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("token", token)
                    apply()
                }

                Toast.makeText(
                    this@MainActivity,
                    "Bienvenido $nombre ($rol)",
                    Toast.LENGTH_SHORT
                ).show()

                // 💡 Navegar a la Activity Contenedora con la barra inferior
                val intent = Intent(this@MainActivity, HomeContainerActivity::class.java)

                // Pasar datos necesarios
                intent.putExtra("nombre", nombre)
                intent.putExtra("rol", rol)

                startActivity(intent)
                finish()
            }

        } catch (e: HttpException) {
            // Manejar errores HTTP (ej. 401 Unauthorized por credenciales incorrectas)
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                // Intenta parsear el campo 'mensaje' del JSON de error de tu API de Node.js
                JSONObject(errorBody).optString("mensaje", "Credenciales incorrectas")
            } catch (jsonE: Exception) {
                "Error de servidor: ${e.code()}"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                Log.e("LoginError", "HTTP ${e.code()}: $errorMessage")
            }
        } catch (e: Exception) {
            // Manejar errores de red general (Timeout, DNS, conexión perdida)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("LoginError", e.stackTraceToString())
            }
        }
    }
}