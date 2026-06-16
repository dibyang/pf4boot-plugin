## pf4boot-plugin Usage Guide (English)
### (for developers building pf4boot plugins with this Gradle plugin)

[中文](developer-guide-zh.md) | [English](developer-guide-en.md)

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

### 2.1 Priority of configuration sources (important)

- If both `plugin.properties` and `pf4bootPlugin` extension define the same key, `pf4bootPlugin` takes precedence.
- Recommendation: keep stable defaults in `plugin.properties` and inject per-build overrides in the extension.

```groovy
// plugin.properties
plugin.id=demo-plugin
plugin.provider=from-file

// build.gradle
pf4bootPlugin {
  provider = 'from-extension'
}
```

Packed result will contain `plugin.provider=from-extension`.

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

Local file dependency example:

```groovy
dependencies {
  bundle files('libs/local-bundle.jar')
  bundleOnly files('libs/local-bundle-only.jar')
  embed files('libs/local-embed.jar')
}
```

### 4. Local development checklist (recommended)

Before release, run:

```text
./gradlew pf4boot
├─ validate build/generated/pf4boot/plugin.properties
├─ verify build/libs/<project>-<version>.zip
├─ ensure zip contains plugin.properties and lib/<project>-<version>.jar
├─ verify bundle / bundleOnly / embed scope matches expected usage
├─ verify UTF-8 fields (description/requires/provider) are readable
└─ ensure required keys (id/class/version) are not empty
```

Optional: run `./gradlew check` for full functional verification.

### 5. Local development flow

- Step 1: configure plugin metadata and apply `net.xdob.pf4boot-plugin`
- Step 2: run `./gradlew pf4boot`
- Step 3: check `build/generated/pf4boot/plugin.properties`
- Step 4: verify `build/libs` zip contents

### 6. Local consumption (development only)

```groovy
dependencies {
  implementation project(path: ':your-plugin', configuration: 'pf4bootElements')
}
```

### 7. Troubleshooting

- Missing required properties: confirm they are set in either file or extension
- Invalid version: set explicit `plugin.version` (rejects empty or `unspecified`)
- Garbled properties file: ensure UTF-8 encoding
- Prefer one source of truth: `pf4bootPlugin` extension has higher priority than `plugin.properties`
- For final effective value checks, compare:
  - `build/generated/pf4boot/plugin.properties`
  - `effective plugin properties` output in build logs

### 7. Continue reading

- Quick start: [usage-en.md](usage-en.md)
- Improvements and roadmap: [improvement-plan-en.md](improvement-plan-en.md)
