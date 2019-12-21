package com.wuhulala.rpc.exception;

/**
 * @author wuhulala<br>
 * @date 2019/12/21<br>
 * @since v1.0<br>
 */
public class RpcExeception extends RuntimeException {

    public RpcExeception() {
        super();
    }

    public RpcExeception(String message) {
        super(message);
    }

    public RpcExeception(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcExeception(Throwable cause) {
        super(cause);
    }

    protected RpcExeception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
