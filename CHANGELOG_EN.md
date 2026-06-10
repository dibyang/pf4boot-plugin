# Changelog

This project follows [Keep a Changelog](https://keepachangelog.com/) and uses
[Semantic Versioning](https://semver.org/).

All release notes are recorded here. Please update this file before each release.

## [Unreleased]

No pending entries yet.

## [1.4.0] - 2026-06-10

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

