package com.wuhulala.rpc.example;

import com.wuhulala.rpc.scanner.annotation.RpcComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wuhulala<br>
 * @date 2019/12/20<br>
 * @since v1.0<br>
 */
@SpringBootApplication
@RpcComponentScan("com.wuhulala.rpc.example.controller")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
//        RpcBootstrap bootstrap = new RpcBootstrap();
//        bootstrap.start();
    }

}
