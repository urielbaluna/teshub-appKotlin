package com.example.teshub_v1.ui.usuarios

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teshub_v1.BuildConfig
import com.example.teshub_v1.R
import com.example.teshub_v1.data.local.dao.PerfilDao
import com.example.teshub_v1.data.local.entity.PerfilEntity
import com.example.teshub_v1.data.local.entity.toEntity
import com.example.teshub_v1.data.model.PerfilResponse
import com.example.teshub_v1.data.model.PublicacionInfo
import com.example.teshub_v1.data.network.RetrofitClient
import com.example.teshub_v1.ui.auth.MainActivity
import com.example.teshub_v1.ui.eventos.EventoDetalleActivity
import com.example.teshub_v1.ui.eventos.EventosAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.CancellationException
import javax.inject.Inject

@AndroidEntryPoint // <--- 1. IMPORTANTE: Permite inyectar dependencias con Hilt
class PerfilFragment : Fragment() {

    // <--- 2. Inyectamos el DAO para usar la base de datos local
    @Inject
    lateinit var perfilDao: PerfilDao

    private lateinit var ivProfileAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserCareer: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserMatricula: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvLocation: TextView

    // Estadísticas
    private lateinit var tvStatPubs: TextView
    private lateinit var tvStatFollowers: TextView
    private lateinit var tvStatFollowing: TextView

    // Intereses
    private lateinit var chipGroupInterests: ChipGroup

