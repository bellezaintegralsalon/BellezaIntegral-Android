# Belleza Integral Android

Aplicación Android nativa para clientes de Belleza Integral.

Backend:
https://bellezaintegral-production.up.railway.app/api/v1/

Stack:
- Kotlin
- Jetpack Compose
- Material 3
- Retrofit
- OkHttp
- MVVM
- GitHub Actions

Funcionalidad inicial:
- Registro e inicio de sesión con JWT.
- Persistencia local de sesión.
- Catálogo de servicios.
- Descuento de membresía en servicios.
- Reserva de citas por profesional, fecha y horario.
- Consulta y cancelación de citas.
- Catálogo de productos.
- Descuento de membresía en productos.
- Carrito y creación de pedidos demostrativos.
- Perfil del cliente.
- Suscripciones, contratación, renovación y cancelación.
- Renovación solamente cuando el backend la habilita.
- Consulta de puntos.
- Consulta de promociones.

La lógica crítica permanece en el backend. La app móvil consume los precios,
descuentos, vigencias y permisos calculados por la API.

Compilación automática:
El workflow .github/workflows/build-apk.yml compila un APK debug con cada push
a dev o main y publica el artifact BellezaIntegral-debug-apk.

Ramas:
- dev: desarrollo y pruebas.
- main: versión estable.

Para abrir localmente:
Abrir la raíz del repositorio con Android Studio usando JDK 17 y Android SDK 35.
