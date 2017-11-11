package com.wuhulala.future;

import java.util.concurrent.Future;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/11
 * @description 作甚的
 */
public interface FutureFetcher {
    /**
     * 拉取数据
     *
     * @return future 例如kafka就是这种方式
     */
    Future<String> fetchData();
}
