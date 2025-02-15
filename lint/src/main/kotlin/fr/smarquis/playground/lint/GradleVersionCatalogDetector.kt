package fr.smarquis.playground.lint

import com.android.tools.lint.client.api.LintTomlDocument
import com.android.tools.lint.client.api.LintTomlLiteralValue
import com.android.tools.lint.client.api.LintTomlMapValue
import com.android.tools.lint.client.api.LintTomlValue
import com.android.tools.lint.client.api.TomlContext
import com.android.tools.lint.client.api.TomlScanner
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Category.Companion.USABILITY
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.GradleScanner
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope.Companion.TOML_SCOPE
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.StringOption
import com.android.tools.lint.detector.api.TextFormat.RAW
import com.android.utils.mapValuesNotNull

public class GradleVersionCatalogDetector : Detector(), TomlScanner, GradleScanner {

    override fun visitTomlDocument(context: TomlContext, document: LintTomlDocument) {
        if (document.getFile().name != CATALOG_NAME.getValue(context)) return
        if (context.isEnabled(DEPENDENCY_NAME)) document.checkDependencyNames(context)
        if (context.isEnabled(SORT)) document.checkSort(context)
        if (context.isEnabled(VERSION_INLINING)) document.checkVersionInlining(context)
        if (context.isEnabled(SIMPLIFICATION)) document.checkSimplification(context)
    }

    private fun LintTomlDocument.checkSort(context: TomlContext) {
        libraries?.getMappedValues()?.toList()?.fold("") { lastKey, (key, library: LintTomlValue) ->
            if (key >= lastKey) return@fold key
            context.report(SORT, library.keyOrFullLocation(), SORT.getBriefDescription(RAW))
            lastKey
        }
    }

    private fun LintTomlDocument.checkDependencyNames(context: TomlContext) {
        libraries?.getMappedValues()?.forEach { (key: String, library: LintTomlValue) ->
            if (!BANNED_DEPENDENCY_NAME_REGEX.getValue(context).orEmpty().toRegex().matches(key)) return@forEach
            context.report(DEPENDENCY_NAME, library.keyOrFullLocation(), DEPENDENCY_NAME.getBriefDescription(RAW))
        }
    }

    private fun LintTomlDocument.checkVersionInlining(context: TomlContext) {
        val versions = versions?.getMappedValues() ?: return
        listOf(libraries, plugins)
            .flatMap { it?.getMappedValues()?.values.orEmpty() }
            .groupBy { versions[it["version"]["ref"]?.getActualValue() as? String] }
            .filter { it.key != null && it.value.size == 1 }.cast<Map<LintTomlValue, List<LintTomlValue>>>()
            .mapValuesNotNull { it.value.single()["version"]["ref"] }
            .forEach { (version, ref) ->
                context.report(VERSION_INLINING, version.getFullLocation(), VERSION_INLINING.getBriefDescription(RAW))
                context.report(VERSION_INLINING, ref.getFullLocation(), VERSION_INLINING.getBriefDescription(RAW))
            }
    }

    private fun LintTomlDocument.checkSimplification(context: TomlContext) {
        plugins?.getMappedValues()?.forEach { (key, value) ->
            val id = value["id"]?.getActualValue() ?: return@forEach
            val version = value["version"]?.takeIf { it is LintTomlLiteralValue }?.getActualValue() ?: return@forEach

            // … = { id = "…", version = "…" }
            context.report(
                issue = SIMPLIFICATION,
                location = value.getLocation(),
                message = SIMPLIFICATION.getBriefDescription(RAW),
                quickfixData = LintFix.create()
                    .name("Replace plugin declaration with simpler form.")
                    .replace().range(value.getFullLocation()).with("""$key = "$id:$version"""")
                    .autoFix().build(),
            )
        }
        libraries?.getMappedValues()?.forEach { (key, value) ->
            val module = value["module"]?.getActualValue()
            val group = value["group"]?.getActualValue()
            val name = value["name"]?.getActualValue()
            val version = value["version"]?.getActualValue()
            val versionRef = value["version"]["ref"]?.getActualValue()

            val replacement = when {
                // … = { group = "…", name = "…", version = "…" }
                group != null && name != null && version != null -> """$key = "$group:$name:$version""""
                // … = { group = "…", name = "…", version.ref = "…" }
                group != null && name != null && versionRef != null -> """$key = { module = "$group:$name", version.ref = "$versionRef""""
                // … = { module = "…", version = "…" }
                module != null && version != null -> """$key = "$module:$version""""
                else -> return@forEach
            }
            context.report(
                issue = SIMPLIFICATION,
                location = value.getLocation(),
                message = SIMPLIFICATION.getBriefDescription(RAW),
                quickfixData = LintFix.create()
                    .name("Replace library declaration with simpler form.")
                    .replace().range(value.getFullLocation()).with(replacement)
                    .autoFix().build(),
            )
        }
    }

    private fun LintTomlValue.keyOrFullLocation() = runCatching { getKeyLocation() }
        .onFailure { throw Exception(this.getFullKey(), it) }
        .getOrNull() ?: getFullLocation()

    private operator fun LintTomlValue?.get(key: String) = (this as? LintTomlMapValue)?.get(key)
    private val LintTomlDocument.versions get() = getValue("versions") as? LintTomlMapValue
    private val LintTomlDocument.libraries get() = getValue("libraries") as? LintTomlMapValue
    private val LintTomlDocument.plugins get() = getValue("plugins") as? LintTomlMapValue

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

        public val VERSION_INLINING: Issue = Issue.create(
            id = "GradleVersionCatalogVersionInlining",
            briefDescription = "Version is used only once, it can be inlined",
            explanation = "Extracting a version in the [versions] section is useful only if it is used more than once or referenced elsewhere.",
            category = CORRECTNESS,
            priority = 1,
            severity = ERROR,
            implementation = IMPLEMENTATION,
        )

        public val SIMPLIFICATION: Issue = Issue.create(
            id = "GradleVersionCatalogSimplification",
            briefDescription = "Dependency declaration can be simplified",
            explanation = "Dependency declaration should use the simplest form possible, omitting unnecessary inline tables.",
            category = CORRECTNESS,
            priority = 1,
            severity = ERROR,
            implementation = IMPLEMENTATION,
        )

        internal val ISSUES = arrayOf(SORT, VERSION_INLINING, SIMPLIFICATION, DEPENDENCY_NAME)
            .onEach { it.setOptions(listOf(CATALOG_NAME, BANNED_DEPENDENCY_NAME_REGEX)) }

    }

}
