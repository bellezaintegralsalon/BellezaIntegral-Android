package com.bellezaintegral.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bellezaintegral.app.data.Personal
import com.bellezaintegral.app.data.Plan
import com.bellezaintegral.app.data.ProductItem
import com.bellezaintegral.app.data.ServiceItem
import com.bellezaintegral.app.data.Slot
import kotlinx.coroutines.flow.filterNotNull

private enum class Screen(val label: String) {
    SERVICES("Servicios"),
    PRODUCTS("Productos"),
    APPOINTMENTS("Citas"),
    ACCOUNT("Cuenta"),
    MORE("Más")
}

@Composable
fun BellezaApp(vm: BellezaViewModel) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            if (vm.user == null) AuthScreen(vm) else CustomerApp(vm)
        }
    }
}

@Composable
private fun AuthScreen(vm: BellezaViewModel) {
    var register by rememberSaveable { mutableStateOf(false) }
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Belleza Integral",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(if (register) "Crea tu cuenta de cliente" else "Ingresa a tu cuenta")

            if (register) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("Correo o teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (register) vm.register(name, email, phone, password)
                    else vm.login(identifier, password)
                },
                enabled = !vm.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (register) "Registrarme" else "Ingresar")
            }

            TextButton(onClick = { register = !register }) {
                Text(if (register) "Ya tengo cuenta" else "Crear una cuenta")
            }

            vm.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                if (register && it.contains("Cuenta creada")) register = false
            }

            if (vm.loading) CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerApp(vm: BellezaViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.SERVICES) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        snapshotFlow { vm.message }
            .filterNotNull()
            .collect {
                snackbar.showSnackbar(it)
                vm.clearMessage()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Belleza Integral · ${screen.label}") },
                actions = {
                    TextButton(onClick = vm::logout) { Text("Salir") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { item ->
                    NavigationBarItem(
                        selected = screen == item,
                        onClick = { screen = item },
                        icon = { Text(item.label.take(1)) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (screen) {
                Screen.SERVICES -> ServicesScreen(vm) { screen = Screen.APPOINTMENTS }
                Screen.PRODUCTS -> ProductsScreen(vm)
                Screen.APPOINTMENTS -> AppointmentsScreen(vm)
                Screen.ACCOUNT -> AccountScreen(vm)
                Screen.MORE -> MoreScreen(vm)
            }
            if (vm.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun ServicesScreen(vm: BellezaViewModel, onReserved: () -> Unit) {
    var selected by remember { mutableStateOf<ServiceItem?>(null) }

    LaunchedEffect(Unit) { vm.refreshServices() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val discount = vm.services.maxOfOrNull { it.descuento_suscripcion } ?: 0
            Text(
                "Servicios",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (discount > 0) "Tu membresía aplica $discount% de descuento."
                else "Elige tu próximo cuidado."
            )
        }

        items(vm.services, key = { it.id }) { service ->
            ServiceCard(service) {
                selected = service
                vm.loadPersonal()
            }
        }
    }

    selected?.let { service ->
        ReservationDialog(
            service = service,
            people = vm.personal,
            slots = vm.slots,
            onDismiss = { selected = null },
            onSearch = { personId, date ->
                vm.loadAvailability(personId, service.id, date)
            },
            onReserve = { personId, date, time ->
                vm.reserve(personId, service.id, date, time) {
                    selected = null
                    onReserved()
                }
            }
        )
    }
}

@Composable
private fun ServiceCard(service: ServiceItem, onReserve: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(service.categoria.orEmpty(), style = MaterialTheme.typography.labelMedium)
            Text(
                service.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            service.descripcion?.let { Text(it) }
            Text("${service.duracion_min} minutos")
            PriceBlock(
                service.precio_original,
                service.precio,
                service.descuento_suscripcion
            )
            Button(onClick = onReserve) { Text("Reservar") }
        }
    }
}

@Composable
private fun PriceBlock(original: String?, finalPrice: String, discount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (discount > 0 && original != null) {
            Text("Q $original", textDecoration = TextDecoration.LineThrough)
        }
        Text(
            "Q $finalPrice",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (discount > 0) Text("-$discount% membresía")
    }
}

@Composable
private fun ReservationDialog(
    service: ServiceItem,
    people: List<Personal>,
    slots: List<Slot>,
    onDismiss: () -> Unit,
    onSearch: (Int, String) -> Unit,
    onReserve: (Int, String, String) -> Unit
) {
    var personId by remember(people) { mutableStateOf(people.firstOrNull()?.id) }
    var personName by remember(people) { mutableStateOf(people.firstOrNull()?.nombre.orEmpty()) }
    var expanded by remember { mutableStateOf(false) }
    var date by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reservar ${service.nombre}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (personName.isBlank()) "Selecciona profesional" else personName)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        people.forEach { person ->
                            DropdownMenuItem(
                                text = { Text(person.nombre) },
                                onClick = {
                                    personId = person.id
                                    personName = person.nombre
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val id = personId ?: return@Button
                        onSearch(id, date)
                    },
                    enabled = personId != null &&
                        date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
                ) {
                    Text("Consultar horarios")
                }

                if (slots.isNotEmpty()) {
                    Text("Horarios disponibles")
                    slots.forEach { slot ->
                        OutlinedButton(
                            onClick = {
                                val id = personId ?: return@OutlinedButton
                                onReserve(id, date, slot.hora_inicio)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${slot.hora_inicio} - ${slot.hora_fin}")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun ProductsScreen(vm: BellezaViewModel) {
    var showCart by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refreshProducts() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Productos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val discount = vm.products.maxOfOrNull { it.descuento_suscripcion } ?: 0
                    Text(
                        if (discount > 0) "Beneficio de membresía: $discount%."
                        else "Catálogo disponible."
                    )
                }
                Button(onClick = { showCart = true }) {
                    Text("Carrito (${vm.cart.values.sum()})")
                }
            }
        }

        items(vm.products, key = { it.id }) { product ->
            ProductCard(product) { vm.addToCart(product.id) }
        }
    }

    if (showCart) CartDialog(vm, onDismiss = { showCart = false })
}

@Composable
private fun ProductCard(product: ProductItem, onAdd: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(product.categoria.orEmpty(), style = MaterialTheme.typography.labelMedium)
            Text(
                product.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            product.descripcion?.let { Text(it) }
            PriceBlock(
                product.precio_original,
                product.precio,
                product.descuento_suscripcion
            )
            Text("${product.stock} disponibles")
            Button(onClick = onAdd, enabled = product.stock > 0) {
                Text(if (product.stock > 0) "Agregar al carrito" else "Agotado")
            }
        }
    }
}

@Composable
private fun CartDialog(vm: BellezaViewModel, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(vm.user?.nombre.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(vm.user?.telefono.orEmpty()) }
    var address by rememberSaveable { mutableStateOf("") }

    val selectedProducts = vm.cart.keys.mapNotNull { id ->
        vm.products.firstOrNull { it.id == id }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Carrito") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedProducts, key = { it.id }) { product ->
                    val qty = vm.cart[product.id] ?: 0
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(10.dp)) {
                            Text(product.nombre, fontWeight = FontWeight.Bold)
                            Text("Q ${product.precio} x $qty")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { vm.changeCart(product.id, -1) }
                                ) { Text("-") }
                                OutlinedButton(
                                    onClick = { vm.changeCart(product.id, 1) }
                                ) { Text("+") }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        name,
                        { name = it },
                        label = { Text("Nombre de quien recibe") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        phone,
                        { phone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        address,
                        { address = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { vm.checkout(name, phone, address, onDismiss) },
                enabled = vm.cart.isNotEmpty() &&
                    name.isNotBlank() &&
                    phone.isNotBlank() &&
                    address.isNotBlank()
            ) {
                Text("Confirmar pedido")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
private fun AppointmentsScreen(vm: BellezaViewModel) {
    LaunchedEffect(Unit) { vm.refreshAppointments() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Mis citas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (vm.appointments.isEmpty()) {
            item { Text("Todavía no tienes citas.") }
        }

        items(vm.appointments, key = { it.id }) { appointment ->
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        appointment.servicio ?: "Cita #${appointment.id}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("${appointment.fecha.orEmpty()} · ${appointment.hora.orEmpty()}")
                    appointment.profesional?.let { Text("Profesional: $it") }
                    Text("Estado: ${appointment.estado.orEmpty()}")

                    if (appointment.estado in listOf("pendiente", "confirmada")) {
                        OutlinedButton(
                            onClick = { vm.cancelAppointment(appointment.id) }
                        ) {
                            Text("Cancelar cita")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountScreen(vm: BellezaViewModel) {
    LaunchedEffect(Unit) { vm.refreshAccount() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Mi cuenta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            vm.user?.let { user ->
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            user.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(user.email ?: "Sin correo")
                        Text(user.telefono ?: "Sin teléfono")
                        Text("Puntos: ${user.puntos ?: vm.points}")
                    }
                }
            }
        }

        item {
            SubscriptionSection(vm)
        }
    }
}

@Composable
private fun SubscriptionSection(vm: BellezaViewModel) {
    val sub = vm.subscription

    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Suscripción",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (sub != null && sub.estado == "activa") {
                Text(sub.plan ?: "Membresía")
                Text("Vigente hasta: ${sub.fecha_fin.orEmpty()}")
                Text("Descuento servicios: ${sub.descuento_servicios_contratado}%")
                Text("Descuento productos: ${sub.descuento_productos_contratado}%")

                if (sub.renovacion_disponible) {
                    Button(onClick = { vm.renewSubscription(sub.id) }) {
                        Text("Renovar")
                    }
                } else {
                    Text(
                        "Renovación disponible desde: " +
                            sub.renovacion_desde.orEmpty()
                    )
                }

                OutlinedButton(onClick = { vm.cancelSubscription(sub.id) }) {
                    Text("Cancelar suscripción")
                }
            } else {
                Text("No tienes una suscripción activa.")
                vm.plans.forEach { plan ->
                    PlanCard(plan) { vm.subscribe(plan.id) }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: Plan, onSubscribe: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(plan.nombre, fontWeight = FontWeight.Bold)
            Text("Q ${plan.precio_mensual} / ${plan.duracion_meses} mes(es)")
            Text(
                "${plan.descuento_servicios}% servicios · " +
                    "${plan.descuento_productos}% productos"
            )
            Button(onClick = onSubscribe) { Text("Suscribirme") }
        }
    }
}

@Composable
private fun MoreScreen(vm: BellezaViewModel) {
    LaunchedEffect(Unit) { vm.refreshExtras() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Beneficios",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Puntos disponibles: ${vm.points}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (vm.promotions.isEmpty()) {
            item { Text("No hay promociones vigentes.") }
        }

        items(vm.promotions, key = { it.id }) { promotion ->
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(promotion.titulo, fontWeight = FontWeight.Bold)
                    promotion.descripcion?.let { Text(it) }
                    if (promotion.descuento_porcentaje > 0) {
                        Text(
                            "Descuento anunciado: " +
                                "${promotion.descuento_porcentaje}%"
                        )
                    }
                    if (promotion.puntos_costo > 0) {
                        Text("Canje: ${promotion.puntos_costo} puntos")
                    }
                    Text(
                        "${promotion.fecha_inicio.orEmpty()} - " +
                            promotion.fecha_fin.orEmpty()
                    )
                }
            }
        }
    }
}
