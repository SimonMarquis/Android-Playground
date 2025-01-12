package fr.smarquis.playground.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CurrentTimeProviderFixturesModule {
    @Provides
    fun provides(): CurrentTimeProvider = CurrentTimeProvider.SYSTEM
}
