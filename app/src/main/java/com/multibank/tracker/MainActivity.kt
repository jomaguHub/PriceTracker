package com.multibank.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.multibank.tracker.ui.theme.PriceTrackerTheme
import com.multibank.tracker.presentation.navigation.NavigationWrapper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PriceTrackerTheme {
                val navController = rememberNavController()
                NavigationWrapper(navController = navController)
            }
        }
    }
}