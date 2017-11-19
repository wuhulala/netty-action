package com.wuhulala.util;

/**
 * 用来计算程序执行的时间
 *
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description 作甚的
 */
public class StopWatcher {
    private final long now;

    public StopWatcher() {
        this.now = System.currentTimeMillis();
    }

    /**
     * 计算当前时间与启动时间间隔
     *
     * @return 当前时间与启动时间间隔
     */
    public long elapsedTime(){
        long now = System.currentTimeMillis();
        return now - this.now;
    }
}
