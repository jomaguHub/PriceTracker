package com.multibank.tracker.domain.model

data class StockSymbol(
    val symbol: String,
    val price: Double? = null,
    val change: PriceChange = PriceChange.NONE,
    val description: String = "",
    val isFlashing: Boolean = false,
)

enum class PriceChange { UP, DOWN, NONE }