package com.multibank.tracker.domain.usecase

import com.multibank.tracker.domain.model.StockSymbol
import com.multibank.tracker.domain.repository.StockRepository
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Returns the live-sorted list of all stock symbols.
 * Presenters collect this StateFlow directly.
 */
class ObserveStocksUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(): StateFlow<List<StockSymbol>> = repository.stocksState
}

/**
 * Returns the SharedFlow that emits a symbol ticker on every WebSocket echo.
 * Used by the ViewModel to schedule flash resets.
 */
class ObservePriceUpdatesUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(): SharedFlow<String> = repository.priceUpdates
}

/**
 * Returns the connection status as a StateFlow.
 */
class ObserveConnectionUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(): StateFlow<Boolean> = repository.isConnected
}

/**
 * Starts the WebSocket price feed.
 */
class StartFeedUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke() = repository.start()
}

/**
 * Stops the WebSocket price feed.
 */
class StopFeedUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke() = repository.stop()
}

/**
 * Resets the flash flag for a single symbol after the 1-second animation.
 */
class ResetFlashUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(symbol: String) = repository.resetFlash(symbol)
}

/**
 * Returns the detail for a single symbol from the current state snapshot.
 */
class GetSymbolDetailUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(symbol: String): StateFlow<List<StockSymbol>> = repository.stocksState
}
