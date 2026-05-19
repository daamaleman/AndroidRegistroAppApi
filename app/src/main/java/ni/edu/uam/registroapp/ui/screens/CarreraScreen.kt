package ni.edu.uam.registroapp.ui.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharedFlow
import ni.edu.uam.registroapp.data.model.Carrera
import ni.edu.uam.registroapp.viewModel.CarreraViewModel
import ni.edu.uam.registroapp.viewModel.UiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarreraScreen(viewModel: CarreraViewModel = viewModel(), modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    CarreraScreenContent(
        modifier = modifier,
        uiState = uiState,
        isSubmitting = isSubmitting,
        onRefresh = viewModel::cargarCarreras,
        onSave = viewModel::guardarCarrera,
        onDelete = viewModel::eliminarCarrera,
        uiEventFlow = viewModel.uiEvent
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarreraScreenContent(
    modifier: Modifier = Modifier,
    uiState: UiState,
    isSubmitting: Boolean,
    onRefresh: () -> Unit,
    onSave: (Long?, String, String, Double) -> Unit,
    onDelete: (Long) -> Unit,
    uiEventFlow: SharedFlow<String>? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    var carreraEnEdicion by remember { mutableStateOf<Carrera?>(null) }
    var carreraAEliminar by remember { mutableStateOf<Carrera?>(null) }
    LaunchedEffect(uiEventFlow) {
        uiEventFlow?.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Carreras",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "CRUD minimalista y profesional",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onRefresh, enabled = !isSubmitting) {
                        Text("Actualizar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            Button(onClick = {
                carreraEnEdicion = null
                mostrarFormulario = true
            }) {
                Text("Nueva carrera")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Success -> ListadoCarreras(
                    carreras = uiState.carreras,
                    isSubmitting = isSubmitting,
                    onEdit = { carrera ->
                        carreraEnEdicion = carrera
                        mostrarFormulario = true
                    },
                    onDelete = { carrera ->
                        carreraAEliminar = carrera
                    },
                    onCreate = {
                        carreraEnEdicion = null
                        mostrarFormulario = true
                    }
                )
                is UiState.Error -> ErrorView(
                    message = uiState.message,
                    onRetry = onRefresh
                )
            }
            if (isSubmitting && uiState is UiState.Success) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
        if (mostrarFormulario) {
            CarreraFormDialog(
                carrera = carreraEnEdicion,
                isSaving = isSubmitting,
                onDismiss = {
                    mostrarFormulario = false
                    carreraEnEdicion = null
                },
                onSave = { nombre, descripcion, costo ->
                    onSave(carreraEnEdicion?.id, nombre, descripcion, costo)
                    mostrarFormulario = false
                    carreraEnEdicion = null
                }
            )
        }
        carreraAEliminar?.let { carrera ->
            DeleteConfirmDialog(
                carrera = carrera,
                isDeleting = isSubmitting,
                onDismiss = { carreraAEliminar = null },
                onConfirm = {
                    onDelete(carrera.id)
                    carreraAEliminar = null
                }
            )
        }
    }
}
@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(44.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Cargando carreras...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun ListadoCarreras(
    carreras: List<Carrera>,
    isSubmitting: Boolean,
    onEdit: (Carrera) -> Unit,
    onDelete: (Carrera) -> Unit,
    onCreate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 1.dp,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Administración de carreras",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Crea, edita y elimina registros desde una vista limpia y enfocada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = carreras.size.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Registros",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isSubmitting) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (carreras.isEmpty()) {
            EmptyState(onCreate = onCreate)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(carreras, key = { it.id }) { carrera ->
                    CarreraCard(
                        carrera = carrera,
                        isSubmitting = isSubmitting,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}
@Composable
fun CarreraCard(
    carrera: Carrera,
    isSubmitting: Boolean,
    onEdit: (Carrera) -> Unit,
    onDelete: (Carrera) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = carrera.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = carrera.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Costo: $${String.format(Locale.getDefault(), "%.2f", carrera.costo)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onEdit(carrera) },
                        enabled = !isSubmitting
                    ) {
                        Text("Editar")
                    }
                    TextButton(
                        onClick = { onDelete(carrera) },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
@Composable
fun EmptyState(onCreate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Text(
                    text = "No hay carreras aún",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Agrega el primer registro con una interfaz simple y ordenada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCreate) {
                    Text("Nueva carrera")
                }
            }
        }
    }
}
@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Text(
                    text = "Error al cargar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedButton(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        }
    }
}
@Composable
fun CarreraFormDialog(
    carrera: Carrera?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (nombre: String, descripcion: String, costo: Double) -> Unit
) {
    val dialogKey = carrera?.id ?: -1L
    var nombre by rememberSaveable(dialogKey) { mutableStateOf(carrera?.nombre.orEmpty()) }
    var descripcion by rememberSaveable(dialogKey) { mutableStateOf(carrera?.descripcion.orEmpty()) }
    var costoTexto by rememberSaveable(dialogKey) { mutableStateOf(carrera?.costo?.toString().orEmpty()) }
    var tocarValidacion by rememberSaveable(dialogKey) { mutableStateOf(false) }
    val nombreError = tocarValidacion && nombre.isBlank()
    val descripcionError = tocarValidacion && descripcion.isBlank()
    val costoDecimal = costoTexto.replace(',', '.').toDoubleOrNull()
    val costoError = tocarValidacion && (costoDecimal == null || costoDecimal <= 0.0)
    AlertDialog(
        onDismissRequest = if (isSaving) ({}) else onDismiss,
        title = {
            Text(if (carrera == null) "Nueva carrera" else "Editar carrera")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nombreError,
                    supportingText = {
                        if (nombreError) Text("Ingresa un nombre válido")
                    }
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción") },
                    minLines = 3,
                    isError = descripcionError,
                    supportingText = {
                        if (descripcionError) Text("Ingresa una descripción válida")
                    }
                )
                OutlinedTextField(
                    value = costoTexto,
                    onValueChange = { costoTexto = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Costo") },
                    singleLine = true,
                    isError = costoError,
                    supportingText = {
                        if (costoError) Text("Ingresa un costo mayor a 0")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    tocarValidacion = true
                    val nombreFinal = nombre.trim()
                    val descripcionFinal = descripcion.trim()
                    val costoFinal = costoTexto.replace(',', '.').toDoubleOrNull()
                    if (nombreFinal.isBlank() || descripcionFinal.isBlank() || costoFinal == null || costoFinal <= 0.0) {
                        return@TextButton
                    }
                    onSave(nombreFinal, descripcionFinal, costoFinal)
                },
                enabled = !isSaving
            ) {
                Text(if (carrera == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}
@Composable
fun DeleteConfirmDialog(
    carrera: Carrera,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (isDeleting) ({}) else onDismiss,
        title = { Text("Eliminar carrera") },
        text = {
            Text(
                text = "¿Deseas eliminar \"${carrera.nombre}\"? Esta acción no se puede deshacer.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("Cancelar")
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
private fun CarreraScreenPreview() {
    val sample = listOf(
        Carrera(1, "Ingeniería en Sistemas", "Formación orientada al desarrollo de software, redes y bases de datos.", 1200.0),
        Carrera(2, "Administración de Empresas", "Carrera enfocada en gestión, finanzas y liderazgo organizacional.", 980.5)
    )
    MaterialTheme {
        CarreraScreenContent(
            uiState = UiState.Success(sample),
            isSubmitting = false,
            onRefresh = {},
            onSave = { _, _, _, _ -> },
            onDelete = {},
            uiEventFlow = null
        )
    }
}
