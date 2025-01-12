package fr.smarquis.playground.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.junit.rules.TemporaryFolder
import java.io.File

open class TestFileManager(private val folder: TemporaryFolder) : FileManager {
    override fun invoke(name: String) = File(folder.root, name).apply { createNewFile() }
    override fun openFileInput(name: String) = this(name).inputStream()
    override fun openFileOutput(name: String) = this(name).outputStream()
    override fun deleteFile(name: String) = this(name).delete()
}

@Module
@InstallIn(SingletonComponent::class)
object FileManagerFixturesModule {
    @Provides
    fun provides(): FileManager = TODO("Please `@Provides` or `@BindValue` a `TestFileManager` instance explicitly!")
}
