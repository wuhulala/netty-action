package com.wuhulala.rpc.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * 编解码
 *
 * @author wuhulala<br>
 * @date 2020/1/9<br>
 * @since v1.0<br>
 */
public interface Codec {

    /**
     * 编码并写入通道
     *
     * @param channel 通道
     * @param buffer buffer
     * @param message request/response/event
     */
    void encode(Channel channel, ByteBuf buffer, Object message);

    /**
     * 解码
     *
     * @return request/response/event
     */
    Object decode(Channel channel, ByteBuf buffer);
}
