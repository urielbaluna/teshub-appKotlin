package com.example.teshub_v1.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.teshub_v1.BuildConfig
import com.example.teshub_v1.MainActivity
import com.example.teshub_v1.R
import com.example.teshub_v1.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar)
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserRole = view.findViewById(R.id.tv_user_role)
        tvUserEmail = view.findViewById(R.id.tv_user_email)
        tvUserMatricula = view.findViewById(R.id.tv_user_matricula)
        tvTotalPublications = view.findViewById(R.id.tv_total_publications)
        tvFeaturedPublicationTitle = view.findViewById(R.id.tv_featured_publication_title)
        layoutFeaturedPublication = view.findViewById(R.id.layout_destacada)

        val btnLogout = view.findViewById<ImageView>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            logout()
        }

        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        val sharedPref = activity?.getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "No hay sesión activa. Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
            logout()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val perfil = RetrofitClient.teshubApi.getPerfil("Bearer $token")

                withContext(Dispatchers.Main) {
                    tvUserName.text = "${perfil.nombre} ${perfil.apellido}"
                    tvUserRole.text = "Rol: ${perfil.rol}"
                    tvUserEmail.text = perfil.correo
                    tvUserMatricula.text = "Matrícula: ${perfil.matricula}"
                    tvTotalPublications.text = perfil.totalPublicaciones.toString()

                    perfil.imagen?.let { imageUrl ->
                        val fullImageUrl = "${BuildConfig.API_BASE_URL}/$imageUrl"
                        Glide.with(this@PerfilFragment)
                            .load(fullImageUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivProfileAvatar)
                    } ?: run {
                        ivProfileAvatar.setImageResource(R.drawable.ic_profile)
                    }

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
                    org.json.JSONObject(errorBody).optString("mensaje", "Error al cargar perfil.")
                } catch (jsonE: Exception) {
                    "Error de servidor: ${e.code()}"
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("PerfilFragment", "HTTP ${e.code()}: $errorMessage")
                    if (e.code() == 401) logout()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
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
}