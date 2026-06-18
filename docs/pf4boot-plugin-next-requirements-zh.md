# pf4boot-plugin 下一阶段需求规划

[中文](pf4boot-plugin-next-requirements-zh.md) | [English](pf4boot-plugin-next-requirements-en.md)

## 背景

`pf4boot-plugin` 1.6.0 已经补齐了插件开发的基础闭环：插件 zip 打包、`bundle` / `bundleOnly` / `embed` / `platformApi` 依赖分组、`pluginLocalRuntimeClasspath` 本地运行支持、依赖诊断任务以及发布前验证任务。

随着 pf4boot 主框架开始强化插件仓库、热替换部署、JPA runtime reload、管理接口治理和 runtime smoke，辅助 Gradle 插件也需要从“能打包插件”进一步升级为“能产出可治理、可验证、可发布、可回滚的插件发布物”。

本文档用于提交给 `pf4boot-plugin` 仓库，作为 1.7.0 后续需求设计和实施规划输入。

## 当前能力基线

1. `pf4boot` 任务能生成插件 zip。
2. `plugin.properties` 可以来自源码文件或 `pf4bootPlugin {}` 扩展。
3. 依赖分组已经明确：
   - `bundle`：打包声明依赖及传递依赖。
   - `bundleOnly`：只打包声明依赖本身。
   - `embed`：当前独立报告，默认仍按打包依赖处理。
   - `platformApi`：宿主提供，编译和本地运行可见，默认不进入 zip。
4. `pluginLocalRuntimeClasspath` 支持本地 JavaExec 显式组合运行 classpath。
5. `pf4bootDependencies` 和 `checkPluginRuntimeClasspath` 支持依赖诊断。
6. `verifyReleaseReadiness` 和 `verifyReleaseTag` 支持发布前只读校验。
7. `pf4bootElements` 可以让其它项目消费插件 zip。

## 总体目标

下一阶段不应继续堆叠隐式行为，而应围绕插件生产化发布链路补强：

1. 插件包内容可验证。
2. 插件发布物可治理。
3. 插件仓库索引可生成。
4. 多模块插件工程更容易组装。
5. 构建产物可复现。
6. 发布到 Maven / 文件仓库时带上 manifest、checksum 和索引片段。

## 下一版本首要目标：`platformApi` 跨库项目传递语义

详细设计见：[platform-api-propagation-design-zh.md](platform-api-propagation-design-zh.md)。

### 背景

真实插件工程中，插件项目经常不会直接使用平台 API，而是通过一个非插件库项目间接使用。例如：

```text
root
├─ plugin-apacheds
└─ apacheds-lib
```

`apacheds-lib` 使用 `org.slf4j:slf4j-api`，但 `slf4j-api` 由宿主平台提供。此时如果 `apacheds-lib` 使用 `implementation` 或插件项目使用 `bundle` / `embed` 显式引入 `slf4j-api`，该依赖可能被打进插件 zip，造成宿主与插件日志 API 边界冲突。

因此，下一版本应把 `platformApi` 从“插件项目自身可用”扩展为“非插件库项目也可用，并能在被插件打包时保持平台依赖边界”。

### 目标

当非插件库项目声明：

```groovy
plugins {
  id 'java-library'
  id 'net.xdob.pf4boot'
}

dependencies {
  platformApi "org.slf4j:slf4j-api:${slf4j_version}"
}
```

并被插件项目打包：

```groovy
dependencies {
  bundle project(':apacheds-lib')
}
```

应满足：

1. `apacheds-lib` 编译可见 `platformApi`。
2. `apacheds-lib` 测试编译和测试运行可见 `platformApi`。
3. `apacheds-lib` 本地直接运行 main / JavaExec 时可见 `platformApi`。
4. `plugin-apacheds` 本地运行 classpath 可见 `apacheds-lib` 的 `platformApi`。
5. `plugin-apacheds` zip 包含 `apacheds-lib.jar`。
6. `plugin-apacheds` zip 不包含 `slf4j-api.jar` 等平台 API 依赖。

### 非目标

1. 不从包含插件包的 `app-run` 反向导入平台依赖，避免形成构建循环。
2. 不默认把 `platformApi` 打入插件 zip。
3. 不自动修改所有 `JavaExec` 任务。
4. 不要求宿主项目暴露 runtimeClasspath 给插件项目。

### 设计约束

