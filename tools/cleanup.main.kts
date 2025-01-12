#!/usr/bin/env kotlin

@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:5.0.2")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import java.io.File
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.SKIP_SUBTREE
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name
import kotlin.io.path.visitFileTree

Cleanup().main(args)

/**
 * This script cleans up old Gradle modules that are no longer in used.
 * When a Gradle module is no longer referenced, it's build directory will live forever...
 */
class Cleanup() : CliktCommand("cleanup.main.kts") {
    override fun help(context: Context): String = "💣 Deletes unlinked Gradle modules…"

    private val dryRun by option("--dry-run", help = "Prints the directories to cleanup without deleting them.")
        .flag()
    private val path: Path by argument(name = "path", help = "The root path to cleanup.")
        .path(mustExist = true, canBeFile = false)

    override fun run() {
        val modules = path.buildDirs().map { it.parent.toFile() }
        val unlinkedModules = modules.filter { it.list()?.toList() == listOf("build") }
        if (unlinkedModules.isEmpty()) return
        if (dryRun) {
            echo(
                """
                    This is a dry run! Beware, there might be false positives!
                    The following paths can be removed:
                    
                    ${unlinkedModules.joinToString("\n") { it.normalize().absolutePath }}
                    """.trimIndent(),
            )
            return
        }
        unlinkedModules.forEach {
            println("🗑️ ${it.absolutePath}")
            it.deleteRecursively()
            it.parentFile.removeEmptyParents()
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun Path.buildDirs() = buildList {
        visitFileTree {
            onPreVisitDirectory { directory, _ ->
                when (directory.name) {
                    "build" -> SKIP_SUBTREE.also { add(directory) }
                    "src" -> SKIP_SUBTREE
                    else -> CONTINUE
                }
            }
        }
    }

    private fun File.removeEmptyParents() {
        if (!isDirectory) return
        if (list().orEmpty().isNotEmpty()) return
        delete()
        parentFile?.removeEmptyParents()
    }
}

