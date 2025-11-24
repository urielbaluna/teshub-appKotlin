package com.example.teshub_v1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ForgotpasswordActivity : AppCompatActivity() {

    private val URL_SEND_CODE = "http://teshub.urielbaluna.com/api/usuarios/codigo-contrasena"

    private var isRequestRunning = false
    private val PREFS = "teshub_prefs"
    private val KEY_LAST_REQUEST_TIME = "last_code_request_time"
    private val COOLDOWN_MS = 60 * 60 * 1000L // 60 minutos (igual que expiración del backend)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSend = findViewById<Button>(R.id.btnSendCode)

        btnSend.setOnClickListener {

            if (isRequestRunning) return@setOnClickListener

            val correo = etEmail.text.toString().trim()
            if (correo.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (estaEnCooldown(correo)) {
                Toast.makeText(this, "Ya se envió un código recientemente. Revisa tu correo.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnSend.isEnabled = false
            isRequestRunning = true
            solicitarCodigo(correo, btnSend)
        }
    }

    private fun solicitarCodigo(correo: String, btnSend: Button) {
        val jsonBody = JSONObject().apply {
            put("correo", correo) // La API espera el correo en el body
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            URL_SEND_CODE,
            jsonBody,
            { _ ->
                isRequestRunning = false
                btnSend.isEnabled = true

                // Guardar cooldown para este correo
                guardarCooldown(correo)

                Toast.makeText(this, "Código enviado a tu correo", Toast.LENGTH_SHORT).show()

                // Ir a ResetpasswordActivity y pasar el correo
                val intent = Intent(this, ResetpasswordActivity::class.java).apply {
                    putExtra("correo", correo)
                }
                startActivity(intent)
                finish()
            },
            { error ->
                isRequestRunning = false
                btnSend.isEnabled = true

                val status = error.networkResponse?.statusCode
                val body = error.networkResponse?.data?.let { String(it) } ?: ""

                // Mensajes amigables según status
                when (status) {
                    400 -> Toast.makeText(this, "El correo es obligatorio.", Toast.LENGTH_LONG).show()
                    404 -> Toast.makeText(this, "Correo no encontrado.", Toast.LENGTH_LONG).show()
                    500 -> Toast.makeText(this, "Error del servidor al enviar el correo.", Toast.LENGTH_LONG).show()
                    else -> Toast.makeText(this, "Error ($status): $body", Toast.LENGTH_LONG).show()
                }
            }
        )

        // Sin reintentos automáticos → evita duplicidad de correos
        request.retryPolicy = DefaultRetryPolicy(
            5000, // timeout 5s
            0,    // sin reintentos
            1f
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun estaEnCooldown(correo: String): Boolean {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val lastTime = prefs.getLong(llaveCorreo(correo), 0L)
        return lastTime != 0L && (System.currentTimeMillis() - lastTime) < COOLDOWN_MS
    }

    private fun guardarCooldown(correo: String) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.edit()
            .putLong(llaveCorreo(correo), System.currentTimeMillis())
            .apply()
    }

    private fun llaveCorreo(correo: String) = "$KEY_LAST_REQUEST_TIME:$correo"
}