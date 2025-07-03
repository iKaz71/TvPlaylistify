# 📺 TV Playlistify ![32](https://github.com/user-attachments/assets/d00a8963-3a2c-41f0-af76-f1b08d9399db)

<div align="center">
  <img src="https://github.com/user-attachments/assets/245eef83-2343-411a-8531-7a6899b4949c" alt="playlistify_logo_transparente" width="350"/>
</div>

**TV Playlistify** es la app central del ecosistema Playlistify. Se instala en cualquier Android TV o dispositivo compatible, y permite que las peticiones de música enviadas desde las apps Playlistify (iOS o Android) se reproduzcan automáticamente en la pantalla grande, utilizando YouTube como backend de reproducción.  

Ideal para bares, reuniones, fiestas y negocios donde la música se vuelve colaborativa y todos pueden aportar a la playlist, sin perder el control ni depender de un solo dispositivo.

---

## 📝 Descripción Técnica

### Objetivo del App

**TV Playlistify** nació de la necesidad de tener una solución centralizada para la música en ambientes colaborativos. Mientras las apps móviles permiten a los usuarios crear y gestionar salas, la app TV actúa como el "DJ central", recibiendo y ejecutando en tiempo real las peticiones de canción de todos los dispositivos conectados.

### ¿Cómo funciona?

- **Conexión en tiempo real:** Las apps Playlistify (Android/iOS) envían peticiones a la TV, que recibe y reproduce la música al instante.
- **Backend unificado:** Utiliza la misma API y servidor que las apps móviles, lo que permite mantener sincronizadas las playlists y salas.
- **YouTube como reproductor:** TV Playlistify utiliza YouTube nativamente para reproducir los videos/música solicitados.
- **Modo espectador:** Nadie controla la TV directamente; las apps móviles son el control remoto.

---

## 📺 Especificaciones técnicas

- **Plataforma recomendada:** Android TV con Android 9.0 o superior.
- **Probado en simulador:**
  - Android TV 12 S ARM – funcionamiento correcto.
  - Android TV Android 14.0 "UpsideDownCake" – funcionamiento correcto.
  - Google TV 16.0 "Baklava" – funcionamiento correcto.
- **Probado en físico:** Sterenbox con Android 9 – funcionamiento perfecto.
- **Probado en Google TV (emulador):** funcionamiento correcto.
- **No probado en Google TV real:** Aunque la app funciona correctamente en el emulador de Google TV, aún falta validación en un dispositivo Google TV físico.
- **Requisito clave:** Se recomienda tener la app de YouTube **actualizada** en el dispositivo.
- **Para música sin interrupciones:** Es necesario utilizar una cuenta de **YouTube Premium** en el dispositivo para evitar anuncios. Sin Premium, la experiencia podría verse interrumpida por publicidad.
- **Detalle importante:** Probar siempre en dispositivos reales para validar la conectividad y experiencia completa del ecosistema Playlistify aunque el simulador para el TV Funciono perfecto.
- **Control:** Totalmente remota, sin requerir interacción local.
- **Dependencias:** Requiere conexión a internet y acceso a YouTube.
  > **Nota:** Al iniciar la app, se solicitará automáticamente el permiso para **superponerse sobre otras aplicaciones** en Android TV. Este permiso es esencial para garantizar que la reproducción musical permanezca visible y estable en todo momento, incluso si se usan otras apps o se navega por el sistema. Si alguna vez no se solicita, puedes activarlo manualmente desde los ajustes del dispositivo.

- **No requiere usuario/contraseña:** Solo se puede escanear el QR y se debe ingresar el código de sala desde la app móvil.
- **Integración:** Compatible con Playlistify Android/iOS y backend Node.js/Express + Firebase.
- **Orientación soportada:** Solo horizontal (landscape), optimizada para pantallas grandes.

- **PLUS:** Aunque **TV Playlistify** está pensado para usarse en pantallas grandes y Android TV, **también puedes instalarlo en un dispositivo móvil** (smartphone o tablet) y conectarlo a una bocina portátil para ambientar lugares sin TV, como la playa o reuniones al aire libre.  
  > **Ten en cuenta:** La app **no está optimizada para orientación vertical**, por lo que la pantalla podría no verse correctamente o estar forzada a modo horizontal. Aun así, esta opción puede sacarte del apuro y seguir disfrutando de la experiencia Playlistify cuando no tienes una TV a mano.


    
