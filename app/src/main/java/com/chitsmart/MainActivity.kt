package com.chitsmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.chitsmart.ui.navigation.ChitSmartNavGraph
import com.chitsmart.ui.theme.ChitSmartTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire ChitSmart Android application.
 * All screens are Composables managed by Jetpack Navigation Compose.
 * Edge-to-edge rendering is enabled — screens handle their own insets.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChitSmartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    ChitSmartNavGraph(navController = navController)
                }
            }
        }
    }
}
