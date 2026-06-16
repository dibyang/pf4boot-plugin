## pf4boot-plugin 使用手册（中文）
### （面向使用该插件开发 pf4boot 插件的开发者）

[中文](developer-guide-zh.md) | [English](developer-guide-en.md)

本文档是完整使用说明，适合在本地开发阶段长期参考。快速跑通请先看 [使用示例](usage-zh.md)。

### 1. 快速回顾

- `pf4boot` 负责打包插件 zip（本地开发核心任务）。
- `plugin.id` 与 `plugin.class` 是必填项。
- 可通过 `plugin.properties` 或 `pf4bootPlugin` 扩展配置元数据。
- `platformApi` 用于宿主提供但本地运行也需要可见的 API。
- 诊断任务默认不改变打包内容，不默认接入 `check`。

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

#### 2.1 配置优先级（关键）

- 当 `plugin.properties` 与 `pf4bootPlugin` 扩展同字段同时存在时，以 `pf4bootPlugin` 扩展值为准。
- 建议把“默认值”放到 `plugin.properties`，把“本次构建覆盖值”放到扩展中。

### 3. 依赖打包规则

```groovy
dependencies {
  bundle 'com.squareup.okio:okio:3.0.0'
  bundleOnly 'org.apache.commons:commons-lang3:3.12.0'
  embed project(':shared-lib')
  platformApi 'org.slf4j:slf4j-api:2.0.7'
}
```

- `bundle`：打包到 zip，包含传递依赖。
- `bundleOnly`：只打包声明依赖本身，不带传递依赖。
- `embed`：当前独立报告，默认仍作为打包依赖处理，保留未来策略扩展空间。
- `platformApi`：宿主平台提供的 API，本地运行可见，默认不进入 zip。

### 4. 本地运行 classpath

`platformApi` 不会进入 zip，但会进入 `pluginLocalRuntimeClasspath`。本地 `JavaExec` 应显式组合完整运行 classpath：

```groovy
tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

插件不会自动适配所有 `JavaExec`。这样做是为了避免悄悄改变用户已有任务的运行时行为。

### 5. 依赖报告与运行时检查

```bash
./gradlew pf4bootDependencies
./gradlew checkPluginRuntimeClasspath
```

重复依赖策略：

```groovy
pf4bootPlugin {
  duplicateDependencyPolicy = 'warn'
  checkRuntimeClasspathOnCheck = false
}
```

- `duplicateDependencyPolicy` 可选值：`warn`、`fail`、`ignore`。默认是 `warn`。
- `checkRuntimeClasspathOnCheck` 默认是 `false`。需要接入 `check` 时设置为 `true`。

### 6. 发布前验证

发布前建议执行：

```powershell
.\gradlew.bat pf4boot
.\gradlew.bat check
.\gradlew.bat verifyReleaseReadiness
```

打 tag 后执行：

```powershell
.\gradlew.bat verifyReleaseTag
```

`verifyReleaseReadiness` 检查版本、changelog、README/Usage 示例版本和 zip 内容。`verifyReleaseTag` 检查 `v<version>` 是否存在且指向当前提交。两个任务都是只读任务。

### 7. 本地开发验收清单

```text
./gradlew pf4boot
├─ 验证 build/generated/pf4boot/plugin.properties
├─ 验证 build/libs/<project>-<version>.zip
├─ 验证 zip 中存在 plugin.properties 与 lib/<project>-<version>.jar
├─ 验证 bundle/bundleOnly/embed 行为与本次需求一致
├─ 验证 platformApi 本地运行可见但不进入 zip lib/
├─ 验证中文字段（description/requires/provider）不会乱码（UTF-8）
└─ 验证必须字段（id/class/version）不为空
```

### 8. 产物消费（本地联调）

```groovy
dependencies {
  implementation project(path: ':your-plugin', configuration: 'pf4bootElements')
}
```

### 9. 常见问题排查入口

常见问题已拆到独立文档：[故障排查手册（中文）](troubleshooting-zh.md)。重点覆盖：

- `NoClassDefFoundError`
- `plugin.properties` 不存在
- `plugin.version=unspecified`
- Windows UTF-8 / 中文乱码
- 平台依赖重复
- 发布前验证失败

### 10. 继续阅读

- 快速上手：[usage-zh.md](usage-zh.md)
- 故障排查：[troubleshooting-zh.md](troubleshooting-zh.md)
- 改进需求与进度：[improvement-plan-zh.md](improvement-plan-zh.md)
- 平台运行时设计：[platform-runtime-design-zh.md](platform-runtime-design-zh.md)