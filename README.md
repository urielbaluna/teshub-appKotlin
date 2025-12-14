Teshub App (Kotlin)
Teshub App es la versión móvil de la plataforma oficial del Tecnológico de Estudios Superiores de Chalco (TESCHA). Esta aplicación está destinada a almacenar, compartir y consultar investigaciones y proyectos académicos, promoviendo la difusión del conocimiento de manera segura y accesible, además de facilitar la retroalimentación y gestión de eventos académicos directamente desde dispositivos Android.

📱 Descripción del Proyecto
Esta aplicación extiende las funcionalidades de la plataforma web Teshub, permitiendo a estudiantes, docentes y asesores interactuar con el repositorio institucional en cualquier lugar. Los usuarios pueden explorar tesis, registrar proyectos, inscribirse a eventos y gestionar sus perfiles académicos.

✨ Características Principales
📚 Repositorio Académico
Exploración de Proyectos: Búsqueda y visualización de tesis y proyectos académicos.

Detalle de Publicaciones: Acceso a información detallada, incluyendo autores, descripciones y archivos adjuntos.

Interacción: Posibilidad de realizar comentarios y valoraciones en las publicaciones.

📅 Gestión de Eventos
Calendario Académico: Visualización de eventos organizados por la institución.

Inscripción: Registro de asistencia a conferencias y talleres.

Ubicación: Integración con Google Maps para localizar la sede de los eventos.

Roles: Funcionalidades diferenciadas para organizadores y asistentes.

👥 Gestión de Usuarios y Perfiles
Autenticación Segura: Inicio de sesión, registro de nuevos usuarios y recuperación de contraseñas.

Perfil Académico: Visualización y edición de perfil, incluyendo foto y datos académicos.

Conexiones: Red de contactos entre miembros de la comunidad.

🎓 Módulo de Asesores
Dashboard de Revisiones: Herramientas exclusivas para asesores que permiten revisar, aprobar, rechazar o solicitar correcciones en los proyectos de tesis asignados.

Gestión de Solicitudes: Administración de solicitudes de asesoría pendientes.

🛠️ Tecnologías y Arquitectura
El proyecto está construido utilizando estándares modernos de desarrollo Android con Kotlin.

Lenguaje: Kotlin (v2.0.21).

Arquitectura: MVVM (Model-View-ViewModel) con Clean Architecture.

Inyección de Dependencias: Dagger Hilt.

Consumo de API REST:

Retrofit 2 para las peticiones HTTP.

Moshi para el parseo de JSON.

OkHttp con Logging Interceptor.

Carga de Imágenes: Glide y CircleImageView.

Mapas: Google Maps SDK for Android.

Diseño de Interfaz:

XML Layouts con ViewBinding habilitado.

Material Design Components.

Concurrencia: Kotlin Coroutines.

⚙️ Configuración del Proyecto
Requisitos Previos
Android Studio Ladybug o superior (recomendado).

JDK 11.

Dispositivo o emulador con Android 7.0 (API 24) o superior.

Variables de Entorno
El proyecto utiliza buildConfigField para gestionar la URL base de la API. Asegúrate de tener configurada la propiedad API_BASE_URL en tu archivo gradle.properties o en las variables de entorno de tu sistema:

Properties

# gradle.properties
API_BASE_URL="http://tu-ip-o-dominio:puerto/api/"
Clave de Google Maps
Para que la funcionalidad de mapas funcione correctamente en SeleccionarUbicacionActivity, debes agregar tu API Key en el archivo AndroidManifest.xml o configurarla como una variable segura.

XML

<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="TU_API_KEY_AQUI" />
🚀 Instalación y Ejecución
Clona el repositorio:

Bash

git clone https://github.com/urielbaluna/teshub-appKotlin.git
Abre el proyecto en Android Studio.

Sincroniza los archivos Gradle para descargar las dependencias.

Configura tu dispositivo virtual o conecta uno físico.

Ejecuta la aplicación (Run 'app').

🤝 Contribución
Las contribuciones son bienvenidas para mejorar la experiencia de la comunidad del TESCHA. Por favor, abre un Issue para discutir cambios mayores antes de enviar un Pull Request.

Desarrollado para el Tecnológico de Estudios Superiores de Chalco.
