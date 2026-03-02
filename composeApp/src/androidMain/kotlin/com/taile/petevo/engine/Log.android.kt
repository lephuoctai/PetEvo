package com.taile.petevo.engine

internal actual fun logDebug(tag: String, message: String) {
    android.util.Log.d(tag, message)
}

