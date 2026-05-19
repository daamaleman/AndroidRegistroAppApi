package ni.edu.uam.registroapp.data.remote

import ni.edu.uam.registroapp.data.model.CarreraRequest
import ni.edu.uam.registroapp.data.model.Carrera
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CarreraApi {

    @GET("api/carreras")
    suspend fun listar() : List<Carrera>

    @POST("api/carreras")
    suspend fun crear(@Body request: CarreraRequest): Carrera

    @PUT("api/carreras/{id}")
    suspend fun actualizar(
        @Path("id") id: Long,
        @Body request: CarreraRequest
    ): Carrera

    @DELETE("api/carreras/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}