package com.wuhulala.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.wuhulala.rpc.bean.RpcDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.wuhulala.rpc.constants.CommonConstants.PATH_KEY;
import static com.wuhulala.rpc.constants.CommonConstants.PROTOCOL_KEY;
import static com.wuhulala.rpc.constants.RegistryConstants.CATEGORY_KEY;
import static com.wuhulala.rpc.constants.RegistryConstants.DEFAULT_CATEGORY;

/**
 * Nacos服务注册中心实现
 *
 * @author wuhulala<br>
 * @date 2019/12/21<br>
 * @since v1.0<br>
 */
public class NacosRegistry implements RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(NacosRegistry.class);

    private NamingService namingService;

    public NacosRegistry(RpcDesc url, NamingService namingService) {
        this.namingService = namingService;
    }

    @Override
    public void register(RpcDesc url) {
        final String serviceName = getServiceName(url);
        final Instance instance = createInstance(url);
        execute(namingService -> namingService.registerInstance(serviceName, instance));
    }

    private Instance createInstance(RpcDesc url) {
        // Append default category if absent
        String category = url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
        RpcDesc newURL = url.addParameter(CATEGORY_KEY, category);
        newURL = newURL.addParameter(PROTOCOL_KEY, url.getProtocol());
        newURL = newURL.addParameter(PATH_KEY, url.getPath());
        String ip = url.getHost();
        int port = url.getPort();
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setMetadata(new HashMap<>(newURL.getParameters()));
        return instance;
    }



    private String getServiceName(RpcDesc url) {
        return getServiceName(url, url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY));
    }

    private String getServiceName(RpcDesc url, String category) {
        return category + ":" + url.getColonSeparatedKey();
    }

    private void execute(NamingServiceCallback callback) {
        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getErrMsg(), e);
            }
        }
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
        final String serviceName = getServiceName(url);
        List<Instance> instances = new ArrayList<>();
        execute(namingService -> {
            instances.addAll(namingService.getAllInstances(serviceName));
        });
        logger.info("查询到服务#{}的提供者有#{}个!", serviceName, instances.size());
        return instances.stream().map(instance -> new RpcDesc(instance.getMetadata().get("protocol"), instance.getIp(), instance.getPort(), instance.getMetadata())).collect(Collectors.toList());
    }

    /**
     * {@link NamingService} Callback
     *
     * @since 2.6.5
     */
    interface NamingServiceCallback {

        /**
         * Callback
         *
         * @param namingService {@link NamingService}
         * @throws NacosException
         */
        void callback(NamingService namingService) throws NacosException;

    }
}
