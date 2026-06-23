# Bug Reporting Guide

[中文](../bug-reporting.md) | [English](bug-reporting.md)

This guide helps users submit reproducible and diagnosable pf4boot-plugin issues. Use the GitHub issue form for normal bugs. For security issues, read the [security policy](SECURITY.md).

## Before filing

- Check whether you are using the latest available version, or explain why you cannot upgrade.
- Check whether the [troubleshooting guide](../troubleshooting-en.md) already covers the issue.
- Prepare a minimal reproducer, test case, or complete Gradle command.
- Make sure the report does not include secrets, tokens, private repository URLs, or security exploit details.

## Required information

Please include:

- pf4boot-plugin version or commit.
- Affected component, such as `net.xdob.pf4boot`, `net.xdob.pf4boot-plugin`, the `pf4boot` task, `platformApi`, or publication tasks.
- Usage path, such as direct Gradle plugin usage, multi-project build, local `JavaExec`, Maven Central publishing, or Gradle Plugin Portal publishing.
- Expected behavior and actual behavior.
- Minimal reproduction.
- Full error logs, stack traces, dependency reports, or ZIP content differences.
- Operating system, JDK, Gradle version, and key dependency coordinates.

## Extra details for Gradle plugin issues

For dependency or packaging issues, include:

- `bundle`, `bundleOnly`, `embed`, and `platformApi` declarations from `dependencies`.
- Output from `./gradlew pf4bootDependencies`.
- Output from `./gradlew checkPluginRuntimeClasspath`.
- Entry list under the generated ZIP `lib/` directory.

For local runtime issues, describe:

- The `JavaExec` `classpath` configuration.
- Whether `configurations.pluginLocalRuntimeClasspath` is explicitly included.
- The full missing class name and first `NoClassDefFoundError` stack trace.

## Minimal reproducer suggestions

The best reproducer is a runnable Gradle TestKit test or a small multi-project build. If a complete project is not possible, provide at least `settings.gradle`, relevant `build.gradle` files, sample source code, and the command you ran.
