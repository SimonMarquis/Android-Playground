package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Category.Companion.PRODUCTIVITY
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.Severity.INFORMATIONAL
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat.TEXT
import com.android.tools.lint.detector.api.isKotlin
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.parameterIndex
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
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

            context.checkMissingNamedParameters(node, method)
            context.checkMismatchNameParameters(node, method)
        }
    }

    private fun JavaContext.checkMissingNamedParameters(node: UCallExpression, method: PsiMethod) {
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

        report(
            issue = MISSING_NAMED_PARAMETERS,
            scope = node,
            location = getCallLocation(call = node, includeReceiver = false, includeArguments = true),
            message = MISSING_NAMED_PARAMETERS.getBriefDescription(TEXT),
        )
    }

    private fun JavaContext.checkMismatchNameParameters(node: UCallExpression, method: PsiMethod) {
        val arguments = evaluator.computeArgumentMapping(node, method)
        val names = arguments.values.associateBy { it.name }
        arguments.forEach { (expression, value) ->
            expression.safeCast<USimpleNameReferenceExpression>()?.identifier
                ?.let(names::get) // Search matching parameter
                ?.takeUnless { it.parameterIndex() == value.parameterIndex() }
                ?: return@forEach
            report(
                issue = MISMATCHED_NAMED_PARAMETERS,
                scope = node,
                location = getLocation(expression),
                message = "This variable has the same name as another parameter!",
            )
        }
    }

    private fun KtValueArgument.type() = getArgumentExpression()
        .toUElementOfType<UExpression>()
        ?.getExpressionType()
        ?.canonicalText

    public companion object {
        @Suppress("LintImplTextFormat")
        public val MISSING_NAMED_PARAMETERS: Issue = Issue.create(
            id = "MissingNamedParameters",
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

        /**
         * Similar to Kotlin's [`DestructuringWrongName`](https://www.jetbrains.com/help/inspectopedia/DestructuringWrongName.html), but for method calls.
         */
        public val MISMATCHED_NAMED_PARAMETERS: Issue = Issue.create(
            id = "MismatchedNamedParameters",
            briefDescription = "Mismatch in variable name and parameter name",
            explanation = "Using a variable for a function call parameter while another parameter with the exact same name exists is probably a bug.",
            category = CORRECTNESS,
            priority = 5,
            severity = ERROR,
            implementation = implementation<NamedParametersDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )
    }

}
