package com.taile.petevo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner

actual class PlatformContext

@Composable
actual fun rememberSystemController(): SystemController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    return remember { AndroidSystemController(context.applicationContext, lifecycleOwner) }
}

@Composable
actual fun rememberPetStorage(): PetStorage {
    val context = LocalContext.current
    return remember { AndroidPetStorage(context.applicationContext) }
}

