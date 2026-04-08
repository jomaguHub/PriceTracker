package com.multibank.tracker.presentation.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun FeedScreen(
    homeViewModel: FeedViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit = {}
) {
    val tabNavController = rememberNavController()
    val navStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navStackEntry?.destination?.route
}