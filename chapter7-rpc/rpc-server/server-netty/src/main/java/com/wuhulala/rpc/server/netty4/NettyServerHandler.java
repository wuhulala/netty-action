/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wuhulala.rpc.server.netty4;


import com.alibaba.cooma.ExtensionLoader;
import com.wuhulala.rpc.serialzation.Cleanable;
import com.wuhulala.rpc.serialzation.ObjectOutput;
import com.wuhulala.rpc.serialzation.Serialization;
import com.wuhulala.rpc.util.BytesUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * NettyServerHandler.
 */
@io.netty.channel.ChannelHandler.Sharable
public class NettyServerHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    // header length.
    protected static final int HEADER_LENGTH = 16;
    // magic header.
    protected static final short MAGIC = (short) 0xdabb;
    protected static final byte MAGIC_HIGH = BytesUtils.short2bytes(MAGIC)[0];
    protected static final byte MAGIC_LOW = BytesUtils.short2bytes(MAGIC)[1];
    // message flag.
    protected static final byte FLAG_REQUEST = (byte) 0x80;
    protected static final byte FLAG_TWOWAY = (byte) 0x40;
    protected static final byte FLAG_EVENT = (byte) 0x20;
    protected static final int SERIALIZATION_MASK = 0x1f;

    public static final byte RESPONSE_WITH_EXCEPTION = 0;
    public static final byte RESPONSE_VALUE = 1;
    public static final byte RESPONSE_NULL_VALUE = 2;
    public static final byte RESPONSE_WITH_EXCEPTION_WITH_ATTACHMENTS = 3;
    public static final byte RESPONSE_VALUE_WITH_ATTACHMENTS = 4;
    public static final byte RESPONSE_NULL_VALUE_WITH_ATTACHMENTS = 5;

    public NettyServerHandler() {

    }


    /**
     * 连接成功
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(">>>>>>>>channelActive:" + ctx.name());
//        ctx.flush();
    }

    /**
     * 断开连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info(">>>>>>>>channelInactive:" + ctx.name());

    }

    /**
     * 可读
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Message)) {
            return;
        }
        Message message = (Message) msg;
        logger.info(">>>>>>>>channelRead#{}: {}", ctx.channel().id(), message.getId());
        // 必须是DefaultHttpResponse 不能是FullHttpResponse
//        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK);
//        ByteBuf buffer = Unpooled.copiedBuffer("hello ....\r\n", CharsetUtil.UTF_8);
//        response.content().writeBytes(buffer);
        ByteBuf buffer = Unpooled.buffer();
//

        int savedWriteIndex = buffer.writerIndex();
//        ctx.channel().
        try {
            Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getExtension("fastjson");
            // header.
            byte[] header = new byte[HEADER_LENGTH];
            // set magic number.
            BytesUtils.short2bytes(MAGIC, header);
            // set request and serialization flag.
            header[2] = serialization.getContentTypeId();

            // set response status.
            byte status = 20;
            header[3] = status;
            // set request id.
            BytesUtils.long2bytes(message.getId(), header, 4);

            buffer.writerIndex(savedWriteIndex + HEADER_LENGTH);
            ChannelBufferOutputStream bos = new ChannelBufferOutputStream(buffer);
            ObjectOutput out = serialization.serialize(bos);
            // encode response data or error message.
//            out.writeByte()|;
            // 设置响应类型
            out.writeByte(RESPONSE_VALUE);
            out.writeUTF("Hello " + message.getId());

            out.flushBuffer();
            if (out instanceof Cleanable) {
                ((Cleanable) out).cleanup();
            }
            bos.flush();
            bos.close();

            int len = bos.writtenBytes();
//            checkPayload(channel, len);
            BytesUtils.int2bytes(len, header, 12);
            // write
            buffer.writerIndex(savedWriteIndex);
            buffer.writeBytes(header); // write header.
            buffer.writerIndex(savedWriteIndex + HEADER_LENGTH + len);
        } catch (Throwable t) {
            // clear buffer
            buffer.writerIndex(savedWriteIndex);
            // send error message to Consumer, otherwise, Consumer will wait till timeout.

            // Rethrow exception
            if (t instanceof IOException) {
                throw (IOException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new RuntimeException(t.getMessage(), t);
            }
        }
        ctx.write(buffer);
    }

    /**
     * 可写
     *
     * @param ctx
     * @param msg
     * @param promise
     * @throws Exception
     */
//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        super.write(ctx, msg, promise);
//        logger.info(">>>>>>>> write:" + ctx.name() + ":" + msg);
////        ctx.channel().flush();
//    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.info(">>>>>>>> userEventTriggered:" + ctx.name() + ":" + evt);

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {

        logger.info("exceptionCaught:" + ctx.name() + ":", cause);
    }
}
