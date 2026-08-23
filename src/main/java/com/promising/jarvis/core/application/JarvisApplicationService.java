package com.promising.jarvis.core.application;

import com.promising.jarvis.core.context.CommandContext;

/** Application boundary for all intelligent command-line requests. */
public interface JarvisApplicationService { void submit(CommandContext context); }