---

## ⚙️ Instalación y Configuración

### 1. Clona el repositorio

bash
- git clone https://github.com/iKaz71/TvPlaylistify.git
- cd TvPlaylistify

### 2. Configura tus claves/API

¡Configuración rápida y sin complicaciones!

- **No necesitas ingresar claves manualmente:** TV Playlistify recibe los datos necesarios directamente desde las apps móviles al conectar tu sala.
- **Solo agrega tu archivo `google-services.json`:** Descárgalo desde tu consola de Firebase y colócalo en la carpeta `/app` del proyecto.
- **YouTube Premium recomendado:** Para garantizar reproducción musical sin interrupciones y sin anuncios, asegúrate de iniciar sesión en YouTube con una cuenta Premium en tu dispositivo Android TV.

¡Con esto, tu TV Playlistify estará lista para funcionar con el ecosistema Playlistify!


⚠️ **No subas ni compartas tus claves reales.**

### 3. Instala dependencias

- Abre el proyecto en Android Studio (versión recomendada: Flamingo 2022.2.1 o superior).
- Sincroniza Gradle y espera a que termine la descarga.

### 4. Ejecuta en tu Android TV o emulador

- Usa Android Studio para instalar la app en tu TV física o emulador.
- Abre la app y sigue las instrucciones para vincularla con tu sala Playlistify.



---


## 📸 Capturas de Pantalla


<div align="center">
  <!-- Ejemplo de placeholder -->
  <img src="https://github.com/user-attachments/assets/f1c54b1b-47d1-4f3b-923c-4e2590e28fd2" alt="Pantalla principal" width="350"/>
  
</div>

---

## 🛠️ Tecnologías y librerías utilizadas

TV Playlistify está construido con lo último del ecosistema Android, asegurando compatibilidad, modernidad y rendimiento:

- **Lenguaje principal:** Kotlin
- **SDK:**  
  - *compileSdk:* 35  
  - *minSdk:* 28 (Android 9)  
  - *targetSdk:* 35  
- **Interfaz:** Jetpack Compose (Material 2 y Material 3), ViewBinding
- **Navegación:** Jetpack Navigation Compose
- **Compatibilidad Android TV:**  
  - AndroidX TV Foundation & TV Material  
- **Carga de imágenes:** Coil
- **Base de datos en tiempo real:** Firebase Realtime Database
- **Autenticación:** Firebase Auth, Google Sign-In
- **Gestión de background:** WorkManager
- **QR generator:** QRGen
- **Comunicación con APIs:** Retrofit + Gson

> **Plugins principales:**  
> - `com.android.application`  
> - `org.jetbrains.kotlin.android`  
> - `org.jetbrains.kotlin.plugin.compose`  
> - `com.google.gms.google-services`

**Notas técnicas:**
- Utiliza Java 11 para máxima compatibilidad con librerías modernas.
- No se utiliza API key local, sino autenticación y conexión en tiempo real vía Firebase y los datos enviados desde las apps móviles.
- Todas las dependencias están gestionadas y actualizadas mediante Gradle.

## 📒 Notas Importantes

- **No compartas tus claves/API ni el archivo `google-services.json`.**
- Esta app está en continuo desarrollo y pruebas.
- Requiere conexión estable a internet y acceso libre a YouTube.
- Para aprovechar todas las funciones, usa siempre la última versión de las apps Playlistify móvil.

---

## 📝 Licencia

TV Playlistify se publica bajo la **licencia MIT**.  
Eres libre de modificar y usar el código, pero no se ofrece garantía ni soporte oficial.

---

## 🔧 Backend/API

El backend es un servidor **Node.js/Express + Firebase**.  
La app TV conecta automáticamente con el endpoint de producción por default.

- [Repositorio backend](https://github.com/iKaz71/playlistify-api)

---

## 🚀 Ecosistema Playlistify

- [Playlistify Android](https://github.com/iKaz71/Playlistify-Android)
- [Playlistify iOS](https://github.com/iKaz71/Playlistify-iOS)
- [TV Playlistify](https://github.com/iKaz71/TvPlaylistify)

---

