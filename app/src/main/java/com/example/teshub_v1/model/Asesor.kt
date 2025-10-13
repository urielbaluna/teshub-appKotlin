package com.example.teshub_v1.model

class Asesor(nombre: String,
             apellido: String,
             correo: String,
             contrasena: String,
             intereses: String,
             val especialidad: String,
             val id: String
): Usuario(nombre,apellido,correo,contrasena,intereses) {
}