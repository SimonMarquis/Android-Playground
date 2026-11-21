package fr.smarquis.playground.buildlogic

import com.android.build.gradle.TestPlugin
import fr.smarquis.playground.buildlogic.dsl.apply
import org.gradle.api.Project

internal fun Project.configurePlaygroundAndroidTest() = with(this) {
        apply<TestPlugin>()
        configurePlaygroundAndroidBase()
    }

