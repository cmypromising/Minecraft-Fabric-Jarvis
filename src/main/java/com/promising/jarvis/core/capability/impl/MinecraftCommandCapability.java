package com.promising.jarvis.core.capability.impl;

import com.promising.jarvis.core.capability.Capability;
import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.core.executor.impl.CommandSafetyPolicy;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import net.minecraft.text.Text;

/** Executes validated Minecraft commands returned by the model. */
public final class MinecraftCommandCapability implements Capability {
    public String id() { return "minecraft.command"; }
    public boolean supports(ContentResponseBody response) {
        return response != null && ("minecraft.command".equals(response.getCapability())
                || (response.getCapability() == null || response.getCapability().isBlank())
                && Integer.valueOf(1).equals(response.getType()));
    }
    public void execute(CommandContext context, ContentResponseBody response) {
        if (!CommandSafetyPolicy.isAllowed(response.getCommand())) {
            context.source().sendError(Text.of("Jarvis 生成的命令未通过安全检查。"));
            return;
        }
        context.source().getServer().getCommandManager().executeWithPrefix(context.source(), response.getCommand());
        context.source().sendMessage(Text.of("Jarvis: " + response.getAdditionalInfo()));
    }
}
