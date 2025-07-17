package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

internal class IssueRegistry : IssueRegistry() {

    override val api: Int = CURRENT_API
    override val minApi: Int = CURRENT_API

    override val issues: List<Issue> = listOf(
        AssertionsDetector.JUNIT_ASSERTION_ISSUE,
        AssertionsDetector.KOTLIN_ASSERT_ISSUE,
        AssertionsDetector.KOTLIN_TYPE_ASSERTION_ISSUE,
        AssertionsDetector.KOTLIN_EQUALITY_ASSERTION_ISSUE,
        *GradleVersionCatalogDetector.ISSUES,
        NamedParametersDetector.MISMATCHED_NAMED_PARAMETERS,
        NamedParametersDetector.MISSING_NAMED_PARAMETERS,
        ReplaceMethodCallDetector.ISSUE,
        TestMethodBannedWordsDetector.ISSUE,
        TypographyDetector.CURLY_QUOTE_ISSUE,
        TypographyDetector.ESCAPED_UNICODE_ISSUE,
        TypographyDetector.REPLACEMENT_ISSUE,
    )

    override val vendor: Vendor = Vendor(
        vendorName = "Simon Marquis",
        feedbackUrl = "https://github.com/SimonMarquis/Android-Playground/issues",
        contact = "https://github.com/SimonMarquis/Android-Playground",
    )

}
