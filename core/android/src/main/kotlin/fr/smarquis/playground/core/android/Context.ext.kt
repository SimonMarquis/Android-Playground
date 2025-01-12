package fr.smarquis.playground.core.android

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import androidx.core.content.pm.PackageInfoCompat.getLongVersionCode

public fun Context.versionCode(): Long? = try {
    packageManager.getPackageInfo(packageName, 0).let(::getLongVersionCode)
} catch (ignore: NameNotFoundException) {
    null
}

public fun Context.versionName(): String? = try {
    packageManager.getPackageInfo(packageName, 0).versionName
} catch (ignore: NameNotFoundException) {
    null
}

public fun Context.packageInfo(): PackageInfo = packageManager.getPackageInfo(packageName, 0)
