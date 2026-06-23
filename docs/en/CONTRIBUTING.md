# Contributing Guide

[中文](../../CONTRIBUTING.md) | [English](CONTRIBUTING.md)

Thank you for improving pf4boot-plugin. This project focuses on local development convenience for pf4boot plugins and currently targets Gradle 7.x and JDK 8.

## Reporting issues

For normal bugs, use the GitHub issue form and read the [bug reporting guide](bug-reporting.md). Do not disclose exploit details publicly for security issues; follow the [security policy](SECURITY.md) instead.

Useful bug reports include:

- pf4boot-plugin version or commit.
- Affected plugin ID, task, or dependency group, such as `net.xdob.pf4boot-plugin`, `pf4boot`, or `platformApi`.
- Minimal reproducible Gradle project, test case, or full reproduction command.
- Gradle, JDK, operating system, and key dependency coordinates.
- Expected behavior, actual behavior, error logs, or ZIP content differences.

## Submitting changes

Before implementation, make sure the requirement and design are clear. Changes involving behavior, compatibility, release flow, or Gradle configuration semantics should update or add design documentation before code changes.

Recommended workflow:

1. Fork the repository or create a local branch.
2. Keep the change focused on one clear problem.
3. Add tests for key behavior, preferably using the existing `functionalTest` style.
4. Keep Chinese primary docs and English copies synchronized.
5. Run the required validation before submitting.

## Local validation

Common commands:

```powershell
.\gradlew.bat check
.\gradlew.bat publishToMavenLocal
.\gradlew.bat publishPlugins --dry-run
```

Docs-only changes usually do not need the full test suite, but should at least run:

```powershell
git diff --check
```

For Chinese Markdown changes, keep UTF-8 encoding and avoid mojibake or U+FFFD replacement characters.

## Documentation rules

- Chinese documentation is authoritative.
- English documentation is a synchronized copy.
- Paired documents should include language-switch links near the top.
- New user-facing documents should include an English copy.

## Pull Request expectations

Please include:

- Purpose of the change.
- Affected scope.
- Compatibility notes.
- Validation commands you ran.
- If tests are missing, explain why and describe remaining risk.
