<h1 align="left">TESHUB Native App Android </h1>

###

<div align="center">
  <img height="200" src="https://i.ibb.co/BV7y8dJv/tes-hub-Photoroom.png"  />
</div>

###

**Teshub App** es la versión móvil de la plataforma oficial del Tecnológico de Estudios Superiores de Chalco (TESCHA). Esta aplicación está destinada a almacenar, compartir y consultar investigaciones y proyectos académicos, promoviendo la difusión del conocimiento de manera segura y accesible, además de facilitar la retroalimentación y gestión de eventos académicos directamente desde dispositivos Android.

## 📱 Descripción del Proyecto

Esta aplicación nativa extiende las funcionalidades de la plataforma web Teshub, permitiendo a estudiantes, docentes y asesores interactuar con el repositorio institucional. Los usuarios pueden explorar tesis, registrar proyectos, inscribirse a eventos y gestionar sus perfiles académicos con una experiencia optimizada para móviles.

## ✨ Características Principales

### 📚 Repositorio Académico
* **Exploración de Proyectos:** Búsqueda y visualización de tesis y proyectos académicos.
* **Detalle de Publicaciones:** Acceso a información detallada, incluyendo autores, descripciones, archivos adjuntos y métricas como vistas y descargas.
* **Interacción:** Posibilidad de realizar comentarios y valoraciones en las publicaciones.

### 📅 Gestión de Eventos
* **Calendario Académico:** Visualización de eventos organizados por la institución con detalles de fecha y descripción.
* **Inscripción y Roles:** Registro de asistencia y gestión diferenciada para organizadores y asistentes.
* **Ubicación:** Integración con **Google Maps** para localizar la sede de los eventos.

### 👥 Gestión de Usuarios y Perfiles
* **Autenticación Segura:** Inicio de sesión y registro de nuevos usuarios.
* **Perfil Académico:** Visualización y edición de perfil.
* **Seguridad:** Gestión de contraseñas y recuperación de cuentas.

### 🎓 Módulo de Asesores
* **Dashboard de Revisiones:** Herramientas exclusivas para asesores que permiten revisar tesis pendientes.
* **Flujo de Aprobación:** Capacidad de aprobar, rechazar o solicitar correcciones (retroalimentación) en los proyectos asignados.
* **Solicitudes:** Administración de solicitudes de asesoría.

## 🛠️ Tecnologías y Arquitectura

El proyecto está construido utilizando estándares modernos de desarrollo Android con **Kotlin**.

* **Lenguaje:** [Kotlin](https://kotlinlang.org/) (v2.0.21).
* **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture.
* **Inyección de Dependencias:** [Dagger Hilt](https://dagger.dev/hilt/).
* **Consumo de API REST:**
    * [Retrofit 2](https://square.github.io/retrofit/) para las peticiones HTTP.
    * [Moshi](https://github.com/square/moshi) para el parseo de JSON.
    * [OkHttp](https://square.github.io/okhttp/) con Logging Interceptor.
* **Carga de Imágenes:** [Glide](https://bumptech.github.io/glide/) y CircleImageView.
* **Mapas:** Google Maps SDK for Android.
* **Diseño de Interfaz:**
    * XML Layouts con ViewBinding habilitado.
    * Material Design Components.
* **Concurrencia:** Kotlin Coroutines.

## ⚙️ Configuración del Proyecto

### Requisitos Previos
* Android Studio Ladybug o superior.
* JDK 11 (definido en `kotlinOptions`).
* Dispositivo con Android 7.0 (Min SDK 24) o superior.

### Variables de Entorno
El proyecto utiliza `buildConfigField` para gestionar la URL base de la API. Asegúrate de tener configurada la propiedad `API_BASE_URL` en tu archivo `gradle.properties` (o variables de sistema) para que el build la reconozca:

``properties
# gradle.properties
API_BASE_URL="http://tu-ip-o-dominio:puerto/api/"
Clave de Google Maps
Para la funcionalidad de mapas, se requiere una API Key válida configurada en el AndroidManifest.xml:

XML

<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="TU_API_KEY_AQUI" />
🚀 Instalación y Ejecución
Clona el repositorio:

Bash

git clone [https://github.com/urielbaluna/teshub-appKotlin.git](https://github.com/urielbaluna/teshub-appKotlin.git)
Abre el proyecto en Android Studio.

Sincroniza los archivos Gradle para descargar las dependencias definidas en libs.versions.toml.

Configura tu dispositivo virtual o físico.

Ejecuta la aplicación (Run 'app').

🤝 Contribución
Las contribuciones son bienvenidas para mejorar la experiencia de la comunidad del TESCHA.

Desarrollado para el Tecnológico de Estudios Superiores de Chalco.
