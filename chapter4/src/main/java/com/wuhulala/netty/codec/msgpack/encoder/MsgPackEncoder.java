package com.wuhulala.netty.codec.msgpack.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description 作甚的
 */
public class MsgPackEncoder extends MessageToByteEncoder<Object>{
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        MessagePack msgPack = new MessagePack();
        byte[] row = msgPack.write(msg);
        out.writeBytes(row);
    }
}
