package com.taile.petevo.platform

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

class WasmWebSystemController : SystemController {

    override fun acquireWakeLock() {
        // WakeLock API not easily accessible in Wasm; no-op for now
    }

    override fun releaseWakeLock() {
        // no-op
    }

    override fun enableFocusMode() {
        // No-op on web
    }

    override fun disableFocusMode() {
        // No-op on web
    }

    override fun toggleConnectivity(enable: Boolean) {
        // No-op on web
    }

    override fun observeAppVisibility(): Flow<Boolean> = callbackFlow {
        val visibilityHandler: (Event) -> Unit = {
            val hidden = js("document.hidden")
            val isHidden = hidden.toString() == "true"
            trySend(!isHidden)
        }
        val blurHandler: (Event) -> Unit = {
            trySend(false)
        }
        val focusHandler: (Event) -> Unit = {
            trySend(true)
        }

        document.addEventListener("visibilitychange", visibilityHandler)
        window.addEventListener("blur", blurHandler)
        window.addEventListener("focus", focusHandler)

        awaitClose {
            document.removeEventListener("visibilitychange", visibilityHandler)
            window.removeEventListener("blur", blurHandler)
            window.removeEventListener("focus", focusHandler)
        }
    }
}

