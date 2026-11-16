package com.finanzasApp.finanzasapp.data.remote

import com.finanzasApp.finanzasapp.data.model.Categoria
import com.finanzasApp.finanzasapp.data.model.Estadisticas
import com.finanzasApp.finanzasapp.data.model.Movimiento
import com.finanzasApp.finanzasapp.data.model.MovimientoRequest
import com.finanzasApp.finanzasapp.data.model.Resumen
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Describe los endpoints del backend. Las rutas son RELATIVAS a BASE_URL
 * (por eso no llevan "/" al inicio).
 */
interface FinanzasApi {

    // Los 4 filtros son opcionales: si un parametro llega null, Retrofit
    // simplemente no lo agrega a la URL, y el backend no filtra por el.
    @GET("movimientos")
    suspend fun obtenerMovimientos(
        @Query("tipo") tipo: String? = null,
        @Query("categoriaId") categoriaId: Long? = null,
        @Query("fechaInicio") fechaInicio: String? = null,
        @Query("fechaFin") fechaFin: String? = null
    ): List<Movimiento>

    @POST("movimientos")
    suspend fun crearMovimiento(@Body request: MovimientoRequest): Movimiento

    // El backend responde 204 (sin cuerpo): se usa Response<Unit> para revisar el codigo.
    @DELETE("movimientos/{id}")
    suspend fun eliminarMovimiento(@Path("id") id: Long): Response<Unit>

    @GET("categorias")
    suspend fun obtenerCategorias(): List<Categoria>

    @GET("resumen")
    suspend fun obtenerResumen(): Resumen

    // "periodo" y "fecha" son parametros de la URL: /api/estadisticas?periodo=SEMANA&fecha=2026-09-19
    // "fecha" es cualquier dia dentro del periodo que se quiere ver (navegacion).
    @GET("estadisticas")
    suspend fun obtenerEstadisticas(
        @Query("periodo") periodo: String,
        @Query("fecha") fecha: String? = null
    ): Estadisticas
}
