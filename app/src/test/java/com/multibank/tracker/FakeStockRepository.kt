package com.multibank.tracker

import com.multibank.tracker.domain.model.PriceChange
import com.multibank.tracker.domain.model.StockSymbol
import com.multibank.tracker.domain.repository.StockRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class FakeStockRepository : StockRepository {
    var startCalled = false
    var stopCalled = false
    val flashResets = mutableListOf<String>()

    // ── Controllable state ────────────────────────────────────────────────────
    private val _stocksState = MutableStateFlow<List<StockSymbol>>(emptyList())
    private val _isConnected = MutableStateFlow(false)
    private val _priceUpdates = MutableSharedFlow<String>(extraBufferCapacity = 64)

    override val stocksState: StateFlow<List<StockSymbol>> = _stocksState
    override val isConnected: StateFlow<Boolean> = _isConnected
    override val priceUpdates: SharedFlow<String> = _priceUpdates


    fun emitStocks(stocks: List<StockSymbol>) {
        _stocksState.value = stocks
    }

    fun emitConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    suspend fun emitPriceUpdate(symbol: String) {
        _priceUpdates.emit(symbol)
    }

    override fun start() {
        startCalled = true
    }

    override fun stop() {
        stopCalled = true
    }

    override fun resetFlash(symbol: String) {
        flashResets.add(symbol)
    }
}


fun makeSymbol(
    ticker: String,
    price: Double? = 100.0,
    change: PriceChange = PriceChange.NONE,
    isFlashing: Boolean = false,
) = StockSymbol(
    symbol = ticker,
    price = price,
    change = change,
    description = "Description of $ticker",
    isFlashing = isFlashing,
)
