package com.multibank.tracker.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.multibank.tracker.domain.usecase.ObserveConnectionUseCase
import com.multibank.tracker.domain.usecase.ObservePriceUpdatesUseCase
import com.multibank.tracker.domain.usecase.ObserveStocksUseCase
import com.multibank.tracker.domain.usecase.ResetFlashUseCase
import com.multibank.tracker.domain.usecase.StartFeedUseCase
import com.multibank.tracker.domain.usecase.StopFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    observeStocks: ObserveStocksUseCase,
    observeConnection: ObserveConnectionUseCase,
    private val observePriceUpdates: ObservePriceUpdatesUseCase,
    private val startFeed: StartFeedUseCase,
    private val stopFeed: StopFeedUseCase,
    private val resetFlash: ResetFlashUseCase,
) : ViewModel() {

    private val _isFeedRunning = MutableStateFlow(false)

    val uiState = combine(
        observeStocks(),
        observeConnection(),
        _isFeedRunning
    ) { stocks, connected, running ->
        FeedUiState(
            stocks = stocks,
            isConnected = connected,
            isFeedRunning = running,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedUiState(),
    )

    init {
        observeFlashes()
    }

    private fun observeFlashes() {
        viewModelScope.launch {
            observePriceUpdates().collect { symbol ->
                delay(1_000)
                resetFlash(symbol)
            }
        }
    }

    fun toggleFeed() {
        if (_isFeedRunning.value) {
            stopFeed()
            _isFeedRunning.value = false
        } else {
            startFeed()
            _isFeedRunning.value = true
        }
    }
}
