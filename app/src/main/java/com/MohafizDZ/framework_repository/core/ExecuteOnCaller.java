package com.MohafizDZ.framework_repository.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

public class ExecuteOnCaller implements Executor {
    public final Handler handler = threadLocalHandler.get();

    private static ThreadLocal<Handler> threadLocalHandler = new ThreadLocal<Handler>() {
        @Override
        protected Handler initialValue() {
            Looper looper = Looper.myLooper();
            if (looper == null)
                looper = Looper.getMainLooper();
            return new Handler(looper);
        }
    };
    @Override
    public void execute(Runnable command) {
        handler.post(command);
    }
}
