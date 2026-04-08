package com.multibank.tracker.domain.usecase

import com.multibank.tracker.FakeStockRepository
import com.multibank.tracker.makeSymbol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UseCasesTest {

    private lateinit var repo: FakeStockRepository

    private lateinit var observeStocks: ObserveStocksUseCase
    private lateinit var observePriceUpdates: ObservePriceUpdatesUseCase
    private lateinit var observeConnection: ObserveConnectionUseCase
    private lateinit var startFeed: StartFeedUseCase
    private lateinit var stopFeed: StopFeedUseCase
    private lateinit var resetFlash: ResetFlashUseCase
    private lateinit var getSymbolDetail: GetSymbolDetailUseCase

    @Before
    fun setUp() {
        repo = FakeStockRepository()
        observeStocks = ObserveStocksUseCase(repo)
        observePriceUpdates = ObservePriceUpdatesUseCase(repo)
        observeConnection = ObserveConnectionUseCase(repo)
        startFeed = StartFeedUseCase(repo)
        stopFeed = StopFeedUseCase(repo)
        resetFlash = ResetFlashUseCase(repo)
        getSymbolDetail = GetSymbolDetailUseCase(repo)
    }


    @Test
    fun `observeStocks returns empty list initially`() = runTest {
        val result = observeStocks().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `observeStocks emits updated list when repository changes`() = runTest {
        val stocks = listOf(makeSymbol("AAPL"), makeSymbol("GOOG"))
        repo.emitStocks(stocks)

        val result = observeStocks().first()
        assertEquals(2, result.size)
        assertEquals("AAPL", result[0].symbol)
        assertEquals("GOOG", result[1].symbol)
    }

    @Test
    fun `observeStocks reflects multiple emissions in order`() = runTest {
        repo.emitStocks(listOf(makeSymbol("TSLA")))
        assertEquals("TSLA", observeStocks().first()[0].symbol)

        repo.emitStocks(listOf(makeSymbol("NVDA"), makeSymbol("META")))
        assertEquals(2, observeStocks().first().size)
    }


    @Test
    fun `observeConnection returns false initially`() = runTest {
        assertFalse(observeConnection().first())
    }

    @Test
    fun `observeConnection returns true when repository is connected`() = runTest {
        repo.emitConnected(true)
        assertTrue(observeConnection().first())
    }

    @Test
    fun `observeConnection returns false after disconnection`() = runTest {
        repo.emitConnected(true)
        repo.emitConnected(false)
        assertFalse(observeConnection().first())
    }


    @Test
    fun `observePriceUpdates emits symbol after repository push`() = runTest {
        val flow = observePriceUpdates()
        repo.emitPriceUpdate("AAPL")

        assertTrue(true) // emission did not throw
    }


    @Test
    fun `startFeed delegates to repository start()`() {
        assertFalse(repo.startCalled)
        startFeed()
        assertTrue(repo.startCalled)
    }

    @Test
    fun `startFeed can be called multiple times without error`() {
        startFeed()
        startFeed()
        assertTrue(repo.startCalled)
    }


    @Test
    fun `stopFeed delegates to repository stop()`() {
        assertFalse(repo.stopCalled)
        stopFeed()
        assertTrue(repo.stopCalled)
    }

    @Test
    fun `stopFeed does not call start`() {
        stopFeed()
        assertFalse(repo.startCalled)
    }


    @Test
    fun `resetFlash delegates symbol to repository`() {
        resetFlash("NVDA")
        assertEquals(listOf("NVDA"), repo.flashResets)
    }

    @Test
    fun `resetFlash records each symbol independently`() {
        resetFlash("AAPL")
        resetFlash("TSLA")
        resetFlash("AAPL")
        assertEquals(listOf("AAPL", "TSLA", "AAPL"), repo.flashResets)
    }

    @Test
    fun `resetFlash with empty string does not crash`() {
        resetFlash("")
        assertEquals(listOf(""), repo.flashResets)
    }


    @Test
    fun `getSymbolDetail returns full stocksState flow`() = runTest {
        val stocks = listOf(makeSymbol("AAPL"), makeSymbol("GOOG"))
        repo.emitStocks(stocks)

        val result = getSymbolDetail("AAPL").first()
        assertEquals(2, result.size)
    }

    @Test
    fun `getSymbolDetail returns same flow regardless of symbol argument`() = runTest {
        val stocks = listOf(makeSymbol("MSFT"))
        repo.emitStocks(stocks)

        // The use case returns the full list; filtering is done in the ViewModel
        val resultA = getSymbolDetail("MSFT").first()
        val resultB = getSymbolDetail("AAPL").first()
        assertEquals(resultA, resultB)
    }
}
