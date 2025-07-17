package fr.smarquis.playground.baselineprofile

import android.os.Build.VERSION_CODES.P
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@RequiresApi(P)
internal class StartupBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = targetAppId(),
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        throwDice()
        toggleSettings()
        scrollHomeList()
    }

}
