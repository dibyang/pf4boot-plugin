# pf4boot-plugin 平台运行时依赖实施计划

[中文](platform-runtime-implementation-plan-zh.md) | [English](platform-runtime-implementation-plan-en.md)

> 中文文档为主文档，英文文档为同步副本。本计划用于追踪 [平台运行时依赖与发布可靠性设计](platform-runtime-design-zh.md) 的实施进度。

## 1. 追踪规则

- 所有阶段实施前必须先确认设计，不直接跳到实现。
- 所有关键实现必须有对应测试代码。
- 每个阶段必须满足“范围”和“验收”后才可标记完成。
- 如果阶段内出现设计变化，先更新设计文档，再更新本计划。
- 默认不引入破坏性行为：不改变 zip 默认内容，不自动修改所有 `JavaExec`，不默认把诊断任务接入 `check`。

## 2. 阶段总览

| 阶段 | 名称 | 状态 | 核心交付 |
| --- | --- | --- | --- |
| 阶段 1 | 本地运行 classpath | 已完成 | `pluginLocalRuntimeClasspath` 与打包依赖分类逻辑。 |
| 阶段 2 | 依赖报告与诊断 | 已完成 | `DependencyReporter`、`pf4bootDependencies`、`checkPluginRuntimeClasspath`。 |
| 阶段 3 | 发布可靠性 | 已完成 | `verifyReleaseReadiness`、`verifyReleaseTag`。 |
| 阶段 4 | 文档与故障排查 | 已完成 | troubleshooting 文档与 usage/developer guide 更新。 |
| 阶段 5 | 字节码级诊断 | 已完成（最小版） | class 引用扫描与已知缺失类映射。 |

## 3. 阶段 1：本地运行 classpath

状态：已完成

### 范围

- 新增 `pluginLocalRuntimeClasspath` configuration。
- 明确本地运行完整 classpath 使用：`sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath`。
- 新增打包依赖分类逻辑，分别解析 `bundle` / `bundleOnly` / `embed`。
- 不新增合并后的 `pluginPackagedClasspath` configuration。
- 不改变 `pf4boot` zip 默认内容。

### 验收

- `platformApi("org.slf4j:slf4j-api:2.0.7")` 可在 `pluginLocalRuntimeClasspath` 解析。
- `slf4j-api` 不出现在 pf4boot zip 的 `lib/`。
- `bundleOnly` 仍保持非传递依赖语义。
- `JavaExec` 示例使用 `sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath`。
- `.\gradlew.bat functionalTest` 通过。
- `.\gradlew.bat check` 通过。

### 测试要求

- 已新增 `shouldExposePlatformApiInPluginLocalRuntimeClasspath`。
- 已新增 `shouldNotPackagePlatformApiByDefault`。
- 已新增 `shouldKeepBundleOnlyNonTransitiveWhenReportingPackagedDependencies`。

## 4. 阶段 2：依赖报告与诊断

状态：已完成

### 范围

- 新增 `ResolvedArtifactInfo`、`DependencyReport`、`DependencyReporter`。
- 新增 `pf4bootDependencies` 任务。
- 新增 `checkPluginRuntimeClasspath` 第一版。
- 诊断基于 Gradle 解析事实与已知 class 引用映射，不承诺完整隐式依赖推断。
- 重复依赖默认 warning，支持 `warn` / `fail` / `ignore` 策略。
- 默认不接入 `check`，提供 `checkRuntimeClasspathOnCheck` opt-in 开关。

### 验收

- 报告能区分 `bundle`、`bundleOnly`、`embed`、platform、local runtime。
- 可识别插件包依赖和平台依赖的重复 module。
- 重复依赖默认输出 warning。
- 解析失败时输出 configuration 名称和失败依赖。
- `checkPluginRuntimeClasspath` 能验证已声明 platform API 是否进入本地运行依赖配置。

### 测试要求

