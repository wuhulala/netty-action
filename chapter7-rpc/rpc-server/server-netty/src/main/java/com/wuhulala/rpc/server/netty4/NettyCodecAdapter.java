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
import com.wuhulala.rpc.serialzation.ObjectInput;
import com.wuhulala.rpc.serialzation.Serialization;
import com.wuhulala.rpc.util.BytesUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * NettyCodecAdapter.
 */
final public class NettyCodecAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyCodecAdapter.class);

    private final ChannelHandler encoder = new StringEncoder();

    private final ChannelHandler decoder = new InternalDecoder();

    public NettyCodecAdapter() {

    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    private static final int HEADER_LENGTH = 16;

    // 适配dubbo协议的解析
    private class InternalEncoder extends MessageToByteEncoder {


        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

//            org.apache.dubbo.remoting.buffer.ChannelBuffer buffer = new NettyBackedChannelBuffer(out);
//            Channel ch = ctx.channel();
//            NettyChannel channel = NettyChannel.getOrAddChannel(ch, url, handler);
//            try {
//                codec.encode(channel, buffer, msg);
//            } finally {
//                NettyChannel.removeChannelIfDisconnected(ch);
//            }
        }
    }

    private class InternalDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
            int saveReaderIndex;

            do {
                // 堆内存
//            ByteBuf messgae =  new UnpooledDirectByteBuf();
                saveReaderIndex = input.readerIndex();


                int readable = input.readableBytes();

                // 1. check header is complete
                if (readable < HEADER_LENGTH) {
                    //重置读取位置
                    input.readerIndex(saveReaderIndex);
                    logger.info("channel#{} message header is not complete！", ctx.channel().id());
                    break;
                }

                byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
                input.readBytes(header);
                // 1.1 parse header
                int len = BytesUtils.bytes2int(header, 12);
                System.out.println(header);
                System.out.println(len);


                // 2. check body is complete
                int expectedLen = len + HEADER_LENGTH;
                // if not complete
                if (expectedLen > readable) {
                    logger.info("channel#{} message body is not complete！ expect body length is {}, actual is {}", ctx.channel().id(), expectedLen, readable);
                    break;
                }


                // 3. resolve body

                // 3.1 read serialization type
                NettyByteBufInputStream is = new NettyByteBufInputStream(input);
                ObjectInput obj = ExtensionLoader.getExtensionLoader(Serialization.class).getExtension("fastjson").deserialize(is);

                // 3.2 read request id
                long id = BytesUtils.bytes2long(header, 4);

                String dubboVersion = obj.readUTF();
                System.out.println(dubboVersion);
                out.add(new Message(id, is));
            } while (input.isReadable());
//            ChannelBuffer message = new NettyBackedChannelBuffer(input);
//
//            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
//
//            try {
//                // decode object.
//                do {
//                    int saveReaderIndex = message.readerIndex();
//                    Object msg = codec.decode(channel, message);
//                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
//                        message.readerIndex(saveReaderIndex);
//                        break;
//                    } else {
//                        //is it possible to go here ?
//                        if (saveReaderIndex == message.readerIndex()) {
//                            throw new IOException("Decode without read data.");
//                        }
//                        if (msg != null) {
//                            out.add(msg);
//                        }
//                    }
//                } while (message.readable());
//            } finally {
//                NettyChannel.removeChannelIfDisconnected(ctx.channel());
//            }
        }
    }


    public static void main(String[] args) {
        byte[] header = new byte[16];
        BytesUtils.int2bytes(183, header, 12);
        int a = BytesUtils.bytes2int(header, 12);
        System.out.println(a);
    }
}