    // Lista
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterPerfil: PerfilPublicacionAdapter
    private lateinit var layoutSinAsesor: LinearLayout
    private lateinit var layoutConAsesor: LinearLayout
    private lateinit var tvAsesorNombre: TextView
    private lateinit var tvAsesorEstado: TextView
    private lateinit var ivAsesorAvatar: ImageView
    private lateinit var btnSolicitarAsesor: Button
    private lateinit var cardAsesor: View
    private lateinit var tabLayout: TabLayout
    private lateinit var rvEventos: RecyclerView
    private lateinit var adapterEventos: EventosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Bind Views
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar)
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserCareer = view.findViewById(R.id.tv_user_career)
        tvUserEmail = view.findViewById(R.id.tv_user_email)
        tvUserMatricula = view.findViewById(R.id.tv_user_matricula)

        tvBio = view.findViewById(R.id.tv_biography)
        tvLocation = view.findViewById(R.id.tv_location)

        tvStatPubs = view.findViewById(R.id.tv_stat_publications)
        tvStatFollowers = view.findViewById(R.id.tv_stat_followers)
        tvStatFollowing = view.findViewById(R.id.tv_stat_following)

        chipGroupInterests = view.findViewById(R.id.chip_group_interests)
        recyclerView = view.findViewById(R.id.rv_publicaciones_usuario)
        tabLayout = view.findViewById(R.id.tab_layout_perfil)
        rvEventos = view.findViewById(R.id.rv_eventos_usuario)

        // Configurar RecyclerView de Eventos
        rvEventos.layoutManager = LinearLayoutManager(context)
        adapterEventos = EventosAdapter(mutableListOf()) { evento ->
            val intent = Intent(context, EventoDetalleActivity::class.java)
            intent.putExtra("evento_id", evento.id)
            startActivity(intent)
        }
        rvEventos.adapter = adapterEventos

        // Configurar RecyclerView de Publicaciones
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapterPerfil = PerfilPublicacionAdapter(
            mutableListOf(),
            onItemClick = { pub ->
                val intent = Intent(context, com.example.teshub_v1.ui.publicaciones.PublicacionDetalleActivity::class.java)
                intent.putExtra("id_publi", pub.id_publi)
                startActivity(intent)
            },
            onDeleteClick = { pub -> confirmDelete(pub) }
        )
        recyclerView.adapter = adapterPerfil

        // Listeners
        view.findViewById<ImageView>(R.id.btn_logout).setOnClickListener { logout() }
        view.findViewById<ImageView>(R.id.iv_edit_profile).setOnClickListener {
            startActivity(Intent(context, ActualizarUsuarioActivity::class.java))
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> mostrarPublicaciones()
                    1 -> mostrarEventos()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        layoutSinAsesor = view.findViewById(R.id.layout_sin_asesor)
        layoutConAsesor = view.findViewById(R.id.layout_con_asesor)
        tvAsesorNombre = view.findViewById(R.id.tv_asesor_nombre)
        tvAsesorEstado = view.findViewById(R.id.tv_asesor_estado)
        ivAsesorAvatar = view.findViewById(R.id.iv_asesor_avatar)
        btnSolicitarAsesor = view.findViewById(R.id.btn_solicitar_asesor)
        cardAsesor = view.findViewById(R.id.card_asesor)

        btnSolicitarAsesor.setOnClickListener { mostrarDialogoSolicitud() }

        loadUserProfile()

        return view
    }

    // <--- 3. Lógica Modificada para usar SQLite y Red
    private fun loadUserProfile() {
        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("token", null)

        if (token.isNullOrEmpty()) {
            logout()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // A) PRIMERO: Intentar cargar datos locales (Modo Offline)
                val perfilLocalEntity = perfilDao.obtenerPerfil()
                if (perfilLocalEntity != null) {
                    // Convertimos Entity -> Response para reusar la UI
                    val perfilLocal = mapEntityToResponse(perfilLocalEntity)
                    actualizarVistaPerfil(perfilLocal, token, esDatosLocales = true)
                }

                // B) SEGUNDO: Obtener datos frescos de la red
                val response = RetrofitClient.usuariosService.getPerfil("Bearer $token")

                if (!isAdded || context == null) return@launch
                if (response.isSuccessful && response.body() != null) {
                    val perfilRemoto = response.body()!!

                    // C) Actualizar UI con datos frescos
                    actualizarVistaPerfil(perfilRemoto, token, esDatosLocales = false)

                    // D) GUARDAR EN BASE DE DATOS LOCAL (Cache)
                    // Usamos la extensión toEntity() que creamos en el paso anterior
                    perfilDao.guardarPerfil(perfilRemoto.toEntity())

                } else {
                    // Si falla la red y no teniamos datos locales, mostramos error
                    if (perfilLocalEntity == null) {
                        Toast.makeText(context, "Error al obtener perfil: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                if (isAdded) {
                    // Si hay error de red pero tenemos datos locales, no asustamos al usuario
                    // Solo mostramos error si no hay nada que mostrar
                    val tieneDatos = perfilDao.obtenerPerfil() != null
                    if (!tieneDatos) {
                        Toast.makeText(context, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.e("PERFIL", "Error", e)
            }
        }
    }

    // <--- 4. Función extraída para "pintar" la pantalla (Reutilizable)
    private fun actualizarVistaPerfil(perfil: PerfilResponse, token: String, esDatosLocales: Boolean) {
        // Datos Básicos
        tvUserName.text = "${perfil.nombre} ${perfil.apellido}"
        tvUserEmail.text = perfil.correo
        tvUserMatricula.text = perfil.matricula

        // Datos Extendidos
        val carrera = perfil.carrera ?: "Carrera no especificada"
        val semestre = perfil.semestre ?: "?"
        tvUserCareer.text = "$carrera • $semestre semestre"

        tvBio.text = if (!perfil.biografia.isNullOrEmpty()) perfil.biografia else "Sin biografía disponible."
        tvLocation.text = if (!perfil.ubicacion.isNullOrEmpty()) perfil.ubicacion else "Ubicación no especificada"

        // Estadísticas
        tvStatPubs.text = perfil.totalPublicaciones.toString()
        tvStatFollowers.text = (perfil.estadisticas?.seguidores ?: 0).toString()
        tvStatFollowing.text = (perfil.estadisticas?.seguidos ?: 0).toString()

        // Imagen
        if (!perfil.imagen.isNullOrEmpty()) {
            // Nota: Si viene de local, asegúrate de que la URL sea completa o relativa según cómo la guardes
            val baseUrl = BuildConfig.API_BASE_URL.removeSuffix("/") + "/"
            val fullUrl = if (perfil.imagen.startsWith("http")) perfil.imagen else baseUrl + perfil.imagen
            Glide.with(this@PerfilFragment)
                .load(fullUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(ivProfileAvatar)
        }

        // Intereses
        chipGroupInterests.removeAllViews()
        perfil.intereses.forEach { interes ->
            val chip = Chip(context)
            chip.text = interes.nombre
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setChipStrokeColorResource(android.R.color.darker_gray)
            chip.chipStrokeWidth = 1f
            chipGroupInterests.addView(chip)
        }

        // Lógica de Asesor
        if (perfil.rol.equals("Estudiante", ignoreCase = true) || perfil.rol.contains("Estudiante")) {
            cardAsesor.visibility = View.VISIBLE
            // Solo cargamos el asesor de la red si no estamos en modo offline estricto, o implementamos cache para asesor también
            if (!esDatosLocales) {
                viewLifecycleOwner.lifecycleScope.launch { cargarMiAsesor(token) }
            }
        } else {
            cardAsesor.visibility = View.GONE
        }

        // Cargar publicaciones (Igual, se podría cachear, por ahora solo red)
        if (!esDatosLocales) {
            loadUserPublications(token)
        }
    }

    // Helper simple para convertir la Entidad de DB a Modelo de UI
    private fun mapEntityToResponse(entity: PerfilEntity): PerfilResponse {
        return PerfilResponse(
            matricula = entity.matricula,
            nombre = entity.nombre,
            apellido = entity.apellido,
            correo = entity.correo,
            rol = entity.rol,
            imagen = entity.imagen,
            carrera = entity.carrera,
            semestre = entity.semestre,
            biografia = entity.biografia,
            ubicacion = entity.ubicacion,
            estado = entity.estado,
            intereses = entity.intereses,
            estadisticas = entity.estadisticas,
            totalPublicaciones = entity.totalPublicaciones,
            publicacionDestacada = entity.publicacionDestacada,
            siguiendo = entity.siguiendo
        )
    }

    private suspend fun cargarMiAsesor(token: String) {
        try {
            val response = RetrofitClient.asesoriasService.obtenerMiAsesor("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!

                if (data.asesor != null) {
                    layoutSinAsesor.visibility = View.GONE
                    layoutConAsesor.visibility = View.VISIBLE

                    tvAsesorNombre.text = "${data.asesor.nombre} ${data.asesor.apellido}"
                    tvAsesorEstado.text = "Estado: ${data.estado}"

                    if (data.estado == "Activo") {
                        tvAsesorEstado.setTextColor(resources.getColor(R.color.green_500, null))
                    } else {
                        tvAsesorEstado.setTextColor(resources.getColor(R.color.accentYellow, null))
                    }

                    if (!data.asesor.imagen.isNullOrEmpty()) {
                        val fullUrl = "${BuildConfig.API_BASE_URL}${data.asesor.imagen}"
                        Glide.with(this).load(fullUrl).into(ivAsesorAvatar)
                    }
                } else {
                    layoutSinAsesor.visibility = View.VISIBLE
                    layoutConAsesor.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            Log.e("ASESOR", "Error cargando asesor", e)
        }
    }

    private fun mostrarDialogoSolicitud() {
        val input = EditText(context)
        input.hint = "Matrícula del Profesor (ej: 99001)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        val container = android.widget.FrameLayout(requireContext())
        val params = android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 20, 50, 0)
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(requireContext())
            .setTitle("Solicitar Asesoría")
            .setMessage("Ingresa la matrícula del docente que revisará tus proyectos.")
            .setView(container)
            .setPositiveButton("Enviar Solicitud") { _, _ ->
                val matricula = input.text.toString().trim()
                if (matricula.isNotEmpty()) {
                    enviarSolicitud(matricula)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enviarSolicitud(matriculaProf: String) {
        val token = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)?.getString("token", null) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val body = mapOf("matricula_asesor" to matriculaProf)
                val response = RetrofitClient.asesoriasService.solicitarAsesoria("Bearer $token", body)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Solicitud enviada correctamente", Toast.LENGTH_LONG).show()
                    cargarMiAsesor(token)
                } else {
                    val errMsg = JSONObject(response.errorBody()?.string()).optString("mensaje", "Error al solicitar")
                    Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserPublications(token: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.publicacionesService.obtenerSoloPublicaciones("Bearer $token")

                if (isAdded && response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    adapterPerfil.updateList(data.publicaciones ?: emptyList())
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                } else {
                    Log.e("PERFIL", "Error cargando publicaciones", e)
                }
            }
        }
    }

    private fun eliminarCuenta() {
        val token = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)?.getString("token", null) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                RetrofitClient.usuariosService.eliminarCuenta("Bearer $token", emptyMap())
                // Al eliminar, también limpiamos la BD local para no dejar rastros
                perfilDao.borrarPerfil()

                Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_LONG).show()
                logout()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(publicacion: PublicacionInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar publicación")
            .setMessage("¿Eliminar \"${publicacion.proyecto_nombre}\"?")
            .setPositiveButton("Sí") { _, _ -> eliminarPublicacion(publicacion) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun mostrarDialogoEliminar() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cuenta")
            .setMessage("¿Seguro que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> eliminarCuenta() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarPublicacion(publicacion: PublicacionInfo) {
        val token = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)?.getString("token", null) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.publicacionesService.eliminarPublicacion(publicacion.id_publi, "Bearer $token")

                if (response.isSuccessful) {
                    adapterPerfil.removeItem(publicacion)
                    Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        // Al cerrar sesión, borramos datos locales para seguridad
        viewLifecycleOwner.lifecycleScope.launch {
            perfilDao.borrarPerfil()

            activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)?.edit()?.remove("token")?.apply()
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun mostrarPublicaciones() {
        recyclerView.visibility = View.VISIBLE
        rvEventos.visibility = View.GONE
    }

    private fun mostrarEventos() {
        recyclerView.visibility = View.GONE
        rvEventos.visibility = View.VISIBLE

        if (adapterEventos.itemCount == 0) {
            cargarMisEventos()
        }
    }

    private fun cargarMisEventos() {
        val token = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
            ?.getString("token", null) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.usuariosService.obtenerMisEventos("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val listaEventos = response.body()!!.eventos
                    adapterEventos.updateList(listaEventos)
                } else {
                    Log.e("PERFIL", "Error respuesta eventos: ${response.code()}")
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("PERFIL", "Error cargando eventos", e)
                }
            }
        }
    }
}