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
    classpath "net.xdob.pf4boot:pf4boot-plugin:1.5.0"
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
- `platformApi`：宿主平台提供、本地运行也需要可见的 API，默认不进入 zip。

### 5) 本地 JavaExec 运行

```groovy
tasks.register('runPluginLocal', JavaExec) {
  classpath = sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath
  mainClass = 'com.example.PluginLocalMain'
}
```

插件不会自动修改所有 `JavaExec`，需要用户显式引用 `pluginLocalRuntimeClasspath`。

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
└─ platformApi 依赖本地运行可见，但不进入 zip lib/
```

出现失败时先对照 [故障排查手册（中文）](troubleshooting-zh.md)。

### 下一步

- 完整配置和开发流程：[开发者手册（中文）](developer-guide-zh.md)
- 常见问题定位：[故障排查手册（中文）](troubleshooting-zh.md)