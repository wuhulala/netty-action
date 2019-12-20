package com.wuhulala.rpc.core;

import com.wuhulala.rpc.LifeCycle;

/**
 * @author wuhulala<br>
 * @date 2019/12/17<br>
 * @since v1.0<br>
 */
public class RpcBootstrap implements LifeCycle {

    @Override
    public void start() {
        // 1. 扫描提供者，实例化到container，并且注册到注册中心

        // 2. 扫描消费者，看是否需要创建本地实例

        // 3. 启动结束
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
