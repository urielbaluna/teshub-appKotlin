package com.example.teshub_v1.ui.comentarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teshub_v1.R
import com.example.teshub_v1.data.model.Comentario

class ComentariosAdapter(
    private val comentarios: List<Comentario>
) : RecyclerView.Adapter<ComentariosAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.tvNombreUsuario)
        val texto: TextView = itemView.findViewById(R.id.tvTextoComentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comentario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comentario = comentarios[position]
        holder.nombre.text = comentario.nombre
        holder.texto.text = comentario.comentario
    }

    override fun getItemCount(): Int = comentarios.size
}
