## pf4boot-plugin 使用示例（中文）

[中文](usage-zh.md) | [English](usage-en.md)

这是「5 分钟上手」文档，目标是帮助你快速跑通最小构建。

### 1) 先引入插件

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

### 2) 配置插件元信息（任选一种）

方式一，使用 `plugin.properties`：

```properties
plugin.id=demo-plugin
plugin.class=com.demo.plugin.DemoPlugin
plugin.version=1.0.0
plugin.provider=your-team
```

方式二，使用 Gradle 扩展：

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

### 4) 检查输出

### 4.1 运行结果

```text
build/libs/<project>-<version>.zip
```

### 4.2 产物内容

- `plugin.properties`（在 zip 根目录）
- `lib/<project>-<version>.jar`
- `lib/` 下的 `bundle` / `bundleOnly` / `embed` 相关依赖 jar

可在 Windows PowerShell 下快速确认：

```powershell
Expand-Archive -Path build/libs/<project>-<version>.zip -DestinationPath build/verify -Force
Get-ChildItem build/verify
```

### 5) 依赖分组验证

#### `bundle`

`bundle` 会把依赖及其传递依赖一起打包。

```groovy
dependencies {
  bundle "com.squareup.okio:okio:3.0.0"
}
```

#### `bundleOnly`

`bundleOnly` 只打包声明的第一层依赖，不带传递依赖。

```groovy
dependencies {
  bundleOnly "org.apache.commons:commons-lang3:3.12.0"
}
```

#### `embed`

`embed` 常用于本地联调需要内嵌的库。

```groovy
dependencies {
  embed project(':shared-lib')
}
```

#### 本地文件依赖

在 `build.gradle` 中用 `files(...)` 声明本地 jar。

```groovy
dependencies {
  bundle files("libs/local-bundle.jar")
  bundleOnly files("libs/local-bundle-only.jar")
  embed files("libs/local-embed.jar")
}
```

### 6) 本地联调验收清单（发布前建议）

```text
./gradlew pf4boot
├─ build/libs/<project>-<version>.zip
├─ build/generated/pf4boot/plugin.properties
├─ plugin.id / plugin.class / plugin.version 与预期一致
├─ 中文配置项（description/requires/provider）不乱码（UTF-8）
└─ bundle/bundleOnly/embed 与本地需求一致
```

出现失败时先对照 [开发者手册（中文）](developer-guide-zh.md) 的“常见问题排查”。

### 下一步

- 如果你要看完整配置、依赖分组、排障建议，请看：
- [开发者手册（中文）](developer-guide-zh.md)
