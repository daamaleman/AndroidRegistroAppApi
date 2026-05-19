package ni.edu.uam.registroapp.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.registroapp.data.model.Carrera
import ni.edu.uam.registroapp.data.remote.RetrofitClient

// Usamos un sealed class local llamado UiState para representar los estados de la UI
sealed class UiState {
    object Loading : UiState()
    data class Success(val carreras: List<Carrera>) : UiState()
    data class Error(val message: String) : UiState()
}

class CarreraViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    private companion object {
        private const val TAG = "CarreraViewModel"
    }

    init {
        cargarCarreras()
    }

    private fun cargarCarreras() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando carga de carreras...")
                val lista = RetrofitClient.carreraApi.listar()
                Log.d(TAG, "Carreras cargadas exitosamente: ${lista.size} carreras")
                _uiState.value = UiState.Success(lista)
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar carreras", e)
                _uiState.value = UiState.Error(
                    "❌ Error de conexión\n\n" +
                            "${e.message ?: "Error desconocido"}\n\n" +
                            "⚠️ Soluciones:\n" +
                            "1. Verifica que el API esté corriendo\n" +
                            "2. Asegúrate que tu API escucha en 0.0.0.0:8080\n" +
                            "3. Revisa que la red WiFi esté activa"
                )
            }

        }
    }

}