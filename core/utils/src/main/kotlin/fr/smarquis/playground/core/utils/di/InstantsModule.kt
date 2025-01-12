package fr.smarquis.playground.core.utils.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.core.di.qualifier.CurrentTime
import fr.smarquis.playground.core.utils.toLocalDateTime
import kotlinx.datetime.LocalDateTime

@Module
@InstallIn(SingletonComponent::class)
internal object InstantsModule {

    @Provides
    @CurrentTime
    fun providesCurrentTime(): Long = System.currentTimeMillis()

    @Provides
    @CurrentTime
    fun providesCurrentLocalDateTime(@CurrentTime it: Long): LocalDateTime = it.toLocalDateTime()

}
