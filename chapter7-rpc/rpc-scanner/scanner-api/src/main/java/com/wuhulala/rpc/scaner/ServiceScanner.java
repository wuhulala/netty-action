package com.wuhulala.rpc.scaner;

import com.alibaba.cooma.Extension;
import com.wuhulala.rpc.bean.Invocation;
import com.wuhulala.rpc.bean.RpcDesc;
import java.util.List;

/**
 * 服务扫描器
 *
 * @author wuhulala<br>
 * @date 2019/12/21<br>
 * @since v1.0<br>
 */
@Extension
public interface ServiceScanner {

    String SCANNER_TYPE = "rpc.service.scan.type";

    List<RpcDesc> scan(String packageName);

    /**
     * 查询Invoker
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getInvoker(Invocation clazz);
}
