package com.wuhulala.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 网络传输的消息构造
 *
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description 作甚的
 */
public class NetMessageUtils {

    public static ByteBuf getInstance(String msg) {
        byte[] bytes = msg.getBytes();
        ByteBuf firstMessage = Unpooled.buffer(bytes.length);
        firstMessage.writeBytes(bytes);
        return firstMessage;
    }

}
