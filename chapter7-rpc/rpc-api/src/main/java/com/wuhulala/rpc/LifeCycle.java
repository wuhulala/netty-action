package com.wuhulala.rpc;

/**
 * @author wuhulala<br>
 * @date 2019/12/17<br>
 * @since v1.0<br>
 */
public interface LifeCycle {

    void start();

    void stop();

    boolean isRunning();

}
