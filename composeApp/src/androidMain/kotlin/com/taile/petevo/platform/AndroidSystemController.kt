package com.taile.petevo.platform

import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class AndroidSystemController(private val context: Context) : SystemController {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun acquireWakeLock() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "PetEvo:FocusWakeLock"
        ).apply {
            acquire(120 * 60 * 1000L) // max 2 hours
        }
    }

    override fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    override fun enableFocusMode() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    override fun disableFocusMode() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    override fun toggleConnectivity(enable: Boolean) {
        // WiFi
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            wifiManager.isWifiEnabled = enable
        } catch (_: Exception) {
            // May fail without proper permissions on newer Android
        }

        // Bluetooth
        try {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val btAdapter = btManager?.adapter
            if (btAdapter != null) {
                @Suppress("DEPRECATION")
                if (enable) btAdapter.enable() else btAdapter.disable()
            }
        } catch (_: Exception) {
            // May fail without BLUETOOTH_CONNECT on API 31+
        }

        // GPS — can only open settings, cannot toggle programmatically
        // We skip GPS toggle as it requires system-level permission
    }

    override fun observeAppVisibility(): Flow<Boolean> = callbackFlow {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                trySend(true)
            }

            override fun onStop(owner: LifecycleOwner) {
                trySend(false)
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)

        awaitClose {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
        }
    }
}

