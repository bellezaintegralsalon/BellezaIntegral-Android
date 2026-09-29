package com.bellezaintegral.app.data

data class ApiEnvelope<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null,
    val meta: Meta? = null
)

data class Meta(
    val pagina: Int? = null,
    val limite: Int? = null,
    val cantidad: Int? = null,
    val paginas: Int? = null
)

data class User(
    val id: Int,
    val nombre: String,
    val email: String? = null,
    val telefono: String? = null,
    val rol: String,
    val puntos: Int? = 0
)

data class LoginRequest(val identificador: String, val password: String)
data class LoginData(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val usuario: User
)

data class RegisterRequest(
    val nombre: String,
    val email: String? = null,
    val telefono: String? = null,
    val password: String
)

data class ServiceItem(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val categoria: String? = null,
    val duracion_min: Int = 0,
    val precio: String = "0.00",
    val precio_original: String? = null,
    val descuento_suscripcion: Int = 0,
    val imagen: String? = null
)

data class ProductItem(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val categoria: String? = null,
    val precio: String = "0.00",
    val precio_original: String? = null,
    val descuento_suscripcion: Int = 0,
    val stock: Int = 0,
    val imagen: String? = null
)

data class Personal(
    val id: Int,
    val nombre: String,
    val email: String? = null,
    val telefono: String? = null
)

data class Slot(val hora_inicio: String, val hora_fin: String)

data class Availability(
    val fecha: String? = null,
    val horarios: List<Slot> = emptyList()
)

data class AppointmentRequest(
    val personal_id: Int,
    val servicio_id: Int,
    val fecha: String,
    val hora: String
)

data class Appointment(
    val id: Int,
    val fecha: String? = null,
    val hora: String? = null,
    val estado: String? = null,
    val servicio: String? = null,
    val profesional: String? = null,
    val duracion_min: Int? = null
)

data class Subscription(
    val id: Int,
    val plan: String? = null,
    val plan_descripcion: String? = null,
    val fecha_inicio: String? = null,
    val fecha_fin: String? = null,
    val estado: String? = null,
    val precio_mensual_contratado: String? = null,
    val descuento_servicios_contratado: Int = 0,
    val descuento_productos_contratado: Int = 0,
    val acumulable_promociones_contratado: Boolean = false,
    val renovacion_desde: String? = null,
    val renovacion_disponible: Boolean = false
)

data class Plan(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val precio_mensual: String = "0.00",
    val duracion_meses: Int = 1,
    val descuento_servicios: Int = 0,
    val descuento_productos: Int = 0,
    val acumulable_promociones: Boolean = false
)

data class SubscriptionPaymentRequest(
    val plan_id: Int? = null,
    val metodo_pago: String = "efectivo",
    val tarjeta_ultimos4: String? = null,
    val clave_operacion: String
)

data class Promotion(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val descuento_porcentaje: Int = 0,
    val fecha_inicio: String? = null,
    val fecha_fin: String? = null,
    val puntos_costo: Int = 0
)

data class PointsBalance(val puntos: Int = 0)

data class OrderLineRequest(val producto_id: Int, val cantidad: Int)

data class OrderRequest(
    val items: List<OrderLineRequest>,
    val nombre_entrega: String,
    val telefono_entrega: String,
    val direccion_entrega: String,
    val metodo_pago: String = "efectivo",
    val clave_operacion: String
)

data class Order(
    val id: Int,
    val total: String? = null,
    val estado: String? = null,
    val fecha: String? = null,
    val descuento_suscripcion: Int? = null
)
