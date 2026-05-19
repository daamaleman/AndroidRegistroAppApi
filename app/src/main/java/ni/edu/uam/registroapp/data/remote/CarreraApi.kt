package ni.edu.uam.registroapp.data.remote

import ni.edu.uam.registroapp.data.model.Carrera
import retrofit2.http.GET

interface CarreraApi {

    @GET("api/carreras")
    suspend fun listar() : List<Carrera>
}