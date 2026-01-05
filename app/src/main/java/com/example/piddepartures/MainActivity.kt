package com.example.piddepartures

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.piddepartures.ui.navigation.AppNavigation
import com.example.piddepartures.ui.theme.PIDDeparturesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PIDDeparturesTheme {
                val navController = rememberNavController()
                val stopId = intent?.getLongExtra("stopId", -1L) ?: -1L
                AppNavigation(
                    navController = navController,
                    startDestination = if (stopId > 0) "detail/$stopId" else "home"
                )
            }
        }
    }
}
