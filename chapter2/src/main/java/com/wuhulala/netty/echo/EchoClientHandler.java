package com.wuhulala.netty.echo;

import com.wuhulala.util.NetMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description 作甚的
 */
public class EchoClientHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EchoClientHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ping = "ping";
        ByteBuf firstMessage = Unpooled.buffer(ping.getBytes().length);
        firstMessage.writeBytes(ping.getBytes());
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("client receive : " + msg);
        ctx.write(NetMessageUtils.getInstance("ping"));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
