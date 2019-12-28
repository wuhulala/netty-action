package com.wuhulala.rpc.example.config;

import com.wuhulala.rpc.scanner.util.SpringContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wuhulala<br>
 * @date 2019/12/22<br>
 * @since v1.0<br>
 */
@Configuration
public class RpcScannerConfig {

    @Bean
    public SpringContext springContext() {
        return new SpringContext();
    }


}
