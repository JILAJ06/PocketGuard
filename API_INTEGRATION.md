# PocketGuard - Integración con API

## 🔧 Configuración de la API

### 1. Configurar la URL Base

Abre el archivo `RetrofitClient.kt` y modifica la constante `BASE_URL`:

```kotlin
// Para emulador Android
private const val BASE_URL = "http://10.0.2.2:3000/"

// Para dispositivo físico (reemplaza con tu IP local)
private const val BASE_URL = "http://192.168.1.X:3000/"

// Para producción
private const val BASE_URL = "https://tu-api.com/"
```

### 2. Endpoints Implementados

La app se conecta a los siguientes endpoints:

- ✅ `POST /api/v1/auth/register` - Registro de usuario
- ✅ `POST /api/v1/auth/login` - Inicio de sesión
- ✅ `POST /api/v1/auth/refresh` - Refrescar token
- ✅ `POST /api/v1/auth/logout` - Cerrar sesión
- ✅ `GET /api/v1/auth/me` - Obtener usuario actual

### 3. Formato de Datos Esperado

#### Registro (Request)
```json
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "password123"
}
```

#### Login (Request)
```json
{
  "email": "juan@example.com",
  "password": "password123"
}
```

#### Response de Autenticación
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "user": {
      "id": "user-id",
      "name": "Juan Pérez",
      "email": "juan@example.com"
    },
    "tokens": {
      "accessToken": "jwt-token-here",
      "refreshToken": "refresh-token-here"
    }
  }
}
```

### 4. Almacenamiento de Tokens

Los tokens se guardan de forma segura usando **DataStore Preferences**:
- `accessToken` - Se envía automáticamente en el header `Authorization: Bearer {token}`
- `refreshToken` - Se usa para renovar el token cuando expira

### 5. Manejo de Errores

La app maneja automáticamente:
- ❌ Errores de red
- ❌ Errores de validación del servidor
- ❌ Tokens expirados
- ❌ Credenciales inválidas

Los errores se muestran al usuario mediante Toast notifications.

---

## 🚀 Cómo Probar

### Opción 1: Emulador Android
1. Asegúrate de que tu API esté corriendo en `localhost:3000`
2. La URL `http://10.0.2.2:3000/` apunta automáticamente al localhost de tu PC
3. Ejecuta la app en el emulador

### Opción 2: Dispositivo Físico
1. Conecta tu dispositivo y PC a la misma red WiFi
2. Encuentra tu IP local:
   - Windows: `ipconfig` → busca "IPv4 Address"
   - Mac/Linux: `ifconfig` → busca "inet"
3. Cambia la URL a `http://TU_IP:3000/`
4. Ejecuta la app en tu dispositivo

### Opción 3: API en Producción
1. Cambia la URL a tu dominio de producción
2. Asegúrate de usar HTTPS
3. Ejecuta la app

---

## 📦 Dependencias Agregadas

```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.5")

// DataStore (almacenamiento seguro)
implementation("androidx.datastore:datastore-preferences:1.1.1")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
```

---

## 🏗️ Arquitectura Implementada

```
app/
├── data/
│   ├── local/
│   │   └── TokenManager.kt          # Manejo seguro de tokens
│   ├── model/
│   │   └── AuthModels.kt            # DTOs de request/response
│   ├── remote/
│   │   ├── AuthApiService.kt        # Definición de endpoints
│   │   └── RetrofitClient.kt        # Cliente HTTP configurado
│   └── repository/
│       └── AuthRepository.kt        # Lógica de negocio
├── presentation/
│   ├── navigation/
│   │   └── NavGraph.kt              # Navegación entre pantallas
│   ├── screens/
│   │   └── HomeScreen.kt            # Pantalla principal
│   └── viewmodel/
│       └── AuthViewModel.kt         # Gestión de estado UI
└── MainActivity.kt                  # Pantallas de Login/Register
```

---

## 🔐 Seguridad

- ✅ Tokens almacenados de forma segura con DataStore
- ✅ HTTPS recomendado para producción
- ✅ Passwords no se almacenan localmente
- ✅ Auto-logout al cerrar sesión
- ✅ Validación de email en el cliente

---

## 🐛 Troubleshooting

### Error: "Unable to resolve host"
- Verifica que la URL de la API sea correcta
- Asegúrate de tener permiso de Internet en el AndroidManifest.xml

### Error: "Connection refused"
- Verifica que tu API esté corriendo
- Si usas emulador, usa `10.0.2.2` en lugar de `localhost`
- Si usas dispositivo físico, verifica que estén en la misma red

### Error: "Unauthorized 401"
- El token expiró o es inválido
- Cierra sesión e inicia sesión nuevamente

---

## 📝 TODO

- [ ] Implementar Google Sign In
- [ ] Agregar refresh automático de token
- [ ] Implementar "Olvidé mi contraseña"
- [ ] Agregar persistencia de sesión al reiniciar la app
- [ ] Implementar pantallas principales (Dashboard, Transacciones, etc.)

---

## 👨‍💻 Desarrollado con

- Kotlin
- Jetpack Compose
- Retrofit 2
- Material Design 3
- MVVM Architecture

