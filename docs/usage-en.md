## pf4boot-plugin Usage (English)

[中文](usage-zh.md) | [English](usage-en.md)

This is a 5-minute quick start guide to get packaging working and to identify the right tasks for local runtime checks, dependency diagnostics, and pre-release validation.

### 1) Apply the plugin

```groovy
buildscript {
  repositories {
    mavenLocal()
    maven { url 'https://maven.aliyun.com/repository/public/' }
    mavenCentral()
  }
  dependencies {
    classpath "net.xdob.pf4boot:pf4boot-plugin:1.4.1"
  }
}

apply plugin: 'java'
apply plugin: 'net.xdob.pf4boot-plugin'
```

### 2) Configure plugin metadata

```properties
plugin.id=demo-plugin
plugin.class=com.demo.plugin.DemoPlugin
plugin.version=1.0.0
plugin.provider=your-team
```

Or:

```groovy
pf4bootPlugin {
  id = 'demo-plugin'
  pluginClass = 'com.demo.plugin.DemoPlugin'
  version = '1.0.0'
  provider = 'your-team'
}
```

### 3) Build

```bash
./gradlew pf4boot
```

Built artifact:

```text
build/libs/<project>-<version>.zip
```

The ZIP should contain:

- `plugin.properties` at the ZIP root
- `lib/<project>-<version>.jar`
- dependency jars under `lib/` from `bundle` / `bundleOnly` / `embed`

On Windows PowerShell:

```powershell
Expand-Archive -Path build/libs/<project>-<version>.zip -DestinationPath build/verify -Force
Get-ChildItem build/verify
```

### 4) Dependency groups

```groovy
dependencies {
  bundle "com.squareup.okio:okio:3.0.0"
  bundleOnly "org.apache.commons:commons-lang3:3.12.0"
  embed project(':shared-lib')
  platformApi "org.slf4j:slf4j-api:2.0.7"
}
```

- `bundle`: packages dependency and transitive dependencies.
- `bundleOnly`: packages only directly declared dependencies.
- `embed`: reported separately; by default it is still treated as a packaged dependency.
- `platformApi`: host-provided APIs needed for local runtime, not packaged into the ZIP by default.

### 5) Local JavaExec runtime

```groovy
tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

The plugin does not auto-modify all `JavaExec` tasks. Users explicitly reference `pluginLocalRuntimeClasspath`.

### 6) Dependency diagnostics

```bash
./gradlew pf4bootDependencies
./gradlew checkPluginRuntimeClasspath
```

Duplicate dependencies warn by default. To fail the check:

```groovy
pf4bootPlugin {
  duplicateDependencyPolicy = 'fail'
}
```

To wire runtime checks into `check`:

```groovy
pf4bootPlugin {
  checkRuntimeClasspathOnCheck = true
}
```

### 7) Pre-release validation tasks

```bash
./gradlew verifyReleaseReadiness
./gradlew verifyReleaseTag
```

Both tasks are read-only. They do not create tags, modify versions, or publish artifacts.

### 8) Local pre-release validation checklist

```text
./gradlew pf4boot
├─ build/libs/<project>-<version>.zip
├─ build/generated/pf4boot/plugin.properties
├─ plugin.id / plugin.class / plugin.version are as expected
├─ UTF-8 text fields (description/requires/provider) are not garbled
├─ bundle / bundleOnly / embed composition is expected
└─ platformApi dependencies are visible locally but not packaged under zip lib/
```

If a build fails, start with the [Troubleshooting Guide](troubleshooting-en.md).

### Next step

- Full configuration and development flow: [Developer Guide](developer-guide-en.md)
- Common issue diagnosis: [Troubleshooting Guide](troubleshooting-en.md)