1. `platformApi` 在所有应用 `net.xdob.pf4boot` 的 Java 项目中统一语义：编译可见、测试可见、本地运行可见、不打包。
2. 插件项目打包 `bundle project(':some-lib')` 时，应识别该库项目的 `platformApi`，并把它加入插件 `pluginLocalRuntimeClasspath`。
3. 插件 zip 仍只打包插件 jar、`bundle`、`bundleOnly`、`embed` 的非平台依赖。
4. `pf4bootDependencies` / `checkPluginRuntimeClasspath` 应能解释平台依赖来源，至少能区分当前插件项目平台依赖和被打包库项目平台依赖。

### 验收

1. 新增功能测试覆盖 `slf4j-api -> apacheds-lib -> plugin-demo` 三项目结构。
2. `apacheds-lib:compileJava` 通过，源码可 `import org.slf4j.Logger`。
3. `apacheds-lib:compileTestJava` 通过，测试源码可 `import org.slf4j.Logger`。
4. `apacheds-lib` 的 `runtimeClasspath` / `testRuntimeClasspath` 包含 `slf4j-api`。
5. `plugin-demo` 的 `pluginLocalRuntimeClasspath` 包含 `slf4j-api`。
6. `plugin-demo` 的 pf4boot zip 包含 `apacheds-lib.jar`，不包含 `slf4j-api.jar`。
7. `.\\gradlew.bat functionalTest` 和 `.\\gradlew.bat check` 通过。
8. `bundle` / `embed` 递归收集 project dependency 的 `platformApi`。
9. `bundleOnly` 只收集直接 project dependency 的 `platformApi`，不递归。
10. `platformApi project(':platform-api')` 的 project jar 本地运行可见，但不进入插件 zip。

## 非目标

1. 不改变 1.6.0 已定义的依赖分组语义。
2. 不默认把 `platformApi` 打进插件 zip。
3. 不默认修改用户已有 `JavaExec` 任务。
4. 不在 Gradle 插件中实现 pf4boot runtime 的加载、热替换或管理 API 调用。
5. 不强制所有项目使用插件仓库；本地 zip 开发模式必须继续保留。
6. 不引入与 Gradle 插件职责无关的运行时框架依赖。

## P1 插件包内容校验与发布物 Manifest

### 目标

新增插件包级校验和 manifest 输出，让每个插件 zip 都能被部署服务、插件仓库和 CI 明确识别。

### 新增任务建议

```text
verifyPf4bootPluginPackage
generatePf4bootPackageManifest
```

### Manifest 输出

建议生成：

```text
build/generated/pf4boot/pf4boot-package-manifest.json
```

可选地写入 zip 根目录：

```text
pf4boot-package-manifest.json
```

默认建议先生成外部文件；是否写入 zip 由配置控制，避免破坏已有包结构。

### Manifest 字段建议

```json
{
  "schemaVersion": "1.0",
  "pluginId": "sample-workflow",
  "pluginClass": "net.xdob.sample.workflow.WorkflowPlugin",
  "pluginVersion": "3.2.0-SNAPSHOT",
  "provider": "pf4boot",
  "description": "sample workflow orchestration plugin",
  "requires": "",
  "dependencies": ["sample-demo-jpa-domain", "sample-user-book-service"],
  "archiveFile": "plugin-workflow-3.2.0-SNAPSHOT.zip",
  "archiveSha256": "...",
  "archiveSize": 123456,
  "mainJar": "lib/plugin-workflow-3.2.0-SNAPSHOT.jar",
  "packagedLibraries": [
    {"path": "lib/plugin-workflow-3.2.0-SNAPSHOT.jar", "sha256": "...", "size": 12345, "source": "project"}
  ],
  "dependencyGroups": {
    "bundle": [],
    "bundleOnly": [],
    "embed": [],
    "platformApi": []
  },
  "build": {
    "gradleVersion": "7.4",
    "javaVersion": "1.8",
    "createdAt": "2026-06-18T00:00:00Z",
    "gitCommit": "...",
    "dirty": false
  }
}
```

### 校验规则

`verifyPf4bootPluginPackage` 至少应校验：

1. zip 存在且可打开。
2. zip 根目录存在 `plugin.properties`。
3. `plugin.id`、`plugin.class`、`plugin.version` 非空。
4. `plugin.class` 能在主插件 jar 中找到对应 `.class`。
5. zip 中存在主插件 jar。
6. `plugin.properties` 使用 UTF-8 编码，中文字段不乱码。
7. `platformApi` 依赖默认不出现在 zip `lib/` 中。
8. `bundleOnly` 不携带传递依赖。
9. zip 中没有重复 jar 文件名。
10. zip 中没有空 jar、损坏 jar、非法路径或绝对路径条目。
11. `plugin.dependencies` 中声明的插件 ID 格式合法。
12. manifest 中的 sha256 与 zip 实际 sha256 一致。

