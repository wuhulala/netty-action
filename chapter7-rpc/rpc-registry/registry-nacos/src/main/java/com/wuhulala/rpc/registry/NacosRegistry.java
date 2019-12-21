package com.wuhulala.rpc.registry;

import com.alibaba.nacos.api.naming.NamingService;
import com.wuhulala.rpc.bean.RpcDesc;
import java.util.List;

/**
 * Nacos服务注册中心实现
 *
 * @author wuhulala<br>
 * @date 2019/12/21<br>
 * @since v1.0<br>
 */
public class NacosRegistry implements RegistryService {

    private NamingService namingService;

    public NacosRegistry(RpcDesc url, NamingService namingService) {
        this.namingService = namingService;
    }

    @Override
    public void register(RpcDesc url) {

    }

    @Override
    public void unregister(RpcDesc url) {

    }

    @Override
    public void subscribe(RpcDesc url, NotifyListener listener) {

    }

    @Override
    public void unsubscribe(RpcDesc url, NotifyListener listener) {

    }

    @Override
    public List<RpcDesc> lookup(RpcDesc url) {
        return null;
    }
}
