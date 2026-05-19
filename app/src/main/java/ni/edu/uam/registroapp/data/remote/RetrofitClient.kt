package ni.edu.uam.registroapp.data.remote

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ni.edu.uam.registroapp.config.ApiConfig

object RetrofitClient {
    private const val TAG = "RetrofitClient"

    val carreraApi: CarreraApi by lazy {
        Log.d(TAG, "Inicializando Retrofit con BASE_URL: ${ApiConfig.BASE_URL}")
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CarreraApi::class.java)
    }
}