### 配置建议

```groovy
pf4bootPlugin {
  packageManifestEnabled = true
  packageManifestInZip = false
  verifyPackageOnCheck = false
  packageVerificationPolicy = 'warn' // warn, fail
}
```

### 验收

1. 正常插件包生成 manifest。
2. 缺少 `plugin.class` 时失败。
3. `plugin.class` 不存在于 jar 时失败。
4. `platformApi` 被错误打包时能报出清晰提示。
5. 中文 `plugin.description` 校验通过且无乱码。

## P2 插件仓库索引生成

### 目标

支持从一组插件 zip 生成 pf4boot 插件仓库索引，服务于插件治理、部署 dry-run、热替换 replace 和离线仓库使用。

### 新增任务建议

```text
generatePf4bootRepositoryIndex
verifyPf4bootRepositoryIndex
```

### 输入

默认扫描当前项目或配置目录中的插件 zip：

```groovy
pf4bootPlugin {
  repositoryIndex {
    enabled = true
    inputDirs = [layout.buildDirectory.dir('libs')]
    outputFile = layout.buildDirectory.file('pf4boot-repository/repository-index.json')
  }
}
```

多模块根项目可聚合：

```groovy
pf4bootRepository {
  inputProjects = [':plugin-a', ':plugin-b']
  outputFile = layout.buildDirectory.file('repository/repository-index.json')
}
```

### 索引字段建议

```json
{
  "schemaVersion": "1.0",
  "repositoryId": "local-sample",
  "generatedAt": "2026-06-18T00:00:00Z",
  "plugins": [
    {
      "pluginId": "sample-workflow",
      "version": "3.2.0-SNAPSHOT",
      "packagePath": "plugins/plugin-workflow-3.2.0-SNAPSHOT.zip",
      "packageSha256": "...",
      "packageSize": 123456,
      "pluginClass": "net.xdob.sample.workflow.WorkflowPlugin",
      "dependencies": ["sample-demo-jpa-domain", "sample-user-book-service"],
      "requires": "",
      "manifestPath": "manifests/plugin-workflow-3.2.0-SNAPSHOT.json"
    }
  ]
}
```

### 校验规则

1. 索引中的每个 packagePath 文件存在。
2. sha256 与实际插件包一致。
3. 同一 `pluginId + version` 不重复。
4. `plugin.dependencies` 指向的插件 ID 在索引或宿主已知插件集合中可解释。
5. 索引 JSON 可被 pf4boot 主框架的 repository resolver 直接读取。

### 验收

1. 单插件项目可生成 repository index。
2. 多插件 sample 可聚合生成 index。
3. sha256 不匹配时 `verifyPf4bootRepositoryIndex` 失败。
4. 缺包、重复版本、损坏包都有清晰错误。

## P3 多模块插件工程组装支持

### 目标

减少复杂 sample 或业务工程中手写 `assembleSamplePlugins` 的重复配置。

### 新增能力建议

在根项目提供聚合任务：

```text
assemblePf4bootPlugins
verifyPf4bootPlugins
```

### 配置示例

```groovy
pf4bootPluginAssembly {
  pluginProjects = [
    ':samples:cross-plugin-jpa:plugin-demo-jpa-domain',
    ':samples:cross-plugin-jpa:plugin-user-book-service',
    ':samples:cross-plugin-jpa:plugin-workflow',
    ':samples:cross-plugin-jpa:plugin-unrelated-service'
  ]
  outputDir = layout.buildDirectory.dir('sample-plugins')
  cleanOutputDir = true
}
```

### 行为

1. 自动依赖每个插件项目的 `pf4boot` 任务。
2. 将生成的插件 zip 复制到统一输出目录。
3. 可选生成 repository index。
4. 可选校验所有插件包。
5. 默认清理输出目录，避免旧版本插件包残留。

### 验收

1. cross-plugin-jpa 这类多插件 sample 不再需要手写 Copy 任务。
2. 输出目录不会残留旧版本插件包。
3. 聚合任务能与 runtime smoke 串联。

## P4 可复现插件包

### 目标

同样源码、同样输入、同样版本下生成稳定 zip，便于 checksum、仓库索引、灰度和回滚。

### 配置建议

```groovy
pf4bootPlugin {
  reproducibleArchive = true
  preserveFileTimestamps = false
  deterministicFileOrder = true
}
```

### 要求

1. zip entry 顺序稳定。
2. zip entry timestamp 可固定或归零。
3. manifest 中的构建时间默认不写入 zip，或允许关闭。
4. 生成 checksum 时使用最终 zip 内容。

