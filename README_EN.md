## pf4boot-plugin

[中文](README.md) | [English](README_EN.md)

pf4boot-plugin is a Gradle plugin for **pf4boot plugin development**, focused on Gradle 7.x.
It helps you quickly package pf4boot plugins locally and generates plugin metadata plus runtime dependencies into the output artifact.

### Core features

- Plugin metadata configured via `plugin.properties` or `pf4bootPlugin` extension;
- Provides `pf4boot` packaging task (ZIP output, default under `build/libs`);
- Supports dependency groups: `bundle`, `bundleOnly`, `embed`;
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
    classpath "net.xdob.pf4boot:pf4boot-plugin:1.4.1"
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

### Configuration (two ways)

#### Option A: `plugin.properties`

Create `plugin.properties` in project root:

```properties
plugin.id=plugin1
plugin.class=net.xdob.demo.plugin1.Plugin1Plugin
plugin.version=0.1.0-SNAPSHOT
plugin.provider=yangzj
plugin.dependencies=
plugin.description=
plugin.requires=
plugin.license=
```

#### Option B: Gradle DSL

In `build.gradle`:

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

### Package

```bash
./gradlew pf4boot
```

This will:

- generate `build/generated/pf4boot/plugin.properties`
- create a ZIP package under `build/libs`
- include plugin jar and configured dependency configurations for local verification.
- include `plugin.properties` at the ZIP root, generated from the build step, and jars under `lib/`.

### Pre-release checklist

- Ensure `plugin.id`, `plugin.class`, and `plugin.version` are not empty;
- Use an explicit `plugin.version` and avoid `unspecified`;
- Run `./gradlew pf4boot` first, then `./gradlew check` when you want full local verification;
- Avoid unintentionally adding undeclared `bundle`/`bundleOnly`/`embed` dependencies to keep artifacts reproducible.

### Docs

- [Usage Examples (English)](docs/usage-en.md) | [中文使用示例](docs/usage-zh.md)
- [Developer Guide / Usage Guide (English)](docs/developer-guide-en.md) | [中文开发者手册（使用手册）](docs/developer-guide-zh.md)
- [Improvement Plan (English)](docs/improvement-plan-en.md) | [改进需求与落地规划（中文）](docs/improvement-plan-zh.md)
- [Platform Runtime Design (English)](docs/platform-runtime-design-en.md) | [平台运行时依赖与发布可靠性设计（中文）](docs/platform-runtime-design-zh.md)
- [Platform Runtime Implementation Plan (English)](docs/platform-runtime-implementation-plan-en.md) | [平台运行时依赖实施计划（中文）](docs/platform-runtime-implementation-plan-zh.md)

### Local development notes

- `plugin.id` and `plugin.class` are required: build fails with clear error if missing;
- It's recommended to set an explicit version to avoid packaging `unspecified`;
- Run `./gradlew pf4boot` first locally to validate output before integrating with CI or releases.

### Improvement planning

The plugin improvement roadmap follows **local developer productivity first** and minimal-risk incremental changes.  
See the bilingual execution plan here: [Improvement Plan (English)](docs/improvement-plan-en.md).
