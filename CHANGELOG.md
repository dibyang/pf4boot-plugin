# 变更日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/) 规范，并采用 [语义化版本](https://semver.org/) 进行版本管理。

所有版本发布内容将在此记录；每次发版前请先更新本文件。

## [Unreleased]

### 已添加
- 新增 `CHANGELOG.md` 与 `CHANGELOG_EN.md`（双语变更日志体系）。

### 已变更
- 发布前文档计划与验收内容补充为可执行清单，新增对 `check` 与 `test` 场景区分的说明。
- 使用说明（Usage）与开发者手册补齐：
  - 增加依赖分组说明（`bundle` / `bundleOnly` / `embed`）。
  - 增加 `files('libs/...')` 本地文件依赖示例。
  - 明确产物校验方式（`build/libs/<project>-<version>.zip` 内容校验）。
  - 增加开发者手册配置优先级、验收清单和常见问题排查。

### 已修复
- 修正中文文档中开发者手册章节编号重复问题（避免阅读与引用歧义）。

## [1.4.0] - 2026-06-10（计划发布）

### 说明
- `1.4.0` 包含现阶段的文档与开发体验改善条目，实际发布日期以正式发布日志为准。

### Added
- Local development ergonomics documentation for plugin metadata, dependency usage, and release checks (Chinese/English).
- Bilingual changelog (`CHANGELOG.md`, `CHANGELOG_EN.md`).

