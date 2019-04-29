package com.joshuayingwhat.androidlearn.executors;

import java.util.concurrent.ThreadFactory;

import android.os.Process;

public class PriorityThreadFactory implements ThreadFactory {

    private int mThreadPriority;

    PriorityThreadFactory(int mThreadPriority) {
        this.mThreadPriority = mThreadPriority;
    }


    @Override
    public Thread newThread(Runnable r) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(mThreadPriority);
                r.run();
            }
        };

        return new Thread(runnable);
    }
}
