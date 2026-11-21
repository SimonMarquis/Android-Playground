#!/usr/bin/env python3
"""Exercise DSL failures and dependency wiring against the real multi-project build."""
from pathlib import Path
import subprocess


def run(*arguments: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["./gradlew", *arguments, "--console=plain"],
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        check=False,
    )


def expect(result: subprocess.CompletedProcess[str], success: bool, *messages: str) -> None:
    if (result.returncode == 0) != success or not all(message in result.stdout for message in messages):
        print(result.stdout)
        raise SystemExit("Playground DSL integration check failed")


def with_edit(path: str, old: str, new: str, check) -> None:
    file = Path(path)
    original = file.read_text()
    if original.count(old) != 1:
        raise SystemExit(f"Could not uniquely locate the test insertion in {path}")
    try:
        file.write_text(original.replace(old, new, 1))
        check()
    finally:
        file.write_text(original)


with_edit(
    "domain/dice/build.gradle.kts",
    "        testFixtures()\n",
    "",
    lambda: expect(
        run(":domain:dice:help"),
        False,
        "Fixture dependencies require testFixtures()",
    ),
)
with_edit(
    "profiling/build.gradle.kts",
    '    androidBenchmark(targetProjectPath = ":app") {\n',
    '    androidBenchmark(targetProjectPath = ":app") {\n        flowTests()\n',
    lambda: expect(run(":profiling:help"), False, "Unresolved reference 'flowTests'"),
)
with_edit(
    "core/ui/build.gradle.kts",
    "        dependencies {\n",
    '        dependencies {\n            debugImplementation(libs.junit)\n            add("debugImplementation", libs.turbine)\n',
    lambda: expect(
        run(":core:ui:dependencies", "--configuration", "debugCompileClasspath"),
        True,
        "junit:junit",
        "app.cash.turbine:turbine",
    ),
)
with_edit(
    "core/ui/build.gradle.kts",
    "        dependencies {\n",
    '        dependencies {\n            add("missingConfiguration", libs.junit)\n',
    lambda: expect(run(":core:ui:help"), False, "missingConfiguration"),
)
print("Playground DSL integration checks passed")
