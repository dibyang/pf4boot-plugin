## pf4boot-plugin

[中文](README.md) | [English](README_EN.md)

pf4boot-plugin 是用于 **pf4boot 插件开发** 的 Gradle 插件（当前支持 Gradle 7.x）。  
它帮助你在本地快速打包 pf4boot 插件，并把插件元数据与运行时依赖一并生成到产物中。

### 核心特性

- 支持通过 `plugin.properties` 文件或 `pf4bootPlugin` 扩展配置插件元数据；
- 提供 `pf4boot` 打包任务（生成 zip 包，默认在 `build/libs` 下）；
- 支持四类依赖分组：`bundle`、`bundleOnly`、`embed`、`platformApi`；
- 提供 `pluginLocalRuntimeClasspath`，让宿主平台 API 在本地单独运行时可见但默认不进入 zip；
- 提供依赖诊断任务：`pf4bootDependencies`、`checkPluginRuntimeClasspath`；
- 提供发布前验证任务：`verifyReleaseReadiness`、`verifyReleaseTag`；
- 提供 `pf4bootElements` 供其他项目以 zip 依赖方式消费。

### 快速使用

#### 1) 在项目中引入插件

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

#### 2) 应用插件（任一 Gradle 项目）

```groovy
apply plugin: 'net.xdob.pf4boot'
```

#### 3) 应用 pf4boot 插件开发插件（插件项目）

```groovy
apply plugin: 'net.xdob.pf4boot-plugin'
```

### 配置方式（两种）

在项目根目录创建 `plugin.properties`，或在 `build.gradle` 中直接配置：

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

### 本地运行平台 API

```groovy
dependencies {
  platformApi "org.slf4j:slf4j-api:2.0.7"
}

tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

### 打包

```bash
./gradlew pf4boot
```

执行后会：

- 生成 `plugin.properties` 到 `build/generated/pf4boot/plugin.properties`
- 打包为 zip，放到 `build/libs`，可直接用于本地验证。
- zip 内会包含 `plugin.properties`（由上一步生成文件）以及 `lib/` 下的 jar 产物。

### 诊断与发布前验证

```bash
./gradlew pf4bootDependencies
./gradlew checkPluginRuntimeClasspath
./gradlew verifyReleaseReadiness
./gradlew verifyReleaseTag
```

### 发布前建议

- 先确认 `plugin.id`、`plugin.class`、`plugin.version` 存在且不为空；
- 确认 `plugin.version` 为显式版本，不要使用 `unspecified`；
- 先在本地运行一次 `./gradlew pf4boot`，再按需执行 `./gradlew check` 做全链路验证；
- 如无特殊需求，不建议把宿主平台已经提供的依赖重复放入 `bundle/bundleOnly/embed`。

### 文档入口

- [使用示例（中文）](docs/usage-zh.md)｜[Usage (English)](docs/usage-en.md)
- [开发者手册（面向插件开发者）/ 使用手册（中文）](docs/developer-guide-zh.md)｜[Developer Guide / Usage Guide (English)](docs/developer-guide-en.md)
- [故障排查手册（中文）](docs/troubleshooting-zh.md)｜[Troubleshooting Guide (English)](docs/troubleshooting-en.md)
- [问题反馈指南（中文）](docs/bug-reporting.md)｜[Bug Reporting Guide (English)](docs/en/bug-reporting.md)
- [贡献指南（中文）](CONTRIBUTING.md)｜[Contributing Guide (English)](docs/en/CONTRIBUTING.md)
- [安全报告（中文）](SECURITY.md)｜[Security Policy (English)](docs/en/SECURITY.md)
- [改进需求与落地规划（中文）](docs/improvement-plan-zh.md)｜[Improvement Plan (English)](docs/improvement-plan-en.md)
- [平台运行时依赖与发布可靠性设计（中文）](docs/platform-runtime-design-zh.md)｜[Platform Runtime Design (English)](docs/platform-runtime-design-en.md)
- [平台运行时依赖实施计划（中文）](docs/platform-runtime-implementation-plan-zh.md)｜[Platform Runtime Implementation Plan (English)](docs/platform-runtime-implementation-plan-en.md)

### 常见问题（本地开发场景）

- `plugin.id` / `plugin.class` 不能为空：缺失会构建失败并提示；
- 版本建议显式配置，避免 `unspecified` 被带进 zip；
- `NoClassDefFoundError` 优先检查 `platformApi` 和本地 `JavaExec` classpath；
- 平台依赖重复时，用 `pf4bootDependencies` 和 `checkPluginRuntimeClasspath` 先定位。
