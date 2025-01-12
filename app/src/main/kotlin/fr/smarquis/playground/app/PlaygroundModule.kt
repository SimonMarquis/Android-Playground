package fr.smarquis.playground.app

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object PlaygroundModule {

    @Provides
    fun providesPlaygroundApplication(app: Application): PlaygroundApplication = app as PlaygroundApplication

}
