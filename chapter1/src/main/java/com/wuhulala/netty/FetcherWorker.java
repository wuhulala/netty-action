package com.wuhulala.netty;

import com.wuhulala.Worker;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/11
 * @description 作甚的
 */
public class FetcherWorker implements Worker{

    @Override
    public void doWork(){
        Fetcher fetcher = callback -> {
            try {
                callback.onData("啦啦啦啦啦");
                throw new Exception("网络错误");
            }catch (Exception e){
                callback.onError(e);
            }
        };

        FetchCallback callback = new FetchCallback() {
            @Override
            public void onData(String data) {
                System.out.println("接收数据：" + data);
            }

            @Override
            public void onError(Throwable cause) {
                System.out.println("失败了，失败了" + cause.getMessage());
            }
        };

        fetcher.fetchData(callback);
    }

    public static void main(String[] args) {
        FetcherWorker worker = new FetcherWorker();
        worker.doWork();
    }
}
