# Agent Task 模型

`AgentTask` 是 Agent 系统中的统一工作单元，不再把请求、提示词和 Future 隐藏在具体 Agent 的私有记录中。

任务包含：

- `id`：可观测、可关联日志的唯一标识。
- `type`：任务类型，例如 `llm.request`、`planner.request`。
- `CommandContext`：玩家、世界和原始请求上下文。
- `promptContext`：当前任务的初始上下文。
- `metadata`：扩展字段，不改变核心接口即可加入来源、追踪号等信息。
- `priority`：低、普通、高优先级。
- `createdAt/deadline`：排队和执行超时边界。
- `maxReasoningSteps`：ReAct 工具调用上限。
- `TaskStatus`：排队、运行、成功、失败、取消、过期。
- `CompletableFuture`：异步结果交付。

Agent 只负责消费任务和推进状态，未来可复用该模型实现规划 Agent、工具 Agent、重试队列和任务观测，而不需要重新设计请求载体。
