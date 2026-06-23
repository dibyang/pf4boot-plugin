# 问题反馈指南

[中文](bug-reporting.md) | [English](en/bug-reporting.md)

本指南用于帮助使用者提交可复现、可诊断的 pf4boot-plugin 问题。普通 bug 请使用 GitHub issue form；安全问题请阅读 [安全报告](../SECURITY.md)。

## 提交前检查

- 确认是否使用了当前可用的最新版本，或说明不能升级的原因。
- 查看 [故障排查手册](troubleshooting-zh.md) 是否已覆盖该问题。
- 准备最小可复现项目、测试用例或完整 Gradle 命令。
- 确认报告中不包含密钥、Token、私有仓库地址或安全 exploit 细节。

## 必要信息

请在 issue 中提供：

- pf4boot-plugin 版本或 commit。
- 受影响组件，例如 `net.xdob.pf4boot`、`net.xdob.pf4boot-plugin`、`pf4boot` 任务、`platformApi` 或发布任务。
- 使用路径，例如直接 Gradle 插件、多项目构建、本地 `JavaExec`、Maven Central 发布或 Gradle Plugin Portal 发布。
- 期望行为和实际行为。
- 最小复现方式。
- 完整错误日志、堆栈、依赖报告或 zip 内容差异。
- 操作系统、JDK、Gradle 版本和关键依赖坐标。

## Gradle 插件问题建议附加信息

如果问题和依赖或打包有关，请附加：

- `dependencies` 中 `bundle`、`bundleOnly`、`embed`、`platformApi` 的声明。
- `./gradlew pf4bootDependencies` 输出。
- `./gradlew checkPluginRuntimeClasspath` 输出。
- 生成 zip 的 `lib/` 条目列表。

如果问题和本地运行有关，请说明：

- `JavaExec` 的 `classpath` 配置。
- 是否显式加入 `configurations.pluginLocalRuntimeClasspath`。
- 缺失类的完整类名和首个 `NoClassDefFoundError` 堆栈。

## 最小复现建议

最好的复现方式是一个可以直接运行的 Gradle TestKit 用例或小型多项目构建。无法提供完整项目时，请至少提供 `settings.gradle`、相关 `build.gradle`、示例源码和执行命令。