- 已新增 `shouldReportPackagedPlatformAndLocalRuntimeDependencies`。
- 已新增 `shouldWarnWhenPackagedDependencyDuplicatesPlatformDependency`。
- 平台 API 本地运行可见性由 `shouldExposePlatformApiInPluginLocalRuntimeClasspath` 覆盖。

## 5. 阶段 3：发布可靠性

状态：已完成

### 范围

- 新增 `verifyReleaseReadiness`。
- 新增 `verifyReleaseTag`。
- `verifyReleaseReadiness` 检查版本、changelog、README、Usage、zip。
- `verifyReleaseTag` 检查 tag 是否存在且指向当前提交。
- 两个任务都只读，不创建 tag、不修改版本、不发布。

### 验收

- `version` 包含 `SNAPSHOT` 时 `verifyReleaseReadiness` 失败。
- changelog 缺当前版本时失败。
- README / Usage 示例版本与当前版本不一致时失败。
- zip 缺 `plugin.properties` 或 `lib/` 时失败。
- `verifyReleaseTag` 在 tag 不存在或不指向 HEAD 时失败。

### 测试要求

- 已新增 `shouldVerifyReleaseReadinessForCurrentVersion`。
- 已新增 `shouldFailReleaseReadinessWhenVersionIsSnapshot`。
- 已新增 `shouldFailReleaseTagWhenTagDoesNotPointToHead`。

## 6. 阶段 4：文档与故障排查

状态：已完成

### 范围

- 新增 `troubleshooting-zh.md`。
- 新增 `troubleshooting-en.md`。
- 更新 `usage-zh.md` / `usage-en.md`。
- 更新 `developer-guide-zh.md` / `developer-guide-en.md`。
- 所有中英文文档头部保持 `[中文](...) | [English](...)`。

### 验收

- 文档覆盖 `NoClassDefFoundError`。
- 文档覆盖 `plugin.properties` 不存在。
- 文档覆盖 `plugin.version=unspecified`。
- 文档覆盖 Windows UTF-8。
- 文档覆盖平台依赖重复。
- 中文文档为主，英文文档内容同步。

### 测试要求

- Gradle 配置示例与功能测试覆盖的行为保持一致。
- 无法自动覆盖的发布动作在文档中标明手动验证命令。

## 7. 阶段 5：字节码级诊断

状态：已完成（最小版）

### 范围

- 扫描 jar/class 中的 class 引用字符串。
- 将典型缺失类映射到 module，例如 `org/slf4j/LoggerFactory -> org.slf4j:slf4j-api`。
- 输出引用缺失类的 jar/class。
- 作为独立增强，不阻塞阶段 1 到阶段 4。

### 验收

- 能指出哪个 jar/class 引用了缺失类。
- 对 `org/slf4j/LoggerFactory` 给出可操作建议。
- 作为最小版诊断，仅对已知映射输出 warning，不承诺完整字节码依赖分析。

### 测试要求

- 已新增包含缺失类引用的 fixture jar。
- 已新增 `shouldReportClassReferenceForKnownMissingPlatformApi`。

## 8. 当前设计决策

| 问题 | 决策 |
| --- | --- |
| `embed` 语义 | 当前保留为未来策略分组，第一阶段不区别于普通打包行为，只单独报告。 |
| 接入 `check` | 默认不接入，提供 opt-in 开关 `checkRuntimeClasspathOnCheck`。 |
| 重复依赖 | 默认 warning，支持 `warn` / `fail` / `ignore`。 |
| JavaExec 适配 | 不自动适配所有 `JavaExec`，用户显式使用 `sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath`。 |
| platform API 来源 | 第一阶段只支持当前项目显式声明；宿主项目导入作为后续增强。 |

## 9. 验收命令

```powershell
.\gradlew.bat functionalTest
.\gradlew.bat check
.\gradlew.bat pf4bootDependencies
.\gradlew.bat checkPluginRuntimeClasspath
.\gradlew.bat verifyReleaseReadiness
.\gradlew.bat verifyReleaseTag
```

本次实现已验证 `.\gradlew.bat check` 通过。后四个任务需要在具体插件项目或发布流程中按场景执行。