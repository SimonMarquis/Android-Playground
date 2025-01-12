package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category.Companion.PRODUCTIVITY
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity.INFORMATIONAL
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat.TEXT
import com.android.tools.lint.detector.api.isKotlin
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.toUElementOfType
import java.util.EnumSet

public class NamedParametersDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<UCallExpression>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            if (!isKotlin(node.lang)) return // Not calling from Kotlin
            if (node.valueArgumentCount <= 1) return // Not enough arguments
            val method = node.resolve() ?: return
            if (!isKotlin(method.language)) return // Not calling Kotlin method
            if (method.name in setOf("put", "set") && node.valueArgumentCount == 2) return // put/set with key/value
            if (method.name.contains("assert(?:Not)?(?:Same|Equals)".toRegex())) return // Ignore assertions
            if (method.parameterList.parameters.last().isVarArgs) return // Ignore vararg (even when other args exists)
            if (method.isKotlinLambda()) return // Named arguments are not allowed for function types

            node.sourcePsi!!
                // Arguments
                .getChildOfType<KtValueArgumentList>()?.getChildrenOfType<KtValueArgument>().orEmpty()
                // Group arguments by type
                .groupBy { it.type() }
                // Search for un-named arguments
                .any { (_, args) -> args.size > 1 && args.all(KtValueArgument::isNamed).not() }
                // Return if no match found
                .ifFalse { return }

            context.report(
                issue = ISSUE,
                scope = node,
                location = context.getCallLocation(call = node, includeReceiver = false, includeArguments = true),
                message = ISSUE.getBriefDescription(TEXT),
            )
        }
    }

    private fun KtValueArgument.type() = getArgumentExpression()
        .toUElementOfType<UExpression>()
        ?.getExpressionType()
        ?.canonicalText

    public companion object {
        @Suppress("LintImplTextFormat")
        public val ISSUE: Issue = Issue.create(
            id = "NamedParameters",
            briefDescription = "Parameters of the same type should be named",
            explanation = """
                          Not specifying parameters name using the same type can lead to unexpected results when refactoring methods signature.
                          Enforcing explicit named parameters also helps detecting mistakes during code review.
                          Quick fix: `⌥⏎` (macOS) or `Alt+Enter` (Windows/Linux) ➝ `Add names to call arguments`.
                          """.trimIndent(),
            category = PRODUCTIVITY,
            priority = 4,
            severity = INFORMATIONAL,
            implementation = implementation<NamedParametersDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )
    }

}
