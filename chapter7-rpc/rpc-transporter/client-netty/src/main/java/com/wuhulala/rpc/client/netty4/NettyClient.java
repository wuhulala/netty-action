package com.wuhulala.rpc.client.netty4;


import com.wuhulala.rpc.bean.RpcDesc;
import com.wuhulala.rpc.client.Client;
import com.wuhulala.rpc.client.exception.RemotingException;
import com.wuhulala.rpc.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Netty服务器实现
 *
 * @author wuhulala<br>
 * @date 2019/12/23<br>
 * @since v1.0<br>
 */
public class NettyClient implements Client {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
    private Bootstrap bootstrap;
    /**
     * 命名：：：NettyClientWorker-4-5
     */
    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(DEFAULT_IO_THREADS, new DefaultThreadFactory("NettyClientWorker", true));

    private RpcDesc url;

    private Channel channel;

    private volatile boolean connected = false;

    public NettyClient(RpcDesc url) {
        this.url = url;
    }

    @Override
    public void connect() throws RpcException {
        final NettyClientHandler nettyClientHandler = new NettyClientHandler();
        bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout())
                .channel(NioSocketChannel.class);

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.max(3000, getConnectTimeout()));
        bootstrap.handler(new ChannelInitializer() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
//                int heartbeatInterval = UrlUtils.getHeartbeat(getUrl());
                NettyClientCodecAdapter adapter = new NettyClientCodecAdapter();
                ch.pipeline()
//                        .addLast("logging",new LoggingHandler(LogLevel.INFO))//for debug
                        .addLast("decoder", adapter.getDecoder())
                        .addLast("encoder", adapter.getEncoder())
//                        .addLast("client-idle-handler", new IdleStateHandler(heartbeatInterval, 0, 0, MILLISECONDS))
                        .addLast("handler", nettyClientHandler);
//                String socksProxyHost = ConfigUtils.getProperty(SOCKS_PROXY_HOST);
//                if (socksProxyHost != null) {
//                    int socksProxyPort = Integer.parseInt(ConfigUtils.getProperty(SOCKS_PROXY_PORT, DEFAULT_SOCKS_PROXY_PORT));
//                    Socks5ProxyHandler socks5ProxyHandler = new Socks5ProxyHandler(new InetSocketAddress(socksProxyHost, socksProxyPort));
//                    ch.pipeline().addFirst(socks5ProxyHandler);
//                }
            }
        });

        ChannelFuture future = bootstrap.connect(getConnectAddress());
        this.channel = future.channel();
        connected = true;
        logger.info("netty client 连接服务器#{} 成功", getConnectAddress());
    }

    private SocketAddress getConnectAddress() {
        return new InetSocketAddress(getUrl().getHost(), getUrl().getPort());
    }

    private int getConnectTimeout() {
        logger.warn("use default connect timeout 3000ms");
        return 3000;
    }

    @Override
    public void reconnect() throws RpcException {
        // TODO
    }

    @Override
    public void disconnect() throws RpcException {
        // TODO
    }

    @Override
    public CompletableFuture<Object> send(Object message) throws RemotingException {
        if (!connected) {
            connect();
        }
        // create request.
        Request req = new Request();
//        req.setVersion(Version.getProtocolVersion());
//        req.setTwoWay(true);
        req.setData(message);
        DefaultFuture future = DefaultFuture.newFuture(channel, req, 3000);
        try {
            while (!channel.isActive()) {
                Thread.sleep(100);
                if (channel.isActive()) {
                    logger.info("发送消息....{}", req);
                    channel.writeAndFlush(req);
                }
            }
        } catch (Exception e) {
            future.cancel();
            throw new RemotingException(channel, "发送请求失败!", e);
        }
        return future;
    }

    public RpcDesc getUrl() {
        return url;
    }

    public void setUrl(RpcDesc url) {
        this.url = url;
    }
}
