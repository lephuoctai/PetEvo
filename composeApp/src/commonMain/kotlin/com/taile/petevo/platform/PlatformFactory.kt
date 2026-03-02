package com.taile.petevo.platform

import androidx.compose.runtime.Composable

/**
 * Expect declarations for platform-specific factory functions.
 * These are provided as composable to allow access to platform context.
 */
expect class PlatformContext

@Composable
expect fun rememberSystemController(): SystemController

@Composable
expect fun rememberPetStorage(): PetStorage

