package fr.smarquis.playground.core.di

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

public interface FileManager {
    public operator fun invoke(name: String): File

    @Throws(FileNotFoundException::class)
    public fun openFileInput(name: String): FileInputStream

    @Throws(FileNotFoundException::class)
    public fun openFileOutput(name: String): FileOutputStream

    public fun deleteFile(name: String): Boolean
}
