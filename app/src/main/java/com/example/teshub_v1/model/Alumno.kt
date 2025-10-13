package com.example.teshub_v1.model

class Alumno( nombre: String,
              apellido: String,
              correo: String,
              contrasena: String,
              intereses: String,
              val carrera: String,
              val matricula: String
): Usuario(nombre,apellido,correo,contrasena,intereses) {
}