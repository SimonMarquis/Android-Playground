package fr.smarquis.playground.lint

import com.android.SdkConstants.ATTR_NAME
import com.android.SdkConstants.TAG_ITEM
import com.android.SdkConstants.TAG_PLURALS
import com.android.SdkConstants.TAG_STRING
import com.android.SdkConstants.TAG_STRING_ARRAY
import com.android.SdkConstants.VALUE_TRUE
import com.android.resources.ResourceFolderType
import com.android.resources.ResourceFolderType.VALUES
import com.android.tools.lint.detector.api.Category.Companion.TYPOGRAPHY
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope.Companion.RESOURCE_FILE_SCOPE
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.Severity.WARNING
import com.android.tools.lint.detector.api.TextFormat.TEXT
import com.android.tools.lint.detector.api.XmlContext
import com.android.utils.SdkUtils
import org.w3c.dom.Element
import xoxo.XmlElement
import xoxo.XmlText
import xoxo.toXmlNode
import xoxo.walk


/**
 * Detect generic typography related issues.
 *
 * Notes:
 * - HTML entities (like `&bull;`, `&#8226;`, `&#x2022;`) are not accessible from the Lint detector since they are automagically replaced by the XML parser ([org.w3c.dom.Node.getNodeValue] returns already processed HTML entities because the DOM parser probably already force this behavior).
 * - Lint detectors do not like nested CDATA, it does not keep up with node location tracking, making it impossible to accurately report and replace elements.
 * - [TypographyEllipsis](https://googlesamples.github.io/android-custom-lint-rules/checks/TypographyEllipsis.md.html)
 * - [TypographyFractions](https://googlesamples.github.io/android-custom-lint-rules/checks/TypographyFractions.md.html)
 * - [TypographyOther](https://googlesamples.github.io/android-custom-lint-rules/checks/TypographyOther.md.html)
 */
public class TypographyDetector : ResourceXmlDetector() {

    public companion object {
        private val ESCAPED_UNICODE_REGEX: Regex = "\\\\u+(\\p{XDigit}{4})".toRegex()
        public val ESCAPED_UNICODE_ISSUE: Issue = Issue.create(
            id = "TypographyJavaEscapedUnicode",
            briefDescription = "Java escaped unicode character can be replaced",
            explanation = "Escaped character are impossible to decipher for a human. Using unescaped character is generally self explanatory.",
            category = TYPOGRAPHY,
            priority = 5,
            severity = WARNING,
            implementation = implementation<TypographyDetector>(RESOURCE_FILE_SCOPE),
        )

        /** Note: Location index does not work as expected with HTML entities... */
        private val REPLACEMENTS = mapOf(
            "->" to "→",
            "-&gt;" to "→",
            "<-" to "←",
            "&lt;-" to "←",
            ">>" to "»",
            "&lt;&lt;" to "»",
            "<<" to "«",
            "&gt;&gt;" to "«",
            "(c)" to "©",
            "(C)" to "©",
            "(r)" to "®",
            "(R)" to "®",
            "(tm)" to "™",
            "(TM)" to "™",
        )
        public val REPLACEMENT_ISSUE: Issue = Issue.create(
            id = "TypographyReplacement",
            briefDescription = "Typography replacement detected",
            explanation = "Typography can be replaced with a better alternative.",
            category = TYPOGRAPHY,
            priority = 5,
            severity = WARNING,
            implementation = implementation<TypographyDetector>(RESOURCE_FILE_SCOPE),
        )

        public val CURLY_QUOTE_ISSUE: Issue = Issue.create(
            id = "TypographyCurlyQuote",
            briefDescription = "Curly quotes must be replaced with straight quote",
            explanation = "Talkback does not properly handle curly quotes.",
            category = TYPOGRAPHY,
            priority = 5,
            severity = ERROR,
            implementation = implementation<TypographyDetector>(RESOURCE_FILE_SCOPE),
        )
    }

    override fun appliesTo(folderType: ResourceFolderType): Boolean = folderType == VALUES
    override fun getApplicableElements(): List<String> = listOf(TAG_STRING, TAG_PLURALS, TAG_STRING_ARRAY)

    override fun visitElement(context: XmlContext, element: Element) {
        if (SdkUtils.isServiceKey(element.getAttribute(ATTR_NAME))) return

        val xmlElement = element.toXmlNode() as XmlElement
        when (xmlElement.name) {
            TAG_STRING -> xmlElement.checkText(context)
            TAG_PLURALS, TAG_STRING_ARRAY -> xmlElement.childElements
                .filter { it.name == TAG_ITEM }
                .forEach { it.checkText(context) }
        }
    }

    private fun XmlElement.checkText(context: XmlContext) = walk()
        .filterIsInstance<XmlText>()
        .forEach {
            checkJavaEscapedUnicode(it, context, asDomNode())
            checkReplacements(it, context)
            checkCurlyQuotes(it, context)
        }

    private fun checkJavaEscapedUnicode(node: XmlText, context: XmlContext, element: Element) {
        ESCAPED_UNICODE_REGEX.findAll(node.content).forEach { match ->
            val location = context.getLocation(node.asDomNode(), match.range.first, match.range.last + 1)
            val replacement = when (val char = match.groupValues[1].toInt(16).toChar()) {
                '%' -> when {
                    // Formatted strings must escape with a double percent character %%
                    element.getAttribute("formatted") == VALUE_TRUE -> "%%"
                    // Primitive detection of String format placeholders
                    "%[^%]".toRegex().containsMatchIn(node.content) -> "%%"
                    else -> "%"
                }
                // inlining single/double quotes requires a backslash (unless already wrapped)
                '"', '\'' -> "\\$char"
                // NBSP are ignored for now since gerrit replaces the unicode character with a regular space
                Typography.nbsp -> return@forEach
                else -> char.toString()
            }
            context.report(
                issue = ESCAPED_UNICODE_ISSUE,
                // scope = node.text,
                location = location,
                message = ESCAPED_UNICODE_ISSUE.getBriefDescription(TEXT),
                quickfixData = fix()
                    .name("Replace ${match.value} with $replacement")
                    .replace().range(location).with(replacement)
                    .autoFix().build(),
            )
        }
    }

    private fun checkReplacements(node: XmlText, context: XmlContext) {
        REPLACEMENTS.entries.forEach { (search, replacement) ->
            if (search !in node.content) return@forEach
            val index = node.content.indexOf(search)
            val location = context.getLocation(node.asDomNode(), index, index + search.length)
            context.report(
                issue = REPLACEMENT_ISSUE,
                location = location,
                message = REPLACEMENT_ISSUE.getBriefDescription(TEXT),
                quickfixData = fix()
                    .name("Replace $search with $replacement")
                    .replace().range(location).with(replacement)
                    .autoFix().build(),
            )
        }
    }

    private fun checkCurlyQuotes(node: XmlText, context: XmlContext) {
        if ("’" !in node.content) return
        context.report(
            issue = CURLY_QUOTE_ISSUE,
            location = context.getLocation(node.asDomNode()),
            message = CURLY_QUOTE_ISSUE.getBriefDescription(TEXT),
            quickfixData = fix()
                .name("Replace curly quote with straight quote")
                .replace().text("’").with("\\'").all()
                .autoFix().build(),
        )
    }

}
