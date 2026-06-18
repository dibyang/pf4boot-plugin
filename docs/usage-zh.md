## pf4boot-plugin 使用示例（中文）

[中文](usage-zh.md) | [English](usage-en.md)

这是「5 分钟上手」文档，目标是帮助你快速跑通最小构建，并知道本地运行、依赖诊断和发布前验证该用哪些任务。

### 1) 先引入插件

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

apply plugin: 'java'
apply plugin: 'net.xdob.pf4boot-plugin'
```

### 2) 配置插件元信息（任选一种）

```properties
plugin.id=demo-plugin
plugin.class=com.demo.plugin.DemoPlugin
plugin.version=1.0.0
plugin.provider=your-team
```

或：

```groovy
pf4bootPlugin {
  id = 'demo-plugin'
  pluginClass = 'com.demo.plugin.DemoPlugin'
  version = '1.0.0'
  provider = 'your-team'
}
```

### 3) 运行打包

```bash
./gradlew pf4boot
```

输出位置：

```text
build/libs/<project>-<version>.zip
```

zip 中应包含：

- `plugin.properties`（在 zip 根目录）
- `lib/<project>-<version>.jar`
- `lib/` 下的 `bundle` / `bundleOnly` / `embed` 相关依赖 jar

Windows PowerShell 快速确认：

```powershell
Expand-Archive -Path build/libs/<project>-<version>.zip -DestinationPath build/verify -Force
Get-ChildItem build/verify
```

### 4) 依赖分组

```groovy
dependencies {
  bundle "com.squareup.okio:okio:3.0.0"
  bundleOnly "org.apache.commons:commons-lang3:3.12.0"
  embed project(':shared-lib')
  platformApi "org.slf4j:slf4j-api:2.0.7"
}
```

- `bundle`：打包依赖及其传递依赖。
- `bundleOnly`：只打包声明的第一层依赖。
- `embed`：当前作为独立分组报告，默认仍按打包依赖处理。
- `platformApi`：宿主平台提供的 API，编译可见、本地运行可见，默认不进入 zip。

当插件打包项目依赖时，`bundle` 与 `embed` 会递归收集项目链路上的 `platformApi` 到 `pluginLocalRuntimeClasspath`，但不会把这些平台 API 打进 zip。`bundleOnly` 只收集直接声明项目的 `platformApi`，不递归收集传递项目的 `platformApi`。

不要为了使用宿主已经提供的 `slf4j-api` 而在插件项目中声明 `implementation`、`bundle` 或 `embed`，否则依赖可能被打进插件包。也不建议插件项目反向依赖包含插件包的 `app-run`，这容易形成构建循环。

### 5) 本地 JavaExec 运行

```groovy
tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

插件不会自动修改所有 `JavaExec`，需要用户显式引用 `pluginLocalRuntimeClasspath`。

### 5.1 非插件库项目中的 `platformApi`

如果插件依赖一个库项目，平台 API 应该声明在真正使用它的库项目里：

```groovy
// apacheds-lib/build.gradle
plugins {
  id 'java-library'
  id 'net.xdob.pf4boot'
}

dependencies {
  platformApi "org.slf4j:slf4j-api:${slf4j_version}"
}
```

插件项目只打包该库：

```groovy
// plugin-apacheds/build.gradle
dependencies {
  bundle project(':apacheds-lib')
}
```

这样 `apacheds-lib` 编译、测试、本地运行可见 `slf4j-api`；插件本地运行也可见；但插件 zip 不会包含 `slf4j-api.jar`。

### 6) 依赖诊断任务

```bash
./gradlew pf4bootDependencies
./gradlew checkPluginRuntimeClasspath
```

默认重复依赖只 warning。需要失败退出时：

```groovy
pf4bootPlugin {
  duplicateDependencyPolicy = 'fail'
}
```

如需把运行时检查接入 `check`：

```groovy
pf4bootPlugin {
  checkRuntimeClasspathOnCheck = true
}
```

### 7) 发布前验证任务

```bash
./gradlew verifyReleaseReadiness
./gradlew verifyReleaseTag
```

两个任务都是只读任务，不会创建 tag，不会修改版本，也不会发布。

### 8) 本地联调验收清单

```text
./gradlew pf4boot
├─ build/libs/<project>-<version>.zip
├─ build/generated/pf4boot/plugin.properties
├─ plugin.id / plugin.class / plugin.version 与预期一致
├─ 中文配置项（description/requires/provider）不乱码（UTF-8）
├─ bundle/bundleOnly/embed 与本地需求一致
└─ platformApi 依赖编译和本地运行可见，但不进入 zip lib/
```

出现失败时先对照 [故障排查手册（中文）](troubleshooting-zh.md)。

### 下一步

- 完整配置和开发流程：[开发者手册（中文）](developer-guide-zh.md)
- 常见问题定位：[故障排查手册（中文）](troubleshooting-zh.md)
