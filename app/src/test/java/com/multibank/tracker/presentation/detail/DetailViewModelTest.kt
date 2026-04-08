package com.multibank.tracker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.multibank.tracker.FakeStockRepository
import com.multibank.tracker.makeSymbol
import com.multibank.tracker.domain.model.PriceChange
import com.multibank.tracker.domain.usecase.GetSymbolDetailUseCase
import com.multibank.tracker.presentation.navigation.NAV_ARG_SYMBOL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeStockRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeStockRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(symbol: String): DetailViewModel {
        val savedState = SavedStateHandle(mapOf(NAV_ARG_SYMBOL to symbol))
        return DetailViewModel(
            savedStateHandle = savedState,
            getSymbolDetail = GetSymbolDetailUseCase(repo),
        )
    }


    @Test
    fun `symbol is read correctly from SavedStateHandle`() {
        val vm = buildViewModel("AAPL")
        assertEquals("AAPL", vm.symbol)
    }

    @Test
    fun `symbol reflects the exact value passed in SavedStateHandle`() {
        val vm = buildViewModel("NVDA")
        assertEquals("NVDA", vm.symbol)
    }


    @Test
    fun `initial uiState has null stock`() = runTest {
        val vm = buildViewModel("AAPL")
        vm.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertNull(vm.uiState.value.stock)
    }


    @Test
    fun `uiState stock is populated when matching symbol is emitted`() = runTest {
        val vm = buildViewModel("AAPL")
        vm.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("AAPL", price = 175.0), makeSymbol("GOOG")))
        advanceUntilIdle()

        val stock = vm.uiState.value.stock
        assertEquals("AAPL", stock?.symbol)
        assertEquals(175.0, stock?.price)
    }

    @Test
    fun `uiState stock is null when symbol is not in emitted list`() = runTest {
        val vm = buildViewModel("TSLA")
        vm.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("AAPL"), makeSymbol("GOOG")))
        advanceUntilIdle()

        assertNull(vm.uiState.value.stock)
    }

    @Test
    fun `uiState stock filters correctly from a large list`() = runTest {
        val vm = buildViewModel("NVDA")
        vm.uiState.launchIn(backgroundScope)

        repo.emitStocks(
            listOf(
                makeSymbol("AAPL", price = 150.0),
                makeSymbol("GOOG", price = 200.0),
                makeSymbol("NVDA", price = 875.0),
                makeSymbol("META", price = 300.0),
                makeSymbol("MSFT", price = 420.0),
            )
        )
        advanceUntilIdle()

        val stock = vm.uiState.value.stock
        assertEquals("NVDA", stock?.symbol)
        assertEquals(875.0, stock?.price)
    }

    @Test
    fun `uiState stock updates when price changes for the observed symbol`() = runTest {
        val vm = buildViewModel("TSLA")
        vm.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("TSLA", price = 200.0)))
        advanceUntilIdle()
        assertEquals(200.0, vm.uiState.value.stock?.price)

        repo.emitStocks(listOf(makeSymbol("TSLA", price = 215.0)))
        advanceUntilIdle()
        assertEquals(215.0, vm.uiState.value.stock?.price)
    }

    @Test
    fun `uiState stock becomes null when symbol disappears from list`() = runTest {
        val vm = buildViewModel("AMZN")
        vm.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("AMZN")))
        advanceUntilIdle()
        assertEquals("AMZN", vm.uiState.value.stock?.symbol)

        repo.emitStocks(listOf(makeSymbol("AAPL")))
        advanceUntilIdle()
        assertNull(vm.uiState.value.stock)
    }


    @Test
    fun `uiState stock reflects PriceChange UP`() = runTest {
        val vm = buildViewModel("CRM")
        vm.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("CRM", change = PriceChange.UP)))
        advanceUntilIdle()

        assertEquals(PriceChange.UP, vm.uiState.value.stock?.change)
    }

    @Test
    fun `uiState stock reflects PriceChange DOWN`() = runTest {
        val vm = buildViewModel("CRM")
        vm.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("CRM", change = PriceChange.DOWN)))
        advanceUntilIdle()

        assertEquals(PriceChange.DOWN, vm.uiState.value.stock?.change)
    }

    @Test
    fun `uiState stock reflects isFlashing true`() = runTest {
        val vm = buildViewModel("INTC")
        vm.uiState.launchIn(backgroundScope)

        repo.emitStocks(listOf(makeSymbol("INTC", isFlashing = true)))
        advanceUntilIdle()

        assertEquals(true, vm.uiState.value.stock?.isFlashing)
    }


    @Test
    fun `two ViewModels with different symbols each see only their symbol`() = runTest {
        val vmAapl = buildViewModel("AAPL")
        val vmGoog = buildViewModel("GOOG")
        vmAapl.uiState.launchIn(backgroundScope)
        vmGoog.uiState.launchIn(backgroundScope)

        repo.emitStocks(
            listOf(
                makeSymbol("AAPL", price = 150.0),
                makeSymbol("GOOG", price = 200.0),
            )
        )
        advanceUntilIdle()

        assertEquals("AAPL", vmAapl.uiState.value.stock?.symbol)
        assertEquals(150.0, vmAapl.uiState.value.stock?.price)
        assertEquals("GOOG", vmGoog.uiState.value.stock?.symbol)
        assertEquals(200.0, vmGoog.uiState.value.stock?.price)
    }


    @Test
    fun `uiState stock description is preserved from repository`() = runTest {
        val vm = buildViewModel("SPOT")
        vm.uiState.launchIn(backgroundScope)

        val stock = makeSymbol("SPOT").copy(description = "Spotify Technology")
        repo.emitStocks(listOf(stock))
        advanceUntilIdle()

        assertEquals("Spotify Technology", vm.uiState.value.stock?.description)
    }
}
