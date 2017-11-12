package com.wuhulala.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuhulala
 * @version 1.0
 * @description
 * @since 17-11-11
 */
public class EchoServer {

    public static final Logger logger = LoggerFactory.getLogger(EchoServer.class);
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //.localAddress(new InetSocketAddress(port))

                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new EchoServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)

                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            if (logger.isInfoEnabled()) {
                logger.info(EchoServer.class.getName() + "started and listen on " + f.channel().localAddress());
            }
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        EchoServer server = new EchoServer(8080);
        try {
            server.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class EchoServerHandler extends ChannelInboundHandlerAdapter {

        private static Map<String, String> loginUsers = new ConcurrentHashMap<>();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String remoteAddress = ctx.channel().remoteAddress().toString();
            updateAddress(remoteAddress);
            System.out.printf("start handle [%s] \n\r", remoteAddress);
            Thread.sleep(3000);
            ByteBuf in = (ByteBuf) msg;
            StringBuilder sb = new StringBuilder();
            while (in.isReadable()) {
                sb.append((char) in.readByte());
            }
            System.out.println("client[" + ctx.channel().remoteAddress() + "]" +sb.toString());
            ctx.write(PingPongUtils.newPongMessage());
            System.out.printf("handle [%s] end \n\r", remoteAddress);

        }

        private void updateAddress(String remoteAddress) {
            loginUsers.put(remoteAddress, DateUtils.nowDateTime());
            System.out.println("now user list size ==================================== "+ loginUsers.size());
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            String userName = ctx.channel().remoteAddress().toString();
            loginUsers.remove(userName);
            logger.error(userName+ "下线了！！", cause);
            ctx.close();
        }
    }
}
