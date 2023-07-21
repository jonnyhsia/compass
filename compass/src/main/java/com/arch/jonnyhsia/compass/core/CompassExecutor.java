package com.arch.jonnyhsia.compass.core;

import android.util.Log;

import com.arch.jonnyhsia.compass.Compass;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CompassExecutor extends ThreadPoolExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int INIT_THREAD_COUNT = CPU_COUNT + 1;
    private static final int MAX_THREAD_COUNT = INIT_THREAD_COUNT;
    private static final long SURPLUS_THREAD_LIFE = 30L;

    private static volatile CompassExecutor INSTANCE;

    public static CompassExecutor getInstance() {
        if (INSTANCE == null) {
            synchronized (CompassExecutor.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CompassExecutor(
                            INIT_THREAD_COUNT, MAX_THREAD_COUNT,
                            SURPLUS_THREAD_LIFE, TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(32),
                            new CompassThreadFactory()
                    );
                }
            }
        }
        return INSTANCE;
    }

    public CompassExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
                Log.d(Compass.TAG, "Too many tasks!");
            }
        });
    }

}
