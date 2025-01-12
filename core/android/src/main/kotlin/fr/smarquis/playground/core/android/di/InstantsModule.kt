package fr.smarquis.playground.core.android.di

import android.content.pm.PackageInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.core.di.qualifier.FirstInstallTime
import fr.smarquis.playground.core.di.qualifier.LastUpdateTime
import fr.smarquis.playground.core.utils.toLocalDateTime
import kotlinx.datetime.LocalDateTime

@Module
@InstallIn(SingletonComponent::class)
internal object InstantsModule {
    @Provides
    @FirstInstallTime
    fun providesFirstInstallTime(packageInfo: PackageInfo): Long = packageInfo.firstInstallTime

    @Provides
    @FirstInstallTime
    fun providesFirstInstallLocalDateTime(@FirstInstallTime it: Long): LocalDateTime = it.toLocalDateTime()

    @Provides
    @LastUpdateTime
    fun providesLastUpdateTime(packageInfo: PackageInfo): Long = packageInfo.lastUpdateTime

    @Provides
    @LastUpdateTime
    fun providesLastUpdateLocalDateTime(@LastUpdateTime it: Long): LocalDateTime = it.toLocalDateTime()
}
