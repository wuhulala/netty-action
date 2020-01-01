package com.wuhulala.rpc.protocol.invoker;

import com.wuhulala.rpc.bean.*;
import com.wuhulala.rpc.client.Client;
import com.wuhulala.rpc.client.exception.RemotingException;
import com.wuhulala.rpc.exception.RpcException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wuhulala<br>
 * @date 2020/1/1<br>
 * @since v1.0<br>
 */
public class RpcInvoker<T> implements Invoker<T> {

    private final AtomicLong index = new AtomicLong(0);


    private Class<T> interfaceClass;

    private Client[] clients;

    public RpcInvoker(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    @Override
    public Class<T> getInterface() {
        return interfaceClass;
    }

    @Override
    public RpcResult invoke(Invocation invocation) throws RpcException {

        // 1. 建立连接
        Client currentClient = selectOneClient();

        // 2. 收取消息处理
        CommonResult commonResult = new CommonResult();
        try {
            CompletableFuture<Object> future = currentClient.send(invocation);
            future.whenComplete((obj, e) -> {
                if (e != null) {
                    commonResult.completeExceptionally(e);
                } else {
                    commonResult.complete((RpcResult) obj);
                }
            });

        } catch (RemotingException e) {
            e.printStackTrace();
        }

        try {
            if (invocation.getInvokeMode() == InvokeMode.FUTURE) {
                return commonResult;
            } else if (invocation.getInvokeMode() == InvokeMode.ASYNC) {
                FutureContext.getContext().setFuture(commonResult);
                // dubbo recreate
                return commonResult.get();
            } else {
                return commonResult.get();
            }
        }catch (Exception e) {
            throw new RpcException("执行失败", e);
        }

    }

    private Client selectOneClient() {
        Client currentClient;
        if (clients.length == 1) {
            currentClient = clients[0];
        } else {
            currentClient = clients[(int) (index.getAndIncrement() % clients.length)];
        }
        return currentClient;
    }
}
