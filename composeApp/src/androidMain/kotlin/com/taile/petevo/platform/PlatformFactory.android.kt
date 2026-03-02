package com.taile.petevo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class PlatformContext

@Composable
actual fun rememberSystemController(): SystemController {
    val context = LocalContext.current
    return remember { AndroidSystemController(context.applicationContext) }
}

@Composable
actual fun rememberPetStorage(): PetStorage {
    val context = LocalContext.current
    return remember { AndroidPetStorage(context.applicationContext) }
}

