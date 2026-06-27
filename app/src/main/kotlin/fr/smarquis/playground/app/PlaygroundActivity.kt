package fr.smarquis.playground.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import fr.smarquis.playground.core.utils.navigation.EntryInstaller
import kotlinx.collections.immutable.toImmutableSet

public class PlaygroundActivity : ComponentActivity() {

    @Inject
    public lateinit var viewModelFactory: MetroViewModelFactory

    @Inject
    public lateinit var entryProviders: Set<@JvmSuppressWildcards EntryInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        @SuppressLint("UnsafeCast")
        (application as PlaygroundApplication).graph.inject(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                PlaygroundApp(entryProviders.toImmutableSet())
            }
        }
    }

}
