package com.wuhulala.rpc.scanner;

import com.wuhulala.rpc.annotation.RpcService;
import com.wuhulala.rpc.bean.RpcDesc;
import com.wuhulala.rpc.exception.RpcExeception;
import com.wuhulala.rpc.scaner.ServiceScanner;
import com.wuhulala.rpc.scanner.util.SpringContext;
import com.wuhulala.rpc.util.ClassUtils;
import com.wuhulala.rpc.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wuhulala.rpc.bean.RpcDesc.COMMA_SPLIT_PATTERN;
import static com.wuhulala.rpc.constants.CommonConstants.*;

/**
 * RpcService 扫描器
 *
 * @author wuhulala<br>
 * @date 2019/12/22<br>
 * @since v1.0<br>
 */
public class RpcServiceAnnotationScanner implements ServiceScanner {

    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

    private static final Logger logger = LoggerFactory.getLogger(RpcServiceAnnotationScanner.class);

    @Nullable
    private ResourcePatternResolver resourcePatternResolver;

    public RpcServiceAnnotationScanner() {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
    }

    @Override
    public List<RpcDesc> scan(String packageName) {
        logger.info("scan {} used RpcServiceAnnotationScanner", packageName);
        Set<String> packages = resolvePackagesToScan(packageName);
        List<RpcDesc> result = new LinkedList<>();
        for (String packageToScan : packages) {
            result.addAll(doScanRpcServices(packageToScan));
        }
        return result;
    }

    @Override
    public <T> T getInvoker(Class<T> clazz) {
        return SpringContext.getBean(clazz);
    }

    @Nullable
    public ResourcePatternResolver getResourcePatternResolver() {
        return resourcePatternResolver;
    }

    public void setResourcePatternResolver(@Nullable ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    private Set<RpcDesc> doScanRpcServices(String basePackage) {
        Set<Class<?>> classes = ClassUtils.getClassSetByAnnotation(basePackage, RpcService.class);
        System.out.println(classes);
        return classes.stream()
                .flatMap(clazz -> parseRpcDesc(clazz).stream())
                .collect(Collectors.toSet());
    }

    private Set<RpcDesc> parseRpcDesc(Class<?> clazz) {
        Set<RpcDesc> set = new LinkedHashSet<>();
        Class<?>[] interfaces = clazz.getInterfaces();
        RpcService rpcService = clazz.getAnnotation(RpcService.class);
        RpcDesc desc = null;
        String[] protocols = rpcService.protocol();
        if (protocols.length == 0) {
            protocols = new String[]{ConfigUtils.getProperty("rpc.protocol")};
        }
        String host = ConfigUtils.getProperty("rpc.host");
        int port = Integer.parseInt(ConfigUtils.getProperty("rpc.port", "9001"));
        for (Class<?> interfaceClazz : interfaces) {
            for (String protocol : protocols) {
                String interfaceName = interfaceClazz.getName();
                desc = new RpcDesc(protocol, host, port)
                        .addParameter(INTERFACE_KEY, interfaceName)
                        .addParameter(GROUP_KEY, ConfigUtils.getProperty("rpc.group", "default"))
                        .addParameter(VERSION_KEY, ConfigUtils.getProperty("rpc.version", "v"));
                set.add(desc);
            }
        }
        return set;
    }

    protected String resolveBasePackage(String basePackage) {
        return org.springframework.util.ClassUtils.convertClassNameToResourcePath(basePackage);
    }


    private Set<String> resolvePackagesToScan(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            throw new RpcExeception("未配置扫描路径");
        }
//        String resolvedPackageName = environment.resolvePlaceholders(packageName);
        return Stream.of(COMMA_SPLIT_PATTERN.split(packageName)).collect(Collectors.toSet());
    }

}
