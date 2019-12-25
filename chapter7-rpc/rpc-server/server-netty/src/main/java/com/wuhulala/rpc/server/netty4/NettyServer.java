package com.wuhulala.rpc.server.netty4;


import com.wuhulala.rpc.server.Server;
import com.wuhulala.rpc.util.ConfigUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;

/**
 * Netty服务器实现
 *
 * @author wuhulala<br>
 * @date 2019/12/23<br>
 * @since v1.0<br>
 */
public class NettyServer implements Server {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * netty server bootstrap.
     */
    private ServerBootstrap bootstrap;
    /**
     * the boss channel that receive connections and dispatch these to worker channel.
     */
    private Channel channel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer() {

    }

    @Override
    public void open() throws Throwable {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();


        ServerBootstrap bootstrap = new ServerBootstrap();
        final NettyServerHandler nettyServerHandler = new NettyServerHandler();

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
//                        ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
//                        ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
//                        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                        NettyCodecAdapter adapter = new NettyCodecAdapter();

                        ch.pipeline()
                                .addLast("logging", new LoggingHandler(LogLevel.INFO))//for debug
                                .addLast("decoder", adapter.getDecoder())
//                                .addLast("http-aggregator", new HttpObjectAggregator(65536))
                                .addLast("encoder", adapter.getEncoder())
//                                .addLast("http-chunked", new ChunkedWriteHandler())
//                                .addLast("server-idle-handler", new IdleStateHandler(0, 0, 60 * 1000, MILLISECONDS))
                                .addLast("handler", nettyServerHandler);
                    }
                });
        InetSocketAddress address = getBindAddress();
        ChannelFuture channelFuture = bootstrap.bind(address);
        //https://my.oschina.net/ditan/blog/646863
        logger.info("Start {} bind {}, export {}", getClass().getSimpleName(), address, address);

        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    private InetSocketAddress getBindAddress() {
        String bindIp = ConfigUtils.getProperty("rpc.host", "127.0.0.1");
        int bindPort = ConfigUtils.getInt("rpc.port", 9001);
        return new InetSocketAddress(bindIp, bindPort);
    }

    @Override
    public void close() {
        try {
            if (channel != null) {
                // unbind.
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (bootstrap != null) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
