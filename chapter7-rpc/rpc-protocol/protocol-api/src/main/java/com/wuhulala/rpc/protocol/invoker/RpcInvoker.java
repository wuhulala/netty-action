package com.wuhulala.rpc.protocol.invoker;

import com.wuhulala.rpc.bean.Invocation;
import com.wuhulala.rpc.exception.RpcException;

/**
 * @author wuhulala<br>
 * @date 2020/1/1<br>
 * @since v1.0<br>
 */
public class RpcInvoker<T> implements Invoker<T> {

    private Class<T> interfaceClass;

    public RpcInvoker(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    @Override
    public Class<T> getInterface() {
        return interfaceClass;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        // 1. 建立连接

        // 2. 发送消息

        // 3. 收取消息处理
        return null;
    }
}
