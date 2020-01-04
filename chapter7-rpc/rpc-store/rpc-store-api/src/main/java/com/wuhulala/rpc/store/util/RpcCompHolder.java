package com.wuhulala.rpc.store.util;

import com.wuhulala.rpc.store.RpcRegistryStore;

/**
 * @author wuhulala<br>
 * @date 2020/1/4<br>
 * @since v1.0<br>
 */
public class RpcCompHolder {

    private static RpcRegistryStore RPC_REGISTRY_STORE;


    public static RpcRegistryStore getRpcRegistryStore(){
        return RPC_REGISTRY_STORE;
    }

    public static void setRegistryStore(RpcRegistryStore rpcRegistryStore){
        RPC_REGISTRY_STORE = rpcRegistryStore;
    }
}
