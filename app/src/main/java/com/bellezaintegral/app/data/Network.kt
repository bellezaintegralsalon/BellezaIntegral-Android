package com.bellezaintegral.app.data

import android.content.Context
import com.bellezaintegral.app.BuildConfig
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.UUID

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("belleza_session", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) {
            prefs.edit().apply {
                if (value == null) remove("token") else putString("token", value)
            }.apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

object ApiFactory {
    fun create(session: SessionStore): ApiService {
        val auth = Interceptor { chain ->
            val request = chain.request().newBuilder().apply {
                session.token?.takeIf { it.isNotBlank() }?.let {
                    header("Authorization", "Bearer $it")
                }
                header("Accept", "application/json")
            }.build()
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class ApiException(message: String, val status: Int? = null) : IOException(message)

class BellezaRepository(
    private val api: ApiService,
    private val session: SessionStore
) {
    private suspend fun <T> unwrap(call: suspend () -> Response<ApiEnvelope<T>>): ApiEnvelope<T> {
        val response = call()
        val body = response.body()
        if (response.isSuccessful && body != null && body.success) return body

        val message = try {
            val raw = response.errorBody()?.string().orEmpty()
            JsonParser.parseString(raw).asJsonObject.get("message")?.asString
        } catch (_: Exception) {
            null
        } ?: body?.message ?: "No fue posible completar la operación."

        if (response.code() == 401) session.clear()
        throw ApiException(message, response.code())
    }

    suspend fun login(identifier: String, password: String): User {
        val result = unwrap { api.login(LoginRequest(identifier.trim(), password)) }
        val data = result.data ?: throw ApiException("Respuesta de inicio de sesión inválida.")
        session.token = data.access_token
        return data.usuario
    }

    suspend fun register(name: String, email: String, phone: String, password: String): User {
        val request = RegisterRequest(
            nombre = name.trim(),
            email = email.trim().ifBlank { null },
            telefono = phone.trim().ifBlank { null },
            password = password
        )
        return unwrap { api.register(request) }.data
            ?: throw ApiException("No fue posible crear la cuenta.")
    }

    suspend fun profile(): User = unwrap { api.profile() }.data
        ?: throw ApiException("No fue posible consultar el perfil.")

    suspend fun services(): List<ServiceItem> = unwrap { api.services() }.data.orEmpty()
    suspend fun products(): List<ProductItem> = unwrap { api.products() }.data.orEmpty()
    suspend fun personal(): List<Personal> = unwrap { api.personal() }.data.orEmpty()

    suspend fun availability(personalId: Int, serviceId: Int, date: String): Availability =
        unwrap { api.availability(personalId, serviceId, date) }.data ?: Availability(fecha = date)

    suspend fun createAppointment(personalId: Int, serviceId: Int, date: String, time: String): Appointment =
        unwrap { api.createAppointment(AppointmentRequest(personalId, serviceId, date, time)) }.data
            ?: throw ApiException("No fue posible registrar la cita.")

    suspend fun appointments(): List<Appointment> = unwrap { api.myAppointments() }.data.orEmpty()

    suspend fun cancelAppointment(id: Int) {
        unwrap { api.cancelAppointment(id) }
    }

    suspend fun subscription(): Subscription? = unwrap { api.mySubscription() }.data
    suspend fun plans(): List<Plan> = unwrap { api.plans() }.data.orEmpty()

    suspend fun subscribe(planId: Int): Subscription =
        unwrap {
            api.subscribe(
                SubscriptionPaymentRequest(
                    plan_id = planId,
                    clave_operacion = operationKey()
                )
            )
        }.data ?: throw ApiException("No fue posible activar la suscripción.")

    suspend fun renew(id: Int): Subscription =
        unwrap {
            api.renew(
                id,
                SubscriptionPaymentRequest(clave_operacion = operationKey())
            )
        }.data ?: throw ApiException("No fue posible renovar la suscripción.")

    suspend fun cancelSubscription(id: Int) {
        unwrap {
            api.cancelSubscription(
                id,
                mapOf("motivo" to "Cancelación solicitada desde Android")
            )
        }
    }

    suspend fun points(): Int = unwrap { api.points() }.data?.puntos ?: 0
    suspend fun promotions(): List<Promotion> = unwrap { api.promotions() }.data.orEmpty()

    suspend fun createOrder(
        items: Map<Int, Int>,
        name: String,
        phone: String,
        address: String
    ): Order {
        val request = OrderRequest(
            items = items.map { OrderLineRequest(it.key, it.value) },
            nombre_entrega = name.trim(),
            telefono_entrega = phone.trim(),
            direccion_entrega = address.trim(),
            clave_operacion = operationKey()
        )
        return unwrap { api.createOrder(request) }.data
            ?: throw ApiException("No fue posible registrar el pedido.")
    }

    suspend fun orders(): List<Order> = unwrap { api.myOrders() }.data.orEmpty()

    fun logout() = session.clear()
    fun hasSession(): Boolean = !session.token.isNullOrBlank()

    private fun operationKey(): String =
        "android_" + UUID.randomUUID().toString().replace("-", "")
}
