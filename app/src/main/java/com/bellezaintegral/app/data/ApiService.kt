package com.bellezaintegral.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginData>>

    @POST("auth/registro")
    suspend fun register(@Body body: RegisterRequest): Response<ApiEnvelope<User>>

    @GET("auth/perfil")
    suspend fun profile(): Response<ApiEnvelope<User>>

    @GET("servicios")
    suspend fun services(@Query("pagina") pagina: Int = 1, @Query("limite") limite: Int = 100): Response<ApiEnvelope<List<ServiceItem>>>

    @GET("productos")
    suspend fun products(@Query("pagina") pagina: Int = 1, @Query("limite") limite: Int = 100): Response<ApiEnvelope<List<ProductItem>>>

    @GET("personal")
    suspend fun personal(): Response<ApiEnvelope<List<Personal>>>

    @GET("disponibilidad")
    suspend fun availability(
        @Query("personal_id") personalId: Int,
        @Query("servicio_id") serviceId: Int,
        @Query("fecha") date: String
    ): Response<ApiEnvelope<Availability>>

    @POST("citas")
    suspend fun createAppointment(@Body body: AppointmentRequest): Response<ApiEnvelope<Appointment>>

    @GET("citas/mias")
    suspend fun myAppointments(@Query("pagina") pagina: Int = 1, @Query("limite") limite: Int = 100): Response<ApiEnvelope<List<Appointment>>>

    @PATCH("citas/{id}/cancelar")
    suspend fun cancelAppointment(@Path("id") id: Int): Response<ApiEnvelope<Appointment>>

    @GET("suscripciones/mia")
    suspend fun mySubscription(): Response<ApiEnvelope<Subscription?>>

    @GET("planes-suscripcion")
    suspend fun plans(@Query("pagina") pagina: Int = 1, @Query("limite") limite: Int = 100): Response<ApiEnvelope<List<Plan>>>

    @POST("suscripciones")
    suspend fun subscribe(@Body body: SubscriptionPaymentRequest): Response<ApiEnvelope<Subscription>>

    @POST("suscripciones/{id}/renovar")
    suspend fun renew(@Path("id") id: Int, @Body body: SubscriptionPaymentRequest): Response<ApiEnvelope<Subscription>>

    @PATCH("suscripciones/{id}/cancelar")
    suspend fun cancelSubscription(@Path("id") id: Int, @Body body: Map<String, String>): Response<ApiEnvelope<Subscription>>

    @GET("puntos")
    suspend fun points(): Response<ApiEnvelope<PointsBalance>>

    @GET("promociones")
    suspend fun promotions(@Query("pagina") pagina: Int = 1, @Query("limite") limite: Int = 100): Response<ApiEnvelope<List<Promotion>>>

    @POST("pedidos")
    suspend fun createOrder(@Body body: OrderRequest): Response<ApiEnvelope<Order>>

    @GET("pedidos/mios")
    suspend fun myOrders(@Query("pagina") pagina: Int = 1, @Query("limite") limite: Int = 100): Response<ApiEnvelope<List<Order>>>
}
