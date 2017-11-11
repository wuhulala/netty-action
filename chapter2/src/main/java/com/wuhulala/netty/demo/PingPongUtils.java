package com.wuhulala.netty.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/11
 * @description 作甚的
 */
public class PingPongUtils {
    public static ByteBuf newPingMessage(){
        ByteBuf ping = Unpooled.buffer();
        ping.writeBytes("ping".getBytes());
        return ping;
    }

    public static ByteBuf newPongMessage(){
        ByteBuf ping = Unpooled.buffer();
        ping.writeBytes("pong".getBytes());
        return ping;
    }
}
