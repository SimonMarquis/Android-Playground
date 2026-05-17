package fr.smarquis.playground.core.android.di

import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import fr.smarquis.playground.core.di.qualifier.DeviceManufacturer
import fr.smarquis.playground.core.di.qualifier.DeviceModel
import fr.smarquis.playground.core.di.qualifier.DeviceProduct
import fr.smarquis.playground.core.di.qualifier.DeviceRelease
import fr.smarquis.playground.core.di.qualifier.DeviceSdkInt

@ContributesTo(AppScope::class)
@BindingContainer
public object DeviceBindings {
    @Provides
    @DeviceManufacturer
    public fun providesDeviceManufacturer(): String = Build.MANUFACTURER

    @Provides
    @DeviceModel
    public fun providesDeviceModel(): String = Build.MODEL

    @Provides
    @DeviceProduct
    public fun providesDeviceProduct(): String = Build.PRODUCT

    @Provides
    @DeviceSdkInt
    public fun providesDeviceSdkInt(): Int = Build.VERSION.SDK_INT

    @Provides
    @DeviceRelease
    public fun providesDeviceRelease(): String = Build.VERSION.RELEASE
}
