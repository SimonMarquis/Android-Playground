package fr.smarquis.playground.core.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import org.junit.rules.TemporaryFolder

@ContributesBinding(AppScope::class)
public class TestFileManager(private val folder: TemporaryFolder) : FileManager {
    override fun invoke(name: String) = folder.root.resolve(name).apply { createNewFile() }
    override fun openFileInput(name: String) = this(name).inputStream()
    override fun openFileOutput(name: String) = this(name).outputStream()
    override fun deleteFile(name: String) = this(name).delete()
}
