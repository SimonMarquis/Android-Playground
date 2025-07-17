package fr.smarquis.playground.core.android

import fr.smarquis.playground.core.di.AssetManager
import javax.inject.Inject
import android.content.res.AssetManager as RealAssetManager

internal class AndroidAssetManager @Inject constructor(private val manager: RealAssetManager) : AssetManager {
    override fun open(name: String) = manager.open(name)
}
