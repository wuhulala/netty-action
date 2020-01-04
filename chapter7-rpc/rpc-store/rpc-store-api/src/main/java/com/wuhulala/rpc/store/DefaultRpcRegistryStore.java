package com.wuhulala.rpc.store;

import com.wuhulala.rpc.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author wuhulala<br>
 * @date 2020/1/4<br>
 * @since v1.0<br>
 */
public class DefaultRpcRegistryStore implements RpcRegistryStore {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRpcRegistryStore.class);

    private Map<String, RegistryService> REGISTRY_SERVICE_CACHE;

    @Override
    public void init() {
        REGISTRY_SERVICE_CACHE = new ConcurrentHashMap<>(64);
        logger.info(">>>>>>>>> DefaultRpcRegistryStore 初始化成功.");
    }

    @Override
    public void clear() {
        REGISTRY_SERVICE_CACHE.clear();
    }

    @Override
    public Optional<RegistryService> get(String key) {
        return Optional.ofNullable(REGISTRY_SERVICE_CACHE.get(key));
    }

    @Override
    public void put(String key, RegistryService registryService) {
        REGISTRY_SERVICE_CACHE.put(key, registryService);
    }

    @Override
    public RegistryService remove(String key) {
        return REGISTRY_SERVICE_CACHE.remove(key);
    }

    @Override
    public int size() {
        return REGISTRY_SERVICE_CACHE.size();
    }

    @Override
    public Collection<RegistryService> findAll() {
        return REGISTRY_SERVICE_CACHE.values();
    }

    @Override
    public Collection<RegistryService> get(String[] registry) {
        return Arrays.stream(registry)
                .map(each -> REGISTRY_SERVICE_CACHE.get(each))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
