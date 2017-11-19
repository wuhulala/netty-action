package com.wuhulala.netty.codec.msgpack.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description 作甚的
 */
public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] array;

        final int length = msg.readableBytes();
        array = new byte[length];

        msg.getBytes(msg.readerIndex(), array, 0, length);

        MessagePack msgPack = new MessagePack();

        out.add(msgPack.read(array));
    }
}
