package com.promising.jarvis.core.register.impl;

import com.promising.jarvis.core.executor.impl.CommandExecutor;
import com.promising.jarvis.core.executor.impl.MemoryCommandExecutor;
import com.promising.jarvis.core.register.NLRegister;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * 通过执行指令
 */
public class CommandRegister implements NLRegister {

    @Override
    public void register() {
        // 使用新的获取参数方法
        CommandRegistrationCallback.EVENT.register(
                (dispatcher,
                 registryAccess,
                 environment) -> {
                    CommandExecutor.register(dispatcher);
                    MemoryCommandExecutor.register(dispatcher);
                });
    }
}
