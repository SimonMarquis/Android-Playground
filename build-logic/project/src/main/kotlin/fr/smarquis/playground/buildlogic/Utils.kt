package fr.smarquis.playground.buildlogic


import java.io.File
import java.util.Locale
import java.util.Properties

public fun String.capitalized(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
}

public fun File.loadAsProperties(): Properties = Properties().apply { inputStream().buffered().use(::load) }

public inline fun <K, V> Iterable<K>.associateWithNotNull(valueSelector: (K) -> V?): Map<K, V> =
    mapNotNull { key -> valueSelector(key)?.let { key to it } }.toMap()
