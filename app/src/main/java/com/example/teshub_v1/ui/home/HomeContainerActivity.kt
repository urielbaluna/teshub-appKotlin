package com.example.teshub_v1.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.teshub_v1.ui.publicaciones.CrearPublicacionActivity
import com.example.teshub_v1.R
import com.example.teshub_v1.ui.usuarios.PerfilFragment
import com.example.teshub_v1.ui.publicaciones.PublicacionesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_container)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Obtener referencia a la barra de búsqueda
        val searchBar: EditText = findViewById(R.id.search_bar)

        //Boton agregar publicacion
        val fab: FloatingActionButton = findViewById(R.id.fab_add_post)

        fab.setOnClickListener {
            val intent = Intent(this, CrearPublicacionActivity::class.java)
            startActivity(intent)
        }

        // Configurar la visibilidad inicial del buscador y el botón de agregar publicación
        searchBar.visibility = View.VISIBLE
        fab.visibility = View.VISIBLE

        //Implementar la lógica de visibilidad basada en la selección
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // En la vista de Inicio (Publicaciones), el buscador y agregar publicación debe ser VISIBLE
                    searchBar.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                    loadFragment(PublicacionesFragment())
                    true
                }
                R.id.nav_profile -> {
                    // En la vista de Perfil, el buscador y el botón de agregar publicación debe ser OCULTO (View.GONE libera el espacio)
                    searchBar.visibility = View.GONE
                    fab.visibility = View.GONE
                    loadFragment(PerfilFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}