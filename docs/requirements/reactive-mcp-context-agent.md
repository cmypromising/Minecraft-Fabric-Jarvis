# ReAct MCP 风格动态上下文 Agent

## 需求

玩家通过 `/nlp <text>` 提出需求后，Jarvis 的 LLM Agent 应先判断是否缺少实时 Minecraft 事实；需要时调用只读上下文工具，将工具结果作为 observation 回灌给模型，最后生成能力响应。

## 功能拆解

1. 建立本地 MCP 风格上下文工具注册表，支持工具名称、描述、发现和调用。
2. 将位置、时间天气、世界规则、玩家状态注册为只读工具。
3. 将单线程 LLM Agent 扩展为 ReAct 循环：思考请求、调用工具、回灌结果、生成最终响应。
4. 工具读取必须在 Minecraft 主线程执行，LLM 网络调用必须在专属 Agent 线程执行。
5. 限制单次请求最多 4 次工具调用，未知工具返回可解释结果，避免无限循环。
6. 明确中间响应协议：`capability=context.tool`、`tool`、`tool_arguments`；最终响应继续使用现有能力协议。

## 非目标

- 本版本不实现外部 MCP Server、网络工具或任意命令执行工具。
- 不允许 LLM 直接执行上下文工具以外的 Minecraft 操作。

## 验收标准

- “我现在在哪儿”等需求能够通过位置工具获取事实后再回答。
- 工具执行发生在 Minecraft 主线程，LLM 调用发生在 `jarvis-llm-agent` 线程。
- 工具注册、ReAct 闭环、协议识别和失败边界有自动化测试。
- `gradlew test`、`gradlew build`、`git diff --check` 全部通过。
