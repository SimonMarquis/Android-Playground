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
import com.android.tools.lint.detector.api.Severity.WARNING
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.renderer.types.impl.KaTypeRendererForSource.WITH_SHORT_NAMES
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.types.Variance.INVARIANT
import org.jetbrains.uast.UBinaryExpressionWithType
import org.jetbrains.uast.UElement
import java.util.EnumSet

public class CastValidityDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UBinaryExpressionWithType::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitBinaryExpressionWithType(node: UBinaryExpressionWithType) {
            val kt = node.sourcePsi as? KtBinaryExpressionWithTypeRHS ?: return
            if (!KtPsiUtil.isCast(kt)) return

            analyze(kt) {
                val fromType = kt.left.expressionType ?: return@analyze
                val toType = kt.right?.type ?: return@analyze

                if (fromType.isSubtypeOf(toType)) return
                val isImpossible = !toType.isSubtypeOf(fromType)
                if (!isImpossible && KtPsiUtil.isSafeCast(kt)) return

                @OptIn(KaExperimentalApi::class)
                fun KaType.shortName() = render(WITH_SHORT_NAMES, INVARIANT)
                val (issue, prefix) = if (isImpossible) IMPOSSIBLE_CAST to "Impossible" else UNSAFE_CAST to "Unsafe"

                Incident(context)
                    .issue(issue)
                    .scope(node)
                    .location(context.getLocation(node))
                    .message("$prefix cast from `${fromType.shortName()}` to `${toType.shortName()}`")
                    .report()
            }
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
    }
}
