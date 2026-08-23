package com.promising.jarvis.core.perception;

import net.minecraft.server.command.ServerCommandSource;

public final class GamePhaseContextTool implements com.promising.jarvis.core.context.ContextTool {
    private final GamePhaseClassifier classifier = new GamePhaseClassifier();
    public String name() { return "player.game_phase"; }
    public String description() { return "推断玩家当前游戏阶段，并返回置信度和判断证据"; }
    public String execute(ServerCommandSource source, String arguments) {
        if (source == null || source.getPlayer() == null) return "玩家不可用";
        var result = classifier.classify(source.getPlayer());
        return String.format("[游戏阶段推断] 阶段=%s，置信度=%.2f，证据=%s。该阶段是启发式推断，不是游戏事实。",
                result.phase(), result.confidence(), String.join("、", result.evidence()));
    }
}