### 验收

1. 连续两次构建同一插件，sha256 一致。
2. 修改源码后 sha256 变化。
3. 修改非打包文件不影响 sha256。

## P5 插件发布集成

### 目标

支持把插件 zip、manifest、checksum、repository index fragment 作为正式发布物发布到 Maven 或文件仓库。

### 能力建议

1. 注册 Maven publication artifact：
   - `<artifactId>-<version>.zip`
   - `<artifactId>-<version>-manifest.json`
   - `<artifactId>-<version>.sha256`
2. 可选生成 repository index fragment。
3. `verifyReleaseReadiness` 检查这些发布物完整性。

### 配置示例

```groovy
pf4bootPlugin {
  publishing {
    publishPluginZip = true
    publishManifest = true
    publishChecksum = true
    publishRepositoryIndexFragment = true
  }
}
```

### 验收

1. `publishToMavenLocal` 后能看到 zip、manifest、checksum。
2. checksum 与 zip 匹配。
3. 发布前验证能发现缺失 artifact。

## P6 本地开发体验增强

### 目标

保持 1.6.0 的克制原则，不默认修改所有 `JavaExec`，但提供更容易复用的模板和提示。

### 能力建议

1. 提供 helper 方法或约定任务：

```groovy
tasks.register('runPluginLocal', JavaExec) {
  classpath = pf4bootPlugin.localRuntimeClasspath(sourceSets.main)
  mainClass = 'com.example.PluginLocalMain'
}
```

2. `pf4bootDependencies` 输出中明确提示：
   - 标准 `runtimeClasspath` 是否缺平台 API。
   - 完整本地运行 classpath 应如何配置。
3. 对常见 `NoClassDefFoundError` 给出依赖建议。

### 验收

1. 使用 helper 的 JavaExec 可以访问 `platformApi`。
2. 不使用 helper 时插件行为不变。
3. 诊断输出能定位 slf4j、Spring API、pf4boot-api 等常见缺失。

## 建议实施顺序

| 阶段 | 目标 | 主要产出 | 优先级 |
| --- | --- | --- | --- |
| P0 | `platformApi` 跨库项目传递语义 | 库项目 platformApi 编译/测试/运行可见，插件本地运行可见但不打包 | 最高 |
| P1 | 包内容校验与 manifest | `verifyPf4bootPluginPackage`、`generatePf4bootPackageManifest` | 高 |
| P2 | 仓库索引生成 | `generatePf4bootRepositoryIndex`、`verifyPf4bootRepositoryIndex` | 高 |
| P3 | 多模块插件组装 | `assemblePf4bootPlugins`、聚合配置 | 中高 |
| P4 | 可复现构建 | 稳定 zip、稳定 sha256 | 中高 |
| P5 | 发布集成 | Maven artifact、checksum、index fragment | 中 |
| P6 | 本地运行增强 | helper API、诊断提示 | 中 |

建议先做 P1 + P2，因为它们能直接支撑 pf4boot 主框架的插件仓库、热替换部署、repository replace 和 runtime smoke。

## 与 pf4boot 主框架的关系

这些需求与 pf4boot 主框架中的以下能力直接关联：

1. 插件热替换部署：需要可信的 pluginId、version、checksum 和包摘要。
2. 插件仓库治理：需要 repository index 和 package manifest。
3. 管理接口部署 dry-run：需要清晰的包校验错误和依赖说明。
4. runtime smoke：需要干净的插件输出目录，避免旧 zip / 旧 jar 污染。
5. 回滚：需要能稳定定位旧包、校验旧包 checksum。

## 风险与兼容性

1. manifest 写入 zip 可能改变已有 checksum，建议默认先不写入 zip，只生成外部 manifest。
2. 可复现构建可能改变 zip entry timestamp，建议可配置并在小版本中默认关闭或灰度开启。
3. repository index schema 需要和 pf4boot 主框架 resolver 对齐。
4. 多模块聚合任务不能假设所有子项目都是插件项目，应显式配置或自动识别后给出 warning。
5. 发布 artifact 集成要避免干扰用户已有 `maven-publish` 配置。

## 最小可落地版本建议

如果只做一个小版本，建议范围控制为：

1. `generatePf4bootPackageManifest`
2. `verifyPf4bootPluginPackage`
3. `generatePf4bootRepositoryIndex`
4. 文档和 functional tests

暂不做 Maven publish 集成、可复现 zip 默认开启、JavaExec 自动适配。

这个最小版本已经可以让插件包从“本地 zip”升级为“可治理发布物”。
