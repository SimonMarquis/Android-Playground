import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.variant.BuiltArtifacts

plugins {
    alias(libs.plugins.playground.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

@Suppress("UnstableApiUsage")
android {
    targetProjectPath = projects.app.path
    testOptions.managedDevices.localDevices {
        create("gmd") {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "aosp-atd"
        }
    }
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    managedDevices += "gmd"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.annotations)
    implementation(libs.androidx.benchmark)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.test.monitor)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.uiautomator)
}

@Suppress("UnstableApiUsage")
androidComponents.onVariants {
    val loader: (Directory) -> BuiltArtifacts? = it.artifacts.getBuiltArtifactsLoader()::load
    it.instrumentationRunnerArguments.put("targetAppId", it.testedApks.map { loader(it)?.applicationId.orEmpty() })
}
