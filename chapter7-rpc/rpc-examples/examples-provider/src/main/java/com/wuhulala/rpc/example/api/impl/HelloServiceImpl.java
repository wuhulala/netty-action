package com.wuhulala.rpc.example.api.impl;

import com.wuhulala.rpc.annotation.RpcService;
import com.wuhulala.rpc.example.api.HelloService;

/**
 * @author wuhulala<br>
 * @date 2019/12/20<br>
 * @since v1.0<br>
 */
@RpcService
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
