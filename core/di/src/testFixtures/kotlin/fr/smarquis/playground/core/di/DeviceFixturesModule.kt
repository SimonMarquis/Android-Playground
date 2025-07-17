package fr.smarquis.playground.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.core.di.qualifier.DeviceManufacturer
import fr.smarquis.playground.core.di.qualifier.DeviceModel
import fr.smarquis.playground.core.di.qualifier.DeviceProduct
import fr.smarquis.playground.core.di.qualifier.DeviceSdkInt

@Module
@InstallIn(SingletonComponent::class)
public object DeviceFixturesModule {
    @Provides @DeviceManufacturer fun providesDeviceManufacturer(): String = "Test"
    @Provides @DeviceModel fun providesDeviceModel(): String = "Test"
    @Provides @DeviceProduct fun providesDeviceProduct(): String = "Test"
    @Provides @DeviceSdkInt fun providesDeviceSdkInt(): Int = 0
}
