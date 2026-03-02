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
        // In Wasm, Document doesn't expose visibilityState directly.
        // visibilitychange fires when tab is hidden/shown — treat it as hidden.
        val visibilityHandler: (Event) -> Unit = {
            // When visibilitychange fires and we can't read the state,
            // conservatively treat it as "hidden" to enforce strict focus.
            trySend(false)
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

