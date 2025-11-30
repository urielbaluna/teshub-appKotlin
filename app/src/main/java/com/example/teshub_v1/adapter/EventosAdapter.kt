package com.example.teshub_v1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teshub_v1.R
import com.example.teshub_v1.data.model.Evento
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EventosAdapter(
    private val eventos: List<Evento>,
    private val onItemClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.bind(evento)
        holder.itemView.setOnClickListener { onItemClick(evento) }
    }

    override fun getItemCount(): Int = eventos.size

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloEvento)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaEvento)

        fun bind(evento: Evento) {
            tvTitulo.text = evento.titulo
            tvFecha.text = formatIsoDate(evento.fecha)
        }

        private fun formatIsoDate(isoDate: String): String {
            return try {
                // 1. El parser entiende que el string de entrada está en UTC
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(isoDate)

                // 2. El formatter convierte la fecha a la zona horaria local del dispositivo
                val formatter = SimpleDateFormat("dd 'de' MMMM, yyyy 'a las' HH:mm 'hrs'", Locale.getDefault())
                formatter.timeZone = TimeZone.getDefault() // <-- LÍNEA CLAVE AÑADIDA
                date?.let { formatter.format(it) } ?: isoDate
            } catch (e: Exception) {
                isoDate
            }
        }
    }
}
