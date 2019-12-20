package com.wuhulala.rpc.dto;

/**
 * @author wuhulala<br>
 * @date 2019/12/17<br>
 * @since v1.0<br>
 */
public class Message {
    /**防止消息重复消费*/
    private String id;
    private Integer length;
    private long timestamp;
    private String version;

    private byte[] body;
}
