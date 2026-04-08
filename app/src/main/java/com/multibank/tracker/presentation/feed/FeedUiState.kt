package com.multibank.tracker.presentation.feed

import com.multibank.tracker.domain.model.StockSymbol

data class FeedUiState(
    val stocks: List<StockSymbol> = emptyList(),
    val isConnected: Boolean = false,
    val isFeedRunning: Boolean = false,
)