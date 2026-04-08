package com.multibank.tracker.data.source

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val TAG = "WebSocketDataSource"
private const val WS_URL = "wss://ws.postman-echo.com/raw"
private const val INTERVAL = 2_000L

@Singleton
class WebSocketDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _rawEchos = MutableSharedFlow<String>(extraBufferCapacity = 256)
    val rawEchos: SharedFlow<String> = _rawEchos.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var scope: CoroutineScope? = null

    private val basePrices: MutableMap<String, Double> =
        ALL_SYMBOLS.associate { it.symbol to Random.nextDouble(50.0, 3_500.0) }.toMutableMap()

    fun connect() {
        if (_isConnected.value) return
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        openSocket()
    }

    fun disconnect() {
        scope?.cancel()
        scope = null
        webSocket?.close(1000, "User stopped feed")
        webSocket = null
        _isConnected.value = false
    }

    private fun openSocket() {
        val request = Request.Builder().url(WS_URL).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "onOpen")
                _isConnected.value = true
                startTicking(ws)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                _rawEchos.tryEmit(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "onFailure: ${t.message}")
                _isConnected.value = false
                scope?.launch {
                    delay(3_000)
                    if (isActive) openSocket()
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "onClosed: $reason")
                _isConnected.value = false
            }
        })
    }

    private fun startTicking(ws: WebSocket) {
        scope?.launch {
            while (isActive) {
                ALL_SYMBOLS.forEach { stock ->
                    val prev     = basePrices[stock.symbol] ?: Random.nextDouble(50.0, 3500.0)
                    val newPrice = (prev + prev * Random.nextDouble(-0.02, 0.02)).coerceAtLeast(1.0)
                    basePrices[stock.symbol] = newPrice

                    val json = JSONObject()
                        .put("symbol", stock.symbol)
                        .put("price", "%.2f".format(newPrice).toDouble())
                        .toString()
                    ws.send(json)
                }
                delay(INTERVAL)
            }
        }
    }
}