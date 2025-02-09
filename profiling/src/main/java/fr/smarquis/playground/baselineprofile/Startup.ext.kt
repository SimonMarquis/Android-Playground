package fr.smarquis.playground.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction.DOWN
import androidx.test.uiautomator.Direction.UP

internal fun targetAppId(): String = InstrumentationRegistry.getArguments().getString("targetAppId")
    ?: error("targetAppId not passed as instrumentation runner arg")

internal fun MacrobenchmarkScope.throwDice() {
    val dice = device.findObject(By.res("dice"))
    repeat(6) { dice.click() }
    dice.longClick()
}

internal fun MacrobenchmarkScope.toggleSettings() {
    device.findObject(By.res("strict-mode")).click()
    device.findObject(By.res("uncaught-exception-handler")).click()
}

internal fun MacrobenchmarkScope.scrollHomeList() {
    val list = device.findObject(By.res("home::list"))
    list.fling(DOWN)
    list.fling(UP)
}
