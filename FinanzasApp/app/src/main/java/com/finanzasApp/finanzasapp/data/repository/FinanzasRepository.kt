package com.finanzasApp.finanzasapp.data.repository

import com.finanzasApp.finanzasapp.data.model.Categoria
import com.finanzasApp.finanzasapp.data.model.Estadisticas
import com.finanzasApp.finanzasapp.data.model.ErrorResponse
import com.finanzasApp.finanzasapp.data.model.Movimiento
import com.finanzasApp.finanzasapp.data.model.MovimientoRequest
import com.finanzasApp.finanzasapp.data.model.Periodo
import com.finanzasApp.finanzasapp.data.model.Resumen
import com.finanzasApp.finanzasapp.data.model.TipoMovimiento
import com.finanzasApp.finanzasapp.data.remote.FinanzasApi
import com.finanzasApp.finanzasapp.data.remote.RetrofitClient
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

/**
 * Intermediario entre los ViewModels y la API. Nunca lanza excepciones:
 * siempre devuelve Resultado.Exito o Resultado.Error con un mensaje comprensible.
 */
class FinanzasRepository(
    private val api: FinanzasApi = RetrofitClient.api
) {

    suspend fun obtenerResumen(): Resultado<Resumen> =
        llamar("No se pudo cargar el resumen.") { api.obtenerResumen() }

    /**
     * Los 4 filtros son opcionales (null = "no filtrar por esto"); el backend
     * los aplica el mismo. Se filtra en el servidor, no en el telefono.
     */
    suspend fun obtenerMovimientos(
        tipo: TipoMovimiento? = null,
        categoriaId: Long? = null,
        fechaInicio: LocalDate? = null,
        fechaFin: LocalDate? = null
    ): Resultado<List<Movimiento>> =
        llamar("No se pudieron cargar los movimientos.") {
            api.obtenerMovimientos(
                tipo = tipo?.name,
                categoriaId = categoriaId,
                fechaInicio = fechaInicio?.toString(), // LocalDate.toString() = "yyyy-MM-dd"
                fechaFin = fechaFin?.toString()
            )
        }

    suspend fun obtenerCategorias(): Resultado<List<Categoria>> =
        llamar("No se pudieron cargar las categorías.") { api.obtenerCategorias() }

    /** "fecha" es cualquier dia dentro del periodo que se quiere ver; null = hoy. */
    suspend fun obtenerEstadisticas(periodo: Periodo, fecha: LocalDate? = null): Resultado<Estadisticas> =
        llamar("No se pudieron cargar las estadísticas.") {
            api.obtenerEstadisticas(periodo.name, fecha?.toString())
        }

    suspend fun crearMovimiento(request: MovimientoRequest): Resultado<Movimiento> =
        llamar("No se pudo guardar el movimiento.") { api.crearMovimiento(request) }

    suspend fun eliminarMovimiento(id: Long): Resultado<Unit> =
        llamar("No se pudo eliminar el movimiento.") {
            val respuesta = api.eliminarMovimiento(id)
            // Con Response<Unit> Retrofit no lanza excepcion en 4xx/5xx: la lanzamos nosotros.
            if (!respuesta.isSuccessful) throw HttpException(respuesta)
        }

    /** Ejecuta la llamada y traduce cualquier fallo a un mensaje para el usuario. */
    private suspend fun <T> llamar(
        mensajePorDefecto: String,
        bloque: suspend () -> T
    ): Resultado<T> {
        return try {
            Resultado.Exito(bloque())
        } catch (e: CancellationException) {
            throw e // Una corrutina cancelada no es un error: se debe propagar
        } catch (e: SocketTimeoutException) {
            Resultado.Error("El servidor tardó demasiado en responder. Intenta de nuevo.")
        } catch (e: IOException) {
            Resultado.Error(
                "No se pudo conectar con el servidor. " +
                    "Verifica que el backend esté encendido y que la URL sea correcta."
            )
        } catch (e: HttpException) {
            Resultado.Error(mensajeDeHttp(e))
        } catch (e: Exception) {
            Resultado.Error(mensajePorDefecto)
        }
    }

    /** Usa el mensaje que envia el backend; si no hay, uno generico segun el codigo HTTP. */
    private fun mensajeDeHttp(e: HttpException): String {
        val mensajeDelServidor = try {
            val cuerpo = e.response()?.errorBody()?.string()
            Gson().fromJson(cuerpo, ErrorResponse::class.java)?.mensaje
        } catch (_: Exception) {
            null
        }
        return mensajeDelServidor ?: when (e.code()) {
            404 -> "El elemento no existe (quizá ya fue eliminado)."
            in 400..499 -> "La solicitud no es válida (código ${e.code()})."
            else -> "El servidor tuvo un problema (código ${e.code()}). Intenta más tarde."
        }
    }
}
