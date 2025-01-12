package fr.smarquis.playground.lint

import com.android.tools.lint.detector.api.AnnotationInfo
import com.android.tools.lint.detector.api.AnnotationUsageInfo
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Scope.TEST_SOURCES
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.StringOption
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UElement
import java.util.EnumSet


/**
 * Reports errors whenever a test method uses a banned word.
 */
public class TestMethodBannedWordsDetector : Detector(), SourceCodeScanner {

    override fun applicableAnnotations(): List<String> = listOf("kotlin.test.Test", "org.junit.Test")

    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo,
    ) {
        val method = usageInfo.referenced as? PsiMethod ?: return
        val words = BANNED_WORDS.getValue(context).orEmpty().split(",").toSet()
        val matches = method.name
            .split("\\s+".toRegex())
            .filter { it.lowercase() in words }
            .takeUnless { it.isEmpty() } ?: return
        context.report(
            issue = ISSUE,
            scope = usageInfo.usage,
            location = context.getNameLocation(method),
            message = "This method uses banned words: $matches",
        )
    }

    public companion object {

        internal val BANNED_WORDS = StringOption(
            name = "banned-words",
            description = "Comma-separated list of banned words",
            defaultValue = "failure,failed",
        )

        @Suppress("LintImplTextFormat")
        public val ISSUE: Issue = Issue.create(
            id = "TestMethodBannedWords",
            briefDescription = "Test method uses a banned word",
            explanation = """
                Test methods name should not contains banned words.
                The default behavior checks for ${BANNED_WORDS.defaultValue} words to reduce collisions when searching through logs.
                """.trimIndent(),
            category = Category.TESTING,
            priority = 5,
            severity = Severity.ERROR,
            implementation = implementation<TestMethodBannedWordsDetector>(EnumSet.of(JAVA_FILE, TEST_SOURCES)),
        ).setOptions(listOf(BANNED_WORDS))

    }

}
