package com.wuhulala.rpc.scanner.bean;

import com.alibaba.cooma.ExtensionLoader;
import com.wuhulala.rpc.bean.RpcDesc;
import com.wuhulala.rpc.bean.RpcInvocation;
import com.wuhulala.rpc.client.Client;
import com.wuhulala.rpc.client.ClientFactory;
import com.wuhulala.rpc.protocol.invoker.RpcInvoker;
import com.wuhulala.rpc.registry.RegistryService;
import com.wuhulala.rpc.store.util.RpcCompHolder;
import com.wuhulala.rpc.util.ConfigUtils;
import lombok.Getter;
import lombok.Setter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.wuhulala.rpc.constants.CommonConstants.*;

/**
 * @author wuhulala<br>
 * @date 2019/12/29<br>
 * @since v1.0<br>
 */
public class RpcReferenceBean<T> {

    @Getter
    @Setter
    private Class<T> interfaceClass;

    @Getter
    @Setter
    private Collection<RegistryService> registryServices;

    @Getter
    @Setter
    private String group;

    @Getter
    @Setter
    private String version;

    /**
     * 服务实现代理
     */
    @Getter
    @Setter
    private T ref;
    /**
     * 协议名称
     */
    @Getter
    @Setter
    private String protocol;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String host;

    @Getter
    @Setter
    private int port;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private Class<?> type;

    @Getter
    @Setter
    private String[] registry;

    @Getter
    @Setter
    private String server;

    private volatile boolean initialized;

    public void init() {
        if (initialized) {
            return;
        }

        initCommonProp();

        setRegistry();

        ref = createServiceProxy();

        initialized = true;
    }

    private void initCommonProp() {
        if (this.protocol == null) {
            this.protocol = ConfigUtils.getProtocol();
        }
        if (this.group == null) {
            this.group = ConfigUtils.getDefaultGroup();
        }
        if (this.version == null) {
            this.version = ConfigUtils.getDefaultVersion();
        }
        if (this.server == null) {
            this.server = ConfigUtils.getDefaultServer();
        }
    }


    private void setRegistry() {

        if (registry == null) {
            // set all
            this.registryServices = RpcCompHolder.getRpcRegistryStore().findAll();
            return;
        }
        setRegistryServices(RpcCompHolder.getRpcRegistryStore().get(registry));
    }

    private T createServiceProxy() {

        RpcDesc desc = toDesc();

        // we should use lazy init clients
        RpcInvoker<T> invoker = new RpcInvoker<T>(interfaceClass, desc);

        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (invoker.isAllClientDestroy()) {
                    invoker.setClients(getClients());
                }
                RpcInvocation rpcInvocation = new RpcInvocation(interfaceClass, method, args);
                rpcInvocation.addAttacments(desc.getParameters());
                return invoker.invoke(rpcInvocation).getValue();
            }
        });
    }

    public List<Client> getClients() {
        // TODO 判断需要和服务端建立多少个连接，每个服务的连接是否隔离还是共享
        RpcDesc desc = toDesc();
        List<RpcDesc> targetRpcDesc = new ArrayList<>();
        registryServices.forEach(registryService -> {
            targetRpcDesc.addAll(registryService.lookup(desc));
        });
        return targetRpcDesc
                .stream()
                .map(rpcDesc -> ExtensionLoader.getExtensionLoader(ClientFactory.class).getExtension(server).getClient(rpcDesc))
                .collect(Collectors.toList());
    }

    private RpcDesc toDesc() {
        RpcDesc desc = new RpcDesc(protocol, host, port);

        desc = desc.addParameter(INTERFACE_KEY, interfaceClass.getName());
        desc = desc.addParameter(VERSION_KEY, version);
        desc = desc.addParameter(GROUP_KEY, group);
        return desc;
    }
}
