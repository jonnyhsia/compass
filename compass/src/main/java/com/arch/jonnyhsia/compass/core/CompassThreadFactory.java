package com.arch.jonnyhsia.compass.core;

import android.util.Log;

import androidx.annotation.NonNull;

import com.arch.jonnyhsia.compass.Compass;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CompassThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final ThreadGroup group;
    private final String namePrefix;

    public CompassThreadFactory() {
        group = Thread.currentThread().getThreadGroup();
        namePrefix = "Compass task pool No." + poolNumber.getAndIncrement() + ", thread No.";
    }

    @Override
    public Thread newThread(final Runnable r) {
        String threadName = namePrefix + threadNumber.getAndIncrement();
        Thread thread = new Thread(group, r, threadName, 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull final Thread t, @NonNull final Throwable e) {
                String msg = "An uncaught exception happened! " +
                        "Thread[" + thread.getName() + "], " +
                        "caused by [" + e.getMessage() + "]";
                Log.d(Compass.TAG, msg);
            }
        });
        return thread;
    }
}
