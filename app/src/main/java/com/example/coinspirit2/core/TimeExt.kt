package com.example.coinspirit2.core

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun Instant.asIso(): String =
    DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(this)
