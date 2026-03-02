package com.taile.petevo.platform

actual fun currentTimeMillis(): Long = kotlin.js.Date.now().toLong()

