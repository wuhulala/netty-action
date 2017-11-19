package com.wuhulala.util;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/17
 * @description 作甚的
 */
public class TimeWatcher {
    private long start;

    public TimeWatcher(){
        this.start = System.currentTimeMillis();
    }

    public long interval(){
        long now = System.currentTimeMillis();
        long result = now- this.start;
        this.start = now;
        return result;
    }
}
