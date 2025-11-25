package com.example.teshub_v1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.teshub_v1.fragments.PerfilFragment
import com.example.teshub_v1.fragments.PublicacionesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_container)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // 1. Obtener referencia a la barra de búsqueda
        val searchBar: EditText = findViewById(R.id.search_bar)

        //Boton agregar publicacion
        val fab: FloatingActionButton = findViewById(R.id.fab_add_post)

        fab.setOnClickListener {
            // Abrir la actividad de creación
            val intent = Intent(this, CrearPublicacionActivity::class.java)
            startActivity(intent)
        }

        // 2. Cargar el fragmento de Publicaciones por defecto
        //loadFragment(PublicacionesFragment())
        searchBar.visibility = View.VISIBLE // Aseguramos que inicie visible

        // 3. Implementar la lógica de visibilidad basada en la selección
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // En la vista de Inicio (Publicaciones), el buscador debe ser VISIBLE
                    searchBar.visibility = View.VISIBLE
                    loadFragment(PublicacionesFragment())
                    true
                }
                R.id.nav_profile -> {
                    // En la vista de Perfil, el buscador debe ser OCULTO (View.GONE libera el espacio)
                    searchBar.visibility = View.GONE
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