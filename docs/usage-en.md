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
    classpath "net.xdob.pf4boot:pf4boot-plugin:1.4.1"
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

### 4.1 Built artifact

```text
build/libs/<project>-<version>.zip
```

### 4.2 Archive contents

```text
plugin.properties (at ZIP root)
lib/<project>-<version>.jar
lib/* from bundle / bundleOnly / embed
```

Check that generated `build/generated/pf4boot/plugin.properties` exists and is the source for packaged metadata.

On Windows PowerShell you can quickly verify:

```powershell
Expand-Archive -Path build/libs/<project>-<version>.zip -DestinationPath build/verify -Force
Get-ChildItem build/verify
```

### 5) Dependency groups

#### `bundle`

`bundle` packages a dependency together with its transitive dependencies.

```groovy
dependencies {
  bundle "com.squareup.okio:okio:3.0.0"
}
```

#### `bundleOnly`

`bundleOnly` packages only directly declared dependencies.

```groovy
dependencies {
  bundleOnly "org.apache.commons:commons-lang3:3.12.0"
}
```

#### `embed`

`embed` is used when you need inlined runtime dependencies during local integration.

```groovy
dependencies {
  embed project(':shared-lib')
}
```

#### Local file dependencies

Use `files(...)` when referencing local jars in `build.gradle`.

```groovy
dependencies {
  bundle files("libs/local-bundle.jar")
  bundleOnly files("libs/local-bundle-only.jar")
  embed files("libs/local-embed.jar")
}
```

### 6) Local pre-release validation checklist

```text
./gradlew pf4boot
├─ build/libs/<project>-<version>.zip
├─ build/generated/pf4boot/plugin.properties
├─ plugin.id / plugin.class / plugin.version are as expected
├─ UTF-8 text fields (description/requires/provider) are not garbled
└─ bundle / bundleOnly / embed composition is expected
```

If build fails, check the [Troubleshooting](developer-guide-en.md#6-troubleshooting) section.

### Next step

- For full usage, dependency rules, and troubleshooting, open:
- [Usage Guide / Developer Guide (English)](developer-guide-en.md)
