package com.promising.jarvis.core.parser;

import com.promising.jarvis.llm.deepseek.ContentResponseBody;

/** Parses one LLM reasoning turn into a structured response envelope. */
public interface NLParser {
    ContentResponseBody parse(String userRequest, String context);
}
