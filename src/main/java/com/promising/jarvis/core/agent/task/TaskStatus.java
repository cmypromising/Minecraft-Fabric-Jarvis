package com.promising.jarvis.core.agent.task;

/** Lifecycle states of a submitted agent task. */
public enum TaskStatus {
    QUEUED, RUNNING, SUCCEEDED, FAILED, CANCELLED, EXPIRED
}
