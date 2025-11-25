package com.example.teshub_v1.ui.publicaciones

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teshub_v1.R
import com.example.teshub_v1.data.model.Publicacion

class PublicacionesAdapter(
    private var publicaciones: List<Publicacion>,
    private val onClick: (Publicacion) -> Unit // Para manejar clicks en el futuro
) : RecyclerView.Adapter<PublicacionesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tv_titulo_proyecto)
        val tvDescripcion: TextView = view.findViewById(R.id.tv_descripcion_proyecto)
        val tvFecha: TextView = view.findViewById(R.id.tv_fecha_proyecto)
        val tvCalificacion: TextView = view.findViewById(R.id.tv_calificacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Necesitas crear el layout 'item_publicacion.xml'
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_publicacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = publicaciones[position]
        holder.tvTitulo.text = item.nombre
        holder.tvDescripcion.text = item.descripcion

        // Formateo simple de fecha (la API la envía como String ISO)
        holder.tvFecha.text = item.fecha.take(10)

        holder.tvCalificacion.text = "★ ${item.calificacion ?: "0.0"}"

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = publicaciones.size

    fun updateList(newList: List<Publicacion>) {
        publicaciones = newList
        notifyDataSetChanged()
    }
}