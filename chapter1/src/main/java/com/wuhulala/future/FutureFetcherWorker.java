package com.wuhulala.future;

import com.wuhulala.Worker;

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
public class FutureFetcherWorker implements Worker{
    ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void doWork() {
        FutureFetcher fetcher = new FutureFetcher() {
            @Override
            public Future<String> fetchData() {
                Callable<String> task = () -> {
                    System.out.println("======== task2 =========");
                    return "nbnbnbnb";
                };

                return executor.submit(task);
            }
        };

        Future<String> future = fetcher.fetchData();

        try {
            while(!future.isDone()) {
                System.out.println("waiting ...");
            }
            System.out.println("Data received: " + future.get());
        } catch (Throwable cause) {
            System.err.println("An error accour: " + cause.getMessage());
        }
    }

    public static void main(String[] args) {
        FutureFetcherWorker worker = new FutureFetcherWorker();
        worker.doWork();
    }
}
