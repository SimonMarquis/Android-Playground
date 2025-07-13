package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UBinaryExpressionWithType
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.UastBinaryExpressionWithTypeKind.InstanceCheck
import org.jetbrains.uast.UastBinaryOperator.Companion.EQUALS
import org.jetbrains.uast.UastBinaryOperator.Companion.IDENTITY_EQUALS
import org.jetbrains.uast.UastBinaryOperator.Companion.IDENTITY_NOT_EQUALS
import org.jetbrains.uast.UastBinaryOperator.Companion.NOT_EQUALS
import org.jetbrains.uast.java.isJava
import org.jetbrains.uast.skipParenthesizedExprDown
import java.util.EnumSet

public class AssertionsDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<UCallExpression>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        if (!context.isTestSource) return null
        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                // Avoid enforcing kotlin-test use in java sources
                if (isJava(node.javaPsi?.language)) return
                val psiMethod = node.resolve() ?: return
                // Kotlin assert
                if (psiMethod.name == "assert" && psiMethod.containingClass?.qualifiedName?.startsWith("kotlin.") == true) {
                    return context.report(
                        issue = KOTLIN_ASSERT_ISSUE,
                        scope = node,
                        location = context.getLocation(node),
                        message = "Use `kotlin.test` assertion",
                    )
                }
                // JUnit assertions
                for (assertionClass in setOf("org.junit.Assert", "junit.framework.Assert")) {
                    if (context.evaluator.isMemberInClass(psiMethod, assertionClass)) {
                        return context.report(
                            issue = JUNIT_ASSERTION_ISSUE,
                            scope = node,
                            location = context.getLocation(node),
                            message = "Use `kotlin.test` assertion",
                        )
                    }
                }
                // Boolean assertion instead of type assertion
                typeAssertionMap[node.methodName ?: psiMethod.name]?.let report@{ replacement ->
                    val assertion = context.computeBooleanAssertion(node, psiMethod)
                    if (assertion.expression !is UBinaryExpressionWithType) return@report
                    if (assertion.expression.operationKind !is InstanceCheck) return@report
                    return context.report(
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
                            .shortenNames(true).reformat(true)
                            .imports(replacement)
                            .autoFix().build(),
                    )
                }
                // Boolean assertion instead of equality assertion
                equalityAssertionMap[node.methodName ?: psiMethod.name]?.let report@{ replacements ->
                    val assertion = context.computeBooleanAssertion(node, psiMethod)
                    if (assertion.expression !is UBinaryExpression) return@report
                    val replacement = replacements[assertion.expression.operator] ?: return@report
                    return context.report(
                        issue = KOTLIN_EQUALITY_ASSERTION_ISSUE,
                        scope = node,
                        location = context.getLocation(node),
                        message = "Replace boolean assertion with `${replacement.substringAfterLast(".")}`",
                        quickfixData = fix()
                            .replace().all().with(
                                buildString {
                                    append(replacement.substringAfterLast("."))
                                    append("(expected = ").append(assertion.expression.rightOperand.asSourceString())
                                    append(", actual = ").append(assertion.expression.leftOperand.asSourceString())
                                    if (assertion.message != null) append(", message = ").append(assertion.message.asSourceString())
                                    append(")")
                                },
                            )
                            .shortenNames(true).reformat(true)
                            .imports(replacement)
                            .autoFix().build(),
                    )
                }
            }
        }
    }

    private data class BooleanAssertion(val expression: UExpression?, val message: UExpression?)

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
            severity = Severity.WARNING,
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
        private val typeAssertionMap = mapOf(
            "assertTrue" to "kotlin.test.assertIs",
            "assertFalse" to "kotlin.test.assertIsNot",
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
        private val equalityAssertionMap = mapOf(
            "assertTrue" to mapOf(
                EQUALS to "kotlin.test.assertEquals",
                NOT_EQUALS to "kotlin.test.assertNotEquals",
                IDENTITY_EQUALS to "kotlin.test.assertSame",
                IDENTITY_NOT_EQUALS to "kotlin.test.assertNotSame",
            ),
            "assertFalse" to mapOf(
                EQUALS to "kotlin.test.assertNotEquals",
                NOT_EQUALS to "kotlin.test.assertEquals",
                IDENTITY_EQUALS to "kotlin.test.assertNotSame",
                IDENTITY_NOT_EQUALS to "kotlin.test.assertSame",
            ),
        )
    }

}
