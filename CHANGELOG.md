# 变更日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/) 规范，并采用 [语义化版本](https://semver.org/) 进行版本管理。

所有版本发布内容将在此记录；每次发版前请先更新本文件。

## [Unreleased]

本版本暂无新增条目。

## [1.4.0] - 2026-06-10

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

