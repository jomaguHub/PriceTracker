package com.multibank.tracker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.multibank.tracker.domain.usecase.GetSymbolDetailUseCase
import com.multibank.tracker.presentation.navigation.NAV_ARG_SYMBOL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSymbolDetail: GetSymbolDetailUseCase
) : ViewModel() {

    val symbol: String = checkNotNull(savedStateHandle[NAV_ARG_SYMBOL])

    val uiState = getSymbolDetail(symbol)
        .map { list -> DetailUiState(stock = list.find { it.symbol == symbol }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState(),
        )
}
