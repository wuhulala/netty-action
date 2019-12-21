package com.wuhulala.rpc.registry;

import com.alibaba.cooma.Extension;
import com.wuhulala.rpc.bean.RpcDesc;

/**
 * @author wuhulala<br>
 * @date 2019/12/21<br>
 * @since v1.0<br>
 */
@Extension
public interface RegistryFactory {

     RegistryService createRegistry(RpcDesc url);

}
