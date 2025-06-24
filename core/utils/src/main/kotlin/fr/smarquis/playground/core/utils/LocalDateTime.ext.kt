package fr.smarquis.playground.core.utils

import kotlin.time.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime

public fun Long.toLocalDateTime(): LocalDateTime = fromEpochMilliseconds(this).toLocalDateTime(currentSystemDefault())
