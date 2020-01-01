package com.wuhulala.rpc.example.api.impl;

import com.wuhulala.rpc.annotation.RpcReference;
import com.wuhulala.rpc.annotation.RpcService;
import com.wuhulala.rpc.example.api.HelloService;
import org.apache.dubbo.demo.DemoService;

/**
 * @author wuhulala<br>
 * @date 2019/12/20<br>
 * @since v1.0<br>
 */
//@Component // TODO 自动注册RpcService
@RpcService
public class HelloServiceImpl implements DemoService {

    @RpcReference
    private HelloService helloService;

    @Override
    public String sayHello(String name) {
        String result = helloService.sayHello(name);
        return "hello " + name;
    }
}
