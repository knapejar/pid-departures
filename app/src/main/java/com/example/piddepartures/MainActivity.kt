package com.example.piddepartures

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.piddepartures.ui.navigation.AppNavigation
import com.example.piddepartures.ui.theme.PIDDeparturesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingStopId = mutableStateOf<Long?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        handleIntent(intent)
        
        setContent {
            PIDDeparturesTheme {
                val navController = rememberNavController()
                val stopIdToNavigate by remember { pendingStopId }
                
                LaunchedEffect(stopIdToNavigate) {
                    stopIdToNavigate?.let { stopId ->
                        navController.navigate("detail/$stopId") {
                            popUpTo("home") { inclusive = false }
                        }
                        pendingStopId.value = null
                    }
                }
                
                AppNavigation(
                    navController = navController,
                    startDestination = "home"
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        val stopId = intent?.getLongExtra("stopId", -1L) ?: -1L
        
        if (stopId > 0) {
            pendingStopId.value = stopId
        }
    }
}
