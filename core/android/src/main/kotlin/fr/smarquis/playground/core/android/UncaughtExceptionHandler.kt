package fr.smarquis.playground.core.android

import android.util.Log

public object UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {

    private var fallback: Thread.UncaughtExceptionHandler? = null

    public fun isInstalled(): Boolean = Thread.getDefaultUncaughtExceptionHandler() == this

    public fun install() {
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        if (handler == this) return
        fallback = handler
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    public fun uninstall() {
        if (Thread.getDefaultUncaughtExceptionHandler() != this) return
        Thread.setDefaultUncaughtExceptionHandler(fallback)
        fallback = null
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.wtf("UncaughtExceptionHandler", "Uncaught Exception on $t", e)
        fallback?.uncaughtException(t, e)
    }

}
