package com.wuhulala.rpc.client.netty4;

import com.wuhulala.rpc.bean.RpcDesc;
import com.wuhulala.rpc.client.Client;
import com.wuhulala.rpc.client.ClientFactory;

/**
 * @author wuhulala<br>
 * @date 2020/1/4<br>
 * @since v1.0<br>
 */
public class NettyClientFactory implements ClientFactory {

    @Override
    public Client getClient(RpcDesc url) {
        return new NettyClient(url);
    }
}
