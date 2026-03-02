package com.taile.petevo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class PlatformContext

@Composable
actual fun rememberSystemController(): SystemController {
    return remember { WebSystemController() }
}

@Composable
actual fun rememberPetStorage(): PetStorage {
    return remember { WebPetStorage() }
}

