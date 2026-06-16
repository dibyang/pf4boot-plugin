## pf4boot-plugin 改进需求与实施规划（中文）

[中文](improvement-plan-zh.md) | [English](improvement-plan-en.md)

本计划用于落地“本地开发优先”的插件改进，目标对象为 **Gradle 7.x**。

## 1. 范围（本次迭代）

### 1.1 纳入范围（In Scope）

- 插件行为稳定性：`pf4boot` 打包任务的输入输出声明、增量构建行为、可重复性。
- 本地开发易用性：属性文件读写、错误诊断信息、打包日志可读性。
- 文档体验：使用示例与完整使用手册的中英一致性。
- 测试可测性：关键路径与典型失败场景覆盖。
- 平台运行时依赖与发布可靠性：后续实现以 [平台运行时依赖与发布可靠性设计](platform-runtime-design-zh.md) 为准，并通过 [平台运行时依赖实施计划](platform-runtime-implementation-plan-zh.md) 追踪。

### 1.2 不纳入范围（Out of Scope）

- 发布到中央仓库的发布流程重构（`publishing`、`release`、签名策略）不在本次核心范围。
- 依赖模型重构（如新增插件 DSL 语法、全新配置模型）不在本次范围。

## 2. 需求清单（可验收）

### P1（必须达成）

- [x] 将 `pf4boot` 任务改为明确声明式输入/输出：
  - `plugin.properties` 文件作为 `inputs`（若存在）
  - `pf4bootPlugin` 扩展关键字段（`id`、`pluginClass`、`version`、`provider`、`description`、`dependencies`、`requires`、`license`）作为 `inputs`
  - 任务产物 zip 与其路径作为 `outputs`
- [x] 统一 `plugin.properties` 的读写编码为 UTF-8。
- [x] 增强必填校验与版本校验：
  - 缺失 `plugin.id`、`plugin.class` 直接 fail
  - `plugin.version` 禁止空值/`unspecified` 等无效值
- [x] 失败信息必须包含：字段名、实际值、修复建议

### P2（建议达成）

- [x] 优化日志输出：打印 zip 产物路径、最终生效配置摘要、入包清单
- [x] 补齐 `bundle`、`bundleOnly`、`embed` 相关文档说明和排障提示
- [x] 在 `developer-guide-*` 增加“常见问题快速定位”章节

### P3（增强）

- [x] 增加本地开发场景的功能测试：  
  - 仅文件配置场景
  - 仅 extension 配置场景
- [x] 增加 failure 测试：  
  - 缺失 `plugin.id`
  - 缺失 `plugin.class`
  - `version=unspecified`

## 3. 实施规划

### 阶段一：基础稳定性

- 输出 `plugin.properties` 读写统一 UTF-8
- 落地 `pf4boot` 任务输入输出声明
- 补充最小验证测试（至少覆盖「配置变更触发 rebuild」）

### 阶段二：可诊断性

- 增强必填/版本校验
- 改造错误提示
- 增加打包日志摘要输出

### 阶段三：完整性验收

- 增补功能/失败测试
- 与 `usage-*` / `developer-guide-*` 文档联动，保持中英一致
- 输出一次本地验收清单

## 4. 验收标准（Acceptance）

- [x] 运行 `./gradlew pf4boot`，在修改 `plugin.properties` 或 `pf4bootPlugin` 配置后，任务输入变化可被识别，构建行为可预期。
- [x] `build/generated/pf4boot/plugin.properties` 与 zip 内容与当前配置一致。
- [x] `plugin.id` / `plugin.class` 缺失时，构建快速失败，并输出明确字段和修复动作。
- [x] `plugin.version=unspecified` 被拒绝并提示有效版本要求。
- [x] `bundle`/`bundleOnly`/`embed` 至少各有对应测试场景且通过。
- [x] 中文/英文两份文档条目、示例、验收与术语同步更新。

### 5. 发布前文档同步验收（本次补齐）

- [x] README 与 Usage、Developer Guide 的中文/英文内容已一致对齐；
- [x] Usage 新增 zip 产物校验与依赖打包规则说明；
- [x] Developer Guide 增补配置优先级与发布前验收流程；
- [x] 增补本地联调失败场景的排障指引。
