package com.example.subscriptiontracker.utils

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.util.DebugLogger

object ImageLoaderProvider {
    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .respectCacheHeaders(false)
            .build()
    }
}

