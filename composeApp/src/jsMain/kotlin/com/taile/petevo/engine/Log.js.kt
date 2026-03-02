package com.taile.petevo.engine

internal actual fun logDebug(tag: String, message: String) {
    console.log("[$tag] $message")
}
