package fr.smarquis.playground.feature.home

import dev.zacsweers.metro.Inject
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
import kotlin.time.Instant

@Inject
internal data class HomeData(
    @AppPackageName val packageName: String,
    @AppVersionCode val versionCode: Long,
    @AppVersionName val versionName: String,

    @DeviceManufacturer val deviceManufacturer: String,
    @DeviceModel val deviceModel: String,
    @DeviceProduct val deviceProduct: String,
    @DeviceSdkInt val deviceSdkInt: Int,
    @DeviceRelease val deviceRelease: String,

    @FirstInstallTime val firstInstallTime: Instant,
    @LastUpdateTime val lastUpdateTime: Instant,
    @CurrentTime val currentTime: Instant,

    val displayMetrics: DisplayMetrics,
)
