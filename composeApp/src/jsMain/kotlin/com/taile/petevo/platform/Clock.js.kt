package com.taile.petevo.platform

actual fun currentTimeMillis(): Long = js("Date.now()").unsafeCast<Double>().toLong()

