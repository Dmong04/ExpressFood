# 🍔 ExpressFood

> Aplicación móvil Android con autenticación de Google mediante Firebase.  
> Esta guía cubre la generación e integración de las huellas digitales SHA-1 necesarias para activar el inicio de sesión con Google.

![Android](https://img.shields.io/badge/Android_Studio-3DDC84?style=flat&logo=android-studio&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)
![Google Auth](https://img.shields.io/badge/Google_Sign--In-4285F4?style=flat&logo=google&logoColor=white)

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
│           └── java/...         ← código fuente
├── build.gradle
└── settings.gradle
```

---

## 🔗 Referencias

- [Firebase Authentication — Google Sign-In](https://firebase.google.com/docs/auth/android/google-signin)
- [Obtener huellas digitales SHA — Documentación Android](https://developers.google.com/android/guides/client-auth)
- [Firebase Console](https://console.firebase.google.com/)
