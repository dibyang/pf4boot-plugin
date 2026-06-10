## pf4boot-plugin 使用手册（中文）
### （面向使用该插件开发 pf4boot 插件的开发者）

[中文](developer-guide-zh.md) | [English](developer-guide-en.md)

本文档是完整使用说明，适合在本地开发阶段长期参考。

### 1. 快速回顾

- `pf4boot` 负责打包插件 zip（本地开发核心任务）
- `plugin.id` 与 `plugin.class` 是必填项
- 可通过 `plugin.properties` 或 `pf4bootPlugin` 扩展配置元数据

### 2. 配置方式

方式 A：使用 `plugin.properties`

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

方式 B：使用 Gradle 扩展

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

### 2.1 配置优先级（关键）

- 当 `plugin.properties` 与 `pf4bootPlugin` 扩展同字段同时存在时，以 `pf4bootPlugin` 扩展值为准。
- 建议把“默认值”放到 `plugin.properties`，把“本次构建覆盖值”放到扩展中。

```groovy
// plugin.properties
plugin.id=demo-plugin
plugin.provider=from-file

// build.gradle
pf4bootPlugin {
  provider = 'from-extension'
}
```

最终打包结果中 `plugin.provider=from-extension`。

### 3. 依赖打包规则

- `bundle`：打包到 zip，包含传递依赖
- `bundleOnly`：只打包声明依赖本身，不带传递依赖
- `embed`：打包到 zip 的 embed 区域，通常用于本地联调需要内嵌的库

示例：

```groovy
dependencies {
  bundle 'com.squareup.okio:okio:3.0.0'
  bundleOnly 'org.apache.commons:commons-lang3:3.12.0'
  embed project(':shared-lib')
}
```

本地文件依赖示例：

```groovy
dependencies {
  bundle files('libs/local-bundle.jar')
  bundleOnly files('libs/local-bundle-only.jar')
  embed files('libs/local-embed.jar')
}
```

### 4. 本地开发验收清单（建议）

发布前建议执行：

```text
./gradlew pf4boot
├─ 验证 build/generated/pf4boot/plugin.properties
├─ 验证 build/libs/<project>-<version>.zip
├─ 验证 zip 中存在 plugin.properties 与 lib/<project>-<version>.jar
├─ 验证 bundle/bundleOnly/embed 行为与本次需求一致
├─ 验证中文字段（description/requires/provider）不会乱码（UTF-8）
└─ 验证必须字段（id/class/version）不为空
```

可选：`./gradlew check` 做本地功能测试完整回归。

### 5. 本地开发流程

- 步骤 1：配置插件元数据并应用 `net.xdob.pf4boot-plugin`
- 步骤 2：执行 `./gradlew pf4boot`
- 步骤 3：检查 `build/generated/pf4boot/plugin.properties`
- 步骤 4：检查 `build/libs` 下 zip 是否包含期望依赖

### 6. 产物消费（本地联调）

```groovy
dependencies {
  implementation project(path: ':your-plugin', configuration: 'pf4bootElements')
}
```

### 7. 常见问题排查

- 构建提示缺少 `plugin.id` 或 `plugin.class` 时，先确认配置源中已填写
- 版本为空或异常值（如 `unspecified`）时，给定显式版本并重试；
- 本地看到乱码时检查属性文件编码是否为 UTF-8；
- 仍有差异时，比较以下两个数据源：
  - `build/generated/pf4boot/plugin.properties`
  - 构建日志中的 `effective plugin properties`
- 需要确认优先级时，以 `pf4bootPlugin` 扩展为准。

### 7. 继续阅读

- 快速上手： [docs/usage-zh.md](usage-zh.md)
- 改进需求与进度： [improvement-plan-zh.md](improvement-plan-zh.md)
