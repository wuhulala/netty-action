package com.wuhulala.rpc.example.api.impl;

import com.wuhulala.rpc.annotation.RpcService;
import org.apache.dubbo.demo.DemoService;
import org.springframework.stereotype.Component;

/**
 * @author wuhulala<br>
 * @date 2019/12/20<br>
 * @since v1.0<br>
 */
@Component // TODO 自动注册RpcService
@RpcService
public class HelloServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
