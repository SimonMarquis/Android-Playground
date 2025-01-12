package fr.smarquis.playground.core.android.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.core.android.versionCode
import fr.smarquis.playground.core.android.versionName
import fr.smarquis.playground.core.di.qualifier.AppPackageName
import fr.smarquis.playground.core.di.qualifier.AppVersionCode
import fr.smarquis.playground.core.di.qualifier.AppVersionName

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {
    @Provides
    @AppPackageName
    fun providesAppPackageName(app: Application): String = app.packageName

    @Provides
    @AppVersionCode
    fun providesAppVersionCode(app: Application): Long = app.versionCode() ?: 0

    @Provides
    @AppVersionName
    fun providesAppVersionName(app: Application): String = app.versionName().orEmpty()
}
