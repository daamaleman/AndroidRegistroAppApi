package ni.edu.uam.registroapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ni.edu.uam.registroapp.data.model.Carrera
import ni.edu.uam.registroapp.ui.screens.CarreraScreen
import ni.edu.uam.registroapp.ui.screens.CarreraScreenContent
import ni.edu.uam.registroapp.viewModel.UiState
import ni.edu.uam.registroapp.ui.theme.RegistroAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroAppTheme {
                CarreraScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CarreraScreenPreview() {
    RegistroAppTheme {
        CarreraScreenContent(
            uiState = UiState.Success(
                listOf(
                    Carrera(1, "Ingeniería en Sistemas", "Formación orientada al desarrollo de software.", 1200.0)
                )
            ),
            isSubmitting = false,
            onRefresh = {},
            onSave = { _, _, _, _ -> },
            onDelete = {},
            uiEventFlow = null
        )
    }
}