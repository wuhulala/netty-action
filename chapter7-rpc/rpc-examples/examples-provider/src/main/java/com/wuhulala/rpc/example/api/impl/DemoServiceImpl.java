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
@RpcService
public class DemoServiceImpl implements DemoService {

    @RpcReference
    private HelloService helloService;

    public DemoServiceImpl() {
        System.out.println("DemoServiceImpl init");
    }

    @Override
    public String sayHello(String name) {
        String result = helloService.sayHello(name);
        System.out.println(result);
        return "hello " + name;
    }
}
