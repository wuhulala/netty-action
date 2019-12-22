package com.wuhulala.rpc.example;

import com.wuhulala.rpc.core.RpcBootstrap;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author wuhulala<br>
 * @date 2019/12/22<br>
 * @since v1.0<br>
 */
@ComponentScan
public class ProviderApplication {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ProviderApplication.class);
        context.start();
        RpcBootstrap bootstrap = new RpcBootstrap();
        bootstrap.start();
    }

}
