package com.wuhulala.rpc.scanner.bean;

import com.wuhulala.rpc.bean.RpcInvocation;
import com.wuhulala.rpc.protocol.invoker.RpcInvoker;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author wuhulala<br>
 * @date 2019/12/29<br>
 * @since v1.0<br>
 */
public class RpcReferenceBean<T> {

    private Class<T> interfaceClass;

    private String group;

    private String version;

    /**
     * 服务实现代理
     */
    private T ref;
    /**
     * 协议名称
     */
    private String protocol;

    private String username;

    private String password;

    private String host;

    private int port;

    private String path;

    private Class<?> type;

    private volatile boolean initialized;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = (Class<T>) interfaceClass;
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public void init() {
        if (initialized) {
            return;
        }

//        Invoker invoker = ExtensionLoader.getExtensionLoader(Protocol.class).getExtension("");
        ref = createServiceProxy();
//        TODO
//        ref = createServiceProxy(map);

        initialized = true;
    }

    private T createServiceProxy() {

        RpcInvoker<T> invoker = new RpcInvoker<T>(interfaceClass);

        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return invoker.invoke(new RpcInvocation(method, args));
            }
        });
    }
}
