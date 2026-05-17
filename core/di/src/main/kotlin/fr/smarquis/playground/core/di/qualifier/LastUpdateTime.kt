package fr.smarquis.playground.core.di.qualifier

import dev.zacsweers.metro.Qualifier
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@Target(FUNCTION, VALUE_PARAMETER)
@Qualifier
public annotation class LastUpdateTime
