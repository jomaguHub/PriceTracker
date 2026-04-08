package com.multibank.tracker.presentation.detail

import com.multibank.tracker.domain.model.StockSymbol

data class DetailUiState(
    val stock: StockSymbol? = null,
)