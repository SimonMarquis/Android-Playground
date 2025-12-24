package fr.smarquis.playground.core.di.qualifier

import javax.inject.Qualifier
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@Target(FUNCTION, VALUE_PARAMETER)
@Qualifier
public annotation class CurrentTime
