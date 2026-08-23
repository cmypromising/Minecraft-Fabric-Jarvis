package com.promising.jarvis.core.agent.task;

/** Scheduling priority for agent work. Higher priority tasks are consumed first. */
public enum TaskPriority {
    LOW(0), NORMAL(10), HIGH(20);

    private final int weight;

    TaskPriority(int weight) { this.weight = weight; }

    public int weight() { return weight; }
}
