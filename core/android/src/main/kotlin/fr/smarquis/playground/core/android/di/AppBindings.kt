package fr.smarquis.playground.core.android.di

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.android.versionCode
import fr.smarquis.playground.core.android.versionName
import fr.smarquis.playground.core.di.qualifier.AppPackageName
import fr.smarquis.playground.core.di.qualifier.AppVersionCode
import fr.smarquis.playground.core.di.qualifier.AppVersionName

@ContributesTo(AppScope::class)
@BindingContainer
public object AppBindings {
    @Provides
    @AppPackageName
    internal fun providesPackageName(app: Application): String = app.packageName

    @Provides
    @AppVersionCode
    internal fun providesVersionCode(app: Application): Long = app.versionCode() ?: 0

    @Provides
    @AppVersionName
    internal fun providesVersionName(app: Application): String = app.versionName().orEmpty()
}
