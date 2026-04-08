package com.multibank.tracker

import com.multibank.tracker.domain.model.PriceChange
import com.multibank.tracker.domain.model.StockSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StockSymbolTest {

    @Test
    fun `default price is null`() {
        val stock = StockSymbol(symbol = "AAPL")
        assertNull(stock.price)
    }

    @Test
    fun `default change is NONE`() {
        val stock = StockSymbol(symbol = "AAPL")
        assertEquals(PriceChange.NONE, stock.change)
    }

    @Test
    fun `default description is empty string`() {
        val stock = StockSymbol(symbol = "AAPL")
        assertEquals("", stock.description)
    }

    @Test
    fun `default isFlashing is false`() {
        val stock = StockSymbol(symbol = "AAPL")
        assertFalse(stock.isFlashing)
    }


    @Test
    fun `copy with new price preserves all other fields`() {
        val original = StockSymbol(
            symbol = "NVDA",
            price = 100.0,
            change = PriceChange.UP,
            description = "NVIDIA Corp.",
            isFlashing = true,
        )
        val updated = original.copy(price = 200.0)

        assertEquals("NVDA", updated.symbol)
        assertEquals(200.0, updated.price)
        assertEquals(PriceChange.UP, updated.change)
        assertEquals("NVIDIA Corp.", updated.description)
        assertTrue(updated.isFlashing)
    }

    @Test
    fun `copy with isFlashing false resets flash without touching price`() {
        val flashing = StockSymbol(
            symbol = "TSLA",
            price = 250.0,
            change = PriceChange.DOWN,
            isFlashing = true,
        )
        val reset = flashing.copy(isFlashing = false)

        assertFalse(reset.isFlashing)
        assertEquals(250.0, reset.price)
        assertEquals(PriceChange.DOWN, reset.change)
    }

    @Test
    fun `copy with new change preserves price and symbol`() {
        val stock = StockSymbol("AMZN", price = 180.0, change = PriceChange.NONE)
        val updated = stock.copy(change = PriceChange.UP)

        assertEquals("AMZN", updated.symbol)
        assertEquals(180.0, updated.price)
        assertEquals(PriceChange.UP, updated.change)
    }


    @Test
    fun `two symbols with same fields are equal`() {
        val a = StockSymbol("GOOG", price = 150.0, change = PriceChange.UP)
        val b = StockSymbol("GOOG", price = 150.0, change = PriceChange.UP)
        assertEquals(a, b)
    }

    @Test
    fun `two symbols with different prices are not equal`() {
        val a = StockSymbol("GOOG", price = 150.0)
        val b = StockSymbol("GOOG", price = 151.0)
        assertNotEquals(a, b)
    }

    @Test
    fun `two symbols with different tickers are not equal`() {
        val a = StockSymbol("AAPL", price = 150.0)
        val b = StockSymbol("GOOG", price = 150.0)
        assertNotEquals(a, b)
    }

    @Test
    fun `two symbols differing only in isFlashing are not equal`() {
        val a = StockSymbol("META", isFlashing = true)
        val b = StockSymbol("META", isFlashing = false)
        assertNotEquals(a, b)
    }

    @Test
    fun `two symbols differing only in change are not equal`() {
        val a = StockSymbol("MSFT", change = PriceChange.UP)
        val b = StockSymbol("MSFT", change = PriceChange.DOWN)
        assertNotEquals(a, b)
    }


    @Test
    fun `PriceChange has exactly three values`() {
        assertEquals(3, PriceChange.entries.size)
    }

    @Test
    fun `PriceChange values are UP, DOWN and NONE`() {
        val values = PriceChange.entries.map { it.name }
        assertTrue(values.contains("UP"))
        assertTrue(values.contains("DOWN"))
        assertTrue(values.contains("NONE"))
    }

    // ── Sorting (used by repository to order the feed list) ───────────────────

    @Test
    fun `list sorted by price descending puts highest first`() {
        val stocks = listOf(
            StockSymbol("LOW", price = 50.0),
            StockSymbol("HIGH", price = 300.0),
            StockSymbol("MID", price = 150.0),
        ).sortedByDescending { it.price }

        assertEquals("HIGH", stocks[0].symbol)
        assertEquals("MID", stocks[1].symbol)
        assertEquals("LOW", stocks[2].symbol)
    }

    @Test
    fun `list sorted by price puts null-price symbols last`() {
        val stocks = listOf(
            StockSymbol("NO_PRICE", price = null),
            StockSymbol("HAS_PRICE", price = 100.0),
        ).sortedByDescending { it.price }

        assertEquals("HAS_PRICE", stocks[0].symbol)
        assertEquals("NO_PRICE", stocks[1].symbol)
    }
}
