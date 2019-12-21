package com.wuhulala.rpc.core;

import com.alibaba.cooma.ExtensionLoader;
import com.wuhulala.rpc.LifeCycle;
import com.wuhulala.rpc.bean.RpcDesc;
import com.wuhulala.rpc.exception.RpcExeception;
import com.wuhulala.rpc.registry.RegistryFactory;
import com.wuhulala.rpc.registry.RegistryService;
import com.wuhulala.rpc.util.PropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * @author wuhulala<br>
 * @date 2019/12/17<br>
 * @since v1.0<br>
 */
public class RpcBootstrap implements LifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(RpcBootstrap.class);
    public static final String DEFAULT_RPC_PROPERTIES_PATH = "rpc.properties";

    private String propPath;

    private Set<RegistryService> REGISTRY_CACHE;

    public RpcBootstrap() {
        this.propPath = DEFAULT_RPC_PROPERTIES_PATH;
        REGISTRY_CACHE = new HashSet<>();
    }

    public RpcBootstrap(String propPath) {
        this.propPath = propPath;
    }

    @Override
    public void start() {
        logger.info("init configuration, props path is {}", propPath);
        // 0. 初始化配置
        Properties rpcProps = loadProperties(propPath);

        init(rpcProps);

        // 0.2

        // 1. 扫描提供者，注册到注册中心

        // 2. 扫描消费者，看是否需要创建本地实例

        // 3. 启动结束
    }

    private void init(Properties rpcProps) {
        // 0.1 初始化注册中心
        initRegistry(rpcProps);

        // 0.2
    }

    private void initRegistry(Properties rpcProps) {
        String registryAddr = rpcProps.getProperty("rpc.registry.url");
        logger.info("init registry of address {}", registryAddr);

        String registryDescStr = Optional.ofNullable(registryAddr)
                .orElseThrow(() -> new RpcExeception("未配置注册中心"));
        RpcDesc registryDesc = RpcDesc.valueOf(registryDescStr);
        RegistryService registry = findRegistryFactory(registryDesc);
        REGISTRY_CACHE.add(registry);
    }

    private RegistryService findRegistryFactory(RpcDesc registryDesc) {
        String registryProtocol = registryDesc.getProtocol();
        return ExtensionLoader.getExtensionLoader(RegistryFactory.class)
                .getExtension(registryProtocol)
                .createRegistry(registryDesc);
    }

    private Properties loadProperties(String propPath) {
        return PropsUtil.loadProps(propPath);
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    public String getPropPath() {
        return propPath;
    }

    public void setPropPath(String propPath) {
        this.propPath = propPath;
    }
}
