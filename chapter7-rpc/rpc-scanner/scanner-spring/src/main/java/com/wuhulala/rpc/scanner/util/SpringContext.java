package com.wuhulala.rpc.scanner.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author wuhulala<br>
 * @date 2019/12/28<br>
 * @since v1.0<br>
 */
public class SpringContext implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringContext.class);

    private static ApplicationContext applicationContext;

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logger.info("<<<<<<scanner-spring#context init success >>>>>>");
        this.applicationContext = applicationContext;
    }


}
