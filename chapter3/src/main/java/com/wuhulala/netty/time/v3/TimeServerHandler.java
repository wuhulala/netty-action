package com.wuhulala.netty.time.v3;

import com.wuhulala.netty.time.Constants;
import com.wuhulala.util.DateUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 时间服务器处理器
 *
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description
 */
public class TimeServerHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TimeServerHandler.class);

    private int count = 0;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.error(cause.getMessage(), cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String body = (String) msg;
        logger.info("The time server receive order : " + body + ", now count is " + ++count);

        String currentTime = getCurrentTime(body) + suffix();

        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.write(resp);
    }

    private String suffix() {
        return Constants.DEFAULT_DELIMITER;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();

    }

    private String getCurrentTime(String body) {
        return !Constants.DEFAULT_CLIENT_REQ.equalsIgnoreCase(body) ? Constants.DEFAULT_BAD_ORDER : DateUtils.nowDateTime();
    }
}
