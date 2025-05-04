package fr.smarquis.playground.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import java.util.EnumSet


internal fun PsiClass.isString(): Boolean = qualifiedName == "java.lang.String"
internal fun PsiClass.isObjectOrAny(): Boolean = qualifiedName == "java.lang.Object"
internal val PsiMethod.returnsUnit get() = returnType?.isVoidOrUnit
internal val PsiType.isVoidOrUnit get() = this == PsiTypes.voidType() || canonicalText == "kotlin.Unit"
internal fun PsiMethod.isInPackage(packageName: String) = (containingFile as? PsiJavaFile)?.packageName == packageName
internal fun PsiMethod.isKotlinLambda() =    """^kotlin\.(jvm\.functions\.)?Function""".toRegex().containsMatchIn(containingClass?.qualifiedName.orEmpty())

internal inline fun <reified T : Detector> implementation(
    scope: EnumSet<Scope>,
    vararg scopes: EnumSet<Scope>,
): Implementation = Implementation(T::class.java, scope, *scopes)

internal inline fun <reified T : Any> Any?.cast(): T = this as T
internal inline fun <reified T : Any> Any?.safeCast(): T? = this as? T
