package com.atguigu.gmall.item.controller;

import java.io.IOException;
import java.util.concurrent.*;

public class ThreadDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        //CompletableFuture.runAsync(() -> {
        //    System.out.println("runAsync没有返回结果集");
        //    int i = 1 / 0;
        //}).whenComplete((t, u) -> {
        //    System.out.println("t: " + t);
        //    System.out.println("u: " + u);
        //});
        //
        //CompletableFuture.supplyAsync(() -> {
        //    System.out.println("supplyAsync有返回值结果");
        //    int i = 1/0;
        //    return "supplyAsync";
        //}).exceptionally(t -> {
        //    System.out.println("异常结果集： " + t);
        //    return "jj";
        //});


        CompletableFuture<String> Afuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("supplyAsync初始化子任务");
//            int i = 1 / 0;
            return "hello CompletableFuture.supplyAsync";
        });

        CompletableFuture<String> future1 = Afuture.thenApplyAsync(t -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果1：" + t);
            return "hello thenApplyAsync1";
        });
        CompletableFuture<String> future2 = Afuture.thenApplyAsync(t -> {
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果2：" + t);
            return "hello thenApplyAsync2";
        });
        CompletableFuture<Void> future3 = Afuture.thenAcceptAsync(t -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("上一个任务的返回结果3：" + t);
        });
        CompletableFuture<Void> future4 = Afuture.thenRunAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("不获取上一个任务的返回结果，也没有自己的返回结果");
        });
        CompletableFuture.anyOf(future1, future2, future3, future4).join();
        System.out.println("主线程打印模拟return");

        //new MyThread().start();
        //new Thread(new MyThread1()).start();
        //new Thread(() -> {
        //    System.out.println(Thread.currentThread().getName() + "通过实现Runnable接口初始化多线程程序，（lambda表达式）");
        //},"afsd").start();

        //try {
        //    FutureTask<String> futureTask = new FutureTask<>(new MyThread2());
        //    new Thread(futureTask).start();
        //    //get()会阻塞当前线程
        //    futureTask.cancel(false);
        //    System.out.println(futureTask.get());
        //} catch (Exception e) {
        //    System.out.println(Thread.currentThread().getName() + "子任务出现异常：" + e.getMessage());
        //    e.printStackTrace();
        //}

        //通过线程池的方式来初始化多线程程序的2种方式： Executors  ThreadPoolExecutor
        //ExecutorService executorService = Executors.newFixedThreadPool(3);
        //定时任务线程池
        //ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        ////scheduledExecutorService.scheduleAtFixedRate(() -> {
        ////    System.out.println("定时任务的线程池" + System.currentTimeMillis());
        ////}, 5, 10, TimeUnit.SECONDS);
        ////
        //ScheduledFuture<String> schedule = scheduledExecutorService.schedule(() -> {
        //    System.out.println("定时任务的线程池" + System.currentTimeMillis());
        //    return "hello";
        //}, 10, TimeUnit.SECONDS);
        //System.out.println(schedule.get());

        //ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        //for (int i = 0;i < 150;i++) {
        //    threadPoolExecutor.execute(() -> {
        //        System.out.println(Thread.currentThread().getName() + "自定义线程池初始化了线程池");
        //    });
        //}

        //scheduledExecutorService.shutdown();

        //for (int i = 0;i < 100000;i++) {
        //    Future<String> stringFuture = executorService.submit(() -> {
        //        //try {
        //        //    Thread.sleep(3000);
        //        //} catch (InterruptedException e) {
        //        //    e.printStackTrace();
        //        //}
        //        System.out.println(Thread.currentThread().getName() + "通过Executors工具初始化了固定大小的线程池！");
        //        return "hello executors" + new Random().nextInt(10);
        //    });
        //    //阻塞子线程
        //    System.out.println(stringFuture.get());
        //}
        //executorService.shutdown();
        System.out.println("主线程：" + Thread.currentThread().getName());
        System.in.read();
    }
}

class MyThread2 implements Callable<String> {
    @Override
    public String call() throws Exception {
        //int i = 1/0;
        Thread.sleep(2000);
        System.out.println("通过实现Callable接口初始化多线程程序！");
        return "hello FutureTask!";
    }
}

class MyThread1 implements Runnable {
    @Override
    public void run() {
        System.out.println("通过实现Runnable接口创建多线程程序！");
    }
}

class MyThread extends Thread {

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("通过继承Thread类来初始化多线程程序");
    }
}