package com.finanzasApp.finanzasapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finanzasApp.finanzasapp.ui.screens.AgregarMovimientoScreen
import com.finanzasApp.finanzasapp.ui.screens.DashboardScreen
import com.finanzasApp.finanzasapp.ui.screens.EstadisticasScreen
import com.finanzasApp.finanzasapp.ui.screens.MovimientosScreen

object Rutas {
    const val DASHBOARD = "dashboard"
    const val AGREGAR = "agregar"
    const val EDITAR = "editar"
    const val MOVIMIENTOS = "movimientos"
    const val ESTADISTICAS = "estadisticas"
}

/** Una pestaña del menu inferior: su ruta, el simbolo que la representa y su etiqueta. */
private data class Pestana(val ruta: String, val simbolo: String, val etiqueta: String)

// Las 3 pantallas principales del menu inferior. "Agregar movimiento" NO esta aqui
// a proposito: es una pantalla de formulario a la que se llega desde el boton
// "+ Agregar" del Dashboard, no una pestaña permanente.
private val pestanas = listOf(
    Pestana(Rutas.DASHBOARD, "🏠", "Inicio"),
    Pestana(Rutas.MOVIMIENTOS, "🧾", "Movimientos"),
    Pestana(Rutas.ESTADISTICAS, "📊", "Estadísticas")
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    val editarMovimiento: (Long) -> Unit = { id ->
        navController.navigate("${Rutas.EDITAR}/$id") { launchSingleTop = true }
    }
    val mostrarMenuInferior = pestanas.any { it.ruta == rutaActual }

    Scaffold(
        bottomBar = {
            if (mostrarMenuInferior) {
                NavigationBar {
                    pestanas.forEach { pestana ->
                        NavigationBarItem(
                            selected = rutaActual == pestana.ruta,
                            onClick = {
                                if (rutaActual != pestana.ruta) {
                                    navController.navigate(pestana.ruta) {
                                        // Evita apilar pantallas al cambiar de pestaña varias veces
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Text(pestana.simbolo, style = MaterialTheme.typography.titleMedium) },
                            label = { Text(pestana.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Rutas.DASHBOARD) {
                DashboardScreen(
                    onAgregar = { navController.navigate(Rutas.AGREGAR) },
                    onVerTodos = { navController.navigate(Rutas.MOVIMIENTOS) },
                    onEstadisticas = { navController.navigate(Rutas.ESTADISTICAS) },
                    onEditar = editarMovimiento
                )
            }
            composable(Rutas.AGREGAR) {
                AgregarMovimientoScreen(onVolver = { navController.popBackStack() })
            }
            composable(
                route = "${Rutas.EDITAR}/{movimientoId}",
                arguments = listOf(navArgument("movimientoId") { type = NavType.LongType })
            ) {
                AgregarMovimientoScreen(onVolver = { navController.popBackStack() })
            }
            composable(Rutas.MOVIMIENTOS) {
                MovimientosScreen(
                    onVolver = { navController.popBackStack() },
                    onEditar = editarMovimiento
                )
            }
            composable(Rutas.ESTADISTICAS) {
                EstadisticasScreen(onVolver = { navController.popBackStack() })
            }
        }
    }
}
