package com.example.teshub_v1.ui.eventos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
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
import kotlinx.coroutines.launch

class EventosFragment : Fragment() {

    private lateinit var rvEventos: RecyclerView
    private lateinit var eventosAdapter: EventosAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private var allEventos: List<Evento> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_eventos, container, false)
        rvEventos = view.findViewById(R.id.rvEventos)
        progressBar = view.findViewById(R.id.progressBarEventos)
        searchView = view.findViewById(R.id.search_view_eventos)

        val fabAddEvent: FloatingActionButton = view.findViewById(R.id.fab_add_event)
        fabAddEvent.setOnClickListener {
            val intent = Intent(activity, CrearEventoActivity::class.java)
            startActivity(intent)
        }

        setupSearchView()
        return view
    }

    override fun onResume() {
        super.onResume()
        cargarEventosDesdeAPI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        eventosAdapter = EventosAdapter(emptyList()) { evento ->
            val intent = Intent(activity, EventoDetalleActivity::class.java)
            intent.putExtra("EVENTO_EXTRA", evento)
            startActivity(intent)
        }
        rvEventos.adapter = eventosAdapter
        rvEventos.layoutManager = LinearLayoutManager(context)
    }

    private fun cargarEventosDesdeAPI() {
        progressBar.visibility = View.VISIBLE

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
                    allEventos = response.body()?.eventos ?: emptyList()
                    eventosAdapter.updateList(allEventos)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("EventosFragment", "Error al cargar eventos: $errorBody")
                    Toast.makeText(context, "Error al cargar eventos: $errorBody", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (!isAdded || context == null) return@launch

                progressBar.visibility = View.GONE
                Log.e("EventosFragment", "Excepción: ${e.message}")
                Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrEmpty()){
                    eventosAdapter.updateList(allEventos)
                } else {
                    buscarEventos(newText)
                }
                return true
            }
        })
    }
    private fun buscarEventos(query: String) {
        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("token", null)

        if (token == null) {
            Toast.makeText(context, "Error de sesión", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
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
                    eventosAdapter.updateList(eventos)
                } else {
                    Log.e("EventosFragment", "Error al buscar eventos: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("EventosFragment", "Excepción en buscarEventos: ${e.message}")
            }
        }
    }
}
