package fr.smarquis.playground.core.android

import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy

public object StrictMode {

    private val defaultThreadPolicy: ThreadPolicy by lazy { ThreadPolicy.Builder().permitAll().build() }
    private val threadPolicy: ThreadPolicy by lazy {
        ThreadPolicy.Builder().detectAll().penaltyFlashScreen().penaltyLog().build()
    }

    private val defaultVmPolicy: VmPolicy by lazy { VmPolicy.Builder().build() }
    private val vmPolicy: VmPolicy by lazy {
        VmPolicy.Builder().detectAll().penaltyLog().build()
    }

    public var isInstalled: Boolean = false
        private set

    public fun install() {
        android.os.StrictMode.setThreadPolicy(threadPolicy)
        android.os.StrictMode.setVmPolicy(vmPolicy)
        isInstalled = true
    }

    public fun uninstall() {
        android.os.StrictMode.setThreadPolicy(defaultThreadPolicy)
        android.os.StrictMode.setVmPolicy(defaultVmPolicy)
        isInstalled = false
    }

}
