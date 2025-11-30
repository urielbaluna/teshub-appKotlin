package com.example.teshub_v1.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.teshub_v1.R
import com.example.teshub_v1.ui.eventos.EventosFragment // Importación añadida
import com.example.teshub_v1.ui.usuarios.PerfilFragment
import com.example.teshub_v1.ui.publicaciones.CrearPublicacionActivity
import com.example.teshub_v1.ui.publicaciones.PublicacionesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_container)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val searchBar: EditText = findViewById(R.id.search_bar)
        val fab: FloatingActionButton = findViewById(R.id.fab_add_post)

        fab.setOnClickListener {
            val intent = Intent(this, CrearPublicacionActivity::class.java)
            startActivity(intent)
        }

        // Configuración inicial
        searchBar.visibility = View.VISIBLE
        fab.visibility = View.VISIBLE

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    searchBar.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                    loadFragment(PublicacionesFragment())
                    true
                }
                R.id.nav_eventos -> {
                    searchBar.visibility = View.GONE
                    fab.visibility = View.GONE
                    loadFragment(EventosFragment())
                    true
                }
                R.id.nav_profile -> {
                    searchBar.visibility = View.GONE
                    fab.visibility = View.GONE
                    loadFragment(PerfilFragment())
                    true
                }
                else -> false
            }
        }

        // Cargar el fragmento inicial
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
