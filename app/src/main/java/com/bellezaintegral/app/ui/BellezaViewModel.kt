package com.bellezaintegral.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bellezaintegral.app.data.Appointment
import com.bellezaintegral.app.data.BellezaRepository
import com.bellezaintegral.app.data.Personal
import com.bellezaintegral.app.data.Plan
import com.bellezaintegral.app.data.ProductItem
import com.bellezaintegral.app.data.Promotion
import com.bellezaintegral.app.data.ServiceItem
import com.bellezaintegral.app.data.Slot
import com.bellezaintegral.app.data.Subscription
import com.bellezaintegral.app.data.User
import kotlinx.coroutines.launch

class BellezaViewModel(
    private val repository: BellezaRepository
) : ViewModel() {
    var user by mutableStateOf<User?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    var services by mutableStateOf<List<ServiceItem>>(emptyList())
        private set
    var products by mutableStateOf<List<ProductItem>>(emptyList())
        private set
    var appointments by mutableStateOf<List<Appointment>>(emptyList())
        private set
    var personal by mutableStateOf<List<Personal>>(emptyList())
        private set
    var slots by mutableStateOf<List<Slot>>(emptyList())
        private set
    var subscription by mutableStateOf<Subscription?>(null)
        private set
    var plans by mutableStateOf<List<Plan>>(emptyList())
        private set
    var promotions by mutableStateOf<List<Promotion>>(emptyList())
        private set
    var points by mutableStateOf(0)
        private set

    val cart = mutableStateMapOf<Int, Int>()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        if (!repository.hasSession()) return
        launchTask {
            user = repository.profile()
            if (user?.rol != "cliente") {
                repository.logout()
                user = null
                error("La aplicación móvil está habilitada para clientes.")
            }
            refreshAll()
        }
    }

    fun clearMessage() {
        message = null
    }

    fun login(identifier: String, password: String) = launchTask {
        user = repository.login(identifier, password)
        if (user?.rol != "cliente") {
            repository.logout()
            user = null
            error("La aplicación móvil está habilitada para clientes.")
        }
        message = "Bienvenido, ${user?.nombre}."
        refreshAll()
    }

    fun register(name: String, email: String, phone: String, password: String) = launchTask {
        repository.register(name, email, phone, password)
        message = "Cuenta creada. Ya puedes iniciar sesión."
    }

    fun logout() {
        repository.logout()
        user = null
        services = emptyList()
        products = emptyList()
        appointments = emptyList()
        subscription = null
        plans = emptyList()
        promotions = emptyList()
        points = 0
        cart.clear()
    }

    fun refreshServices() = launchTask { services = repository.services() }
    fun refreshProducts() = launchTask { products = repository.products() }
    fun refreshAppointments() = launchTask { appointments = repository.appointments() }

    fun refreshAccount() = launchTask {
        user = repository.profile()
        subscription = repository.subscription()
        plans = repository.plans()
    }

    fun refreshExtras() = launchTask {
        points = repository.points()
        promotions = repository.promotions()
    }

    fun loadPersonal() = launchTask { personal = repository.personal() }

    fun loadAvailability(personalId: Int, serviceId: Int, date: String) = launchTask {
        slots = repository.availability(personalId, serviceId, date).horarios
        if (slots.isEmpty()) message = "No hay horarios disponibles para esa fecha."
    }

    fun reserve(personalId: Int, serviceId: Int, date: String, time: String, done: () -> Unit) =
        launchTask {
            repository.createAppointment(personalId, serviceId, date, time)
            appointments = repository.appointments()
            message = "Cita registrada correctamente."
            done()
        }

    fun cancelAppointment(id: Int) = launchTask {
        repository.cancelAppointment(id)
        appointments = repository.appointments()
        message = "Cita cancelada."
    }

    fun subscribe(planId: Int) = launchTask {
        subscription = repository.subscribe(planId)
        services = repository.services()
        products = repository.products()
        message = "Suscripción activada."
    }

    fun renewSubscription(id: Int) = launchTask {
        subscription = repository.renew(id)
        message = "Suscripción renovada."
    }

    fun cancelSubscription(id: Int) = launchTask {
        repository.cancelSubscription(id)
        subscription = repository.subscription()
        services = repository.services()
        products = repository.products()
        message = "Suscripción cancelada."
    }

    fun addToCart(productId: Int) {
        val product = products.firstOrNull { it.id == productId } ?: return
        val next = (cart[productId] ?: 0) + 1
        if (next <= product.stock) cart[productId] = next
    }

    fun changeCart(productId: Int, delta: Int) {
        val product = products.firstOrNull { it.id == productId } ?: return
        val next = (cart[productId] ?: 0) + delta
        when {
            next <= 0 -> cart.remove(productId)
            next <= product.stock -> cart[productId] = next
        }
    }

    fun checkout(name: String, phone: String, address: String, done: () -> Unit) = launchTask {
        if (cart.isEmpty()) error("El carrito está vacío.")
        val order = repository.createOrder(cart.toMap(), name, phone, address)
        cart.clear()
        products = repository.products()
        message = "Pedido #${order.id} registrado. Total Q ${order.total ?: "0.00"}."
        done()
    }

    private suspend fun refreshAll() {
        services = repository.services()
        products = repository.products()
        appointments = repository.appointments()
        subscription = repository.subscription()
        plans = repository.plans()
        points = repository.points()
        promotions = repository.promotions()
    }

    private fun launchTask(block: suspend () -> Unit) {
        viewModelScope.launch {
            loading = true
            try {
                block()
            } catch (e: Exception) {
                if (!repository.hasSession()) user = null
                message = e.message ?: "Ocurrió un error."
            } finally {
                loading = false
            }
        }
    }
}

class BellezaViewModelFactory(
    private val repository: BellezaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BellezaViewModel(repository) as T
    }
}
