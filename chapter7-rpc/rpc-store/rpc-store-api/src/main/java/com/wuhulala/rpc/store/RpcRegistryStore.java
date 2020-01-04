package com.wuhulala.rpc.store;

import com.alibaba.cooma.Extension;
import com.wuhulala.rpc.registry.RegistryService;
import java.util.Collection;
import java.util.Optional;

/**
 * Store all @{@link RegistryService}
 *
 * @author wuhulala<br>
 * @date 2020/1/4<br>
 * @since v1.0<br>
 */
@Extension("default")
public interface RpcRegistryStore extends RpcStore {

    Optional<RegistryService> get(String key);

    void put(String key, RegistryService registryService);

    RegistryService remove(String key);

    int size();

    Collection<RegistryService> findAll();

    Collection<RegistryService> get(String[] registry);
}
