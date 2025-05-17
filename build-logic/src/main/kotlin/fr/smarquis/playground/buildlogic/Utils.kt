package fr.smarquis.playground.buildlogic


import java.io.File
import java.util.Locale
import java.util.Properties
import kotlin.system.measureNanoTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

public fun String.capitalized(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
}

public fun File.loadAsProperties(): Properties = Properties().apply { inputStream().buffered().use(::load) }

public inline fun <K, V> Iterable<K>.associateWithNotNull(valueSelector: (K) -> V?): Map<K, V> =
    mapNotNull { key -> valueSelector(key)?.let { key to it } }.toMap()

// kotlin.time.TimedValue is not yet available
public data class TimedValue<T>(val value: T, val duration: Duration)

// kotlin.time.measureTimedValue is not yet available
public inline fun <T> measureTimedValue(block: () -> T): TimedValue<T> {
    var value: T
    val nanos = measureNanoTime { value = block() }
    return TimedValue(value, nanos.nanoseconds)
}
