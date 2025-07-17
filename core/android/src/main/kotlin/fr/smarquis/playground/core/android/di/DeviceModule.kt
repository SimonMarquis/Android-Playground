package fr.smarquis.playground.core.android.di

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.smarquis.playground.core.di.qualifier.DeviceManufacturer
import fr.smarquis.playground.core.di.qualifier.DeviceModel
import fr.smarquis.playground.core.di.qualifier.DeviceProduct
import fr.smarquis.playground.core.di.qualifier.DeviceRelease
import fr.smarquis.playground.core.di.qualifier.DeviceSdkInt

@Module
@InstallIn(SingletonComponent::class)
internal object DeviceModule {
    @Provides
    @DeviceManufacturer
    fun providesDeviceManufacturer(): String = Build.MANUFACTURER

    @Provides
    @DeviceModel
    fun providesDeviceModel(): String = Build.MODEL

    @Provides
    @DeviceProduct
    fun providesDeviceProduct(): String = Build.PRODUCT

    @Provides
    @DeviceSdkInt
    fun providesDeviceSdkInt(): Int = Build.VERSION.SDK_INT

    @Provides
    @DeviceRelease
    fun providesDeviceRelease(): String = Build.VERSION.RELEASE
}
