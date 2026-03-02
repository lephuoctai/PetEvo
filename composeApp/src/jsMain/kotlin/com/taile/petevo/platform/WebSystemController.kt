package com.taile.petevo.platform

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

class WebSystemController : SystemController {

    private var wakeLockSentinel: dynamic = null

    override fun acquireWakeLock() {
        val navigator = window.asDynamic().navigator
        if (navigator.wakeLock != null && navigator.wakeLock != undefined) {
            navigator.wakeLock.request("screen").then { sentinel: dynamic ->
                wakeLockSentinel = sentinel
            }
        }
    }

    override fun releaseWakeLock() {
        wakeLockSentinel?.release()
        wakeLockSentinel = null
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
            val hidden = document.asDynamic().hidden as Boolean
            trySend(!hidden)
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

