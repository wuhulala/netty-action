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
package com.wuhulala.rpc.client.netty4;

import com.alibaba.cooma.ExtensionLoader;
import com.wuhulala.rpc.bean.CommonResult;
import com.wuhulala.rpc.bean.RpcInvocation;
import com.wuhulala.rpc.constants.CommonConstants;
import com.wuhulala.rpc.serialzation.Cleanable;
import com.wuhulala.rpc.serialzation.ObjectInput;
import com.wuhulala.rpc.serialzation.ObjectOutput;
import com.wuhulala.rpc.serialzation.Serialization;
import com.wuhulala.rpc.util.BytesUtils;
import com.wuhulala.rpc.util.ReflectUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;

import static com.wuhulala.rpc.client.netty4.NettyClientHandler.*;

/**
 * TODO 和server合并
 * <p>
 * NettyCodecAdapter.
 */
final public class NettyClientCodecAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientCodecAdapter.class);

    private final ChannelHandler encoder = new InternalEncoder();

    private final ChannelHandler decoder = new InternalDecoder();

    public NettyClientCodecAdapter() {

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
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buffer) throws Exception {
            logger.info("encode {}", msg);
            if (!(msg instanceof Request)) {
                return;
            }
            int savedWriteIndex = buffer.writerIndex();
            try {
                Request request = (Request) msg;
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
                BytesUtils.long2bytes(request.getId(), header, 4);

                buffer.writerIndex(savedWriteIndex + HEADER_LENGTH);
                ChannelBufferOutputStream bos = new ChannelBufferOutputStream(buffer);
                ObjectOutput out = serialization.serialize(bos);
                // encode response data or error message.
//            out.writeByte()|;
                // 设置响应类型
                RpcInvocation rpcInvocation = (RpcInvocation) request.getData();
                out.writeUTF(rpcInvocation.getAttachment(CommonConstants.DUBBO_VERSION_KEY, "2.0.2"));
                out.writeUTF(rpcInvocation.getAttachment(CommonConstants.PATH_KEY));
                out.writeUTF(rpcInvocation.getAttachment(CommonConstants.VERSION_KEY));

                out.writeUTF(rpcInvocation.getMethodName());
                out.writeUTF(ReflectUtils.getDesc(rpcInvocation.getParameterTypes()));
                Object[] args = rpcInvocation.getArguments();
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        out.writeObject(args[i]);
                    }
                }
                out.writeObject(rpcInvocation.getAttachments());

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

                ctx.writeAndFlush(buffer);
            } catch (Throwable t) {
                t.printStackTrace();
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
        }
    }

    private class InternalDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
            int saveReaderIndex;

            do {
                // 记录初始读取指针，便于当消息不是一个完整的报文的时候，重置到当前位置
                saveReaderIndex = input.readerIndex();
                int readable = input.readableBytes();

                // 1. check header is complete
                if (readable < HEADER_LENGTH) {
                    //重置读取位置
                    input.readerIndex(saveReaderIndex);
                    logger.info("channel#{} message header is not complete！", ctx.channel().id());
                    break;
                }

                // 1.1 parse header
                byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
                input.readBytes(header);

                // 2. check body is complete
                int len = BytesUtils.bytes2int(header, 12);
                int expectedLen = len + HEADER_LENGTH;
                // if not complete
                if (expectedLen > readable) {
                    input.readerIndex(saveReaderIndex);
                    logger.info("channel#{} message body is not complete！ expect body length is {}, actual is {}", ctx.channel().id(), expectedLen, readable);
                    break;
                }

                //////////////// 3. resolve body ////////////////
                Object result = decodeRequest(input, header);
                logger.info("received message is {}", result);
                out.add(result);
            } while (input.isReadable());
        }

        private Object decodeRequest(ByteBuf input, byte[] header) throws IOException {
            // 3.1 read serialization type
            NettyByteBufInputStream is = new NettyByteBufInputStream(input);
            ObjectInput obj = ExtensionLoader.getExtensionLoader(Serialization.class).getExtension("fastjson").deserialize(is);

            // 3.2 read request id
            long id = BytesUtils.bytes2long(header, 4);

            // TODO 判断当前是request还是 response
            byte flag = header[2], proto = (byte) (flag & SERIALIZATION_MASK);
            if ((flag & FLAG_REQUEST) == 0) {
                // decode response.
                Response res = new Response(id);
                if ((flag & FLAG_EVENT) != 0) {
                    res.setEvent(true);
                }
                // get status.
                byte status = header[3];
                res.setStatus(status);

                // TODO 解析
                res.setResult(new CommonResult("hello"));
                //
                return res;

            } else {
                // 3.3 read request
//                Request request = new Request(id, obj);
//                request.decode();
//                return request;
                return null;
            }
        }
    }


}
