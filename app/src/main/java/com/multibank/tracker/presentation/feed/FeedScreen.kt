package com.multibank.tracker.presentation.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.multibank.tracker.domain.model.PriceChange
import com.multibank.tracker.domain.model.StockSymbol

private val GreenColor = Color(0xFF4CAF50)
private val RedColor   = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onSymbolClick: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Price Tracker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // ── 🟢 / 🔴 connection dot ────────────────────────────────
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isConnected) GreenColor else RedColor)
                    )
                },
                actions = {
                    Button(
                        onClick = { viewModel.toggleFeed() },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isFeedRunning) RedColor else GreenColor
                        )
                    ) {
                        Text(if (uiState.isFeedRunning) "Stop" else "Start")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(items = uiState.stocks, key = { it.symbol }) { stock ->
                StockRow(stock = stock, onClick = { onSymbolClick(stock.symbol) })
            }
        }
    }
}


@Composable
private fun StockRow(stock: StockSymbol, onClick: () -> Unit) {
    val flashTarget = when {
        stock.isFlashing && stock.change == PriceChange.UP   -> GreenColor.copy(alpha = 0.15f)
        stock.isFlashing && stock.change == PriceChange.DOWN -> RedColor.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }
    val animatedBg by animateColorAsState(
        targetValue    = flashTarget,
        animationSpec  = tween(400),
        label          = "row_flash",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = stock.symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text       = stock.price?.let { "$%.2f".format(it) } ?: "—",
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            PriceArrow(stock.change)
        }
    }
}


@Composable
fun PriceArrow(change: PriceChange) {
    val (arrow, color) = when (change) {
        PriceChange.UP   -> "↑" to GreenColor
        PriceChange.DOWN -> "↓" to RedColor
        PriceChange.NONE -> "—" to Color.Gray
    }
    Text(text = arrow, color = color, fontWeight = FontWeight.Bold)
}
