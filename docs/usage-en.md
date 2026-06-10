## pf4boot-plugin Usage (English)

[English](usage-en.md) | [中文](usage-zh.md)

This is a 5-minute quick start guide to get pf4boot plugin packaging working.

### 1) Apply the plugin

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

### 2) Configure plugin metadata (choose one)

Option A, `plugin.properties`:

```properties
plugin.id=demo-plugin
plugin.class=com.demo.plugin.DemoPlugin
plugin.version=1.0.0
plugin.provider=your-team
```

Option B, Gradle extension:

```groovy
pf4bootPlugin {
  id = 'demo-plugin'
  pluginClass = 'com.demo.plugin.DemoPlugin'
  version = '1.0.0'
  provider = 'your-team'
}
```

### 3) Build

```bash
./gradlew pf4boot
```

### 4) Verify output

```text
build/libs/<project>.zip
```

Check that generated `build/generated/pf4boot/plugin.properties` is inside and valid.

### Next step

- For full usage, dependency rules, and troubleshooting, open:
- [Usage Guide / Developer Guide (English)](developer-guide-en.md)
