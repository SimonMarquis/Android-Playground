package fr.smarquis.playground.core.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import org.junit.rules.TemporaryFolder
import java.io.InputStream

@ContributesBinding(AppScope::class)
public class TestAssetManager(private val folder: TemporaryFolder) : AssetManager {
    override fun open(name: String): InputStream = folder.root.resolve(name).inputStream()
}
