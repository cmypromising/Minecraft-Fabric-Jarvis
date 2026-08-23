package com.promising.jarvis.core.capability.impl;

import com.promising.jarvis.core.capability.Capability;
import com.promising.jarvis.core.context.CommandContext;
import com.promising.jarvis.llm.deepseek.ContentResponseBody;
import net.minecraft.text.Text;

/** Handles informational and conversational responses. */
public final class InformationalResponseCapability implements Capability {
    public String id() { return "minecraft.information"; }
    public boolean supports(ContentResponseBody response) {
        return response != null && ("minecraft.information".equals(response.getCapability())
                || (response.getCapability() == null || response.getCapability().isBlank())
                && (Integer.valueOf(2).equals(response.getType()) || Integer.valueOf(3).equals(response.getType())));
    }
    public void execute(CommandContext context, ContentResponseBody response) {
        context.source().sendMessage(Text.of("Jarvis: " + response.getAdditionalInfo()));
    }
}
