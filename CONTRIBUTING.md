# 贡献指南

[中文](CONTRIBUTING.md) | [English](docs/en/CONTRIBUTING.md)

感谢你愿意改进 pf4boot-plugin。这个项目主要服务 pf4boot 插件本地开发便利性，当前重点兼容 Gradle 7.x 和 JDK 8。

## 反馈问题

普通 bug 请优先使用 GitHub issue form，并参考 [问题反馈指南](docs/bug-reporting.md)。安全问题不要公开提交 exploit 细节，请按 [安全报告](SECURITY.md) 处理。

提交 bug 时请尽量提供：

- pf4boot-plugin 版本或 commit。
- 受影响插件 ID、任务名或依赖分组，例如 `net.xdob.pf4boot-plugin`、`pf4boot`、`platformApi`。
- 最小可复现 Gradle 项目、测试用例或完整复现命令。
- Gradle、JDK、操作系统和关键依赖坐标。
- 期望行为、实际行为、错误日志或 zip 内容差异。

## 提交变更

开始实现前，请先确认需求和设计是否已经讨论清楚。涉及行为、兼容性、发布流程或 Gradle 配置语义的变更，应先补充或更新设计文档，再开始代码实现。

建议流程：

1. Fork 或创建本地分支。
2. 用最小范围修改解决一个明确问题。
3. 为关键行为补充测试，优先使用现有 `functionalTest` 模式。
4. 同步更新中文主文档和英文副本。
5. 提交前运行必要验证。

## 本地验证

常用命令：

```powershell
.\gradlew.bat check
.\gradlew.bat publishToMavenLocal
.\gradlew.bat publishPlugins --dry-run
```

文档类改动通常不需要完整测试，但提交前应至少运行：

```powershell
git diff --check
```

如果改动中文 Markdown，请保持 UTF-8 编码，避免出现乱码或 U+FFFD 替换字符。

## 文档规则

- 中文文档是主文档。
- 英文文档是同步副本。
- 成对文档顶部应提供中英切换链接。
- 新增用户可见文档时，应同时提供英文副本。

## Pull Request 要求

PR 描述请包含：

- 变更目的。
- 影响范围。
- 兼容性说明。
- 已执行的验证命令。
- 如果没有测试，请说明原因和剩余风险。
