## pf4boot-plugin

[English](README_EN.md) | [中文](README.md)

pf4boot-plugin 是用于 **pf4boot 插件开发** 的 Gradle 插件（当前支持 Gradle 7.x）。  
它帮助你在本地快速打包 pf4boot 插件，并把插件元数据与运行时依赖一并生成到产物中。

### 核心特性

- 支持通过 `plugin.properties` 文件或 `pf4bootPlugin` 扩展配置插件元数据；
- 提供 `pf4boot` 打包任务（生成 zip 包，默认在 `build/libs` 下）；
- 支持三类依赖分组：`bundle`、`bundleOnly`、`embed`；
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
    classpath "net.xdob.pf4boot:pf4boot-plugin:1.4.0-SNAPSHOT"
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

#### 方式一：plugin.properties 文件

在项目根目录创建 `plugin.properties`：

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

#### 方式二：Gradle DSL

在 `build.gradle` 中直接配置：

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

### 打包

```bash
./gradlew pf4boot
```

执行后会：

- 生成 `plugin.properties` 到 `build/generated/pf4boot/plugin.properties`
- 打包为 zip，放到 `build/libs`，可直接用于本地验证。

### 文档入口

- [使用示例（中文）](docs/usage-zh.md)｜[Usage (English)](docs/usage-en.md)
- [开发者手册（面向插件开发者）/ 使用手册（中文）](docs/developer-guide-zh.md)｜[Developer Guide / Usage Guide (English)](docs/developer-guide-en.md)
- [改进需求与落地规划（中文）](docs/improvement-plan-zh.md)｜[Improvement Plan (English)](docs/improvement-plan-en.md)

### 常见问题（本地开发场景）

- `plugin.id` / `plugin.class` 不能为空：缺失会构建失败并提示；
- 版本建议显式配置，避免 `unspecified` 被带进 zip；
- 推荐在本地先跑 `./gradlew pf4boot`，确认产物与配置正确后再接入发布流程。

### 改进规划（开发执行约定）

本项目后续优化采用“**可用性先行、最小改动优先**”原则。  
对应文档请见：[改进需求与落地规划（中文）](docs/improvement-plan-zh.md)。
