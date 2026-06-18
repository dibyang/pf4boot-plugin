# pf4boot-plugin Next-stage Requirements Plan

[中文](pf4boot-plugin-next-requirements-zh.md) | [English](pf4boot-plugin-next-requirements-en.md)

> The Chinese document is the primary source. This English document is a synchronized summary for collaboration.

## Background

`pf4boot-plugin` 1.6.0 has completed the basic plugin development loop: plugin ZIP packaging, dependency groups (`bundle` / `bundleOnly` / `embed` / `platformApi`), `pluginLocalRuntimeClasspath`, dependency diagnostics, and release-readiness tasks.

As the pf4boot runtime evolves toward plugin repositories, hot replacement, deployment governance, and runtime smoke checks, this Gradle plugin should evolve from "can package a plugin" to "can produce governable, verifiable, publishable, and rollback-friendly plugin artifacts".

## Current Baseline

1. `pf4boot` generates plugin ZIPs.
2. `plugin.properties` can come from a source file or the `pf4bootPlugin {}` extension.
3. Dependency groups are explicit:
   - `bundle`: packages declared dependencies and transitives.
   - `bundleOnly`: packages only directly declared dependencies.
   - `embed`: currently reported as a separate group and still packaged by default.
   - `platformApi`: host-provided, compile/local-runtime visible, and not packaged by default.
4. `pluginLocalRuntimeClasspath` supports explicit local JavaExec classpath composition.
5. `pf4bootDependencies` and `checkPluginRuntimeClasspath` support diagnostics.
6. `verifyReleaseReadiness` and `verifyReleaseTag` support read-only release checks.
7. `pf4bootElements` lets other projects consume plugin ZIPs.

## Next-version Primary Goal: `platformApi` Propagation Across Library Projects

Detailed design: [platform-api-propagation-design-en.md](platform-api-propagation-design-en.md).

Real plugin projects often use platform APIs indirectly through non-plugin library projects:

```text
root
├─ plugin-apacheds
└─ apacheds-lib
```

If `apacheds-lib` uses `org.slf4j:slf4j-api`, that API should be provided by the host platform. It should be visible to the library's compile/test/local runtime, visible to the plugin's local runtime, but not packaged into the plugin ZIP.

Acceptance:

1. `apacheds-lib:compileJava` can import `org.slf4j.Logger`.
2. `apacheds-lib:compileTestJava` can import `org.slf4j.Logger`.
3. `apacheds-lib` `runtimeClasspath` / `testRuntimeClasspath` contain `slf4j-api`.
4. `plugin-demo` `pluginLocalRuntimeClasspath` contains `slf4j-api`.
5. `plugin-demo` ZIP contains `apacheds-lib.jar`.
6. `plugin-demo` ZIP does not contain `slf4j-api.jar`.
7. `.\gradlew.bat functionalTest` and `.\gradlew.bat check` pass.
8. `bundle` / `embed` recursively collect project dependency `platformApi`.
9. `bundleOnly` collects only direct project dependency `platformApi`, not recursively.
10. `platformApi project(':platform-api')` project jar is visible locally but not packaged into the plugin ZIP.

## Later Goals

1. P0: `platformApi` propagation across library projects, targeting `1.7.0`.
2. Plugin package verification and manifest generation.
3. Plugin repository index generation.
4. Multi-module plugin assembly support.
5. Reproducible plugin archives.
6. Plugin publishing integration.
7. Local development helpers and diagnostics.

## Non-goals

1. Do not change existing dependency group semantics.
2. Do not package `platformApi` by default.
3. Do not automatically mutate all user `JavaExec` tasks.
4. Do not implement pf4boot runtime loading or management API calls in the Gradle plugin.
5. Do not force every project to use plugin repositories.
6. Do not add runtime framework dependencies unrelated to Gradle plugin responsibilities.
