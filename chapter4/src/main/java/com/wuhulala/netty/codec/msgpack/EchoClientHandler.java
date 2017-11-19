package com.wuhulala.netty.codec.msgpack;

import com.wuhulala.netty.codec.v1.UserInfo;
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
    private final int batchSize;

    public EchoClientHandler(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserInfo[] userInfoList = myUserInfoList();
        for (UserInfo userInfo : userInfoList){
//            ByteBuf out = Unpooled.buffer();
//            MessagePack msgPack = new MessagePack();
//            byte[] row = msgPack.write(userInfo);
//            out.writeBytes(row);
            ctx.write(userInfo);
        }
        ctx.flush();
    }

    private UserInfo[] myUserInfoList(){
        UserInfo[] result = new UserInfo[batchSize];
        UserInfo userInfo;
        for (int i = 0; i < batchSize; i++) {

            userInfo = new UserInfo();
            userInfo.setUserId(i + 1);
            userInfo.setUserName("ABC ----> " + (i+1));
            result[i] = userInfo;
        }
        return result;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("client receive : " + msg);
        //ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
