package com.taile.petevo.platform

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AndroidSystemController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : SystemController {

    private var wakeLock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout")
    override fun acquireWakeLock() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
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

    @SuppressLint("MissingPermission")
    override fun toggleConnectivity(enable: Boolean) {
        // WiFi
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            wifiManager.isWifiEnabled = enable
        } catch (_: Exception) { }

        // Bluetooth
        toggleBluetooth(enable)
    }

    @SuppressLint("MissingPermission")
    private fun toggleBluetooth(enable: Boolean) {
        try {
            // Always check permission first
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) ==
                    PackageManager.PERMISSION_GRANTED
            }
            if (!hasPermission) return

            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                ?: return
            val btAdapter: BluetoothAdapter = btManager.adapter ?: return
            @Suppress("DEPRECATION")
            if (enable) btAdapter.enable() else btAdapter.disable()
        } catch (_: SecurityException) { }
        catch (_: Exception) { }
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

        withContext(Dispatchers.Main) {
            lifecycleOwner.lifecycle.addObserver(observer)
        }

        awaitClose {
            MainScope().launch {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}

