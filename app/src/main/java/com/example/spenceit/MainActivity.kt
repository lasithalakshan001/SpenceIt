package com.example.spenceit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.spenceit.ui.theme.SpenceItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpenceItTheme {
                // We removed the Scaffold. AppNavigation is now the boss of the screen.
                AppNavigation(modifier = Modifier.fillMaxSize())
            }
        }
    }
}