package com.wuhulala.rpc.core;

import com.alibaba.cooma.ExtensionLoader;
import com.wuhulala.rpc.LifeCycle;
import com.wuhulala.rpc.bean.RpcDesc;
import com.wuhulala.rpc.exception.RpcException;
import com.wuhulala.rpc.registry.RegistryFactory;
import com.wuhulala.rpc.registry.RegistryService;
import com.wuhulala.rpc.scaner.ServiceScanner;
import com.wuhulala.rpc.server.Server;
import com.wuhulala.rpc.util.ConfigUtils;
import com.wuhulala.rpc.util.PropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wuhulala.rpc.core.constants.RpcConfigConstants.RPC_SCANNER_PACKAGE_TEMPLATE;
import static com.wuhulala.rpc.core.constants.RpcConfigConstants.RPC_SCANNER_TYPE;

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
        ConfigUtils.setProperties(rpcProps);

        init(rpcProps);

        // 0.2

        // 1. 扫描提供者，注册到注册中心
        List<RpcDesc> rpcDescs = scanRpcDescList(rpcProps);
        saveToRegisterCenter(rpcDescs);

        // 2. 扫描消费者，看是否需要创建本地实例
        // 读取所有ReferenceBean 初始化


        // 3. 对外提供服务
        try {
            openServer();
        } catch (Throwable e) {
            logger.error("启动服务器失败", e);
            return;
        }

        // 4. 启动结束
        while (true){}
    }

    private void openServer() throws Throwable {
        ExtensionLoader.getExtensionLoader(Server.class).getExtension(ConfigUtils.getProperty("rpc.server.type")).open();
    }

    private void saveToRegisterCenter(List<RpcDesc> rpcDescs) {
        logger.info("开始将服务注册到注册中心...");
        REGISTRY_CACHE.forEach(registry -> {
            rpcDescs.forEach(registry::register);
            logger.info("服务注册到注册中心{}成功，并成功注册了{}个服务", registry.getClass(), rpcDescs.size());
        });
    }

    private List<RpcDesc> scanRpcDescList(Properties rpcProps) {
        String scanTypes = Optional.ofNullable(rpcProps.getProperty(RPC_SCANNER_TYPE))
                .orElseThrow(() -> new RpcException("未配置服务扫描器类型"));
        logger.info("scan service use [{}] Scanner ", scanTypes);
        return Stream.of(RpcDesc.COMMA_SPLIT_PATTERN.split(scanTypes)).flatMap(scanType -> {
            String packageName = rpcProps.getProperty(MessageFormat.format(RPC_SCANNER_PACKAGE_TEMPLATE, scanType));
            return ExtensionLoader.getExtensionLoader(ServiceScanner.class)
                    .getExtension(scanType)
                    .scan(packageName)
                    .stream();
        }).collect(Collectors.toList());
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
                .orElseThrow(() -> new RpcException("未配置注册中心"));
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
