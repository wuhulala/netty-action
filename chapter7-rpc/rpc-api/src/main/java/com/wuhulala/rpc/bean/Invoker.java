package com.wuhulala.rpc.bean;

import com.wuhulala.rpc.exception.RpcException;

/**
 * 执行器
 *
 * @author wuhulala<br>
 * @date 2019/12/30<br>
 * @since v1.0<br>
 */
public interface Invoker<T> {

    /**
     * get service interface.
     *
     * @return service interface.
     */
    Class<T> getInterface();

    /**
     * invoke.
     *
     * @param invocation 调用描述
     * @return result 结果
     * @throws RpcException 异常
     */
    RpcResult invoke(Invocation invocation) throws RpcException;

}
