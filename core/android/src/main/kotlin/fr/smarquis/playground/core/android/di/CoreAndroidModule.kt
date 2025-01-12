package fr.smarquis.playground.core.android.di

import android.app.Application
import android.content.pm.PackageInfo
import android.util.DisplayMetrics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.core.android.AndroidAssetManager
import fr.smarquis.playground.core.android.AndroidFileManager
import fr.smarquis.playground.core.android.packageInfo
import fr.smarquis.playground.core.di.AssetManager
import fr.smarquis.playground.core.di.FileManager
import android.content.res.AssetManager as RealAssetManager

@Module
@InstallIn(SingletonComponent::class)
internal interface CoreAndroidModule {

    @Binds
    fun bindsFileManager(it: AndroidFileManager): FileManager

    @Binds
    fun bindsAssetManager(it: AndroidAssetManager): AssetManager

    companion object {
        @Provides
        fun providesRealAssetManager(app: Application): RealAssetManager = app.assets

        @Provides
        fun providesPackageInfo(app: Application): PackageInfo = app.packageInfo()

        @Provides
        fun providesDisplayMetrics(app: Application): DisplayMetrics = app.resources.displayMetrics
    }

}
