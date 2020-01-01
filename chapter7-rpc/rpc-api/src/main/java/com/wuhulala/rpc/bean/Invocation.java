package com.wuhulala.rpc.bean;

import java.util.Map;

/**
 * @author wuhulala<br>
 * @date 2019/12/29<br>
 * @since v1.0<br>
 */
public interface Invocation {

    Class<?> getServiceClass();

    String getMethodName();

    Class<?>[] getParameterTypes();

    Object[] getArguments();

    Map<String, String> getAttachments();

    Class<?> getReturnType();

    InvokeMode getInvokeMode();

}
