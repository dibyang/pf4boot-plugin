# pf4boot-plugin Troubleshooting Guide (English)

[中文](troubleshooting-zh.md) | [English](troubleshooting-en.md)

> The Chinese document is the primary source. This English document is a synchronized copy. This guide focuses on common issues during local pf4boot plugin development, packaging, local runtime checks, and pre-release validation.

## 1. Quick Diagnosis Entry Points

Use this order first:

1. Run `./gradlew pf4boot` and confirm the ZIP is generated.
2. Run `./gradlew pf4bootDependencies` to inspect packaged, platform, and local runtime dependency groups.
3. Run `./gradlew checkPluginRuntimeClasspath` to check duplicate platform dependencies, platform API local-runtime visibility, and known missing class references.
4. Before release, run `./gradlew verifyReleaseReadiness`.
5. After tagging, run `./gradlew verifyReleaseTag`.

On Windows PowerShell:

```powershell
.\gradlew.bat pf4boot
.\gradlew.bat pf4bootDependencies
.\gradlew.bat checkPluginRuntimeClasspath
.\gradlew.bat verifyReleaseReadiness
.\gradlew.bat verifyReleaseTag
```

## 2. `NoClassDefFoundError`

A local `JavaExec` task or host application may fail with:

```text
java.lang.NoClassDefFoundError: org/slf4j/LoggerFactory
```

Common causes:

- A dependency is declared as `platformApi`, so it is not packaged into the plugin ZIP, but local standalone execution did not add platform APIs to the runtime classpath.
- A dependency is incorrectly declared as `compileOnly` or excluded upstream.
- The dependency should be provided by the host, but the local run does not start the full host.

Recommended fix. `platformApi` means compile-visible, local-runtime-visible, and not packaged.

```groovy
dependencies {
  platformApi "org.slf4j:slf4j-api:2.0.7"
}

tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

Then run:

```powershell
.\gradlew.bat checkPluginRuntimeClasspath
```

`pluginLocalRuntimeClasspath` only makes platform APIs visible for local development runs. It does not change default ZIP packaging.

Do not make plugin projects depend back on an `app-run` project just to obtain `slf4j-api`, because `app-run` usually consumes plugin ZIPs and reverse dependencies can create build cycles.

If the missing class is used by a packaged library project such as `apacheds-lib`, apply `net.xdob.pf4boot` in that library and declare `platformApi` there. When the plugin packages the library through `bundle project(':apacheds-lib')`, the library platform APIs are added to plugin local runtime but not packaged into the ZIP.

If the library project chain contains deeper project dependencies, `bundle` and `embed` recursively propagate `platformApi` from those projects. `bundleOnly` only handles directly declared projects. If local execution still misses a platform API from a transitive project, use `bundle` / `embed`, or explicitly declare the required `platformApi` in the plugin project.

## 3. Missing `plugin.properties`

Gradle 7 may report:

```text
Type 'org.gradle.api.tasks.bundling.Zip' property '$1' specifies file '.../plugin.properties' which doesn't exist.
```

Recommended fix after upgrading to a fixed version:

```groovy
pf4bootPlugin {
  id = 'demo-plugin'
  pluginClass = 'com.demo.plugin.DemoPlugin'
  version = '1.0.0'
  provider = 'your-team'
}
```

The plugin generates:

```text
build/generated/pf4boot/plugin.properties
```

The ZIP root `plugin.properties` comes from that generated file.

## 4. `plugin.version=unspecified`

Generated metadata may contain:

```properties
plugin.version=unspecified
```

Prefer declaring an explicit metadata version:

```groovy
pf4bootPlugin {
  version = '1.0.0'
}
```

Before release, run:

```powershell
.\gradlew.bat verifyReleaseReadiness
```

This task blocks `SNAPSHOT`, empty, and `unspecified` versions from release readiness.

## 5. Windows UTF-8 or Garbled Chinese Text

Symptoms:

- Chinese text in `plugin.description`, `plugin.requires`, or `plugin.provider` is garbled in artifacts or logs.
- Functional tests, Java comments, or Markdown documents contain unreadable characters.

Recommended fix:

- Save Markdown, properties, and Java source files as UTF-8.
- Configure Java compilation to use UTF-8.
- When reading or writing Chinese documents in PowerShell, specify UTF-8 explicitly.

Example:

```powershell
Get-Content -Path README.md -Encoding UTF8
```

## 6. Duplicate Platform Dependencies

`pf4bootDependencies` or `checkPluginRuntimeClasspath` may report a module that appears in both platform dependencies and plugin packaged dependencies.

If the dependency is provided by the host platform, keep `platformApi` and remove duplicate packaged declarations:

```groovy
dependencies {
  platformApi "org.slf4j:slf4j-api:2.0.7"
}
```

If the dependency must ship with the plugin ZIP, do not declare it as `platformApi`; use `bundle` or `bundleOnly` instead.

The default policy is warning. To fail local or CI checks on duplicates:

```groovy
pf4bootPlugin {
  duplicateDependencyPolicy = 'fail'
}
```

Allowed values: `warn`, `fail`, `ignore`.

## 7. Pre-release Validation Failures

`verifyReleaseReadiness` checks:

- Current version is not `SNAPSHOT`, empty, or `unspecified`.
- `CHANGELOG.md` / `CHANGELOG_EN.md` contain the current version.
- README and Usage examples match the current version.
- The `pf4boot` ZIP contains root `plugin.properties` and `lib/` content.

`verifyReleaseTag` checks:

- The repository has a `v<version>` tag.
- The tag points to current `HEAD`.

The task is read-only. It does not create tags or modify versions.

## 8. Continue Reading

- Quick start: [usage-en.md](usage-en.md)
- Developer guide: [developer-guide-en.md](developer-guide-en.md)
- Platform runtime design: [platform-runtime-design-en.md](platform-runtime-design-en.md)
- Platform runtime implementation plan: [platform-runtime-implementation-plan-en.md](platform-runtime-implementation-plan-en.md)
