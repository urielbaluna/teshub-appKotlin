package com.example.teshub_v1

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ResetpasswordActivity : AppCompatActivity() {

    private val URL_RESET_PASSWORD = "http://teshub.urielbaluna.com/api/usuarios/actualizar-contrasena"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val correo = intent.getStringExtra("correo") ?: ""

        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etNueva = findViewById<EditText>(R.id.etNuevaContrasena)
        val etRepetir = findViewById<EditText>(R.id.etRepetirContrasena)
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)

        btnActualizar.setOnClickListener {
            val codigo = etCodigo.text.toString().replace("\\s".toRegex(), "")
            val nueva = etNueva.text.toString()
            val repetir = etRepetir.text.toString()

            if (correo.isEmpty()) {
                Toast.makeText(this, "Falta el correo. Vuelve a solicitar el código.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (codigo.isEmpty() || nueva.isEmpty() || repetir.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (codigo.length != 6 || !codigo.all { it.isDigit() }) {
                Toast.makeText(this, "El código debe ser de 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nueva != repetir) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!cumplePolitica(nueva)) {
                Toast.makeText(this, "La contraseña debe tener mayúscula, minúscula, número, caracter especial y mínimo 4 caracteres.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnActualizar.isEnabled = false

            val jsonBody = JSONObject().apply {
                put("correo", correo)
                put("codigo", codigo)
                put("nuevaContrasena", nueva)
            }

            val request = JsonObjectRequest(
                Request.Method.PUT,
                URL_RESET_PASSWORD,
                jsonBody,
                { _ ->
                    Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                { error ->
                    btnActualizar.isEnabled = true
                    val status = error.networkResponse?.statusCode
                    val body = error.networkResponse?.data?.let { String(it) } ?: ""
                    val mensaje = try {
                        JSONObject(body).optString("mensaje")
                    } catch (_: Exception) { "" }

                    when {
                        status == 400 && mensaje.contains("Código", ignoreCase = true) ->
                            Toast.makeText(this, "Código inválido o expirado. Solicita uno nuevo.", Toast.LENGTH_LONG).show()
                        status == 400 && mensaje.contains("contraseña", ignoreCase = true) ->
                            Toast.makeText(this, "Contraseña no válida. Verifica los requisitos.", Toast.LENGTH_LONG).show()
                        status == 404 ->
                            Toast.makeText(this, "Usuario no encontrado o desactivado.", Toast.LENGTH_LONG).show()
                        status == 500 ->
                            Toast.makeText(this, "Error del servidor al actualizar.", Toast.LENGTH_LONG).show()
                        else ->
                            Toast.makeText(this, "Error ($status): $mensaje", Toast.LENGTH_LONG).show()
                    }
                }
            )

            Volley.newRequestQueue(this).add(request)
        }
    }

    // Política igual que el backend: mayúscula, minúscula, número, especial, mínimo 4
    private fun cumplePolitica(pwd: String): Boolean {
        val tieneMayus = pwd.any { it.isUpperCase() }
        val tieneMinus = pwd.any { it.isLowerCase() }
        val tieneNum = pwd.any { it.isDigit() }
        val tieneEspecial = pwd.any { !it.isLetterOrDigit() }
        return pwd.length >= 4 && tieneMayus && tieneMinus && tieneNum && tieneEspecial
    }
}