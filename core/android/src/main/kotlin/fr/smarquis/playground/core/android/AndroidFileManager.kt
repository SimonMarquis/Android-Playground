package fr.smarquis.playground.core.android

import android.app.Application
import android.content.Context.MODE_PRIVATE
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import fr.smarquis.playground.core.di.FileManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@ContributesBinding(AppScope::class)
public class AndroidFileManager(private val app: Application) : FileManager {
    override fun invoke(name: String): File = File(app.filesDir, name)
    override fun openFileInput(name: String): FileInputStream = app.openFileInput(name)
    override fun openFileOutput(name: String): FileOutputStream = app.openFileOutput(name, MODE_PRIVATE)
    override fun deleteFile(name: String): Boolean = app.deleteFile(name)
}
