/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wuhulala.rpc.scanner;


import com.wuhulala.rpc.annotation.RpcReference;
import com.wuhulala.rpc.annotation.RpcService;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import static com.wuhulala.rpc.scanner.util.AnnotationUtils.getAttribute;
import static com.wuhulala.rpc.scanner.util.AnnotationUtils.resolveInterfaceName;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

/**
 * Dubbo {@link RpcService @Service} Bean Builder
 *
 * @since 2.6.5
 */
public class ServiceBeanNameBuilder {

    private static final String SEPARATOR = ":";

    // Required
    private final String interfaceClassName;

    private final Environment environment;

    // Optional
    private String version;

    private String group;

    private ServiceBeanNameBuilder(Class<?> interfaceClass, Environment environment) {
        this(interfaceClass.getName(), environment);
    }

    private ServiceBeanNameBuilder(String interfaceClassName, Environment environment) {
        this.interfaceClassName = interfaceClassName;
        this.environment = environment;
    }

    private ServiceBeanNameBuilder(AnnotationAttributes attributes, Class<?> defaultInterfaceClass, Environment environment) {
        this(resolveInterfaceName(attributes, defaultInterfaceClass), environment);
        this.group(getAttribute(attributes,"group"));
        this.version(getAttribute(attributes,"version"));
    }

    /**
     * @param attributes
     * @param defaultInterfaceClass
     * @param environment
     * @return
     * @since 2.7.3
     */
    public static ServiceBeanNameBuilder create(AnnotationAttributes attributes, Class<?> defaultInterfaceClass, Environment environment) {
        return new ServiceBeanNameBuilder(attributes, defaultInterfaceClass, environment);
    }

    public static ServiceBeanNameBuilder create(Class<?> interfaceClass, Environment environment) {
        return new ServiceBeanNameBuilder(interfaceClass, environment);
    }

    public static ServiceBeanNameBuilder create(RpcService service, Class<?> interfaceClass, Environment environment) {
        return create(getAnnotationAttributes(service, false, false), interfaceClass, environment);
    }

    public static ServiceBeanNameBuilder create(RpcReference reference, Class<?> interfaceClass, Environment environment) {
        return create(getAnnotationAttributes(reference, false, false), interfaceClass, environment);
    }

    private static void append(StringBuilder builder, String value) {
        if (StringUtils.hasText(value)) {
            builder.append(SEPARATOR).append(value);
        }
    }

    public ServiceBeanNameBuilder group(String group) {
        this.group = group;
        return this;
    }

    public ServiceBeanNameBuilder version(String version) {
        this.version = version;
        return this;
    }

    public String build() {
        StringBuilder beanNameBuilder = new StringBuilder("RpcServiceBean");
        // Required
        append(beanNameBuilder, interfaceClassName);
        // Optional
        append(beanNameBuilder, version);
        append(beanNameBuilder, group);
        // Build and remove last ":"
        String rawBeanName = beanNameBuilder.toString();
        // Resolve placeholders
        return environment.resolvePlaceholders(rawBeanName);
    }
}
