package com.wuhulala.rpc.bean;

/**
 * 执行模式
 * <ul>
 * <li>1. FUTURE 异步并且返回的是future</li>
 * <li>2. ASYNC 异步 在recreate的时候，如果future完成了返回结果，否则返回一个新的AppResult，只能通过RpcContext.getFuture()再次获取了;</li>
 * <li>3. SYNC 同步 如果是同步了，返回的时候直接调用Future.get()</li>
 * </ul>
 */
public enum InvokeMode {

    SYNC, ASYNC, FUTURE

}
