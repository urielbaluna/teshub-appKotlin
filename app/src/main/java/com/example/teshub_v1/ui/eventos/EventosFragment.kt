package com.example.teshub_v1.ui.eventos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teshub_v1.R
import com.example.teshub_v1.adapter.EventosAdapter
import com.example.teshub_v1.data.model.Evento
import com.example.teshub_v1.data.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventosFragment : Fragment() {

    private lateinit var rvEventos: RecyclerView
    private lateinit var eventosAdapter: EventosAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var tvEmptyView: TextView
    private var allEventos: MutableList<Evento> = mutableListOf()
    private var searchJob: Job? = null

    private val detalleEventoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val deletedEventId = result.data?.getIntExtra("EVENTO_ELIMINADO_ID", -1)
            val updatedEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra("EVENTO_ACTUALIZADO", Evento::class.java)
            } else {
                @Suppress("DEPRECATION") result.data?.getParcelableExtra("EVENTO_ACTUALIZADO")
            }

            if (deletedEventId != null && deletedEventId != -1) {
                allEventos.removeAll { it.id == deletedEventId }
                updateUI(allEventos)
                Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
            } else if (updatedEvent != null) {
                val index = allEventos.indexOfFirst { it.id == updatedEvent.id }
                if (index != -1) {
                    allEventos[index] = updatedEvent
                    updateUI(allEventos)
                    Toast.makeText(context, "Evento actualizado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_eventos, container, false)
        rvEventos = view.findViewById(R.id.rvEventos)
        progressBar = view.findViewById(R.id.progressBarEventos)
        searchView = view.findViewById(R.id.search_view_eventos)
        tvEmptyView = view.findViewById(R.id.tv_empty_view_eventos)

        val fabAddEvent: FloatingActionButton = view.findViewById(R.id.fab_add_event)
        fabAddEvent.setOnClickListener {
            val intent = Intent(activity, CrearEventoActivity::class.java)
            startActivity(intent)
        }

        setupSearchView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        if (allEventos.isEmpty()) {
            cargarEventosDesdeAPI()
        }
    }

    private fun setupRecyclerView() {
        eventosAdapter = EventosAdapter(emptyList()) { evento ->
            Log.d("EventosFragment", "Iniciando detalle para evento: ${evento.id}, esOrganizador: ${evento.esOrganizador}")
            val intent = Intent(activity, EventoDetalleActivity::class.java)
            intent.putExtra("EVENTO_EXTRA", evento)
            detalleEventoLauncher.launch(intent)
        }
        rvEventos.adapter = eventosAdapter
        rvEventos.layoutManager = LinearLayoutManager(context)
    }

    private fun cargarEventosDesdeAPI() {
        progressBar.visibility = View.VISIBLE
        tvEmptyView.visibility = View.GONE
        rvEventos.visibility = View.GONE

        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("token", null)

        if (token == null) {
            progressBar.visibility = View.GONE
            if (isAdded && context != null) {
                Toast.makeText(context, "Error de sesión. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            }
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.eventosService.getEventos("Bearer $token")

                if (!isAdded || context == null) return@launch

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val eventos = response.body()?.eventos ?: emptyList()
                    allEventos = eventos.toMutableList()
                    updateUI(allEventos)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("EventosFragment", "Error al cargar eventos: $errorBody")
                    Toast.makeText(context, "Error al cargar eventos: $errorBody", Toast.LENGTH_SHORT).show()
                    updateUI(emptyList()) // Muestra el estado vacío si hay un error
                }
            } catch (e: Exception) {
                if (!isAdded || context == null) return@launch

                progressBar.visibility = View.GONE
                Log.e("EventosFragment", "Excepción: ${e.message}")
                Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
                updateUI(emptyList()) // Muestra el estado vacío si hay una excepción
            }
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel() // Cancela la búsqueda anterior
                searchJob = lifecycleScope.launch {
                    delay(500) // Espera 500ms antes de buscar
                    if (newText.isNullOrEmpty()) {
                        updateUI(allEventos)
                    } else {
                        buscarEventos(newText)
                    }
                }
                return true
            }
        })
    }

    private fun buscarEventos(query: String) {
        progressBar.visibility = View.VISIBLE
        tvEmptyView.visibility = View.GONE
        rvEventos.visibility = View.GONE

        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("token", null)

        if (token == null) {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Error de sesión", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.eventosService.buscarEventos(
                    token = "Bearer $token",
                    palabra = query,
                    latitud = null,
                    longitud = null,
                    radioKm = null,
                    fechaInicio = null,
                    fechaFin = null
                )
                if (response.isSuccessful) {
                    val eventos = response.body()?.eventos ?: emptyList()
                    updateUI(eventos, isSearch = true)
                } else {
                    Log.e("EventosFragment", "Error al buscar eventos: ${response.errorBody()?.string()}")
                    updateUI(emptyList(), isSearch = true)
                }
            } catch (e: Exception) {
                Log.e("EventosFragment", "Excepción en buscarEventos: ${e.message}")
                updateUI(emptyList(), isSearch = true)
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(eventos: List<Evento>, isSearch: Boolean = false) {
        eventosAdapter.updateList(eventos)
        if (eventos.isEmpty()) {
            rvEventos.visibility = View.GONE
            tvEmptyView.visibility = View.VISIBLE
            tvEmptyView.text = if (isSearch) "No se encontraron resultados" else "No hay eventos disponibles"
        } else {
            rvEventos.visibility = View.VISIBLE
            tvEmptyView.visibility = View.GONE
        }
    }
}
