package fr.smarquis.playground.core.android

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import fr.smarquis.playground.core.di.AssetManager
import java.io.InputStream
import android.content.res.AssetManager as RealAssetManager

@ContributesBinding(AppScope::class)
public class AndroidAssetManager(private val manager: RealAssetManager) : AssetManager {
    override fun open(name: String): InputStream = manager.open(name)
}
