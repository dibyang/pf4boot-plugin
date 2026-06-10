## pf4boot-plugin Usage Guide (English)
### (for developers building pf4boot plugins with this Gradle plugin)

[English](developer-guide-en.md) | [中文](developer-guide-zh.md)

This is the full reference guide. For quick setup, read [Usage Guide](usage-en.md) first.

### 1. Quick recap

- `pf4boot` is the packaging task used in local development
- `plugin.id` and `plugin.class` are required
- Plugin metadata can come from `plugin.properties` or `pf4bootPlugin` extension

### 2. Configuration options

Option A: `plugin.properties`

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

Option B: Gradle extension

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

When both exist, extension values take precedence.

### 3. Dependency packaging rules

- `bundle`: packages dependency + transitive dependencies
- `bundleOnly`: packages only direct dependency
- `embed`: packages selected embed dependencies for local runtime bundling

Example:

```groovy
dependencies {
  bundle 'com.squareup.okio:okio:3.0.0'
  bundleOnly 'org.apache.commons:commons-lang3:3.12.0'
  embed project(':shared-lib')
}
```

### 4. Local development flow

- Step 1: configure plugin metadata and apply `net.xdob.pf4boot-plugin`
- Step 2: run `./gradlew pf4boot`
- Step 3: check `build/generated/pf4boot/plugin.properties`
- Step 4: verify `build/libs` zip contents

### 5. Local consumption (development only)

```groovy
dependencies {
  implementation project(path: ':your-plugin', configuration: 'pf4bootElements')
}
```

### 6. Troubleshooting

- Missing required properties: confirm they are set in either file or extension
- Invalid version: set explicit `plugin.version`
- Garbled properties file: ensure UTF-8 encoding
- Prefer one source of truth when checking final value resolution

### 7. Continue reading

- Quick start: [usage-en.md](usage-en.md)
- Improvements and roadmap: [improvement-plan-en.md](improvement-plan-en.md)
