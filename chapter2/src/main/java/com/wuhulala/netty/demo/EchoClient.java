package com.wuhulala.netty.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wuhulala
 * @version 1.0
 * @description
 * @since 17-11-11
 */
public class EchoClient {

    public static final Logger logger = LoggerFactory.getLogger(EchoClient.class);

    private final String host;
    private final int port;

    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            ChannelFuture f = b.connect(host, port).sync();

            f.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                EchoClient client = new EchoClient("127.0.0.1", 8080);
                try {
                    client.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }


    }


    /**
     * handler
     */
    static class EchoClientHandler extends SimpleChannelInboundHandler {
        private final ByteBuf firstMessage;

        /**
         * Creates a client-side handler.
         */
        public EchoClientHandler() {
            firstMessage = PingPongUtils.newPingMessage();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(firstMessage);
        }


//        /**
//         * not release msg
//         * @param ctx
//         * @param msg
//         * @throws Exception
//         */
//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
//            ByteBuf in = (ByteBuf) msg;
//
//            StringBuilder sb = new StringBuilder();
//            while (in.isReadable()) {
//                sb.append((char) in.readByte());
//            }
//            System.out.println("server: " + sb.toString());
//            Thread.sleep(100);
//            ctx.write(PingPongUtils.newPingMessage());
//        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;

            StringBuilder sb = new StringBuilder();
            while (in.isReadable()) {
                sb.append((char) in.readByte());
            }
            System.out.println("server: " + sb.toString());
            Thread.sleep(10000);
            ctx.write(PingPongUtils.newPingMessage());
        }
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            logger.error("服务器gg了，我也下线了" + cause.getMessage(), cause);

            ctx.close();
        }
    }
}
