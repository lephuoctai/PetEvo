package com.taile.petevo.platform

import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific controller for hardware/system features.
 */
interface SystemController {
    /** Prevent screen from sleeping */
    fun acquireWakeLock()
    fun releaseWakeLock()

    /** Enable DND / focus mode (Android only, no-op on web) */
    fun enableFocusMode()
    fun disableFocusMode()

    /** Toggle WiFi/BT/GPS off/on (Android only, no-op on web) */
    fun toggleConnectivity(enable: Boolean)

    /** Emits false when user leaves the app (tab hidden / app backgrounded) */
    fun observeAppVisibility(): Flow<Boolean>
}

