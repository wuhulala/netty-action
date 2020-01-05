package com.wuhulala.rpc.server;

/**
 * @author wuhulala<br>
 * @date 2019/12/23<br>
 * @since v1.0<br>
 */

import com.alibaba.cooma.Extension;

@Extension
public interface Server {

    void open() throws Throwable;

    void close();
}
