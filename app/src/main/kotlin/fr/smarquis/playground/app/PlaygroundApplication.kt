package fr.smarquis.playground.app

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.smarquis.playground.core.android.ProfileVerifierLogger
import fr.smarquis.playground.core.android.StrictMode
import fr.smarquis.playground.core.android.UncaughtExceptionHandler
import fr.smarquis.playground.domain.settings.SettingsSource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltAndroidApp
public class PlaygroundApplication : Application() {

    // FIXME: https://github.com/google/dagger/issues/3601
    @Inject
    @ApplicationContext
    public lateinit var context: Context

    @Inject
    public lateinit var settingsSource: SettingsSource

    @Inject
    public lateinit var profileVerifierLogger: ProfileVerifierLogger

    override fun onCreate() {
        super.onCreate()
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
