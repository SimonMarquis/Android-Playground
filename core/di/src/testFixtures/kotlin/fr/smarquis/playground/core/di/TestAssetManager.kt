package fr.smarquis.playground.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.junit.rules.TemporaryFolder
import java.io.File

open class TestAssetManager(private val folder: TemporaryFolder) : AssetManager {
    override fun open(name: String) = File(folder.root, name).inputStream()
}

@Module
@InstallIn(SingletonComponent::class)
object AssetManagerFixturesModule {
    @Provides
    fun provides(): AssetManager = TODO("Please `@Provides` or `@BindValue` a `TestAssetManager` instance explicitly!")
}
