package fr.smarquis.playground.core.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.di.qualifier.DeviceManufacturer
import fr.smarquis.playground.core.di.qualifier.DeviceModel
import fr.smarquis.playground.core.di.qualifier.DeviceProduct
import fr.smarquis.playground.core.di.qualifier.DeviceSdkInt

@ContributesTo(AppScope::class)
@BindingContainer
public object DeviceFixturesBindings {
    @Provides @DeviceManufacturer fun providesDeviceManufacturer(): String = "Test"
    @Provides @DeviceModel fun providesDeviceModel(): String = "Test"
    @Provides @DeviceProduct fun providesDeviceProduct(): String = "Test"
    @Provides @DeviceSdkInt fun providesDeviceSdkInt(): Long = 0
}
