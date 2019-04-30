package com.joshuayingwhat.androidlearn.executors;

public class PriortyRunnable implements Runnable {

    private final Priority priority;

    public PriortyRunnable(Priority priority) {
        this.priority = priority;
    }

    @Override
    public void run() {

    }

    Priority getPriority() {
        return priority;
    }
}
