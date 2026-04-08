package com.multibank.tracker.domain.repository

import com.multibank.tracker.domain.model.StockSymbol
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


interface StockRepository {

    val stocksState: StateFlow<List<StockSymbol>>

    val priceUpdates: SharedFlow<String>

    val isConnected: StateFlow<Boolean>

    fun start()

    fun stop()

    fun resetFlash(symbol: String)
}