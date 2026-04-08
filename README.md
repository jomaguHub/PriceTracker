# 📈 Real-Time Price Tracker

This is an Android application with **Jetpack Compose, MVVM, Clean Architecture, Dagger Hilt, SOLID, OkHttp WS, and WebSockets** that displays live price updates for 25 stock symbols using a WebSocket server.


---

## Architecture

```
app/
└── src/main/java/com/multibank/tracker/
    ├── domain/
    │   ├── model/
    │   │   └── StockSymbol.kt      # Data class + enum + 25 symbols
    │   ├── repository/
    │   │   └── StockRepository.kt
    │   └── usecase/
    │       └── GetFeedsUseCase.kt
    ├── data/
    │   ├── source/
    │   │   ├── WebSocketDataSource.kt     # OkHttp WS + StateFlow
    │   │   └── SymbolConstants.kt
    │   └── repository/
    │       └── StockRepositoryImpl.kt
    ├── di/
    │   └── AppModules.kt
    ├── presentation/
    │   ├── feed/
    │   │   ├── FeedUiState.kt       
    │   │   ├── FeedViewModel.kt     # Toggle feed, observe stocks
    │   │   └── FeedScreen.kt        # LazyColumn + TopBar
    │   ├── detail/
    │   │   ├── DetailUiState.kt
    │   │   ├── DetailViewModel.kt   # SavedStateHandle, filter by symbol
    │   │   └── DetailScreen.kt      # Price card + description card
    │   └── navigation/
    │       └── NavGraph.kt          # NavHost, routes, deep links
    ├── ui/
    │   └── theme/
    │       ├── Color.kt
    │       ├── Theme.kt             # Light + Dark + Material
    │       └── Type.kt
    ├── PriceTrackerApplication.kt
    └── MainActivity.kt    
```

### Key design decisions

- **Single WebSocket connection** — `WebSocketDataSource` is `@Singleton`. Both screens observe the same `stocksState: StateFlow<List<StockSymbol>>` from `StockRepositoryImpl` without opening duplicate connections.
- **Clean Architecture layers** — ViewModels depend only on use cases from the Domain layer; they never import anything from `data.*`. The only bridge between layers is the `StockRepository` interface.
- **Immutable UI state** — `StockSymbol` is a `data class`. Every update produces a new sorted list via `.map { }` and `.sortedByDescending { }`.
- **Flash animation** — `StockRepositoryImpl` sets `isFlashing = true` on each echo. `FeedViewModel` collects `priceUpdates: SharedFlow<String>`, waits 1 second with `delay(1_000)`, and calls `ResetFlashUseCase(symbol)`. The Compose UI animates the background color with `animateColorAsState`.
- **SavedStateHandle** — `DetailViewModel` reads `NAV_ARG_SYMBOL` from `SavedStateHandle` (populated automatically by Navigation Compose), so the selected symbol survives process death and screen rotation.
- **Dependency Injection** — Hilt provides `OkHttpClient` as a singleton via `NetworkModule`, and binds `StockRepositoryImpl` to the `StockRepository` interface via `RepositoryModule` using `@Binds`.

---

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (100%)
- **Architecture**: MVVM + Clean Architecture
- **Navigation**: Navigation Compose
- **DI**: Dagger Hilt
- **Async**: Kotlin Coroutines + StateFlow + SharedFlow
- **WebSocket**: OkHttp 4
- **Min SDK**: 24

---

## Getting Started

1. Clone the repository.
2. Open in **Android Studio Hedgehog** or later.
3. Run on a device / emulator with internet access.
4. Tap **Start** in the top-right corner to begin the live feed.
