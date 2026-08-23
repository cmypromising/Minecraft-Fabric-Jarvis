# Prompt Governance

Jarvis 的提示词属于可版本化的产品配置，而不是散落在 Java 代码中的字符串。

## 目录契约

提示词资源位于 `assets/jarvis/llm/system-prompts.json`，顶层必须包含：

- `schema_version`：提示词目录格式版本。
- `prompts`：按稳定 key 管理的提示词集合。

每个 prompt 必须包含：

- `version`：该提示词的语义版本。
- `description`：用途和变更意图。
- `messages`：有序消息列表；当前只允许 `system` 角色。

## 变更要求

- 行为、协议或安全边界变化时升级 prompt `version`。
- 目录格式变化时升级 `schema_version`，并同步加载器和迁移说明。
- 不在运行时代码中重复定义同一套系统提示词。
- 提示词必须明确输出协议、工具边界、权限边界、失败行为和不确定性处理。
- API Token、玩家隐私数据和内部实现细节不得写入提示词资源或日志。

## 校验与测试

`SystemPromptLoader` 在启动时校验资源完整性、schema 版本、prompt 元数据、消息角色和非空内容。测试必须至少验证：

1. 当前目录可加载并包含主 prompt。
2. 主 prompt 包含 ReAct 工具协议和最终能力协议。
3. 缺失 prompt 返回显式 absent，而不是伪造默认内容。

提示词发布前应记录 prompt 版本、兼容的响应协议和回滚版本。
