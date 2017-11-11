package com.wuhulala.netty;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/11
 * @description 作甚的
 */
public interface Fetcher {
    /**
     * 拉取数据接口
     *
     * @param callback 回调函数
     */
    void fetchData(FetchCallback callback);
}
