# pf4boot-plugin Platform Runtime Dependency Implementation Plan

[中文](platform-runtime-implementation-plan-zh.md) | [English](platform-runtime-implementation-plan-en.md)

> The Chinese document is the primary source. This English document is a synchronized copy. This plan tracks implementation of the [Platform Runtime Dependency and Release Reliability Design](platform-runtime-design-en.md).

## 1. Tracking Rules

- Discuss and confirm the design before implementing any phase.
- Every key implementation must have corresponding test code.
- A phase can be marked complete only after both scope and acceptance criteria are satisfied.
- If implementation changes the design, update the design document first, then update this plan.
- Do not introduce breaking defaults: do not change ZIP content by default, do not auto-modify all `JavaExec` tasks, and do not wire diagnostics into `check` by default.

## 2. Phase Overview

| Phase | Name | Status | Main deliverable |
| --- | --- | --- | --- |
| Phase 1 | Local runtime classpath | Pending | `pluginLocalRuntimeClasspath` and packaged dependency classification. |
| Phase 2 | Dependency report and diagnostics | Pending | `DependencyReporter`, `pf4bootDependencies`, `checkPluginRuntimeClasspath`. |
| Phase 3 | Release reliability | Pending | `verifyReleaseReadiness`, `verifyReleaseTag`. |
| Phase 4 | Documentation and troubleshooting | Pending | Troubleshooting docs and usage/developer guide updates. |
| Phase 5 | Bytecode-level diagnostics | To evaluate | Optional class constant-pool scanning and missing-class mapping. |

## 3. Phase 1: Local Runtime Classpath

Status: Pending

### Scope

- Add the `pluginLocalRuntimeClasspath` configuration.
- Define the full local run classpath as:

```gradle
sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
```

- Add packaged dependency classification that resolves `bundle` / `bundleOnly` / `embed` separately.
- Do not add a merged `pluginPackagedClasspath` configuration.
- Do not change default `pf4boot` ZIP content.
- Add functional tests proving platform APIs are local-runtime-visible but not packaged.

### Acceptance

- `platformApi("org.slf4j:slf4j-api:2.0.7")` resolves in `pluginLocalRuntimeClasspath`.
- `slf4j-api` is not present in ZIP `lib/`.
- `bundleOnly` remains non-transitive.
- JavaExec examples use `sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath`.
- `.\gradlew.bat functionalTest` passes.
- `.\gradlew.bat check` passes.

### Test Requirements

- Add `shouldExposePlatformApiInPluginLocalRuntimeClasspath`.
- Add `shouldNotPackagePlatformApiByDefault`.
- Add `shouldKeepBundleOnlyNonTransitiveWhenReportingPackagedDependencies`.

## 4. Phase 2: Dependency Report and Diagnostics

Status: Pending

### Scope

- Add `ResolvedArtifactInfo`.
- Add `DependencyReport`.
- Add `DependencyReporter`.
- Add the `pf4bootDependencies` task.
- Add the first version of `checkPluginRuntimeClasspath`.
- Phase 1 diagnostics use only Gradle resolution facts and do not infer bytecode-level missing dependencies.
- Duplicate dependencies warn by default; reserve `warn` / `fail` / `ignore` policies.
- Do not wire into `check` by default; reserve the `checkRuntimeClasspathOnCheck` opt-in switch.

### Acceptance

- Report distinguishes `bundle`, `bundleOnly`, `embed`, platform, and local runtime.
- Duplicate modules between packaged dependencies and platform dependencies are detected.
- Duplicate dependencies print warnings by default.
- Resolution failures include the configuration name and failed dependency.
- `checkPluginRuntimeClasspath` verifies declared platform APIs are present in the local runtime dependency configuration.
- It does not claim to detect implicit runtime needs caused by excludes.

### Test Requirements

- Add `shouldReportPackagedPlatformAndLocalRuntimeDependencies`.
- Add `shouldWarnWhenPackagedDependencyDuplicatesPlatformDependency`.
- Add `shouldFailRuntimeClasspathCheckWhenPlatformDependencyNotInLocalRuntime`.

## 5. Phase 3: Release Reliability

Status: Pending

### Scope

- Add `verifyReleaseReadiness`.
- Add `verifyReleaseTag`.
- `verifyReleaseReadiness` checks version, changelog, README, Usage, and ZIP content.
- `verifyReleaseTag` checks whether the tag exists and points to the current commit.
- Both tasks are read-only; they do not create tags, modify versions, or publish.

### Acceptance

- `verifyReleaseReadiness` fails when `version` contains `SNAPSHOT`.
- It fails when changelog does not contain the current version.
- It fails when README / Usage example versions do not match the current version.
- It fails when ZIP lacks `plugin.properties` or `lib/`.
- `verifyReleaseTag` fails when the tag is missing or does not point to HEAD.

### Test Requirements

- Add `shouldVerifyReleaseReadinessForCurrentVersion`.
- Add `shouldFailReleaseReadinessWhenVersionIsSnapshot`.
- Add `shouldFailReleaseTagWhenTagDoesNotPointToHead`.

## 6. Phase 4: Documentation and Troubleshooting

Status: Pending

### Scope

- Add `troubleshooting-zh.md`.
- Add `troubleshooting-en.md`.
- Update `usage-zh.md` / `usage-en.md`.
- Update `developer-guide-zh.md` / `developer-guide-en.md`.
- Keep `[中文](...) | [English](...)` at the top of all paired docs.

### Acceptance

- Docs cover `NoClassDefFoundError`.
- Docs cover missing `plugin.properties`.
- Docs cover `plugin.version=unspecified`.
- Docs cover Windows UTF-8.
- Docs cover duplicate platform dependencies.
- Chinese is primary; English content stays synchronized.

### Test Requirements

- Gradle examples in docs should be covered by functional tests where practical.
- Examples that cannot be automated must include manual verification commands.

## 7. Phase 5: Bytecode-Level Diagnostics

Status: To evaluate

### Scope

- Optionally scan jar/class constant pools.
- Map typical missing classes to modules, for example:

```text
org/slf4j/LoggerFactory -> org.slf4j:slf4j-api
```

- Output the jar/class that references the missing class.
- Keep this as an independent enhancement; it does not block phases 1-4.

### Acceptance

- Reports which jar/class references the missing class.
- Provides actionable suggestions for `org/slf4j/LoggerFactory`.
- False positives are controlled; if not guaranteed, downgrade to warning or a separate diagnostic task.

### Test Requirements

- Add a fixture jar or test class that references a missing class.
- Add `shouldReportClassReferenceForKnownMissingPlatformApi`.

## 8. Current Design Decisions

| Question | Decision |
| --- | --- |
| `embed` semantics | Keep it as a future strategy group; phase 1 does not distinguish it from normal packaging behavior except in reports. |
| Wiring into `check` | Do not wire by default; provide opt-in switch `checkRuntimeClasspathOnCheck`. |
| Duplicate dependencies | Warn by default; support `warn` / `fail` / `ignore`. |
| JavaExec adaptation | Do not auto-adapt all `JavaExec`; users explicitly use `sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath`. |
| platform API source | Phase 1 only supports explicit declaration in the current project; host project import is a phase 2 capability. |

## 9. Manual Acceptance Commands

```powershell
.\gradlew.bat functionalTest
.\gradlew.bat check
.\gradlew.bat pf4bootDependencies
.\gradlew.bat checkPluginRuntimeClasspath
.\gradlew.bat verifyReleaseReadiness
.\gradlew.bat verifyReleaseTag
```

Note: the last four tasks can be executed after their corresponding phases are implemented.

