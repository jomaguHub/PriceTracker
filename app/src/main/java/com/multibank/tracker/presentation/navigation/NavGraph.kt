package com.multibank.tracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.multibank.tracker.presentation.detail.DetailScreen
import com.multibank.tracker.presentation.feed.FeedScreen


const val ROUTE_FEED = "feed"
const val ROUTE_DETAIL = "detail/{symbol}"
const val NAV_ARG_SYMBOL = "symbol"

private const val DEEP_LINK_URI_PATTERN = "stocks://symbol/{symbol}"

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = ROUTE_FEED,
    ) {

        composable(route = ROUTE_FEED) {
            FeedScreen(
                onSymbolClick = { symbol ->
                    navController.navigate("detail/$symbol")
                }
            )
        }

        composable(
            route     = ROUTE_DETAIL,
            arguments = listOf(
                navArgument(NAV_ARG_SYMBOL) { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN }
            ),
        ) {

            DetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
