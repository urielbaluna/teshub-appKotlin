package com.example.teshub_v1.ui.eventos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.teshub_v1.BuildConfig
import com.example.teshub_v1.R
import com.example.teshub_v1.data.model.Evento
import com.example.teshub_v1.data.network.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EventoDetalleActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var evento: Evento
    private lateinit var mMap: GoogleMap

    private val editarEventoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Esto ya no es suficiente, la actividad debe recargarse con datos nuevos
            finish() // Cierra la actividad actual
            startActivity(intent) // Y la vuelve a abrir para recargar todo
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento_detalle)

        evento = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EVENTO_EXTRA", Evento::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra("EVENTO_EXTRA")
        }!!

        val ivFoto: ImageView = findViewById(R.id.ivFotoEvento)
        val tvTitulo: TextView = findViewById(R.id.tvTituloDetalle)
        val tvOrganizadores: TextView = findViewById(R.id.tvOrganizadoresDetalle)
        val tvFecha: TextView = findViewById(R.id.tvFechaDetalle)
        val tvDescripcion: TextView = findViewById(R.id.tvDescripcionDetalle)

        tvTitulo.text = evento.titulo
        tvOrganizadores.text = evento.organizadoresTexto()
        tvFecha.text = formatIsoDate(evento.fecha)
        tvDescripcion.text = evento.descripcion

        evento.urlFoto?.let {
            val fullImageUrl = if (it.startsWith("http")) it else "${BuildConfig.API_BASE_URL}/$it"
            Glide.with(this)
                .load(fullImageUrl)
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(ivFoto)
        } ?: run {
            // Si no hay foto, mostrar placeholder
            ivFoto.setImageResource(android.R.color.darker_gray)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        verificarPermisosDeUsuario()
    }

    private fun verificarPermisosDeUsuario() {
        val sharedPref = getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val matriculaUsuarioActual = sharedPref.getString("matricula", null)

        val creadorDelEvento = evento.organizadores.firstOrNull()

        if (matriculaUsuarioActual != null && creadorDelEvento != null && matriculaUsuarioActual == creadorDelEvento.matricula) {
            val layoutBotones: LinearLayout = findViewById(R.id.layoutBotonesAdmin)
            val btnEditar: Button = findViewById(R.id.btnEditarEvento)
            val btnEliminar: Button = findViewById(R.id.btnEliminarEvento)

            layoutBotones.visibility = View.VISIBLE

            btnEliminar.setOnClickListener { mostrarDialogoDeConfirmacion() }
            btnEditar.setOnClickListener { 
                val intent = Intent(this, EditarEventoActivity::class.java)
                intent.putExtra("EVENTO_EXTRA", evento)
                editarEventoLauncher.launch(intent)
            }
        }
    }

    private fun mostrarDialogoDeConfirmacion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar este evento? Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarEvento()
            }
            .show()
    }

    private fun eliminarEvento() {
        val sharedPref = getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.eventosService.eliminarEvento(evento.id, "Bearer $token")
                if (response.isSuccessful) {
                    Toast.makeText(this@EventoDetalleActivity, response.body()?.mensaje ?: "Evento eliminado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("EventoDetalleActivity", "Error al eliminar: $errorBody")
                    Toast.makeText(this@EventoDetalleActivity, "Error: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("EventoDetalleActivity", "Excepción al eliminar: ${e.message}")
                Toast.makeText(this@EventoDetalleActivity, "Excepción: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val ubicacion = LatLng(evento.ubicacion.latitud, evento.ubicacion.longitud)
        mMap.addMarker(MarkerOptions().position(ubicacion).title(evento.titulo))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 18f))
    }

    private fun formatIsoDate(isoDate: String): String {
        return try {
            // --- CORRECCIÓN: Intentar primero con el formato que incluye zona horaria ---
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            val date = parser.parse(isoDate)
            val formatter = SimpleDateFormat("dd 'de' MMMM, yyyy 'a las' hh:mm a", Locale.getDefault())
            date?.let { formatter.format(it) } ?: isoDate
        } catch (e: Exception) {
            try {
                // Fallback por si la fecha es antigua (formato UTC)
                val fallbackParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                fallbackParser.timeZone = TimeZone.getTimeZone("UTC")
                val date = fallbackParser.parse(isoDate)
                val formatter = SimpleDateFormat("dd 'de' MMMM, yyyy 'a las' hh:mm a", Locale.getDefault())
                formatter.timeZone = TimeZone.getDefault() // Convertir a la zona local para mostrar
                date?.let { formatter.format(it) } ?: isoDate
            } catch (e2: Exception) {
                isoDate // Si todo falla, mostrar la fecha tal cual
            }
        }
    }
}
