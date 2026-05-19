package ni.edu.uam.registroapp.config

/**
 * Configuración de la API
 *
 * Para cambiar la URL base:
 * 1. Emulador: Usa "http://10.0.2.2:8080/"
 * 2. Dispositivo físico: Usa la IP local de tu máquina, ej: "http://192.168.x.x:8080/"
 */
object ApiConfig {
    // URL base del servidor API
    // Emulador: 10.0.2.2 (acceso a localhost del host)
    // Dispositivo físico: cambiar por la IP local de tu máquina
    const val BASE_URL = "http://10.0.2.2:8080/"
}

