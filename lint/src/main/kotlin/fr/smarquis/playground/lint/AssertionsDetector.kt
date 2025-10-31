package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.isJava
import com.intellij.psi.PsiMethod
import fr.smarquis.playground.lint.AssertionsDetector.EqualityAssertionReplacement.Binary
import fr.smarquis.playground.lint.AssertionsDetector.EqualityAssertionReplacement.UnaryNull
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UBinaryExpressionWithType
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.UastBinaryExpressionWithTypeKind.InstanceCheck
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.UastBinaryOperator.Companion.EQUALS
import org.jetbrains.uast.UastBinaryOperator.Companion.IDENTITY_EQUALS
import org.jetbrains.uast.UastBinaryOperator.Companion.IDENTITY_NOT_EQUALS
import org.jetbrains.uast.UastBinaryOperator.Companion.NOT_EQUALS
import org.jetbrains.uast.isNullLiteral
import org.jetbrains.uast.kotlin.KotlinBinaryExpressionWithTypeKinds.NEGATED_INSTANCE_CHECK
import org.jetbrains.uast.skipParenthesizedExprDown
import java.util.EnumSet

public class AssertionsDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        if (!context.isTestSource) return null
        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                // Avoid enforcing kotlin-test use in java sources
                if (node.javaPsi?.language?.let(::isJava) == true) return
                val psiMethod = node.resolve() ?: return
                checkJunitAssertion(context, node, psiMethod)
                checkKotlinAssert(context, node, psiMethod)
                checkTypeAssertion(context, node, psiMethod)
                checkEqualityAssertion(context, node, psiMethod)
            }
        }
    }

    private fun checkJunitAssertion(context: JavaContext, node: UCallExpression, psiMethod: PsiMethod) {
        for (assertionClass in setOf("org.junit.Assert", "junit.framework.Assert")) {
            if (!context.evaluator.isMemberInClass(psiMethod, assertionClass)) continue
            context.report(
                issue = JUNIT_ASSERTION_ISSUE,
                scope = node,
                location = context.getLocation(node),
                message = "Use `kotlin.test` assertion",
            )
        }
    }

    private fun checkKotlinAssert(context: JavaContext, node: UCallExpression, psiMethod: PsiMethod) {
        if (psiMethod.name != "assert") return
        if (psiMethod.containingClass?.qualifiedName?.startsWith("kotlin.") != true) return
        context.report(
            issue = KOTLIN_ASSERT_ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "Use `kotlin.test` assertion",
        )
    }

    @Suppress("UnstableApiUsage")
    private fun checkTypeAssertion(context: JavaContext, node: UCallExpression, psiMethod: PsiMethod) {
        if (psiMethod.name != "assertFalse" && psiMethod.name != "assertTrue") return
        val assertion = context.computeBooleanAssertion(node, psiMethod)
        if (assertion.expression !is UBinaryExpressionWithType) return
        if (assertion.expression.operationKind !is InstanceCheck) return
        val replacement = when (psiMethod.name) {
            "assertFalse" -> when (assertion.expression.operationKind) {
                InstanceCheck.INSTANCE -> "kotlin.test.assertIsNot"
                NEGATED_INSTANCE_CHECK -> "kotlin.test.assertIs"
                else -> return
            }

            "assertTrue" -> when (assertion.expression.operationKind) {
                InstanceCheck.INSTANCE -> "kotlin.test.assertIs"
                NEGATED_INSTANCE_CHECK -> "kotlin.test.assertIsNot"
                else -> return
            }

            else -> return
        }
        context.report(
            issue = KOTLIN_TYPE_ASSERTION_ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "Replace boolean assertion with `${replacement.substringAfterLast(".")}`",
            quickfixData = fix()
                .replace().all()
                .with(
                    buildString {
                        append(replacement.substringAfterLast("."))
                        append("<").append(assertion.expression.type.canonicalText).append(">")
                        append("(")
                        append(assertion.expression.operand.asSourceString())
                        if (assertion.message != null) append(", message = ").append(assertion.message.asSourceString())
                        append(")")
                    },
                )
                .imports(replacement)
                .shortenNames(true).reformat(true).autoFix()
                .build(),
        )
    }

    private fun checkEqualityAssertion(context: JavaContext, node: UCallExpression, psiMethod: PsiMethod) {
        val assertion = context.computeBooleanAssertion(node, psiMethod)
        if (assertion.expression !is UBinaryExpression) return
        val replacement = when {
            assertion.expression.leftOperand.isNullLiteral() -> UnaryNull(
                fqfn = psiMethod.simplifiedNullableAssertion(assertion.expression.operator) ?: return,
                actual = assertion.expression.rightOperand,
            )

            assertion.expression.rightOperand.isNullLiteral() -> UnaryNull(
                fqfn = psiMethod.simplifiedNullableAssertion(assertion.expression.operator) ?: return,
                actual = assertion.expression.leftOperand,
            )

            else -> Binary(
                fqfn = psiMethod.simplifiedBinaryAssertion(assertion.expression.operator) ?: return,
                expected = assertion.expression.rightOperand,
                actual = assertion.expression.leftOperand,
            )
        }

        return context.report(
            issue = KOTLIN_EQUALITY_ASSERTION_ISSUE,
            scope = node,
            location = context.getLocation(node),
            message = "Replace boolean assertion with `${replacement.fqfn.substringAfterLast(".")}`",
            quickfixData = fix()
                .replace().all().with(
                    buildString {
                        append(replacement.fqfn.substringAfterLast("."))
                        when (replacement) {
                            is UnaryNull -> {
                                append("(").append(replacement.actual.asSourceString())
                            }

                            is Binary -> {
                                val param = if ("assertNot" in replacement.fqfn) "illegal" else "expected"
                                append("($param = ").append(replacement.expected.asSourceString())
                                append(", actual = ").append(replacement.actual.asSourceString())
                            }
                        }
                        if (assertion.message != null) append(", message = ").append(assertion.message.asSourceString())
                        append(")")
                    },
                )
                .imports(replacement.fqfn)
                .shortenNames(true).reformat(true).autoFix()
                .build(),
        )
    }

    private fun PsiMethod.simplifiedNullableAssertion(operator: UastBinaryOperator): String? = when (name) {
        "assertTrue" -> when (operator) {
            EQUALS -> "kotlin.test.assertNull"
            NOT_EQUALS -> "kotlin.test.assertNotNull"
            IDENTITY_EQUALS -> "kotlin.test.assertNull"
            IDENTITY_NOT_EQUALS -> "kotlin.test.assertNotNull"
            else -> null
        }

        "assertFalse" -> when (operator) {
            EQUALS -> "kotlin.test.assertNotNull"
            NOT_EQUALS -> "kotlin.test.assertNull"
            IDENTITY_EQUALS -> "kotlin.test.assertNotNull"
            IDENTITY_NOT_EQUALS -> "kotlin.test.assertNull"
            else -> null
        }

        else -> null
    }

    private fun PsiMethod.simplifiedBinaryAssertion(operator: UastBinaryOperator): String? = when (name) {
        "assertTrue" -> when (operator) {
            EQUALS -> "kotlin.test.assertEquals"
            NOT_EQUALS -> "kotlin.test.assertNotEquals"
            IDENTITY_EQUALS -> "kotlin.test.assertSame"
            IDENTITY_NOT_EQUALS -> "kotlin.test.assertNotSame"
            else -> null
        }

        "assertFalse" -> when (operator) {
            EQUALS -> "kotlin.test.assertNotEquals"
            NOT_EQUALS -> "kotlin.test.assertEquals"
            IDENTITY_EQUALS -> "kotlin.test.assertNotSame"
            IDENTITY_NOT_EQUALS -> "kotlin.test.assertSame"
            else -> null
        }

        else -> null
    }

    public sealed class EqualityAssertionReplacement {
        public abstract val fqfn: String

        public class UnaryNull(override val fqfn: String, public val actual: UExpression) : EqualityAssertionReplacement()
        public class Binary(override val fqfn: String, public val expected: UExpression, public val actual: UExpression) :
            EqualityAssertionReplacement()
    }

    private data class BooleanAssertion(val expression: UExpression?, val message: UExpression?)

    @OptIn(UnsafeCastFunction::class)
    private fun JavaContext.computeBooleanAssertion(call: UCallExpression, method: PsiMethod) = evaluator
        .computeArgumentMapping(call, method)
        .entries.associateBy(keySelector = { it.value.name }, valueTransform = { it.key })
        .let {
            val actual = it["actual"]
            val block = it["block"]
            val message = it["message"]
            val expression = when {
                actual != null -> actual
                // Special case where a block is used with a single return expression
                block != null -> block.skipParenthesizedExprDown()
                    .safeCast<ULambdaExpression>()?.body
                    .safeCast<UBlockExpression>()?.expressions?.singleOrNull()
                    .safeCast<UReturnExpression>()?.returnExpression

                else -> null
            }?.skipParenthesizedExprDown()
            BooleanAssertion(expression = expression, message = message)
        }

    public companion object {
        public val JUNIT_ASSERTION_ISSUE: Issue = Issue.create(
            id = "JUnitAssertionsUsage",
            briefDescription = "JUnit test framework assertion in Kotlin unit test",
            explanation = "Prefer using `kotlin.test` assertions instead of JUnit's in Kotlin unit tests.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = implementation<AssertionsDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )

        public val KOTLIN_ASSERT_ISSUE: Issue = Issue.create(
            id = "KotlinAssertUsage",
            briefDescription = "Kotlin `assert` in unit test",
            explanation = "Prefer using `kotlin.test` assertions instead of `assert` in unit tests. Its execution requires a specific JVM option to be enabled on the JVM.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = implementation<AssertionsDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )

        public val KOTLIN_TYPE_ASSERTION_ISSUE: Issue = Issue.create(
            id = "KotlinTypeAssertion",
            briefDescription = "Replace boolean assertion with proper type assertion",
            explanation = "Prefer using `assertIs` and `assertIsNot` assertions when checking for types instead of boolean assertions.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = implementation<AssertionsDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )

        /**
         * Based on Kotlin's [`ReplaceAssertBooleanWithAssertEquality`](https://www.jetbrains.com/help/inspectopedia/ReplaceAssertBooleanWithAssertEquality.html), but smarter!
         */
        public val KOTLIN_EQUALITY_ASSERTION_ISSUE: Issue = Issue.create(
            id = "KotlinEqualityAssertion",
            briefDescription = "Replace boolean assertion with proper equality assertion",
            explanation = "Prefer using `assertEquals`/`assertSame` and `assertNotEquals`/`assertNotSame` assertions when checking for equality instead of boolean assertions.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = implementation<AssertionsDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )

    }
}
