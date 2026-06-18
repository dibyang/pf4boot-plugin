# 变更日志

[中文](CHANGELOG.md) | [English](CHANGELOG_EN.md)

本项目遵循 [Keep a Changelog](https://keepachangelog.com/) 规范，并采用 [语义化版本](https://semver.org/) 进行版本管理。

所有版本发布内容将在此记录；每次发版前请先更新本文件。

## [Unreleased]

### 已添加
- 强化并测试 `platformApi` 三重语义：编译可见、本地运行可见、不打包。
- 非插件库项目的 `platformApi` 现在对库项目编译、测试、本地运行可见；当库项目被插件打包依赖时，其平台 API 会进入插件本地运行 classpath，但不会进入插件 zip。

### 已变更
- 文档明确不建议插件项目反向依赖包含插件包的 `app-run` 项目，避免构建循环。

## [1.5.0] - 2026-06-16

### 已添加
- 新增 `platformApi` 本地运行支持：平台 API 可通过 `pluginLocalRuntimeClasspath` 在本地 `JavaExec` 中可见，但默认不进入插件 zip。
- 新增依赖报告与诊断任务：`pf4bootDependencies`、`checkPluginRuntimeClasspath`。
- 新增重复依赖策略 `duplicateDependencyPolicy`，支持 `warn` / `fail` / `ignore`，默认 `warn`。
- 新增 `checkRuntimeClasspathOnCheck` 开关，可选择把运行时检查接入 `check`。
- 新增发布前验证任务：`verifyReleaseReadiness`、`verifyReleaseTag`。
- 新增最小版字节码引用诊断，用于提示已知缺失类（例如 `org/slf4j/LoggerFactory`）对应的建议依赖。
- 新增双语故障排查手册，并同步更新 README、Usage、开发者手册和平台运行时实施计划。

## [1.4.1] - 2026-06-11

### 已修复
- 修复仅使用 `pf4bootPlugin` 扩展配置、且项目根目录不存在 `plugin.properties` 时，`pf4boot` 任务在 Gradle 7 输入文件校验阶段失败的问题。
- 为 Java 编译任务统一配置 UTF-8 编码，避免 Windows 默认编码导致中文源码、注释和功能测试内容异常。

## [1.4.0] - 2026-06-11

### 已添加
- 新增 `CHANGELOG.md` 与 `CHANGELOG_EN.md`（中文/英文双语变更日志）。
- 新增发布前文档检查清单，区分 `check` 与 `test` 验证边界。
- 补齐使用说明（Usage）与开发者手册（Developer Guide）文档内容：
  - 增加依赖分组说明：`bundle` / `bundleOnly` / `embed`。
  - 增加本地文件依赖写法：`files('libs/...')`。
  - 明确本地构建产物（`build/libs/<project>-<version>.zip`）校验流程与验收项。
  - 新增配置优先级说明、开发者验收清单与常见问题排查。

### 已修复
- 修正开发者手册中章节编号重复，避免引用和阅读歧义。
- 补齐发布前文档规划与验收动作，提升本地开发发布一致性。
