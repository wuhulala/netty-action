package com.wuhulala.rpc.transport.api;

import java.util.Date;

/**
 * 心跳信息
 *
 * @author wuhulala<br>
 * @date 2020/1/9<br>
 * @since v1.0<br>
 */
public class HeartBeatEvent extends Event {

    private Date sendTime;

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public boolean hasTimeout() {
        return false;
    }
}
