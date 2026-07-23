package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.Severity.INFORMATIONAL
import com.android.tools.lint.detector.api.Severity.WARNING
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KaDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KaTypeRendererForSource.WITH_SHORT_NAMES
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbolModality
import org.jetbrains.kotlin.analysis.api.types.KaCapturedType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
import org.jetbrains.kotlin.types.Variance.INVARIANT
import org.jetbrains.uast.UBinaryExpressionWithType
import org.jetbrains.uast.UElement
import java.util.EnumSet

public class CastValidityDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UBinaryExpressionWithType::class.java)

    @OptIn(KaExperimentalApi::class)
    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitBinaryExpressionWithType(node: UBinaryExpressionWithType) {
            val kt = node.sourcePsi as? KtBinaryExpressionWithTypeRHS ?: return
            if (!KtPsiUtil.isCast(kt)) return

            analyze(kt) {
                @OptIn(KaExperimentalApi::class)
                fun KaType.shortName() = render(WITH_SHORT_NAMES, INVARIANT)
                @OptIn(KaExperimentalApi::class)
                fun Pair<KaType, KaType>.shortName() = "from [${first.shortName()}] to [${second.shortName()}]"

                fun debug(node: UElement, message: String) = Incident(context)
                    .issue(DEBUG)
                    .scope(node)
                    .location(context.getLocation(node))
                    .message(message)
                    .report()

                fun unsafe(node: UElement, fromType: KaType, toType: KaType, message: String) = Incident(context)
                    .issue(UNSAFE_CAST)
                    .scope(node)
                    .location(context.getLocation(node))
                    .message("Unsafe cast from `${fromType.shortName()}` to `${toType.shortName()}` ($message)")
                    .report()

                val fromType = kt.left.expressionType ?: return@analyze
                val toType = kt.right?.type ?: return@analyze
                val fromTypeUnwrapped = fromType.unwrapCaptured()
                val toTypeUnwrapped = toType.unwrapCaptured()
                val isSafeCast = KtPsiUtil.isSafeCast(kt)


                if (fromType.semanticallyEquals(toType)) return debug(node, "semanticallyEquals [${fromType.shortName()}]")
                // if (toType.hasCommonSubtypeWith(fromType)) return debug(node, "hasCommonSubtypeWith")

                fromType.withNullability(false)


                val fromSymbol = fromTypeUnwrapped.symbol
                val toSymbol = toTypeUnwrapped.symbol
                val fromTo = fromType to toType
                val fromToUnwrapped = fromTypeUnwrapped to toTypeUnwrapped

                if (fromType is KaTypeParameterType) return debug(node, "[${fromType.shortName()}] is KaTypeParameterType")


                if (fromType.isSubtypeOf(toType)) return debug(node, "[${fromType.shortName()}] isSubtypeOf [${toType.shortName()}]")
                if (fromTypeUnwrapped.isSubtypeOf(toTypeUnwrapped)) return debug(node, "[${fromType.shortName()}] isSubtypeOf(unwrapped) [${toType.shortName()}]")

                val fromTypeIsExtensible = fromType.symbol?.modality?.let { it == KaSymbolModality.OPEN || it == KaSymbolModality.ABSTRACT } == true
                if (toType.expandedSymbol?.classKind == KaClassKind.INTERFACE && fromTypeIsExtensible) {
                    return if (isSafeCast) debug(node, "[${fromType.shortName()}] is open/abstract and [${toType.shortName()}] is interface (safe cast [${kt.operationReference.text}])")
                    else unsafe(node, fromType = fromType, toType = toType, "[${fromType.shortName()}] is open/abstract and [${toType.shortName()}] is interface")
                }






                val toTypeParameterSymbol = (toTypeUnwrapped as? KaTypeParameterType)?.symbol

                if (toType is KaTypeParameterType) {
                    System.err.println("toType isCompatibleWithTypeParameter? = " + toType.symbol.upperBounds.any { fromType.isSubtypeOf(it) })
                }
                if (toTypeUnwrapped is KaTypeParameterType) {
                    System.err.println("toTypeUnwrapped isCompatibleWithTypeParameter? = " + toTypeUnwrapped.symbol.upperBounds.any { fromTypeUnwrapped.isSubtypeOf(it) })
                    return@analyze
                }

                System.err.println("toType.enhancedType = " + toTypeUnwrapped.enhancedType)
                System.err.println("toType.enhancedTypeOrSelf = " + toTypeUnwrapped.enhancedTypeOrSelf)
                System.err.println("toType.fullyExpandedType = " + toTypeUnwrapped.fullyExpandedType)
                System.err.println("toType.allSupertypes = " + toTypeUnwrapped.allSupertypes.toList())
                System.err.println("toType.allSupertypes(true) = " + toTypeUnwrapped.allSupertypes(true).toList())
                System.err.println("toType.directSupertypes = " + toTypeUnwrapped.directSupertypes.toList())
                System.err.println("toType.directSupertypes(true) = " + toTypeUnwrapped.directSupertypes(true).toList())

                // val toIsTypeParameter = toSymbol is KaTypeParameterSymbol
                // val fromIsSubtypeOfUpperBound = toRawSymbol?.upperBounds?.any { fromType.isSubtypeOf(it) } == true

                System.err.println("FROM (raw)= ${fromType.shortName()}")
                System.err.println("FROM      = ${fromTypeUnwrapped.shortName()}")
                System.err.println("TO   (raw)= ${toType.shortName()}")
                System.err.println("TO        = ${toTypeUnwrapped.shortName()}")
                System.err.println("TO        = ${toTypeParameterSymbol?.upperBounds}")

                System.err.println("FROM SYMBOL = ${fromTypeUnwrapped.expandedSymbol}")
                System.err.println("FROM NULLABLE = ${fromTypeUnwrapped.nullability}")
                System.err.println("fromType::class = ${fromTypeUnwrapped::class}")
                System.err.println("fromType.javaClass.name = ${fromTypeUnwrapped.javaClass.name}")

                System.err.println("TO SYMBOL = ${toTypeUnwrapped.expandedSymbol}")
                System.err.println("TO NULLABLE = ${toTypeUnwrapped.nullability}")
                System.err.println("toType::class = ${toTypeUnwrapped::class}")
                System.err.println("toType.javaClass.name = ${toTypeUnwrapped.javaClass.name}")

                System.err.println("toType :::"+ (toTypeUnwrapped as? KaTypeParameterType)?.name)
                System.err.println("toType :::"+ (toTypeUnwrapped as? KaTypeParameterType)?.symbol?.upperBounds)

                // System.err.println("FROM CLASSIFIER = ${fromType.classifier}")
                System.err.println("node.operand.getExpressionType = " + node.operand.getExpressionType())

                System.err.println("kt.left = " + kt.left::class)
                System.err.println("kt.left.text = " + kt.left.text)

                System.err.println(fromType)

                kt.left.forEachDescendantOfType<KtNameReferenceExpression> {
                    System.err.println("${it.text} -> ${it.expressionType}")
                }
                val diagnostics = kt.left.diagnostics(KaDiagnosticCheckerFilter.EXTENDED_AND_COMMON_CHECKERS)
                diagnostics.forEach { System.err.println(it) }

                if (KtPsiUtil.isSafeCast(kt)) {
                    // A? as?
                    // if (fromType.isNullable && fromType.withNullability(false).isSubtypeOf(toType))
                }



                val isImpossible = !toTypeUnwrapped.isSubtypeOf(fromTypeUnwrapped)
                if (!isImpossible && KtPsiUtil.isSafeCast(kt)) return
                val (issue, prefix) = if (isImpossible) IMPOSSIBLE_CAST to "Impossible" else UNSAFE_CAST to "Unsafe"

                Incident(context)
                    .issue(issue)
                    .scope(node)
                    .location(context.getLocation(node))
                    .message("$prefix cast from `${fromTypeUnwrapped.shortName()}` to `${toTypeUnwrapped.shortName()}`")
                    .report()
            }
        }

        fun KaType.unwrapCaptured(): KaType =
            when (this) {
                is KaCapturedType -> projection.type!!
                else -> this
            }
    }

    public companion object {
        public val IMPOSSIBLE_CAST: Issue = Issue.create(
            id = "ImpossibleCast",
            briefDescription = "Cast between unrelated types can never succeed",
            explanation = """
                Reports Kotlin cast expressions (`as`/`as?`) where the source and target types have no possible runtime compatibility. \
                These casts are always invalid according to the Kotlin type system (e.g. `A as B` where `A` and `B` are unrelated final classes).
            """.trimIndent(),
            category = CORRECTNESS,
            priority = 5,
            severity = ERROR,
            implementation = implementation<CastValidityDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )
        public val UNSAFE_CAST: Issue = Issue.create(
            id = "UnsafeCast",
            briefDescription = "Cast that may fail at runtime",
            explanation = """
                Reports Kotlin unsafe cast expressions (`as`) that are type-compatible in general but not guaranteed to succeed at runtime. \
                These include casts from broader types (e.g. `Any`) to more specific types (e.g. `String`).
            """.trimIndent(),
            category = CORRECTNESS,
            priority = 5,
            severity = WARNING,
            implementation = implementation<CastValidityDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )
        public val DEBUG: Issue = Issue.create(
            id = "DEBUG",
            briefDescription = "DEBUG",
            explanation = "DEBUG",
            category = CORRECTNESS,
            priority = 5,
            severity = INFORMATIONAL,
            implementation = implementation<CastValidityDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )
    }
}
