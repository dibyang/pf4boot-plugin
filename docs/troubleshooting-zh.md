# pf4boot-plugin 故障排查手册（中文）

[中文](troubleshooting-zh.md) | [English](troubleshooting-en.md)

> 中文文档为主文档，英文文档为同步副本。本文聚焦使用 pf4boot-plugin 进行本地开发、打包、运行联调和发布前验证时的常见问题。

## 1. 快速定位入口

优先按下面顺序排查：

1. 执行 `./gradlew pf4boot`，确认能生成 zip。
2. 执行 `./gradlew pf4bootDependencies`，查看打包依赖、平台依赖、本地运行依赖的分类。
3. 执行 `./gradlew checkPluginRuntimeClasspath`，检查平台依赖重复、平台 API 本地运行可见性和已知缺失类引用。
4. 发布前执行 `./gradlew verifyReleaseReadiness`。
5. 打 tag 后执行 `./gradlew verifyReleaseTag`。

Windows PowerShell 下使用：

```powershell
.\gradlew.bat pf4boot
.\gradlew.bat pf4bootDependencies
.\gradlew.bat checkPluginRuntimeClasspath
.\gradlew.bat verifyReleaseReadiness
.\gradlew.bat verifyReleaseTag
```

## 2. `NoClassDefFoundError`

### 现象

本地以 `JavaExec` 或宿主应用启动插件时，出现类似错误：

```text
java.lang.NoClassDefFoundError: org/slf4j/LoggerFactory
```

### 常见原因

- 依赖被声明为 `platformApi`，不会进入插件 zip，但本地单独运行插件时没有把平台 API 加入运行 classpath。
- 依赖被错误放到 `compileOnly` 或被上游排除，导致运行时不可见。
- 依赖应该由宿主提供，但当前本地联调没有启动完整宿主。

### 推荐修复

在插件项目中显式声明平台 API：

```groovy
dependencies {
  platformApi "org.slf4j:slf4j-api:2.0.7"
}
```

本地 `JavaExec` 使用完整运行 classpath：

```groovy
tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

然后执行：

```powershell
.\gradlew.bat checkPluginRuntimeClasspath
```

### 设计边界

`pluginLocalRuntimeClasspath` 只解决“本地开发运行可见性”，不会改变 zip 默认打包内容。平台 API 默认仍不进入 `lib/`，避免插件包和宿主平台重复携带同一依赖。

## 3. `plugin.properties` 不存在

### 现象

Gradle 7 报告 zip 任务输入文件不存在，类似：

```text
Type 'org.gradle.api.tasks.bundling.Zip' property '$1' specifies file '.../plugin.properties' which doesn't exist.
```

### 常见原因

- 项目没有根目录 `plugin.properties`，但旧实现或旧版本插件把它声明成了 zip 输入。
- 只使用 `pf4bootPlugin` 扩展配置元数据，但任务输入仍引用了不存在的文件。

### 推荐修复

升级到包含该修复的版本后，可以只使用扩展配置：

```groovy
pf4bootPlugin {
  id = 'demo-plugin'
  pluginClass = 'com.demo.plugin.DemoPlugin'
  version = '1.0.0'
  provider = 'your-team'
}
```

打包时插件会生成：

```text
build/generated/pf4boot/plugin.properties
```

zip 根目录中的 `plugin.properties` 来自该生成文件。

## 4. `plugin.version=unspecified`

### 现象

生成的 `plugin.properties` 或 zip 文件中出现：

```properties
plugin.version=unspecified
```

### 常见原因

- Gradle 项目没有显式设置 `version`。
- `plugin.properties` 和 `pf4bootPlugin.version` 都没有提供插件版本。

### 推荐修复

优先在插件元数据中声明明确版本：

```groovy
pf4bootPlugin {
  version = '1.0.0'
}
```

发布前执行：

```powershell
.\gradlew.bat verifyReleaseReadiness
```

该任务会阻止 `SNAPSHOT`、空版本、`unspecified` 等不适合正式发布的版本进入发布流程。

## 5. Windows 中文乱码或编码异常

### 现象

- `plugin.description`、`plugin.requires`、`plugin.provider` 中的中文在产物或日志中乱码。
- 功能测试、源码注释或 Markdown 文档出现不可读字符。

### 推荐修复

- 所有 Markdown、properties、Java 源码使用 UTF-8 保存。
- Java 编译任务应使用 UTF-8。
- PowerShell 读写中文文档时显式指定 UTF-8，不使用默认 ANSI/GBK 编码。

示例：

```powershell
Get-Content -Path README.md -Encoding UTF8
```

## 6. 平台依赖重复

### 现象

`pf4bootDependencies` 或 `checkPluginRuntimeClasspath` 提示某个 module 同时出现在平台依赖和插件打包依赖中。

### 常见原因

- 依赖既声明在 `platformApi`，又声明在 `bundle` / `bundleOnly` / `embed`。
- 插件误把宿主已经提供的 API 打进了 zip。

### 推荐修复

如果依赖由宿主平台提供，保留 `platformApi`，移除打包分组中的重复声明：

```groovy
dependencies {
  platformApi "org.slf4j:slf4j-api:2.0.7"
}
```

如果依赖必须随插件包发布，则不要声明为 `platformApi`，改用 `bundle` 或 `bundleOnly`。

默认策略是 warning。需要在本地或 CI 中阻止重复依赖时，可以配置：

```groovy
pf4bootPlugin {
  duplicateDependencyPolicy = 'fail'
}
```

可选值：`warn`、`fail`、`ignore`。

## 7. 发布前验证失败

### `verifyReleaseReadiness` 失败

该任务检查：

- 当前版本不是 `SNAPSHOT`、空值或 `unspecified`。
- `CHANGELOG.md` / `CHANGELOG_EN.md` 包含当前版本。
- README 与 Usage 示例版本和当前版本一致。
- `pf4boot` zip 中包含根目录 `plugin.properties` 和 `lib/` 内容。

### `verifyReleaseTag` 失败

该任务检查：

- 当前仓库存在 `v<version>` tag。
- tag 指向当前 `HEAD`。

该任务只读，不创建 tag，也不修改版本。

## 8. 继续阅读

- 快速使用：[usage-zh.md](usage-zh.md)
- 开发者手册：[developer-guide-zh.md](developer-guide-zh.md)
- 平台运行时设计：[platform-runtime-design-zh.md](platform-runtime-design-zh.md)
- 平台运行时实施计划：[platform-runtime-implementation-plan-zh.md](platform-runtime-implementation-plan-zh.md)