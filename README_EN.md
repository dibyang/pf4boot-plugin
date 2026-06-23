## pf4boot-plugin

[中文](README.md) | [English](README_EN.md)

pf4boot-plugin is a Gradle plugin for **pf4boot plugin development**, focused on Gradle 7.x.
It helps you quickly package pf4boot plugins locally and generates plugin metadata plus runtime dependencies into the output artifact.

### Core features

- Plugin metadata configured via `plugin.properties` or `pf4bootPlugin` extension;
- Provides the `pf4boot` packaging task (ZIP output, default under `build/libs`);
- Supports dependency groups: `bundle`, `bundleOnly`, `embed`, and `platformApi`;
- Provides `pluginLocalRuntimeClasspath`, making host platform APIs visible for local standalone runs without packaging them into the ZIP by default;
- Provides diagnostic tasks: `pf4bootDependencies`, `checkPluginRuntimeClasspath`;
- Provides pre-release validation tasks: `verifyReleaseReadiness`, `verifyReleaseTag`;
- Exposes `pf4bootElements` for consuming the generated ZIP as a dependency.

### Quick start

#### 1) Add plugin to your buildscript

```groovy
buildscript {
  repositories {
    mavenLocal()
    maven { url 'https://maven.aliyun.com/repository/public/' }
    mavenCentral()
  }
  dependencies {
    classpath "net.xdob.pf4boot:pf4boot-plugin:1.6.0"
  }
}
```

#### 2) Apply in normal pf4boot application module

```groovy
apply plugin: 'net.xdob.pf4boot'
```

#### 3) Apply in plugin project

```groovy
apply plugin: 'net.xdob.pf4boot-plugin'
```

### Configuration

Create `plugin.properties` in project root, or configure directly in `build.gradle`:

```groovy
pf4bootPlugin{
    id = 'plugin1'
    pluginClass = 'net.xdob.demo.plugin1.Plugin1Plugin'
    version = '0.1.0-SNAPSHOT'
    provider = 'yangzj'
    dependencies = ''
    description = ''
    requires = ''
    license = ''
}
```

### Local runtime platform APIs

```groovy
dependencies {
  platformApi "org.slf4j:slf4j-api:2.0.7"
}

tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

### Package

```bash
./gradlew pf4boot
```

This will:

- generate `build/generated/pf4boot/plugin.properties`
- create a ZIP package under `build/libs`
- include `plugin.properties` at the ZIP root and jars under `lib/`.

### Diagnostics and pre-release validation

```bash
./gradlew pf4bootDependencies
./gradlew checkPluginRuntimeClasspath
./gradlew verifyReleaseReadiness
./gradlew verifyReleaseTag
```

### Pre-release checklist

- Ensure `plugin.id`, `plugin.class`, and `plugin.version` are not empty;
- Use an explicit `plugin.version` and avoid `unspecified`;
- Run `./gradlew pf4boot` first, then `./gradlew check` when you want full local verification;
- Avoid packaging dependencies already provided by the host platform into `bundle`/`bundleOnly`/`embed` unless intentionally required.

### Docs

- [Usage Examples (English)](docs/usage-en.md) | [中文使用示例](docs/usage-zh.md)
- [Developer Guide / Usage Guide (English)](docs/developer-guide-en.md) | [中文开发者手册（使用手册）](docs/developer-guide-zh.md)
- [Troubleshooting Guide (English)](docs/troubleshooting-en.md) | [故障排查手册（中文）](docs/troubleshooting-zh.md)
- [Bug Reporting Guide (English)](docs/en/bug-reporting.md) | [问题反馈指南（中文）](docs/bug-reporting.md)
- [Contributing Guide (English)](docs/en/CONTRIBUTING.md) | [贡献指南（中文）](CONTRIBUTING.md)
- [Security Policy (English)](docs/en/SECURITY.md) | [安全报告（中文）](SECURITY.md)
- [Improvement Plan (English)](docs/improvement-plan-en.md) | [改进需求与落地规划（中文）](docs/improvement-plan-zh.md)
- [Platform Runtime Design (English)](docs/platform-runtime-design-en.md) | [平台运行时依赖与发布可靠性设计（中文）](docs/platform-runtime-design-zh.md)
- [Platform Runtime Implementation Plan (English)](docs/platform-runtime-implementation-plan-en.md) | [平台运行时依赖实施计划（中文）](docs/platform-runtime-implementation-plan-zh.md)

### Local development notes

- `plugin.id` and `plugin.class` are required: build fails with clear error if missing;
- It's recommended to set an explicit version to avoid packaging `unspecified`;
- For `NoClassDefFoundError`, first check `platformApi` and local `JavaExec` classpath;
- For duplicate platform dependencies, start with `pf4bootDependencies` and `checkPluginRuntimeClasspath`.
