package com.finanzasApp.finanzasapp.data.repository

/**
 * Resultado de una operacion de red: o salio bien (Exito) o fallo (Error)
 * con un mensaje listo para mostrar al usuario.
 */
sealed interface Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>
    data class Error(val mensaje: String) : Resultado<Nothing>
}
