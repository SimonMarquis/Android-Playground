package fr.smarquis.playground.data.licenses

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.domain.licenses.LicensesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface LicensesRepositoryModule {
    @Binds
    @Singleton
    fun bindsLicensesRepository(it: LicensesRepositoryImpl): LicensesRepository
}
