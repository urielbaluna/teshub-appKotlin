package com.example.teshub_v1.ui.eventos

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.teshub_v1.R
import com.example.teshub_v1.data.model.EditarEventoRequest
import com.example.teshub_v1.data.model.Evento
import com.example.teshub_v1.data.network.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class EditarEventoActivity : AppCompatActivity() {

    private lateinit var etTitulo: TextInputEditText
    private lateinit var etOrganizadores: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var etCupoMaximo: TextInputEditText
    private lateinit var etFecha: TextInputEditText
    private lateinit var btnSeleccionarUbicacion: Button
    private lateinit var tvCoordenadas: TextView
    private lateinit var btnGuardarCambios: Button

    private lateinit var eventoActual: Evento
    private val fechaHoraSeleccionada = Calendar.getInstance()
    private var latitudSeleccionada: Double? = null
    private var longitudSeleccionada: Double? = null

    private val locationPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            latitudSeleccionada = data?.getDoubleExtra("latitud", 0.0)
            longitudSeleccionada = data?.getDoubleExtra("longitud", 0.0)
            tvCoordenadas.text = String.format("Lat: %.4f, Lng: %.4f", latitudSeleccionada, longitudSeleccionada)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_evento)

        val eventoRecibido = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EVENTO_EXTRA", Evento::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra<Evento>("EVENTO_EXTRA")
        }

        if (eventoRecibido == null) {
            Log.e("EditarEventoActivity", "No se recibió el evento para editar.")
            Toast.makeText(this, "Error: No se pudo cargar el evento.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        eventoActual = eventoRecibido

        setupViews()
        populateViews()

        etFecha.setOnClickListener { mostrarDatePicker() }
        btnSeleccionarUbicacion.setOnClickListener { locationPickerLauncher.launch(Intent(this, SeleccionarUbicacionActivity::class.java)) }
        btnGuardarCambios.setOnClickListener { guardarCambios() }
    }

    private fun setupViews() {
        etTitulo = findViewById(R.id.etTituloEvento)
        etOrganizadores = findViewById(R.id.etOrganizadoresEvento)
        etDescripcion = findViewById(R.id.etDescripcionEvento)
        etCupoMaximo = findViewById(R.id.etCupoMaximo)
        etFecha = findViewById(R.id.etFechaEvento)
        btnSeleccionarUbicacion = findViewById(R.id.btnSeleccionarUbicacion)
        tvCoordenadas = findViewById(R.id.tvCoordenadasSeleccionadas)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
    }

    private fun populateViews() {
        etTitulo.setText(eventoActual.titulo)
        etDescripcion.setText(eventoActual.descripcion)
        etOrganizadores.setText(eventoActual.organizadores?.joinToString(", ") { it.matricula } ?: "")
        etCupoMaximo.setText(eventoActual.cupoMaximo?.toString() ?: "")

        latitudSeleccionada = eventoActual.latitud?.toDoubleOrNull()
        longitudSeleccionada = eventoActual.longitud?.toDoubleOrNull()
        if (latitudSeleccionada != null && longitudSeleccionada != null) {
            tvCoordenadas.text = String.format("Lat: %.4f, Lng: %.4f", latitudSeleccionada, longitudSeleccionada)
        } else {
            tvCoordenadas.text = "Ubicación no disponible"
        }

        eventoActual.fecha?.let { dateString ->
            var parsedDate: Date? = null
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                parsedDate = parser.parse(dateString)
            } catch (e: ParseException) {
                try {
                    val fallbackParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    fallbackParser.timeZone = TimeZone.getTimeZone("UTC")
                    parsedDate = fallbackParser.parse(dateString)
                } catch (e2: ParseException) {
                    Log.e("EditarEventoActivity", "No se pudo parsear la fecha: '$dateString'", e2)
                }
            }

            if (parsedDate != null) {
                fechaHoraSeleccionada.time = parsedDate
                val formatoUsuario = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                etFecha.setText(formatoUsuario.format(fechaHoraSeleccionada.time))
            } else {
                etFecha.setText(dateString) // Muestra la fecha original si no se pudo parsear
            }
        } ?: run {
            etFecha.setText("Fecha no especificada")
        }
    }

    private fun mostrarDatePicker() {
        val calendario = fechaHoraSeleccionada
        val datePickerDialog = DatePickerDialog(this, {
            _, year, month, dayOfMonth ->
            fechaHoraSeleccionada.set(Calendar.YEAR, year)
            fechaHoraSeleccionada.set(Calendar.MONTH, month)
            fechaHoraSeleccionada.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            mostrarTimePicker()
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun mostrarTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(fechaHoraSeleccionada.get(Calendar.HOUR_OF_DAY))
            .setMinute(fechaHoraSeleccionada.get(Calendar.MINUTE))
            .setTitleText("Selecciona la hora del evento")
            .build()

        picker.addOnPositiveButtonClickListener {
            fechaHoraSeleccionada.set(Calendar.HOUR_OF_DAY, picker.hour)
            fechaHoraSeleccionada.set(Calendar.MINUTE, picker.minute)

            val formatoUsuario = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            etFecha.setText(formatoUsuario.format(fechaHoraSeleccionada.time))
        }
        picker.show(supportFragmentManager, "TimePicker")
    }

    private fun guardarCambios() {
        val sharedPref = getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Error de autenticación.", Toast.LENGTH_SHORT).show()
            return
        }

        if (latitudSeleccionada == null || longitudSeleccionada == null) {
            Toast.makeText(this, "Por favor, selecciona una ubicación.", Toast.LENGTH_SHORT).show()
            return
        }
        
        eventoActual.id?.let { event_Id ->
            lifecycleScope.launch {
                try {
                    val formatoISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                    val fechaISO = formatoISO.format(fechaHoraSeleccionada.time)

                    val eventoRequest = EditarEventoRequest(
                        titulo = etTitulo.text.toString(),
                        descripcion = etDescripcion.text.toString(),
                        fecha = fechaISO,
                        latitud = latitudSeleccionada!!,
                        longitud = longitudSeleccionada!!,
                        organizadores = etOrganizadores.text.toString(),
                        cupo_maximo = etCupoMaximo.text.toString().toIntOrNull()
                    )

                    val response = RetrofitClient.eventosService.actualizarEvento(
                        id = event_Id,
                        token = "Bearer $token",
                        evento = eventoRequest
                    )

                    if (response.isSuccessful) {
                        val eventoActualizado = eventoActual.copy(
                            titulo = etTitulo.text.toString(),
                            descripcion = etDescripcion.text.toString(),
                            fecha = fechaISO,
                            latitud = latitudSeleccionada!!.toString(),
                            longitud = longitudSeleccionada!!.toString(),
                            cupoMaximo = etCupoMaximo.text.toString().toIntOrNull() ?: eventoActual.cupoMaximo
                        )

                        val intentResult = Intent()
                        intentResult.putExtra("EVENTO_ACTUALIZADO", eventoActualizado)
                        setResult(Activity.RESULT_OK, intentResult)
                        finish()
                    } else {
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            val errorBody = withContext(Dispatchers.IO) {
                                response.errorBody()?.string()
                            }
                            Log.e("EditarEventoActivity", "Error al actualizar: $errorBody")
                            Toast.makeText(this@EditarEventoActivity, "Error: $errorBody", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        Log.e("EditarEventoActivity", "Excepción al actualizar: ${e.message}")
                        Toast.makeText(this@EditarEventoActivity, "Excepción: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}