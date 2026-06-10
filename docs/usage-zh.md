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
    classpath "net.xdob.pf4boot:pf4boot-plugin:1.4.0-SNAPSHOT"
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

```text
build/libs/<project>.zip
```

并确认 `build/generated/pf4boot/plugin.properties` 在 zip 中。

### 下一步

- 如果你要看完整配置、依赖分组、排障建议，请看：
- [开发者手册（中文）](developer-guide-zh.md)
