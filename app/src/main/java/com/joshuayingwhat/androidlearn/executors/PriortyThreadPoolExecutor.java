package com.joshuayingwhat.androidlearn.executors;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 扩展threadPoolExecutor
 * 可以设置线程执行的优先级  不懂这个是怎么设置优先级的
 *
 * @author joshuayingwhat
 */
public class PriortyThreadPoolExecutor extends ThreadPoolExecutor {
    public PriortyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                     TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    public Future<?> submit(Runnable task) {
        PriortyFutureTask futureTask = new PriortyFutureTask((PriortyRunnable) task);
        execute(futureTask);
        return futureTask;
    }

    private static final class PriortyFutureTask extends FutureTask<PriortyRunnable>
            implements Comparable<PriortyFutureTask> {

        private final PriortyRunnable priortyRunnable;

        PriortyFutureTask(PriortyRunnable priortyRunnable) {
            super(priortyRunnable, null);
            this.priortyRunnable = priortyRunnable;
        }

        @Override
        public int compareTo(PriortyFutureTask o) {
            Priority priority1 = priortyRunnable.getPriority();
            Priority priority2 = o.priortyRunnable.getPriority();
            Log.e("tag", "priority1=" + priority1 + "," + "priorty2=" + priority2 + "," + "比较结果:" + (priority2.ordinal() - priority1.ordinal()));
            return priority2.ordinal() - priority1.ordinal();
        }
    }
}
