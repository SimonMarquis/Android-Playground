package fr.smarquis.playground.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import fr.smarquis.playground.core.utils.navigation.EntryInstaller
import kotlinx.collections.immutable.toImmutableSet
import javax.inject.Inject


@AndroidEntryPoint
public class PlaygroundActivity : ComponentActivity() {

    @Inject
    public lateinit var app: PlaygroundApplication

    @Inject
    public lateinit var entryProviders: Set<@JvmSuppressWildcards EntryInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PlaygroundApp(entryProviders.toImmutableSet()) }
    }

}
