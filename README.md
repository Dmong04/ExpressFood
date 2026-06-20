# 🍔 ExpressFood
> Aplicación móvil Android para pedidos de comida, con doble interfaz para **Clientes** y **Administradores**, arquitectura offline-first y autenticación con Google mediante Firebase.

![Android](https://img.shields.io/badge/Android_Studio-3DDC84?style=flat&logo=android-studio&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)
![Google Auth](https://img.shields.io/badge/Google_Sign--In-4285F4?style=flat&logo=google&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-3FCF8E?style=flat&logo=supabase&logoColor=white)

---

## 📖 Descripción general

ExpressFood es una app Android orientada a la gestión de pedidos de comida. Soporta dos roles de usuario:

- **Clientes:** navegan el menú, realizan pedidos y consultan reportes de gasto.
- **Administradores:** gestionan el cumplimiento de pedidos y el inventario.

La aplicación sigue una filosofía **offline-first**: el usuario puede ver el menú y su historial incluso sin conexión activa a internet, gracias a la persistencia local con Room y sincronización en segundo plano.

### ✨ Características principales

- 🔐 **Autenticación con Google:** inicio de sesión seguro usando Firebase Auth y Credential Manager.
- 📡 **Arquitectura offline-first:** persistencia local con Room y sincronización en background.
- ⚡ **Actualizaciones en tiempo real:** seguimiento de pedidos en vivo mediante snapshots de Firestore.
- 🧭 **Acceso basado en roles:** enrutamiento automático al dashboard de Cliente o Admin según el perfil del usuario.
- 📊 **Analítica de gasto:** reportes mensuales y diarios para que los clientes controlen sus gastos en comida.

---

## 🛠️ Stack tecnológico

| Capa                       | Tecnologías                                                  |
| --------------------------- | ------------------------------------------------------------- |
| **Lenguaje**                | Kotlin (Coroutines, Flow)                                     |
| **Framework de UI**         | ViewBinding (Fragments/XML) y Jetpack Compose (Theming)       |
| **Base de datos local**     | Room (SQLite)                                                 |
| **Base de datos remota**    | Firebase Firestore                                            |
| **Almacenamiento**          | Supabase Storage (imágenes de productos)                      |
| **Autenticación**           | Firebase Auth + Google Sign-In (Credential Manager)            |
| **Inyección de dependencias** | Manual, mediante `AppContainer`                              |
| **Tareas en segundo plano** | WorkManager (sincronización periódica)                        |

---

## 🏗️ Arquitectura del sistema

El proyecto sigue el patrón **MVVM (Model-View-ViewModel)** combinado con una capa de **Repository** que abstrae las fuentes de datos. La clase `AppContainer` actúa como el contenedor central de inyección de dependencias, proveyendo instancias singleton de repositorios y servicios a los ViewModels.

### Decisiones arquitectónicas clave

**1. Inyección de dependencias manual**
En lugar de usar frameworks como Hilt o Koin, el proyecto implementa un patrón de DI manual a través de `AppContainer`, inicializado en la clase `ExpressFoodApp` y que mantiene el ciclo de vida de la lógica principal de la app.

**2. Sincronización en segundo plano**
La app usa `WorkManager` para mantener la consistencia de los datos. Un `SyncOrdersWorker` se ejecuta periódicamente (cada hora) para enviar a Firestore los pedidos realizados sin conexión.

**3. Estrategia de almacenamiento híbrida**
ExpressFood distribuye sus datos entre tres proveedores:

- **Room:** caché local para productos, pedidos y el carrito de compras activo.
- **Firestore:** fuente de verdad principal para perfiles de usuario y estado de los pedidos.
- **Supabase:** almacenamiento de imágenes de productos del menú, optimizado para alto rendimiento.

---

## 📱 Capturas de pantalla

| Login | Menú principal | Búsqueda |
| :---: | :---: | :---: |
| ![Login con Google](screenshots/login.jpg) | ![Menú de productos](screenshots/menu.jpg) | ![Búsqueda de platillos](screenshots/busqueda.jpg) |

| Carrito y checkout | Historial de pedidos |
| :---: | :---: |
| ![Carrito y checkout](screenshots/carrito.jpg) | ![Historial de pedidos](screenshots/historial.jpg) |

> Las imágenes se encuentran en la carpeta [`screenshots/`](screenshots/) del repositorio.

---

## 📚 Documentación ampliada

Para más detalle sobre cada módulo del proyecto, consulta la wiki generada (DeepWiki):

- [Getting Started & Build Configuration](https://deepwiki.com/Dmong04/ExpressFood/1.1-getting-started-and-build-configuration)
- [Application Entry Point & Dependency Injection](https://deepwiki.com/Dmong04/ExpressFood/1.2-application-entry-point-and-dependency-injection)
- [Architecture & Data Layer](https://deepwiki.com/Dmong04/ExpressFood/2-architecture-and-data-layer)
- [Authentication](https://deepwiki.com/Dmong04/ExpressFood/3-authentication)
- [Client-Facing UI](https://deepwiki.com/Dmong04/ExpressFood/4-client-facing-ui)
- [Admin UI](https://deepwiki.com/Dmong04/ExpressFood/5-admin-ui)
- [UI Design System & Resources](https://deepwiki.com/Dmong04/ExpressFood/6-ui-design-system-and-resources)
- [Firebase & Backend Configuration](https://deepwiki.com/Dmong04/ExpressFood/7-firebase-and-backend-configuration)
- [Testing](https://deepwiki.com/Dmong04/ExpressFood/8-testing)
- [Glossary](https://deepwiki.com/Dmong04/ExpressFood/9-glossary)

---

## 📋 Prerrequisitos

- Android Studio instalado
- JDK disponible en el sistema (incluye `keytool`)
- Proyecto creado en [Firebase Console](https://console.firebase.google.com/)
- PowerShell (Windows)

---

## 🔧 Paso 1 — Generar el keystore de debug

Ejecuta el siguiente comando en **PowerShell** para crear el keystore de debug de Android:

```powershell
keytool -genkey -v `
  -keystore "$env:USERPROFILE\.android\debug.keystore" `
  -storepass android `
  -alias androiddebugkey `
  -keypass android `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000 `
  -dname "CN=Android Debug, O=Android, C=US"
```

> **Nota:** Este comando genera un par de claves RSA de 2048 bits con una validez de 10 000 días.
> Si el archivo `debug.keystore` ya existe, puedes omitir este paso.

---

## 🔑 Paso 2 — Obtener la huella digital SHA-1

Ejecuta este comando para extraer el SHA-1 de tu keystore:

```powershell
keytool -list -v `
  -alias androiddebugkey `
  -keystore "$env:USERPROFILE\.android\debug.keystore"
```

- **Contraseña del keystore:** `android`
- Copia el valor que aparece en la línea **`SHA1:`** del output.

> ⚠️ **Importante:** El SHA-1 del keystore de **debug** es solo para desarrollo local.
> Para publicar en Google Play Store necesitarás el SHA-1 de tu keystore de **release**.

---

## ☁️ Paso 3 — Registrar el SHA-1 en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/) y abre tu proyecto **ExpressFood**.
2. Haz clic en el ícono de engranaje ⚙️ → **Configuración del proyecto**.
3. En la sección **Tus apps**, selecciona tu app Android.
4. Desplázate hasta **Huellas digitales del certificado SHA**.
5. Haz clic en **Agregar huella digital** y pega el SHA-1 obtenido.
6. Guarda los cambios.

---

## 📥 Paso 4 — Actualizar google-services.json

Después de registrar la huella digital:

1. En la misma pantalla de configuración, descarga el archivo **`google-services.json`** actualizado.
2. Reemplaza el archivo existente en tu proyecto:

```
app/
└── google-services.json   ← reemplazar aquí
```

3. Sincroniza el proyecto en Android Studio: **File → Sync Project with Gradle Files**.

---

## 📁 Estructura relevante del proyecto

```
ExpressFood/
├── app/
│   ├── google-services.json     ← configuración de Firebase
│   └── src/
│       └── main/
│           └── java/
│               └── com/project/expressfood/
│                   ├── data/            ← repositorios, Room, Firestore, Supabase, WorkManager
│                   ├── ui/               ← pantallas de Cliente y Admin, theming
│                   └── ExpressFoodApp.kt ← punto de entrada y AppContainer (DI)
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📱 Capturas de pantalla

| Inicio de sesión | Menú principal | Búsqueda de platillos |
| :---: | :---: | :---: |
| <img width="1080" height="2340" alt="login" src="https://github.com/user-attachments/assets/29599d24-b3a6-4556-9a37-2801b660f46d" />
| <img width="1080" height="2340" alt="menu" src="https://github.com/user-attachments/assets/bc119a58-34d5-4866-856f-8bcdd9deef24" />
| <img width="1080" height="2340" alt="busqueda" src="https://github.com/user-attachments/assets/9cb5951a-7d82-41d8-85b6-c807faf441d2" />
| |
| Pantalla de bienvenida con inicio de sesión mediante Google. | Listado del menú con platillos, precios y calificaciones. | Búsqueda de platillos por nombre o ingrediente. |

| Carrito de compras | Historial de pedidos | |
| :---: | :---: | :---: |
| <img width="1080" height="2340" alt="carrito" src="https://github.com/user-attachments/assets/a6a3d503-c33d-407a-9cc0-6327ce6b66a6" />
| <img width="1080" height="2340" alt="historial" src="https://github.com/user-attachments/assets/ea94b59b-b9c8-48ff-8b24-e729272acfd7" />
| |
| Resumen de la orden con subtotal, impuestos (13% IVA) y total. | Historial de pedidos con fecha, estado y monto. | |

---

## 🔗 Referencias

- [Firebase Authentication — Google Sign-In](https://firebase.google.com/docs/auth/android/google-signin)
- [Obtener huellas digitales SHA — Documentación Android](https://developers.google.com/android/guides/client-auth)
- [Firebase Console](https://console.firebase.google.com/)
- [DeepWiki — ExpressFood Project Overview](https://deepwiki.com/Dmong04/ExpressFood/1-expressfood-project-overview)
