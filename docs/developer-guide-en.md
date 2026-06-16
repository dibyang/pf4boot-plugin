## pf4boot-plugin Usage Guide (English)
### (for developers building pf4boot plugins with this Gradle plugin)

[中文](developer-guide-zh.md) | [English](developer-guide-en.md)

This is the full reference guide for local development. For quick setup, read [Usage](usage-en.md) first.

### 1. Quick recap

- `pf4boot` is the ZIP packaging task used during local plugin development.
- `plugin.id` and `plugin.class` are required.
- Plugin metadata can come from `plugin.properties` or the `pf4bootPlugin` extension.
- `platformApi` is for APIs provided by the host but also needed for local plugin execution.
- Diagnostic tasks do not change packaging and are not wired into `check` by default.

### 2. Configuration options

Option A: `plugin.properties`

```properties
plugin.id=demo-plugin
plugin.class=com.demo.plugin.DemoPlugin
plugin.version=1.0.0
plugin.provider=your-team
plugin.dependencies=
plugin.description=
plugin.requires=
plugin.license=
```

Option B: Gradle extension

```groovy
pf4bootPlugin {
  id = 'demo-plugin'
  pluginClass = 'com.demo.plugin.DemoPlugin'
  version = '1.0.0'
  provider = 'your-team'
  dependencies = ''
  description = ''
  requires = ''
  license = ''
}
```

#### 2.1 Priority of configuration sources (important)

- If both `plugin.properties` and `pf4bootPlugin` define the same key, `pf4bootPlugin` takes precedence.
- Recommendation: keep stable defaults in `plugin.properties` and inject per-build overrides in the extension.

### 3. Dependency packaging rules

```groovy
dependencies {
  bundle 'com.squareup.okio:okio:3.0.0'
  bundleOnly 'org.apache.commons:commons-lang3:3.12.0'
  embed project(':shared-lib')
  platformApi 'org.slf4j:slf4j-api:2.0.7'
}
```

- `bundle`: packaged into the ZIP with transitive dependencies.
- `bundleOnly`: packages only directly declared dependencies.
- `embed`: reported separately; by default it is still treated as a packaged dependency and kept open for future strategy-specific behavior.
- `platformApi`: host-provided APIs that are visible for local runtime but not packaged into the ZIP by default.

### 4. Local runtime classpath

`platformApi` is not packaged into the ZIP, but it is included in `pluginLocalRuntimeClasspath`. Local `JavaExec` tasks should explicitly combine the full runtime classpath:

```groovy
tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

The plugin does not auto-adapt every `JavaExec` task. This avoids silently changing existing task runtime behavior.

### 5. Dependency report and runtime checks

```bash
./gradlew pf4bootDependencies
./gradlew checkPluginRuntimeClasspath
```

Duplicate dependency policy:

```groovy
pf4bootPlugin {
  duplicateDependencyPolicy = 'warn'
  checkRuntimeClasspathOnCheck = false
}
```

- `duplicateDependencyPolicy` allowed values: `warn`, `fail`, `ignore`. The default is `warn`.
- `checkRuntimeClasspathOnCheck` defaults to `false`. Set it to `true` to opt into `check` integration.

### 6. Pre-release validation

Recommended before release:

```powershell
.\gradlew.bat pf4boot
.\gradlew.bat check
.\gradlew.bat verifyReleaseReadiness
```

After tagging:

```powershell
.\gradlew.bat verifyReleaseTag
```

`verifyReleaseReadiness` checks version, changelog, README/Usage example versions, and ZIP content. `verifyReleaseTag` checks whether `v<version>` exists and points to current HEAD. Both tasks are read-only.

### 7. Local development checklist

```text
./gradlew pf4boot
├─ validate build/generated/pf4boot/plugin.properties
├─ verify build/libs/<project>-<version>.zip
├─ ensure zip contains plugin.properties and lib/<project>-<version>.jar
├─ verify bundle / bundleOnly / embed behavior matches the current need
├─ verify platformApi is visible locally but not packaged under zip lib/
├─ verify UTF-8 fields (description/requires/provider) are readable
└─ ensure required keys (id/class/version) are not empty
```

### 8. Local consumption (development only)

```groovy
dependencies {
  implementation project(path: ':your-plugin', configuration: 'pf4bootElements')
}
```

### 9. Troubleshooting entry point

Common issues are now in a dedicated document: [Troubleshooting Guide](troubleshooting-en.md). It covers:

- `NoClassDefFoundError`
- missing `plugin.properties`
- `plugin.version=unspecified`
- Windows UTF-8 / garbled Chinese text
- duplicate platform dependencies
- pre-release validation failures

### 10. Continue reading

- Quick start: [usage-en.md](usage-en.md)
- Troubleshooting: [troubleshooting-en.md](troubleshooting-en.md)
- Improvements and roadmap: [improvement-plan-en.md](improvement-plan-en.md)
- Platform runtime design: [platform-runtime-design-en.md](platform-runtime-design-en.md)