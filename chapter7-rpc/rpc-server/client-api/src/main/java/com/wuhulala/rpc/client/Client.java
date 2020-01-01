package com.wuhulala.rpc.client;

import com.wuhulala.rpc.exception.RpcException;
import java.util.concurrent.CompletableFuture;

/**
 * 客户端
 *
 * @author wuhulala<br>
 * @date 2020/1/1<br>
 * @since v1.0<br>
 */
public interface Client {

    /**
     * connect
     * @throws RpcException
     */
    void connect() throws RpcException;


    /**
     * reconnect.
     */
    void reconnect() throws RpcException;


    /**
     * 断开连接
     * @throws RpcException
     */
    void disconnect() throws RpcException;


    /**
     * send message.
     *
     * @param message
     */
    CompletableFuture<Object> send(Object message) throws RpcException, com.wuhulala.rpc.client.exception.RemotingException;
}
