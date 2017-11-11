package com.wuhulala.future;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/11
 * @description 作甚的
 */
public class FutureExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Runnable task1 = () -> {
            System.out.println("==========task1===========");
        };

        Callable<Integer> task2 = () -> {
            System.out.println("======== task2 =========");
            return 1;
        };

        Future<?> future1 = executor.submit(task1);
        Future<Integer> future2 = executor.submit(task2);
        while (!future1.isDone() || !future2.isDone()) {
            // do something else
            System.out.println("something is never done!!!");
            if(future1.isDone() && future2.isDone()){
                System.out.println("everything is done");
            }
        }

    }
}
