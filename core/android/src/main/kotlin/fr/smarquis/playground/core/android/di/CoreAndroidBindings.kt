package fr.smarquis.playground.core.android.di

import android.app.Application
import android.content.pm.PackageInfo
import android.content.res.AssetManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.android.packageInfo
import fr.smarquis.playground.core.di.DisplayMetrics
import android.util.DisplayMetrics as AndroidDisplayMetrics

@ContributesTo(AppScope::class)
@BindingContainer
public object CoreAndroidBindings {

    @Provides
    internal fun providesAssetManager(app: Application): AssetManager = app.assets

    @Provides
    internal fun providesPackageInfo(app: Application): PackageInfo = app.packageInfo()

    @Provides
    internal fun providesDisplayMetrics(app: Application): DisplayMetrics = app.resources.displayMetrics.toDisplayMetrics()

    private fun AndroidDisplayMetrics.toDisplayMetrics() = DisplayMetrics(
        widthPixels = widthPixels,
        heightPixels = heightPixels,
        densityDpi = densityDpi,
    )

}
