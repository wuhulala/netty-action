package com.wuhulala.netty.codec.msgpack;

import com.wuhulala.netty.codec.msgpack.decoder.MsgPackDecoder;
import com.wuhulala.netty.codec.msgpack.encoder.MsgPackEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 应答客户端
 *
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description 作甚的
 */
public class EchoClient {

    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try{
            Bootstrap b = new Bootstrap();

            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("frame Decoder",
                                    new LengthFieldBasedFrameDecoder(65536, 0, 2, 0, 2));
                            ch.pipeline().addLast("msgPack Decoder", new MsgPackDecoder());
                            ch.pipeline().addLast("frame Encoder",
                                    new LengthFieldPrepender(2));
                            ch.pipeline().addLast("msgPack Encoder", new MsgPackEncoder());
                            ch.pipeline().addLast(new EchoClientHandler(10));
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();

            f.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new EchoClient("127.0.0.1", 8080).connect();
    }
}
