package com.multibank.tracker.presentation.feed

import com.multibank.tracker.FakeStockRepository
import com.multibank.tracker.makeSymbol
import com.multibank.tracker.domain.usecase.ObserveConnectionUseCase
import com.multibank.tracker.domain.usecase.ObservePriceUpdatesUseCase
import com.multibank.tracker.domain.usecase.ObserveStocksUseCase
import com.multibank.tracker.domain.usecase.ResetFlashUseCase
import com.multibank.tracker.domain.usecase.StartFeedUseCase
import com.multibank.tracker.domain.usecase.StopFeedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeStockRepository
    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeStockRepository()
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = FeedViewModel(
        observeStocks = ObserveStocksUseCase(repo),
        observeConnection = ObserveConnectionUseCase(repo),
        observePriceUpdates = ObservePriceUpdatesUseCase(repo),
        startFeed = StartFeedUseCase(repo),
        stopFeed = StopFeedUseCase(repo),
        resetFlash = ResetFlashUseCase(repo),
    )


    @Test
    fun `initial uiState has empty stocks, not connected, feed not running`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.stocks.isEmpty())
        assertFalse(state.isConnected)
        assertFalse(state.isFeedRunning)
    }


    @Test
    fun `uiState stocks updates when repository emits new list`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitStocks(
            listOf(
                makeSymbol("AAPL", price = 150.0),
                makeSymbol("GOOG", price = 200.0)
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.stocks.size)
        assertEquals("AAPL", state.stocks[0].symbol)
    }

    @Test
    fun `uiState stocks reflects multiple sequential emissions`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("TSLA")))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.stocks.size)

        repo.emitStocks(listOf(makeSymbol("NVDA"), makeSymbol("META"), makeSymbol("AMZN")))
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.stocks.size)
    }

    @Test
    fun `uiState stocks is empty when repository emits empty list`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("AAPL")))
        advanceUntilIdle()
        repo.emitStocks(emptyList())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.stocks.isEmpty())
    }


    @Test
    fun `uiState isConnected becomes true when repository connects`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitConnected(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isConnected)
    }

    @Test
    fun `uiState isConnected becomes false after disconnection`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitConnected(true)
        advanceUntilIdle()
        repo.emitConnected(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isConnected)
    }


    @Test
    fun `toggleFeed starts feed when not running`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        assertFalse(repo.startCalled)
        viewModel.toggleFeed()
        advanceUntilIdle()

        assertTrue(repo.startCalled)
        assertTrue(viewModel.uiState.value.isFeedRunning)
    }

    @Test
    fun `toggleFeed stops feed when already running`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.toggleFeed()
        advanceUntilIdle()
        viewModel.toggleFeed()
        advanceUntilIdle()

        assertTrue(repo.stopCalled)
        assertFalse(viewModel.uiState.value.isFeedRunning)
    }

    @Test
    fun `toggleFeed start-stop-start cycles correctly`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.toggleFeed()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isFeedRunning)

        viewModel.toggleFeed()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isFeedRunning)

        viewModel.toggleFeed()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isFeedRunning)
    }

    @Test
    fun `toggleFeed does not call stop on first invocation`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.toggleFeed()
        advanceUntilIdle()

        assertFalse(repo.stopCalled)
    }

    @Test
    fun `toggleFeed does not call start when stopping`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.toggleFeed()
        advanceUntilIdle()
        repo.startCalled = false

        viewModel.toggleFeed()
        advanceUntilIdle()

        assertFalse(repo.startCalled)
    }


    @Test
    fun `isFeedRunning is false before any toggle`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isFeedRunning)
    }

    @Test
    fun `isFeedRunning is true immediately after start toggle`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.toggleFeed()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFeedRunning)
    }

    @Test
    fun `isFeedRunning is false immediately after stop toggle`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        viewModel.toggleFeed()
        advanceUntilIdle()
        viewModel.toggleFeed()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFeedRunning)
    }


    @Test
    fun `flash reset is called for symbol after 1 second delay`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitPriceUpdate("AAPL")
        advanceTimeBy(1_100)
        advanceUntilIdle()

        assertTrue(repo.flashResets.contains("AAPL"))
    }

    @Test
    fun `flash reset is NOT called before 1 second elapses`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitPriceUpdate("TSLA")
        advanceTimeBy(500)


        assertFalse(repo.flashResets.contains("TSLA"))
    }

    @Test
    fun `flash reset is called for each emitted symbol independently`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitPriceUpdate("AAPL")
        repo.emitPriceUpdate("GOOG")
        advanceTimeBy(1_100)
        advanceUntilIdle()

        assertTrue(repo.flashResets.contains("AAPL"))
        assertTrue(repo.flashResets.contains("GOOG"))
    }

    @Test
    fun `flash reset is called multiple times for repeated emissions of same symbol`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitPriceUpdate("NVDA")
        advanceTimeBy(1_100)
        advanceUntilIdle()
        repo.emitPriceUpdate("NVDA")
        advanceTimeBy(1_100)
        advanceUntilIdle()

        assertEquals(2, repo.flashResets.count { it == "NVDA" })
    }


    @Test
    fun `uiState combines stocks, connection and running state correctly`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("AAPL"), makeSymbol("MSFT")))
        repo.emitConnected(true)
        viewModel.toggleFeed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.stocks.size)
        assertTrue(state.isConnected)
        assertTrue(state.isFeedRunning)
    }

    @Test
    fun `uiState reflects independent changes to each field`() = runTest {
        viewModel.uiState.launchIn(backgroundScope)

        repo.emitConnected(true)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertTrue(state.isConnected)
        assertFalse(state.isFeedRunning)
        assertTrue(state.stocks.isEmpty())

        viewModel.toggleFeed()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue(state.isConnected)
        assertTrue(state.isFeedRunning)
    }
}
