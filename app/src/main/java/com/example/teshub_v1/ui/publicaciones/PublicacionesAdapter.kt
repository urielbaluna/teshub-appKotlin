package com.example.teshub_v1.ui.publicaciones

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.teshub_v1.R
import com.example.teshub_v1.data.model.Publicacion
import com.example.teshub_v1.ui.comentarios.ComentariosActivity
import java.text.SimpleDateFormat
import java.util.*

class PublicacionesAdapter(
    private var publicaciones: List<Publicacion>
) : RecyclerView.Adapter<PublicacionesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tv_titulo_proyecto)
        val descripcion: TextView = itemView.findViewById(R.id.tv_descripcion_proyecto)
        val calificacion: TextView = itemView.findViewById(R.id.tv_calificacion)
        val fecha: TextView = itemView.findViewById(R.id.tv_fecha_proyecto)
        val comentariosLayout: LinearLayout = itemView.findViewById(R.id.comentariosLayout)
        val contadorComentarios: TextView = itemView.findViewById(R.id.contadorComentarios)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_publicacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val publicacion = publicaciones[position]

        holder.titulo.text = publicacion.titulo ?: "Sin título"
        holder.descripcion.text = publicacion.descripcion ?: "Sin descripción"
        holder.calificacion.text = "★ ${publicacion.calificacion ?: 0.0}"
        holder.fecha.text = formatearFecha(publicacion.fecha)
        holder.contadorComentarios.text = publicacion.comentarios?.size?.toString() ?: "0"

        holder.comentariosLayout.setOnClickListener {
            val context = holder.itemView.context
            val id = publicacion.id

            if (id != null && id > 0) {
                val intent = Intent(context, ComentariosActivity::class.java)
                intent.putExtra("idPublicacion", id)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "ID de publicación no disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = publicaciones.size

    fun updateList(nuevas: List<Publicacion>) {
        publicaciones = nuevas
        notifyDataSetChanged()
    }

    private fun formatearFecha(fechaIso: String?): String {
        return try {
            val formatoIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            formatoIso.timeZone = TimeZone.getTimeZone("UTC")
            val date = fechaIso?.let { formatoIso.parse(it) }
            val formatoLatam = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
            date?.let { formatoLatam.format(it) } ?: "Sin fecha"
        } catch (e: Exception) {
            "Sin fecha"
        }
    }
}