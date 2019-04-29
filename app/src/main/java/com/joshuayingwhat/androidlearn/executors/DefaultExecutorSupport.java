package com.joshuayingwhat.androidlearn.executors;

import android.os.Process;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultExecutorSupport {

    private final ThreadPoolExecutor forBackGroundTasks;
    private final MainThreadExecutor mMainThreadExecutor;

    public DefaultExecutorSupport getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final DefaultExecutorSupport INSTANCE = new DefaultExecutorSupport();
    }

    private DefaultExecutorSupport() {

        /**
         * 获取cpu核心数
         */
        final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

        /**
         * 设置核心线程数
         */
        final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

        /**
         * 设置最大线程数
         */
        final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2 + 1;

        final long KEEP_ALIVE_SECONDS = 60L;

        /**
         * 设置阻塞队列
         */
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

        /**
         * 设置线程优先级
         */
        PriorityThreadFactory priorityThreadFactory = new PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);

        /**
         * 初始化后台任务线程池
         */
        forBackGroundTasks = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS, queue, priorityThreadFactory);

        /**
         *  设置UI线程池
         */
        mMainThreadExecutor = new MainThreadExecutor();
    }
}
