package com.wuhulala.rpc.client;

import com.alibaba.cooma.Extension;
import com.wuhulala.rpc.bean.RpcDesc;

/**
 * @author wuhulala<br>
 * @date 2020/1/4<br>
 * @since v1.0<br>
 */
@Extension
public interface ClientFactory {

    Client getClient(RpcDesc url);

}
