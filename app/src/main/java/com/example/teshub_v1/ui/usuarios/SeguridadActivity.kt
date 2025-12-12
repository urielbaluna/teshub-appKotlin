package com.example.teshub_v1.ui.usuarios

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teshub_v1.R
import com.example.teshub_v1.data.network.RetrofitClient
import com.example.teshub_v1.databinding.ActivitySeguridadBinding
import com.example.teshub_v1.ui.auth.MainActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class SeguridadActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeguridadBinding
    private var accion: String? = null // "PASSWORD" o "DELETE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeguridadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir qué acción queremos hacer
        accion = intent.getStringExtra("ACCION_TIPO")

        configurarUI()

        binding.btnSolicitarCodigo.setOnClickListener {
            solicitarCodigo()
        }

        binding.btnConfirmarPass.setOnClickListener {
            cambiarContrasena()
        }

        binding.btnConfirmarEliminar.setOnClickListener {
            eliminarCuenta()
        }
    }

    private fun configurarUI() {
        if (accion == "PASSWORD") {
            binding.tvTituloSeguridad.text = "Cambiar Contraseña"
            binding.layoutCambioPass.visibility = View.VISIBLE
            binding.layoutEliminarCuenta.visibility = View.GONE
        } else {
            binding.tvTituloSeguridad.text = "Eliminar Cuenta"
            binding.layoutCambioPass.visibility = View.GONE
            binding.layoutEliminarCuenta.visibility = View.VISIBLE
        }
    }

    private fun solicitarCodigo() {
        val token = "Bearer ${getSharedPreferences("sesion", Context.MODE_PRIVATE).getString("token", "")}"
        binding.btnSolicitarCodigo.isEnabled = false
        binding.btnSolicitarCodigo.text = "Enviando..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.usuariosService.solicitarCodigoSesion(token)
                if (response.isSuccessful) {
                    Toast.makeText(this@SeguridadActivity, "Código enviado a tu correo", Toast.LENGTH_SHORT).show()
                    binding.layoutVerificacion.visibility = View.VISIBLE
                    binding.btnSolicitarCodigo.visibility = View.GONE // Ocultamos el botón para que se enfoque en el código
                } else {
                    Toast.makeText(this@SeguridadActivity, "Error al enviar código", Toast.LENGTH_SHORT).show()
                    binding.btnSolicitarCodigo.isEnabled = true
                    binding.btnSolicitarCodigo.text = "Reintentar envío"
                }
            } catch (e: Exception) {
                Toast.makeText(this@SeguridadActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSolicitarCodigo.isEnabled = true
            }
        }
    }

    private fun cambiarContrasena() {
        val codigo = binding.etCodigo.text.toString().trim()
        val nuevaPass = binding.etNuevaPass.text.toString().trim()

        if (codigo.length != 6 || nuevaPass.length < 4) {
            Toast.makeText(this, "Verifica el código y la contraseña (mín 4 caracteres)", Toast.LENGTH_SHORT).show()
            return
        }

        val token = "Bearer ${getSharedPreferences("sesion", Context.MODE_PRIVATE).getString("token", "")}"
        val passPart = nuevaPass.toRequestBody("text/plain".toMediaTypeOrNull())
        val codigoPart = codigo.toRequestBody("text/plain".toMediaTypeOrNull())

        lifecycleScope.launch {
            try {
                // Reutilizamos actualizarUsuario que ya soporta cambio de pass con código
                val response = RetrofitClient.usuariosService.actualizarUsuario(
                    token = token,
                    nombre = null, apellido = null, correo = null, // No cambiamos estos
                    contrasena = passPart,
                    codigo = codigoPart
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@SeguridadActivity, "Contraseña actualizada exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@SeguridadActivity, "Error: Código incorrecto o expirado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SeguridadActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarCuenta() {
        val codigo = binding.etCodigo.text.toString().trim()
        if (codigo.length != 6) {
            Toast.makeText(this, "Ingresa el código de 6 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        val token = "Bearer ${getSharedPreferences("sesion", Context.MODE_PRIVATE).getString("token", "")}"

        // Enviamos el código en el body
        val body = mapOf("codigo" to codigo)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.usuariosService.eliminarCuenta(token, body)

                if (response.isSuccessful) {
                    Toast.makeText(this@SeguridadActivity, "Cuenta eliminada. Hasta luego.", Toast.LENGTH_LONG).show()
                    logout()
                } else {
                    Toast.makeText(this@SeguridadActivity, "Error: Código incorrecto", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SeguridadActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        getSharedPreferences("sesion", Context.MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}