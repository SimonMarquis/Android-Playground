package fr.smarquis.playground.buildlogic.utils

import com.autonomousapps.DependencyAnalysisExtension
import com.autonomousapps.DependencyAnalysisPlugin
import com.autonomousapps.DependencyAnalysisSubExtension
import fr.smarquis.playground.buildlogic.dsl.apply
import fr.smarquis.playground.buildlogic.dsl.configure
import org.gradle.api.Project

internal object PlaygroundDependencyAnalysis {

    fun configureRootProject(project: Project) = with(project) {
        apply<DependencyAnalysisPlugin>()
        extensions.configure<DependencyAnalysisExtension> {
            useTypesafeProjectAccessors(true)
            usage {
                analysis {
                    checkSuperClasses(true)
                }
            }
            structure {
                ignoreKtx(true)
            }
        }
    }

    fun configureProject(project: Project) = with(project) {
        apply<DependencyAnalysisPlugin>()
        extensions.configure<DependencyAnalysisSubExtension> {
            issues {
                onAny {
                    exclude(
                        "com.willowtreeapps.assertk:assertk",
                        "junit:junit",
                        "org.jetbrains.kotlin:kotlin-test",
                    )
                }
            }
        }
    }
}
