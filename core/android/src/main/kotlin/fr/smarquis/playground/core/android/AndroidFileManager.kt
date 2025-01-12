package fr.smarquis.playground.core.android

import android.app.Application
import android.content.Context.MODE_PRIVATE
import fr.smarquis.playground.core.di.FileManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

internal class AndroidFileManager @Inject constructor(private val app: Application) : FileManager {
    override fun invoke(name: String): File = File(app.filesDir, name)
    override fun openFileInput(name: String): FileInputStream = app.openFileInput(name)
    override fun openFileOutput(name: String): FileOutputStream = app.openFileOutput(name, MODE_PRIVATE)
    override fun deleteFile(name: String): Boolean = app.deleteFile(name)
}
