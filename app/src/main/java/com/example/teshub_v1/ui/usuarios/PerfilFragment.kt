package com.example.teshub_v1.ui.usuarios

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.teshub_v1.BuildConfig
import com.example.teshub_v1.R
import com.example.teshub_v1.ui.usuarios.ActualizarUsuarioActivity
import com.example.teshub_v1.data.network.RetrofitClient
import com.example.teshub_v1.ui.auth.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class PerfilFragment : Fragment() {

    private lateinit var ivProfileAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserMatricula: TextView
    private lateinit var tvTotalPublications: TextView
    private lateinit var tvFeaturedPublicationTitle: TextView
    private lateinit var layoutFeaturedPublication: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Inicializar vistas
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar)
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserRole = view.findViewById(R.id.tv_user_role)
        tvUserEmail = view.findViewById(R.id.tv_user_email)
        tvUserMatricula = view.findViewById(R.id.tv_user_matricula)
        tvTotalPublications = view.findViewById(R.id.tv_total_publications)
        tvFeaturedPublicationTitle = view.findViewById(R.id.tv_featured_publication_title)
        layoutFeaturedPublication = view.findViewById(R.id.layout_destacada)

        // Configurar botón de logout
        val btnLogout = view.findViewById<ImageView>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            logout()
        }

        val btnSettings = view.findViewById<ImageView>(R.id.iv_edit_name)
        btnSettings.setOnClickListener {
            startActivity(Intent(context, ActualizarUsuarioActivity::class.java))
        }

        val btnConfig = view.findViewById<ImageView>(R.id.btn_settings)
        btnConfig.setOnClickListener {
            val popup = PopupMenu(requireContext(), btnConfig)
            popup.menu.add("Eliminar cuenta")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Eliminar cuenta"){
                mostrarDialogoEliminar()
                }
                true
            }
            popup.show()
        }

        // Cargar datos del perfil
        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
            // Redirigir al login si no hay token
            logout()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val perfil = RetrofitClient.usuariosService.getPerfil("Bearer $token")

                withContext(Dispatchers.Main) {
                    tvUserName.text = "${perfil.nombre} ${perfil.apellido}"
                    tvUserRole.text = "Rol: ${perfil.rol}"
                    tvUserEmail.text = perfil.correo
                    tvUserMatricula.text = "Matrícula: ${perfil.matricula}"
                    tvTotalPublications.text = perfil.totalPublicaciones.toString()

                    // Manejar la imagen de perfil
                    if (!perfil.imagen.isNullOrEmpty()) {
                        val baseUrl = BuildConfig.API_BASE_URL
                        val finalBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

                        val fullImageUrl = finalBaseUrl + perfil.imagen

                        Glide.with(this@PerfilFragment)
                            .load(fullImageUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivProfileAvatar)
                    } else {
                        ivProfileAvatar.setImageResource(R.drawable.ic_profile)
                    }

                    // Manejar la publicación destacada
                    if (!perfil.publicacionDestacada.isNullOrEmpty()) {
                        tvFeaturedPublicationTitle.text = perfil.publicacionDestacada
                        layoutFeaturedPublication.visibility = View.VISIBLE
                    } else {
                        layoutFeaturedPublication.visibility = View.GONE
                    }
                }

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody).optString("mensaje", "Error al cargar perfil.")
                } catch (jsonE: Exception) {
                    "Error de servidor: ${e.code()}"
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("PerfilFragment", "HTTP ${e.code()}: $errorMessage")
                    // Si es 401 Unauthorized, el token ha expirado o es inválido.
                    if (e.code() == 401) logout()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    Log.e("PerfilFragment", e.stackTraceToString())
                }
            }
        }
    }

    private fun logout() {
        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        with(sharedPref?.edit()) {
            this?.remove("token")
            this?.apply()
        }
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun eliminarCuenta(){
        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
            // Redirigir al login si no hay token
            logout()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = emptyMap<String, String>()
                RetrofitClient.usuariosService.eliminarCuenta("Bearer $token", body)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Cuenta eliminada exitosamente", Toast.LENGTH_LONG).show()
                    logout()
                }


            } catch (e: Exception){
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al eliminar cuenta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarDialogoEliminar(){
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cuenta")
            .setMessage("¿Estás seguro de que deseas eliminar tu cuenta?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCuenta()
            }
            .setNegativeButton("Cancelar", null)
            .show()

    }


}