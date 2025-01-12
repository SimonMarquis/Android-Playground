package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.LintTomlDocument
import com.android.tools.lint.client.api.LintTomlMapValue
import com.android.tools.lint.client.api.LintTomlValue
import com.android.tools.lint.client.api.TomlContext
import com.android.tools.lint.client.api.TomlScanner
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Category.Companion.USABILITY
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.GradleScanner
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope.Companion.TOML_SCOPE
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.StringOption
import com.android.tools.lint.detector.api.TextFormat.RAW

public class GradleVersionCatalogDetector : Detector(), TomlScanner, GradleScanner {

    override fun visitTomlDocument(context: TomlContext, document: LintTomlDocument) {
        if (document.getFile().name != CATALOG_NAME.getValue(context)) return
        if (context.isEnabled(DEPENDENCY_NAME)) document.checkDependencyNames(context)
        if (context.isEnabled(SORT)) document.checkSort(context)
    }

    private fun LintTomlDocument.checkSort(context: TomlContext) {
        val toList = libraries.getMappedValues().toList()
        toList.fold("") { lastKey, (key, library: LintTomlValue) ->
            if (key >= lastKey) return@fold key
            context.report(SORT, library.keyOrFullLocation(), SORT.getBriefDescription(RAW))
            lastKey
        }
    }

    private fun LintTomlDocument.checkDependencyNames(context: TomlContext) {
        libraries.getMappedValues().forEach { (key: String, library: LintTomlValue) ->
            if (!BANNED_DEPENDENCY_NAME_REGEX.getValue(context).orEmpty().toRegex().matches(key)) return@forEach
            context.report(DEPENDENCY_NAME, library.keyOrFullLocation(), DEPENDENCY_NAME.getBriefDescription(RAW))
        }
    }

    private fun LintTomlValue.keyOrFullLocation() = runCatching { getKeyLocation() }
        .onFailure { throw Exception(this.getFullKey(), it) }
        .getOrNull() ?: getFullLocation()

    private val LintTomlDocument.versions get() = getValue("versions") as LintTomlMapValue
    private val LintTomlDocument.libraries get() = getValue("libraries") as LintTomlMapValue

    public companion object {

        private val IMPLEMENTATION = implementation<GradleVersionCatalogDetector>(TOML_SCOPE)

        internal val CATALOG_NAME = StringOption(
            name = "catalog-name",
            description = "The version catalog file name hosting the dependencies.",
            defaultValue = "libs.versions.toml",
        )

        internal val BANNED_DEPENDENCY_NAME_REGEX = StringOption(
            name = "banned-dependency-name-regex",
            description = "Banned dependency name regex.",
            defaultValue = null,
        )

        public val SORT: Issue = Issue.create(
            id = "GradleVersionCatalogSort",
            briefDescription = "Dependencies are not sorted correctly",
            explanation = "Dependencies should be sorted alphabetically to maintain consistency and readability.",
            category = USABILITY,
            priority = 1,
            severity = ERROR,
            implementation = IMPLEMENTATION,
        )

        public val DEPENDENCY_NAME: Issue = Issue.create(
            id = "GradleVersionCatalogDependencyName",
            briefDescription = "Dependency name does not follow the expected format",
            explanation = "Dependencies should follow the configured regex. (default: `${BANNED_DEPENDENCY_NAME_REGEX.defaultValue}`)",
            category = CORRECTNESS,
            priority = 1,
            severity = ERROR,
            implementation = IMPLEMENTATION,
        )

        internal val ISSUES = arrayOf(SORT, DEPENDENCY_NAME)
            .onEach { it.setOptions(listOf(CATALOG_NAME, BANNED_DEPENDENCY_NAME_REGEX)) }

    }

}
