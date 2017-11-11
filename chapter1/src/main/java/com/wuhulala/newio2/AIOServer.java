package com.wuhulala.newio2;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author noname
 */
public class AIOServer {
    public final static int PORT = 9888;
    private AsynchronousServerSocketChannel server;

    public AIOServer() throws IOException {
        server = AsynchronousServerSocketChannel.open().bind(
                new InetSocketAddress(PORT));
    }

    public void startWithFuture() throws InterruptedException,
            ExecutionException, TimeoutException {
        System.out.println("Server listen on " + PORT);
        Future<AsynchronousSocketChannel> future = server.accept();
        AsynchronousSocketChannel socket = future.get();
        ByteBuffer readBuf = ByteBuffer.allocate(1024);
        readBuf.clear();
        socket.read(readBuf).get(100, TimeUnit.SECONDS);
        readBuf.flip();
        System.out.printf("received message:" + new String(readBuf.array()));
        System.out.println(Thread.currentThread().getName());

    }

    public void startWithCompletionHandler() throws InterruptedException,
            ExecutionException, TimeoutException {
        System.out.println("Server listen on " + PORT);
        //注册事件和事件完成后的处理器
        server.accept(null,
                new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    final ByteBuffer buffer = ByteBuffer.allocate(1024);

                    public void completed(AsynchronousSocketChannel result,
                                          Object attachment) {
                        System.out.println(Thread.currentThread().getName());
                        System.out.println("start");
                        try {
                            buffer.clear();
                            result.read(buffer).get(100, TimeUnit.SECONDS);
                            buffer.flip();
                            System.out.println("received message: "
                                    + new String(buffer.array()));
                        } catch (InterruptedException | ExecutionException e) {
                            System.out.println(e.toString());
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } finally {

                            try {
                                result.close();
                                server.accept(null, this);
                            } catch (Exception e) {
                                System.out.println(e.toString());
                            }
                        }

                        System.out.println("end");
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        System.out.println("failed: " + exc);
                    }
                });
        // 主线程继续自己的行为
        while (true) {
            System.out.println("main thread");
            Thread.sleep(1000);
        }

    }

    public static void main(String args[]) throws Exception {
        new AIOServer().startWithCompletionHandler();
    }
}