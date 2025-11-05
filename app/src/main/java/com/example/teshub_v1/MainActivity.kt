package com.example.teshub_v1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val us = findViewById<EditText>(R.id.etUsuario)
        val pass = findViewById<EditText>(R.id.etPassword)
        val login = findViewById<Button>(R.id.btnLogin)

        login.setOnClickListener {
            val usuario = us.text.toString()
            val password = pass.text.toString()
            if (usuario.equals("are")&& password.equals("123")){
                val intencion = Intent(this, Home::class.java)
                intencion.putExtra("usuario", usuario)
                startActivity(intencion)
            }else{
                Toast.makeText(this,"No bienvenido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}