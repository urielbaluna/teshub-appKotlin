package com.example.teshub_v1.ui.usuarios

import android.view.LayoutInflater
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.teshub_v1.R
import com.example.teshub_v1.data.model.EventoInfo

class PerfilEventoAdapter(
    private var eventos: List<EventoInfo>,
    private val onItemClick: (EventoInfo) -> Unit
) : RecyclerView.Adapter<PerfilEventoAdapter.EventoViewHolder>(){
    inner class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tv_evento_titulo)
        val tvFecha: TextView = itemView.findViewById(R.id.tv_evento_fecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_usuario, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.tvTitulo.text = evento.evento_nombre
        holder.tvFecha.text = evento.fecha
        holder.itemView.setOnClickListener { onItemClick(evento) }
    }

    override fun getItemCount() = eventos.size

    fun updateList(newList: List<EventoInfo>) {
        eventos = newList
        notifyDataSetChanged()
    }


}