package com.MohafizDZ.framework_repository.Utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyAsyncTask<T> {
    private static final String TAG = MyAsyncTask.class.getSimpleName();

    private final Callable<T> backgroundTask;
    private final ExecutorListener<T> executorListener;

    private MyAsyncTask(
            @NonNull Callable<T> backgroundTask,
            ExecutorListener<T> executorListener) {
        this.backgroundTask = backgroundTask;
        this.executorListener = executorListener;
    }

    public void execute() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        if (executorListener != null) {
            executorListener.onPreExecute();
        }

//        Future<T> future = executorService.submit(backgroundTask);

        executorService.submit(() -> {
            try {
                T result = backgroundTask.call();
                if (executorListener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> executorListener.onPostExecute(result));
//                    executorListener.onPostExecute(result);
                }
            } catch (Exception e) {
                if (executorListener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> executorListener.onFailed(e));
//                    executorListener.onFailed(e);
                }
            }
            executorService.shutdown();
        });
    }

    public static class Builder<T> {

        private Callable<T> backgroundTask;
        private ExecutorListener<T> executorListener;

        public Builder<T> setBackgroundTask(Callable<T> backgroundTask) {
            this.backgroundTask = backgroundTask;
            return this;
        }

        public Builder<T> setExecutorListener(ExecutorListener<T> executorListener) {
            this.executorListener = executorListener;
            return this;
        }

        public MyAsyncTask<T> build() {
            return new MyAsyncTask<>(
                    backgroundTask,
                    executorListener
            );
        }
    }

    public interface ExecutorListener<T>{
        void onPreExecute();
        void onPostExecute(T result);
        void onFailed(Exception exception);
    }
}

