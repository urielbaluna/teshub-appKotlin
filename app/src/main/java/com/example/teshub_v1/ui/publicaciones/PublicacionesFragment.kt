package com.example.teshub_v1.ui.publicaciones

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teshub_v1.R
import com.example.teshub_v1.data.model.Publicacion
import com.example.teshub_v1.data.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PublicacionesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PublicacionesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_publicaciones, container, false)

        recyclerView = view.findViewById(R.id.rv_publicaciones)
        progressBar = view.findViewById(R.id.progress_bar)
        tvEmpty = view.findViewById(R.id.tv_empty_view)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PublicacionesAdapter(emptyList())
        recyclerView.adapter = adapter

        cargarPublicaciones()

        return view
    }

    private fun cargarPublicaciones() {
        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("token", null)

        if (token == null) {
            Toast.makeText(context, "Error de sesión", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.publicacionesService.listarPublicaciones("Bearer $token")

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (response.publicaciones.isNotEmpty()) {
                        adapter.updateList(response.publicaciones)
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        tvEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Log.e("PublicacionesFragment", "Error: ${e.message}")
                    Toast.makeText(context, "Error al cargar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}