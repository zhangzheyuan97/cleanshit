package com.meritdata.dam.datapacket.plan.factory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author fanpeng
 * @Date 2023/4/23
 * @Describe 线程处理类
 */
public class ExecutorProcessPool {
    private ExecutorService executor;
    private static ExecutorProcessPool pool = new ExecutorProcessPool();

    private ExecutorProcessPool() {
        executor = Executors.newCachedThreadPool();
    }

    public static ExecutorProcessPool getInstance() {
        return pool;
    }

    /**
     * 关闭线程池，调用关闭线程池的方法后，线程也会执行完队列中的所有任务才会退出
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 提交任务到线程池，可以接收线程返回值
     *
     * @param task
     * @return
     */
    public Future<?> submit(Callable<?> task) {
        return executor.submit(task);
    }

    /**
     * 提交任务到线程池，可以接收线程返回值
     *
     * @param task
     * @return
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * 提交任务到线程池，无返回值
     *
     * @param task
     */
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
