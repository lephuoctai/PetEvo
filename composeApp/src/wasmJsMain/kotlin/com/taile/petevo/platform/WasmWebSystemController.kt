package com.taile.petevo.platform

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => document.hidden")
private external fun isDocumentHidden(): Boolean

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { try { navigator.vibrate([150, 100, 150]); } catch(e) {} }")
private external fun jsVibrateSuccess()

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { try { navigator.vibrate([400, 200, 400]); } catch(e) {} }")
private external fun jsVibrateFailure()

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("""(freq, duration) => {
    try {
        var ctx = new (window.AudioContext || window.webkitAudioContext)();
        var osc = ctx.createOscillator();
        var gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.frequency.value = freq;
        gain.gain.value = 0.3;
        osc.start();
        osc.stop(ctx.currentTime + duration / 1000.0);
    } catch(e) {}
}""")
private external fun jsBeep(freq: Double, duration: Double)

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

    override fun vibrate(success: Boolean) {
        try {
            if (success) jsVibrateSuccess() else jsVibrateFailure()
        } catch (_: Exception) { }
    }

    override fun playNotificationSound(success: Boolean) {
        if (success) {
            // Two cheerful beeps
            jsBeep(880.0, 200.0)
            jsBeep(1100.0, 300.0)
        } else {
            // Low warning tone
            jsBeep(330.0, 500.0)
        }
    }

    override fun observeAppVisibility(): Flow<Boolean> = callbackFlow {
        val visibilityHandler: (Event) -> Unit = {
            val hidden = isDocumentHidden()
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

