package com.multibank.tracker.data.repository

import com.multibank.tracker.data.source.ALL_SYMBOLS
import com.multibank.tracker.data.source.WebSocketDataSource
import com.multibank.tracker.domain.model.PriceChange
import com.multibank.tracker.domain.model.StockSymbol
import com.multibank.tracker.domain.repository.StockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StockRepositoryImpl @Inject constructor(
    private val dataSource: WebSocketDataSource
) : StockRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val initialStocks = ALL_SYMBOLS
    private val _stocksState = MutableStateFlow(initialStocks)
    override val stocksState: StateFlow<List<StockSymbol>> = _stocksState.asStateFlow()

    private val _priceUpdates = MutableSharedFlow<String>(extraBufferCapacity = 128)
    override val priceUpdates: SharedFlow<String> = _priceUpdates.asSharedFlow()

    override val isConnected: StateFlow<Boolean> = dataSource.isConnected


    init {
        repositoryScope.launch {
            dataSource.rawEchos.collect { raw -> handleEcho(raw) }
        }
    }


    override fun start() = dataSource.connect()
    override fun stop() = dataSource.disconnect()

    override fun resetFlash(symbol: String) {
        _stocksState.value = _stocksState.value.map { stock ->
            if (stock.symbol == symbol) stock.copy(isFlashing = false) else stock
        }
    }


    private fun handleEcho(raw: String) {
        try {
            val json = JSONObject(raw)
            val symbol = json.getString("symbol")
            val newPrice = json.getDouble("price")

            _priceUpdates.tryEmit(symbol)

            val updated = _stocksState.value.map { stock ->
                if (stock.symbol != symbol) return@map stock
                val change = when {
                    stock.price == null -> PriceChange.NONE
                    newPrice > stock.price -> PriceChange.UP
                    newPrice < stock.price -> PriceChange.DOWN
                    else -> PriceChange.NONE
                }
                stock.copy(
                    price = newPrice,
                    change = change,
                    isFlashing = change != PriceChange.NONE,
                )
            }.sortedByDescending { it.price }

            _stocksState.value = updated
        } catch (e: Exception) {
            android.util.Log.w("StockRepo", "Failed to parse echo: $raw — ${e.message}")
        }
    }
}
