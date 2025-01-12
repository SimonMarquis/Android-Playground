package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.java.isJava
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
            }
        }
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
    }

}
