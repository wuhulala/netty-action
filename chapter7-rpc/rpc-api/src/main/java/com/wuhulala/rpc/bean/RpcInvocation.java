package com.wuhulala.rpc.bean;

import com.wuhulala.rpc.constants.CommonConstants;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author wuhulala<br>
 * @date 2019/12/29<br>
 * @since v1.0<br>
 */
@SuppressWarnings("ALL")
public class RpcInvocation implements Invocation {

    private Class<?> serviceClass;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

    private Map<String, String> attachments;

    private transient Class<?> returnType;

    private InvokeMode invokeMode;

    public RpcInvocation() {
    }

    public RpcInvocation(Class<?> serviceClass, Method method, Object[] args) {
        this.serviceClass = serviceClass;
        this.methodName = method.getName();
        this.arguments = args;
        this.parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            this.parameterTypes[i] = args[i].getClass();
        }
        getAttachments().put(CommonConstants.INTERFACE_KEY, serviceClass.getName());
        getAttachments().put(CommonConstants.PATH_KEY, serviceClass.getName());
    }

    @Override
    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public Map<String, String> getAttachments() {
        if (attachments == null) {
            attachments = new HashMap<>();
        }
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    @Override
    public InvokeMode getInvokeMode() {
        return invokeMode;
    }

    public void setInvokeMode(InvokeMode invokeMode) {
        this.invokeMode = invokeMode;
    }

    public String getAttachment(String pathKey) {
        return getAttachments().get(pathKey);
    }

    public String getAttachment(String pathKey, String defaultValue){
        return Optional.ofNullable(getAttachment(pathKey)).orElse(defaultValue);
    }

    public void addAttacments(Map<String, String> parameters) {
        getAttachments().putAll(parameters);
    }
}
