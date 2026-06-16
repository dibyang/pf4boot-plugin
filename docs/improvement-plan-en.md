## pf4boot-plugin Improvement Requirements and Implementation Plan

[中文](improvement-plan-zh.md) | [English](improvement-plan-en.md)

This plan defines the next iteration focused on local developer experience for **Gradle 7.x**.

## 1. Scope

### In Scope

- Plugin behavior stability: explicit input/output modeling for the `pf4boot` task, incremental correctness, reproducible output.
- Local usability: UTF-8 property I/O, clearer validation errors, richer package logging.
- Documentation quality: keep Chinese and English guides aligned for usage flow.
- Test coverage: add deterministic and failure-path tests.
- Platform runtime dependency and release reliability: future implementation follows the [Platform Runtime Design](platform-runtime-design-en.md) and is tracked in the [Platform Runtime Implementation Plan](platform-runtime-implementation-plan-en.md).

### Out of Scope

- Publish/signing pipeline refactors (`publishing`, `release`, credentials strategy) are out of scope for this iteration.
- Dependency model redesign (new DSL syntax or major configuration redesign) is out of scope.

## 2. Requirements (accepted work items)

### P1 (must-have)

- [x] Make `pf4boot` task inputs/outputs explicit:
  - `plugin.properties` as input (if present)
  - extension fields as inputs: `id`, `pluginClass`, `version`, `provider`, `description`, `dependencies`, `requires`, `license`
  - generated ZIP artifact and path as outputs
- [x] Standardize UTF-8 read/write for plugin metadata.
- [x] Strengthen required validation:
  - fail fast when `plugin.id` / `plugin.class` are missing
  - reject invalid `plugin.version` values such as empty string or `unspecified`
- [x] Error messages must include: field name, current value, and remediation guidance.

### P2 (required in this iteration)

- [x] Improve task logging: zip path, resolved config summary, packaged file list.
- [x] Document `bundle`/`bundleOnly`/`embed` behavior and troubleshooting guidance.
- [x] Add a quick troubleshooting section in `developer-guide-*`.

### P3 (enhancement)

- [x] Add local functional coverage for:
  - only-file configuration
  - only-extension configuration
- [x] Add failure tests for:
  - missing `plugin.id`
  - missing `plugin.class`
  - `version=unspecified`

## 3. Implementation plan

### Phase 1: foundation stability

- Set metadata file I/O to UTF-8.
- Implement explicit task inputs/outputs for `pf4boot`.
- Add baseline verification for config-driven incremental behavior.

### Phase 2: diagnosability

- Add validation checks and actionable errors.
- Add package summary logging output.

### Phase 3: closure

- Add functional and negative tests.
- Align `usage-*` and `developer-guide-*` docs in both languages.
- Final local acceptance walkthrough.

## 4. Acceptance criteria

- [x] `./gradlew pf4boot` reacts predictably when `plugin.properties` or `pf4bootPlugin` values change.
- [x] Generated `build/generated/pf4boot/plugin.properties` matches effective config and is packaged correctly.
- [x] Missing `plugin.id` / `plugin.class` fails with clear actionable errors.
- [x] `plugin.version=unspecified` is rejected with explicit guidance.
- [x] Tests cover `bundle` / `bundleOnly` / `embed` usage and failure scenarios.
- [x] Chinese and English docs reflect the same features, sequence, and acceptance criteria.

### 5. Pre-release documentation checklist (this release)

- [x] README and Usage/Developer Guide content are aligned in Chinese and English.
- [x] Usage docs include archive verification steps and dependency packaging examples.
- [x] Developer Guide includes configuration precedence and local release-checklist.
- [x] Troubleshooting guidance is added for common local development failures.
