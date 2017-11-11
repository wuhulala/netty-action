package com.wuhulala.netty;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/11
 * @description 作甚的
 */
public interface FetchCallback {

    /**
     * 接收数据回调函数
     *
     * @param data 数据
     */
    void onData(String data);

    /**
     * 出错时回调函数
     *
     * @param cause 错误
     */
    void onError(Throwable cause);
}
