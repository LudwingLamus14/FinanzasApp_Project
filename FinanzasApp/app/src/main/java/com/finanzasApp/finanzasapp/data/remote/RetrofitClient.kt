package com.finanzasApp.finanzasapp.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Unico lugar donde se configura la conexion con el backend.
 *
 * URL segun donde corra la app:
 *  - Emulador de Android Studio:  http://10.0.2.2:8080/api/
 *    (10.0.2.2 es el "localhost" del PC visto desde el emulador)
 *  - Telefono fisico (misma red WiFi que el PC): http://IP_DEL_PC:8080/api/
 *    Ejemplo: http://192.168.1.25:8080/api/
 *
 * IMPORTANTE: la URL debe terminar en "/".
 */
object RetrofitClient {

    const val BASE_URL = "http://192.168.1.5:8080/api/"

    // Si el servidor no responde en 10 s, se lanza un timeout (y la app no se cierra).
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: FinanzasApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FinanzasApi::class.java)
    }
}
