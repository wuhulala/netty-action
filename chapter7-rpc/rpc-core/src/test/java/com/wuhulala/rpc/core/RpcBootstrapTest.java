package com.wuhulala.rpc.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author wuhulala<br>
 * @date 2019/12/21<br>
 * @since v1.0<br>
 */
public class RpcBootstrapTest {
    RpcBootstrap bootstrap;

    @Before
    public void init() {
        bootstrap = new RpcBootstrap();
    }

    @Test
    public void start() {
        bootstrap.start();
    }

    @Test
    public void stop() {
    }

    @Test
    public void isRunning() {
    }
}