package com.wuhulala.rpc.bean;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author wuhulala<br>
 * @date 2020/1/1<br>
 * @since v1.0<br>
 */
public class CommonResult extends CompletableFuture<RpcResult> implements RpcResult {

    private static final long serialVersionUID = -6925924956850004727L;

    private Object result;

    private Throwable exception;

    private Map<String, String> attachments = new HashMap<String, String>();

    public CommonResult() {
    }

    public CommonResult(Object result) {
        this.result = result;
    }

    public CommonResult(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public Object getValue()  {
        if (exception != null) {
            try {
                // get Throwable class
                Class clazz = exception.getClass();
                while (!clazz.getName().equals(Throwable.class.getName())) {
                    clazz = clazz.getSuperclass();
                }
                // get stackTrace value
                Field stackTraceField = clazz.getDeclaredField("stackTrace");
                stackTraceField.setAccessible(true);
                Object stackTrace = stackTraceField.get(exception);
                if (stackTrace == null) {
                    exception.setStackTrace(new StackTraceElement[0]);
                }
            } catch (Exception e) {
                // ignore
            }
//            throw exception;
            // TODO wrap exception
        }
        return result;
    }

    @Override
    public void setValue(Object value) {
        this.result = value;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public void setException(Throwable e) {
        this.exception = e;
    }

    @Override
    public boolean hasException() {
        return exception != null;
    }
}
