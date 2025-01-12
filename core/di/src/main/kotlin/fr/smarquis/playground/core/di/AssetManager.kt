package fr.smarquis.playground.core.di

import java.io.IOException
import java.io.InputStream

public fun interface AssetManager {
    @Throws(IOException::class)
    public fun open(name: String): InputStream
}
