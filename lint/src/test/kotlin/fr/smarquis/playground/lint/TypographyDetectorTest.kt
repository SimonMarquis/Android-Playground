package fr.smarquis.playground.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import fr.smarquis.playground.lint.TypographyDetector.Companion.CURLY_QUOTE_ISSUE
import fr.smarquis.playground.lint.TypographyDetector.Companion.ESCAPED_UNICODE_ISSUE
import fr.smarquis.playground.lint.TypographyDetector.Companion.REPLACEMENT_ISSUE
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypographyDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = TypographyDetector()
    override fun getIssues() = emptyList<Issue>()

    @Test
    fun `curly quotes`() = lint()
        .issues(CURLY_QUOTE_ISSUE)
        .files(
            xml(
                "/res/values/strings.xml",
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="quotes">Good ol’ apostrophe</string>
                </resources>
                """.trimIndent(),
            ),
        ).run()
        .expect(
            """
            res/values/strings.xml:3: Error: Curly quotes must be replaced with straight quote [TypographyCurlyQuote]
                <string name="quotes">Good ol’ apostrophe</string>
                                      ~~~~~~~~~~~~~~~~~~~
            1 errors, 0 warnings
            """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for res/values/strings.xml line 3: Replace curly quote with straight quote:
            @@ -3 +3
            -     <string name="quotes">Good ol’ apostrophe</string>
            +     <string name="quotes">Good ol\' apostrophe</string>
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `escaped unicode`() = lint()
        .issues(ESCAPED_UNICODE_ISSUE)
        .files(
            xml(
                "/res/values/strings.xml",
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="bullet">Bullet: \u2022</string>
                    <string name="trademark">Trademark: \u2122</string>
                    <string name="copyright">Copyright: \u00A9</string>
                    <string name="double_quote">Double quote: \u0022OK\u0022</string>

                    <!-- NBSP is ignored for now -->
                    <string name="spacing">Spacing:\u00A0!</string>
                </resources>
                """.trimIndent(),
            ),
        ).run()
        .expect(
            """
            res/values/strings.xml:3: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="bullet">Bullet: \u2022</string>
                                              ^
            res/values/strings.xml:4: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="trademark">Trademark: \u2122</string>
                                                    ^
            res/values/strings.xml:5: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="copyright">Copyright: \u00A9</string>
                                                    ^
            res/values/strings.xml:6: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="double_quote">Double quote: \u0022OK\u0022</string>
                                                          ^
            res/values/strings.xml:6: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="double_quote">Double quote: \u0022OK\u0022</string>
                                                                  ^
            0 errors, 5 warnings
            """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for res/values/strings.xml line 3: Replace \u2022 with •:
            @@ -3 +3
            -     <string name="bullet">Bullet: \u2022</string>
            +     <string name="bullet">Bullet: •</string>
            Autofix for res/values/strings.xml line 4: Replace \u2122 with ™:
            @@ -4 +4
            -     <string name="trademark">Trademark: \u2122</string>
            +     <string name="trademark">Trademark: ™</string>
            Autofix for res/values/strings.xml line 5: Replace \u00A9 with ©:
            @@ -5 +5
            -     <string name="copyright">Copyright: \u00A9</string>
            +     <string name="copyright">Copyright: ©</string>
            Autofix for res/values/strings.xml line 6: Replace \u0022 with \":
            @@ -6 +6
            -     <string name="double_quote">Double quote: \u0022OK\u0022</string>
            +     <string name="double_quote">Double quote: \"OK\u0022</string>
            Autofix for res/values/strings.xml line 6: Replace \u0022 with \":
            @@ -6 +6
            -     <string name="double_quote">Double quote: \u0022OK\u0022</string>
            +     <string name="double_quote">Double quote: \u0022OK\"</string>
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun `test nested strings`() = lint()
        .issues(ESCAPED_UNICODE_ISSUE)
        .files(
            xml(
                "/res/values/strings.xml",
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="html">start <b>\u2122</b> end</string>
                    <string name="cdata"><![CDATA[start \u2122 end]]></string>
                    <plurals name="plurals">
                        <item quantity="other">start \u2122 end</item>
                    </plurals>
                    <string-array name="string-array">
                        <item>start \u00A9 end</item>
                    </string-array>
                </resources>
                """.trimIndent(),
            ),
        ).run()
        .expect(
            """
            res/values/strings.xml:3: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="html">start <b>\u2122</b> end</string>
                                             ^
            res/values/strings.xml:4: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="cdata"><![CDATA[start \u2122 end]]></string>
                                                    ^
            res/values/strings.xml:6: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                    <item quantity="other">start \u2122 end</item>
                                                 ^
            res/values/strings.xml:9: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                    <item>start \u00A9 end</item>
                                ^
            0 errors, 4 warnings
            """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for res/values/strings.xml line 3: Replace \u2122 with ™:
            @@ -3 +3
            -     <string name="html">start <b>\u2122</b> end</string>
            +     <string name="html">start <b>™</b> end</string>
            Autofix for res/values/strings.xml line 4: Replace \u2122 with ™:
            @@ -4 +4
            -     <string name="cdata"><![CDATA[start \u2122 end]]></string>
            +     <string name="cdata"><![CDATA[start ™ end]]></string>
            Autofix for res/values/strings.xml line 6: Replace \u2122 with ™:
            @@ -6 +6
            -         <item quantity="other">start \u2122 end</item>
            +         <item quantity="other">start ™ end</item>
            Autofix for res/values/strings.xml line 9: Replace \u00A9 with ©:
            @@ -9 +9
            -         <item>start \u00A9 end</item>
            +         <item>start © end</item>
            """.trimIndent(),
        )
        .cleanup()


    @Test
    fun `percent character`() = lint()
        .issues(ESCAPED_UNICODE_ISSUE)
        .files(
            xml(
                "/res/values/strings.xml",
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="percent_1">Foo 100\u0025!</string>
                    <string name="percent_2" formatted="false">Foo 100\u0025s!</string>
                    <string name="percent_3">Foo %1＄d\u0025!</string>
                    <string name="percent_4" formatted="true">Foo 10%% or 42\u0025s!</string>
                </resources>
                """.trimIndent(),
            ),
        ).run()
        .expect(
            """
            res/values/strings.xml:3: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="percent_1">Foo 100\u0025!</string>
                                                ^
            res/values/strings.xml:4: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="percent_2" formatted="false">Foo 100\u0025s!</string>
                                                                  ^
            res/values/strings.xml:5: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="percent_3">Foo %1＄d\u0025!</string>
                                                 ^
            res/values/strings.xml:6: Warning: Java escaped unicode character can be replaced [TypographyJavaEscapedUnicode]
                <string name="percent_4" formatted="true">Foo 10%% or 42\u0025s!</string>
                                                                        ^
            0 errors, 4 warnings
            """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for res/values/strings.xml line 3: Replace \u0025 with %:
            @@ -3 +3
            -     <string name="percent_1">Foo 100\u0025!</string>
            +     <string name="percent_1">Foo 100%!</string>
            Autofix for res/values/strings.xml line 4: Replace \u0025 with %:
            @@ -4 +4
            -     <string name="percent_2" formatted="false">Foo 100\u0025s!</string>
            +     <string name="percent_2" formatted="false">Foo 100%s!</string>
            Autofix for res/values/strings.xml line 5: Replace \u0025 with %%:
            @@ -5 +5
            -     <string name="percent_3">Foo %1${'$'}d\u0025!</string>
            +     <string name="percent_3">Foo %1${'$'}d%%!</string>
            Autofix for res/values/strings.xml line 6: Replace \u0025 with %%:
            @@ -6 +6
            -     <string name="percent_4" formatted="true">Foo 10%% or 42\u0025s!</string>
            +     <string name="percent_4" formatted="true">Foo 10%% or 42%%s!</string>
            """.trimIndent(),
        )
        .cleanup()

    @Test
    fun replacements() = lint()
        .issues(REPLACEMENT_ISSUE)
        .files(
            xml(
                "/res/values/strings.xml",
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="right_arrow">start -> end</string>
                    <string name="left_arrow">end &lt;- start</string>
                    <string name="right_guillemet">end >> start</string>
                    <string name="left_guillemet">end &lt;&lt; start</string>
                    <string name="copyright">(c)</string>
                    <string name="registered">(r)</string>
                    <string name="trademark">(tm)</string>
                </resources>
                """.trimIndent(),
            ),
        ).run()
        .expect(
            """
            res/values/strings.xml:3: Warning: Typography replacement detected [TypographyReplacement]
                <string name="right_arrow">start -> end</string>
                                                 ^
            res/values/strings.xml:4: Warning: Typography replacement detected [TypographyReplacement]
                <string name="left_arrow">end &lt;- start</string>
                                              ^
            res/values/strings.xml:5: Warning: Typography replacement detected [TypographyReplacement]
                <string name="right_guillemet">end >> start</string>
                                                   ^
            res/values/strings.xml:6: Warning: Typography replacement detected [TypographyReplacement]
                <string name="left_guillemet">end &lt;&lt; start</string>
                                                  ^
            res/values/strings.xml:7: Warning: Typography replacement detected [TypographyReplacement]
                <string name="copyright">(c)</string>
                                         ^
            res/values/strings.xml:8: Warning: Typography replacement detected [TypographyReplacement]
                <string name="registered">(r)</string>
                                          ^
            res/values/strings.xml:9: Warning: Typography replacement detected [TypographyReplacement]
                <string name="trademark">(tm)</string>
                                         ^
            0 errors, 7 warnings
            """.trimIndent(),
        )
        .expectFixDiffs(
            """
            Autofix for res/values/strings.xml line 3: Replace -> with →:
            @@ -3 +3
            -     <string name="right_arrow">start -> end</string>
            +     <string name="right_arrow">start → end</string>
            Autofix for res/values/strings.xml line 4: Replace <- with ←:
            @@ -4 +4
            -     <string name="left_arrow">end &lt;- start</string>
            +     <string name="left_arrow">end ←t;- start</string>
            Autofix for res/values/strings.xml line 5: Replace >> with »:
            @@ -5 +5
            -     <string name="right_guillemet">end >> start</string>
            +     <string name="right_guillemet">end » start</string>
            Autofix for res/values/strings.xml line 6: Replace << with «:
            @@ -6 +6
            -     <string name="left_guillemet">end &lt;&lt; start</string>
            +     <string name="left_guillemet">end «t;&lt; start</string>
            Autofix for res/values/strings.xml line 7: Replace (c) with ©:
            @@ -7 +7
            -     <string name="copyright">(c)</string>
            +     <string name="copyright">©</string>
            Autofix for res/values/strings.xml line 8: Replace (r) with ®:
            @@ -8 +8
            -     <string name="registered">(r)</string>
            +     <string name="registered">®</string>
            Autofix for res/values/strings.xml line 9: Replace (tm) with ™:
            @@ -9 +9
            -     <string name="trademark">(tm)</string>
            +     <string name="trademark">™</string>
            """.trimIndent(),
        )
        .cleanup()

}

