package com.finanzasApp.finanzasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.finanzasApp.finanzasapp.ui.theme.FinanzasAppTheme
import com.finanzasApp.finanzasapp.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanzasAppTheme {
                AppNavigation()
            }
        }
    }
}