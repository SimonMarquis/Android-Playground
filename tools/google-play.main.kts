#!/usr/bin/env kotlin

@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:5.0.3")
@file:DependsOn("com.google.apis:google-api-services-androidpublisher:v3-rev20250903-2.0.0")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisher.Edits
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import com.google.api.services.androidpublisher.model.Apk
import com.google.api.services.androidpublisher.model.AppEdit
import com.google.api.services.androidpublisher.model.Bundle
import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream
import java.util.concurrent.TimeUnit.MINUTES
import javax.annotation.CheckReturnValue


GooglePlay()
    .subcommands(Publish())
    .main(args)

/**
 * [android-publisher](https://developers.google.com/android-publisher)
 */
private class GooglePlay : CliktCommand("google-play.main.kts") {
    override fun help(context: Context): String = "Google Play"
    override fun run() = Unit
}

private class Publish : CliktCommand() {
    override fun help(context: Context) = "Publish to Google Play Console"
    private val appId by option().required()
    private val releaseName by option()
    private val track by option().choice("production", "qa", "alpha", "beta", "internal").required()
    private val status by option().choice("draft", "inProgress", "halted", "completed").required()
    private val credentials by option().file(mustExist = true, mustBeReadable = true, canBeDir = false).required()
    private val file by option().file(mustExist = true, mustBeReadable = true, canBeDir = false).required()

    override fun run() {
        val credentials = GoogleCredentials
            .fromStream(FileInputStream(credentials))
            .createScoped(AndroidPublisherScopes.all())

        val publisher = AndroidPublisher.Builder(
            /* transport = */ GoogleNetHttpTransport.newTrustedTransport(),
            /* jsonFactory = */ GsonFactory.getDefaultInstance(),
            /* httpRequestInitializer */ credentials.httpRequestInitializer(),
        )
            .setApplicationName("Google Play Console upload")
            .build()

        publisher.edits {
            val uploadedVersionCode = when (val extension = file.extension) {
                "apk" -> uploadApk(it).getOrThrow().versionCode
                "aab" -> uploadAab(it).getOrThrow().versionCode
                else -> error("Unsupported app file extension: $extension")
            }
            updateTrack(it, uploadedVersionCode).getOrThrow()
        }.getOrThrow()
    }

    private fun GoogleCredentials.httpRequestInitializer() = HttpRequestInitializer {
        it.connectTimeout = MINUTES.toMillis(5).toInt()
        it.readTimeout = MINUTES.toMillis(5).toInt()
        HttpCredentialsAdapter(this).initialize(it)
    }

    @CheckReturnValue
    private fun AndroidPublisher.edits(action: Edits.(AppEdit) -> Unit) = with(edits()) {
        echo("📝 Creating edit…")
        val appEdit = insert(appId, null).execute()
        echo("📝 Edit created!")
        runCatching {
            action(appEdit)
        }.mapCatching {
            echo("✅ Committing edit…")
            commit(appId, appEdit.id).execute()
            echo("✅ Edit committed!")
        }.onFailure {
            echo("♻️ Deleting edit…")
            delete(appId, appEdit.id).execute()
            echo("♻️ Edit deleted!")
        }
    }

    @CheckReturnValue
    private fun Edits.uploadAab(edit: AppEdit): Result<Bundle> = runCatching {
        echo("📡 Uploading ${file.name}…")
        bundles().upload(appId, edit.id, FileContent("application/octet-stream", file)).execute()
    }.onSuccess {
        echo("📡 AAB uploaded! (versionCode=${it.versionCode})")
    }.onFailure {
        echo(err = true, message = "🚨 Failed to upload AAB!")
        echo(err = true, message = it)
    }

    @CheckReturnValue
    private fun Edits.uploadApk(edit: AppEdit): Result<Apk> = runCatching {
        echo("📡 Uploading ${file.name}…")
        apks().upload(appId, edit.id, FileContent("application/vnd.android.package-archive", file)).execute()
    }.onSuccess {
        echo("📡 APK uploaded! ${it.versionCode}")
    }.onFailure {
        echo(err = true, message = "🚨 Failed to upload APK!")
        echo(err = true, message = it)
    }

    @CheckReturnValue
    private fun Edits.updateTrack(edit: AppEdit, uploadedVersionCode: Int): Result<Track> = runCatching {
        echo("🛤️ Updating $track track!")
        tracks().update(appId, edit.id, track, track(uploadedVersionCode.toLong())).execute()
    }.onSuccess {
        echo("🛤️ Track ${it.track} updated")
    }.onFailure {
        echo(err = true, message = "🚨 Failed to update track!")
        echo(err = true, message = it)
    }

    private fun track(uploadedVersionCode: Long) = TrackRelease()
        .setName(releaseName)
        .setVersionCodes(uploadedVersionCode.let(::listOf))
        .setStatus(status)
        .let { Track().setReleases(listOf(it)) }
}
