package com.wuhulala.rpc.protocol.invoker;

import com.wuhulala.rpc.bean.*;
import com.wuhulala.rpc.client.Client;
import com.wuhulala.rpc.client.exception.RemotingException;
import com.wuhulala.rpc.exception.RpcException;
import java.util.List;
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

    private RpcDesc referenceDesc;

    public RpcInvoker(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public RpcInvoker(Class<T> interfaceClass, List<Client> clients) {
        this.interfaceClass = interfaceClass;
        this.clients = clients.toArray(new Client[0]);
    }

    public RpcInvoker(Class<T> interfaceClass, RpcDesc desc) {
        this.interfaceClass = interfaceClass;
        this.referenceDesc =  desc;
    }

    @Override
    public Class<T> getInterface() {
        return interfaceClass;
    }

    public Client[] getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients.toArray(new Client[0]);
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
        if (clients.length == 0) {
            throw new RpcException(referenceDesc.toFullString() + "没有可用的服务提供者");
        }
        Client currentClient;
        if (clients.length == 1) {
            currentClient = clients[0];
        } else {
            currentClient = clients[(int) (index.getAndIncrement() % clients.length)];
        }
        return currentClient;
    }


    public boolean isAllClientDestroy() {
        return true;
    }
}
