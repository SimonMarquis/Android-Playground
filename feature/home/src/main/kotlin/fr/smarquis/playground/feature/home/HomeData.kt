package fr.smarquis.playground.feature.home

import fr.smarquis.playground.core.di.DisplayMetrics
import fr.smarquis.playground.core.di.qualifier.AppPackageName
import fr.smarquis.playground.core.di.qualifier.AppVersionCode
import fr.smarquis.playground.core.di.qualifier.AppVersionName
import fr.smarquis.playground.core.di.qualifier.CurrentTime
import fr.smarquis.playground.core.di.qualifier.DeviceManufacturer
import fr.smarquis.playground.core.di.qualifier.DeviceModel
import fr.smarquis.playground.core.di.qualifier.DeviceProduct
import fr.smarquis.playground.core.di.qualifier.DeviceRelease
import fr.smarquis.playground.core.di.qualifier.DeviceSdkInt
import fr.smarquis.playground.core.di.qualifier.FirstInstallTime
import fr.smarquis.playground.core.di.qualifier.LastUpdateTime
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

internal data class HomeData @Inject constructor(
    @param:AppPackageName val packageName: String,
    @param:AppVersionCode val versionCode: Long,
    @param:AppVersionName val versionName: String,

    @param:DeviceManufacturer val deviceManufacturer: String,
    @param:DeviceModel val deviceModel: String,
    @param:DeviceProduct val deviceProduct: String,
    @param:DeviceSdkInt val deviceSdkInt: Int,
    @param:DeviceRelease val deviceRelease: String,

    @param:FirstInstallTime val firstInstallTime: LocalDateTime,
    @param:LastUpdateTime val lastUpdateTime: LocalDateTime,
    @param:CurrentTime val currentTime: LocalDateTime,

    val displayMetrics: DisplayMetrics,
)
