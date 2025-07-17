package fr.smarquis.playground.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat.RAW
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import java.util.EnumSet

public class ReplaceMethodCallDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("foo")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod): Unit =
        Incident(context)
            .issue(ISSUE)
            .at(node)
            .message(ISSUE.getBriefDescription(RAW))
            .fix(
                fix()
                    .name("Replace `foo` with `Unit`")
                    .replace().all().with("Unit")
                    .robot(true).independent(true).build(),
            )
            .report()

    public companion object {
        public val ISSUE: Issue = Issue.create(
            id = "ReplaceMethodCall",
            briefDescription = "The method `foo()` should not be called!",
            explanation = "The method `foo()` should not be called!",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = implementation<ReplaceMethodCallDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        )
    }

}
