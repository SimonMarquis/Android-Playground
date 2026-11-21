import androidx.baselineprofile.gradle.producer.BaselineProfileProducerExtension
import com.android.build.api.variant.BuiltArtifacts

plugins {
    alias(libs.plugins.playground.build)
    alias(libs.plugins.androidx.baselineprofile)
}

@Suppress("UnstableApiUsage")
playground {
    androidBenchmark(targetProjectPath = ":app") {
        android {
            testOptions.managedDevices.localDevices {
                create("gmd") {
                    device = "Pixel 6"
                    apiLevel = 35
                    systemImageSource = "aosp-atd"
                    testedAbi = "arm64-v8a"
                }
            }
            experimentalProperties["android.experimental.self-instrumenting"] = true
        }

        dependencies {
            implementation(libs.androidx.annotations)
            implementation(libs.androidx.benchmark)
            implementation(libs.androidx.junit)
            implementation(libs.androidx.test.monitor)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.uiautomator)
        }


        androidComponents {
            onVariants {
                val loader: (Directory) -> BuiltArtifacts? = it.artifacts.getBuiltArtifactsLoader()::load
                it.instrumentationRunnerArguments.put("targetAppId", it.testedApks.map { loader(it)?.applicationId.orEmpty() })
            }
        }
    }
}

configure<BaselineProfileProducerExtension> {
    managedDevices += "gmd"
    useConnectedDevices = false
}
