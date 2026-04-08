package com.multibank.tracker.data.source

import com.multibank.tracker.domain.model.StockSymbol

/**
 * Static list of the 25 traded symbols with their descriptions.
 * Lives in the Data layer because it is a data-source concern.
 */
val ALL_SYMBOLS: List<StockSymbol> = listOf(
    StockSymbol("AAPL",  description = "Apple Inc. – Consumer electronics, software & services."),
    StockSymbol("GOOG",  description = "Alphabet Inc. – Internet services, search & cloud."),
    StockSymbol("TSLA",  description = "Tesla Inc. – Electric vehicles & clean energy."),
    StockSymbol("AMZN",  description = "Amazon.com Inc. – E-commerce, cloud (AWS) & streaming."),
    StockSymbol("MSFT",  description = "Microsoft Corp. – Productivity software, Azure & Xbox."),
    StockSymbol("NVDA",  description = "NVIDIA Corp. – GPUs, AI chips & data-center hardware."),
    StockSymbol("META",  description = "Meta Platforms – Social media: Facebook, Instagram, WhatsApp."),
    StockSymbol("NFLX",  description = "Netflix Inc. – Subscription video streaming worldwide."),
    StockSymbol("ADBE",  description = "Adobe Inc. – Creative Cloud & document software."),
    StockSymbol("CRM",   description = "Salesforce Inc. – Cloud-based CRM & enterprise software."),
    StockSymbol("ORCL",  description = "Oracle Corp. – Database software & cloud infrastructure."),
    StockSymbol("INTC",  description = "Intel Corp. – Semiconductor chips & computing hardware."),
    StockSymbol("AMD",   description = "Advanced Micro Devices – CPUs, GPUs & semi solutions."),
    StockSymbol("PYPL",  description = "PayPal Holdings – Digital payments & fintech services."),
    StockSymbol("UBER",  description = "Uber Technologies – Ride-hailing & food delivery platform."),
    StockSymbol("LYFT",  description = "Lyft Inc. – Ride-sharing network in North America."),
    StockSymbol("SPOT",  description = "Spotify Technology – Music & podcast streaming service."),
    StockSymbol("SHOP",  description = "Shopify Inc. – E-commerce platform for businesses."),
    StockSymbol("SQ",    description = "Block Inc. (Square) – Mobile payments & financial tools."),
    StockSymbol("SNAP",  description = "Snap Inc. – Multimedia messaging app Snapchat."),
    StockSymbol("TWTR",  description = "X Corp. (formerly Twitter) – Social networking platform."),
    StockSymbol("ZOOM",  description = "Zoom Video Comm. – Video conferencing & collaboration."),
    StockSymbol("PINS",  description = "Pinterest Inc. – Visual discovery & idea-sharing platform."),
    StockSymbol("COIN",  description = "Coinbase Global – Cryptocurrency exchange & wallet."),
    StockSymbol("RBLX",  description = "Roblox Corp. – Online gaming platform & metaverse.")
)