package com.example.subscriptiontracker.ui.add

import com.example.subscriptiontracker.R

data class ServiceItem(
    val id: String,
    val name: String,
    val drawableResId: Int
)

// Local services list - add new services here
val services = listOf(
    ServiceItem("appletv", "Apple TV", R.drawable.appletv),
    ServiceItem("facebook", "Facebook", R.drawable.facebook),
    ServiceItem("google", "Google", R.drawable.google),
    ServiceItem("googleads", "Google Ads", R.drawable.googleads),
    ServiceItem("instagram", "Instagram", R.drawable.instagram),
    ServiceItem("netflix", "Netflix", R.drawable.netflix),
    ServiceItem("shopify", "Shopify", R.drawable.shopify),
    ServiceItem("spotify", "Spotify", R.drawable.spotify),
    ServiceItem("steam", "Steam", R.drawable.steam),
    ServiceItem("udemy", "Udemy", R.drawable.udemy),
    ServiceItem("youtube", "YouTube", R.drawable.youtube)
)

