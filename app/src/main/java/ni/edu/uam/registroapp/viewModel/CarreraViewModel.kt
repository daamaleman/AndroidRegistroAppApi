package ni.edu.uam.registroapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ni.edu.uam.registroapp.data.model.Carrera
import ni.edu.uam.registroapp.data.model.CarreraRequest
import ni.edu.uam.registroapp.data.remote.RetrofitClient

sealed class UiState {
    object Loading : UiState()
    data class Success(val carreras: List<Carrera>) : UiState()
    data class Error(val message: String) : UiState()
}

class CarreraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiEvent = _uiEvent.asSharedFlow()

    private companion object {
        private const val TAG = "CarreraViewModel"
    }

    init {
        cargarCarreras()
    }

    fun cargarCarreras() {
        viewModelScope.launch {
            cargarCarrerasInterno()
        }
    }

    fun guardarCarrera(
        id: Long?,
        nombre: String,
        descripcion: String,
        costo: Double
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val request = CarreraRequest(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    costo = costo
                )

                if (id == null) {
                    Log.d(TAG, "Creando carrera: ${request.nombre}")
                    RetrofitClient.carreraApi.crear(request)
                    _uiEvent.tryEmit("Carrera creada correctamente.")
                } else {
                    Log.d(TAG, "Actualizando carrera $id")
                    RetrofitClient.carreraApi.actualizar(id, request)
                    _uiEvent.tryEmit("Carrera actualizada correctamente.")
                }

                cargarCarrerasInterno()
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar carrera", e)
                _uiEvent.tryEmit(
                    e.message ?: "No se pudo guardar la carrera. Verifica tu conexión y vuelve a intentarlo."
                )
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun eliminarCarrera(id: Long) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                Log.d(TAG, "Eliminando carrera $id")
                RetrofitClient.carreraApi.eliminar(id)
                _uiEvent.tryEmit("Carrera eliminada correctamente.")
                cargarCarrerasInterno()
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar carrera", e)
                _uiEvent.tryEmit(
                    e.message ?: "No se pudo eliminar la carrera. Verifica tu conexión y vuelve a intentarlo."
                )
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private suspend fun cargarCarrerasInterno() {
        _uiState.value = UiState.Loading
        try {
            Log.d(TAG, "Iniciando carga de carreras...")
            val lista = RetrofitClient.carreraApi.listar()
            Log.d(TAG, "Carreras cargadas exitosamente: ${lista.size} carreras")
            _uiState.value = UiState.Success(lista)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar carreras", e)
            _uiState.value = UiState.Error(
                "No se pudo conectar con el servidor.\n\n" +
                        "${e.message ?: "Error desconocido"}\n\n" +
                        "Verifica que el API esté corriendo en http://10.0.2.2:8080/ y que la app tenga acceso a Internet."
            )
        }
    }

}
