package fr.smarquis.playground.app

import android.app.Application
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import fr.smarquis.playground.app.di.AppGraph
import fr.smarquis.playground.core.android.ProfileVerifierLogger
import fr.smarquis.playground.core.android.StrictMode
import fr.smarquis.playground.core.android.UncaughtExceptionHandler
import fr.smarquis.playground.domain.settings.SettingsSource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


public class PlaygroundApplication : Application() {

    public val graph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    @Inject
    public lateinit var settingsSource: SettingsSource

    @Inject
    public lateinit var profileVerifierLogger: ProfileVerifierLogger

    override fun onCreate() {
        super.onCreate()
        graph.inject(this)
        registerSettings()
        profileVerifierLogger()
    }

    public fun isDebuggable(): Boolean = 0 != applicationInfo.flags and FLAG_DEBUGGABLE

    // NOTE: this could be moved to an app Initializer
    private fun registerSettings() = settingsSource
        .settings
        .onEach {
            if (it.strictMode) StrictMode.install() else StrictMode.uninstall()
            if (it.uncaughtExceptionHandler) UncaughtExceptionHandler.install() else UncaughtExceptionHandler.uninstall()
        }
        .launchIn(ProcessLifecycleOwner.get().lifecycleScope)

}
