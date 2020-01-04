package com.wuhulala.rpc.example.controller;

import com.wuhulala.rpc.annotation.RpcReference;
import org.apache.dubbo.demo.DemoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wuhulala<br>
 * @date 2019/12/20<br>
 * @since v1.0<br>
 */
@RestController
public class HelloController {

    @RpcReference
    private DemoService demoService;

    @RequestMapping("hello/{name}")
    public String hello(@PathVariable String name){
        System.out.println(name);
        return demoService.sayHello(name);
    }

}
