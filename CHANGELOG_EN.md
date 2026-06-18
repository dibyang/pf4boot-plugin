# Changelog

[中文](CHANGELOG.md) | [English](CHANGELOG_EN.md)

This project follows [Keep a Changelog](https://keepachangelog.com/) and uses
[Semantic Versioning](https://semver.org/).

All release notes are recorded here. Please update this file before each release.

## [Unreleased]

No pending entries yet.

## [1.7.0] - 2026-06-18

### Added
- Added tests for `bundle` / `embed` / `bundleOnly` propagation of packaged library `platformApi`, covering recursive propagation, non-recursive boundaries, and ZIP exclusion.

### Changed
- `bundle` and `embed` now recursively collect `platformApi` dependencies from packaged project chains, adding them to plugin local runtime but not to the plugin ZIP.
- `bundleOnly` only collects `platformApi` from directly declared projects and does not recursively collect transitive project `platformApi`, preserving its first-level dependency semantics.

## [1.6.0] - 2026-06-18

### Added
- Strengthened and tested the `platformApi` contract: compile-visible, local-runtime-visible, and not packaged.
- `platformApi` in non-plugin library projects is now visible to library compilation, tests, and local runs; when the library is packaged by a plugin, its platform APIs are added to plugin local runtime but not to the plugin ZIP.

### Changed
- Documented that plugin projects should not depend back on an `app-run` project that packages plugins, avoiding build cycles.

## [1.5.0] - 2026-06-16

### Added
- Added `platformApi` local runtime support: platform APIs are visible through `pluginLocalRuntimeClasspath` for local `JavaExec` runs but are not packaged into the plugin ZIP by default.
- Added dependency report and diagnostic tasks: `pf4bootDependencies`, `checkPluginRuntimeClasspath`.
- Added `duplicateDependencyPolicy`, supporting `warn` / `fail` / `ignore`, defaulting to `warn`.
- Added `checkRuntimeClasspathOnCheck` to optionally wire runtime checks into `check`.
- Added pre-release validation tasks: `verifyReleaseReadiness`, `verifyReleaseTag`.
- Added minimal bytecode reference diagnostics for known missing classes, such as mapping `org/slf4j/LoggerFactory` to the suggested dependency.
- Added bilingual troubleshooting guides and synchronized README, Usage, Developer Guide, and platform runtime implementation plan updates.

## [1.4.1] - 2026-06-11

### Fixed
- Fixed Gradle 7 task input validation failure when `pf4bootPlugin` extension configuration is used without a root `plugin.properties` file.
- Configured Java compilation to use UTF-8 consistently, avoiding Windows default-encoding issues in Chinese source comments and functional tests.

## [1.4.0] - 2026-06-11

### Added
- Added bilingual changelogs: `CHANGELOG.md` and `CHANGELOG_EN.md`.
- Added pre-release documentation checks, including explicit `check` vs `test` scope.
- Expanded Usage and Developer Guide documentation:
  - Added dependency group explanation (`bundle` / `bundleOnly` / `embed`).
  - Added local file dependency example with `files('libs/...')`.
  - Added artifact validation flow for `build/libs/<project>-<version>.zip`.
  - Added extension precedence details, release checklists, and troubleshooting guidance.

### Fixed
- Fixed duplicate section numbering in Developer Guide for local documentation navigation and references.
- Added pre-release documentation planning/checklist, improving local development consistency.
