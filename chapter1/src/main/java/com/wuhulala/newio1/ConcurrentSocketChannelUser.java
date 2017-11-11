package com.wuhulala.newio1;


import com.wuhulala.utils.UTF8Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by xueah20964 on 2017/5/23.
 */
public class ConcurrentSocketChannelUser {

    private Selector selector = null;
    private SocketChannel socketChannel = null;
    private String name;
    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            new Thread(new TestClient()).start();
        }
    }

    public static class TestClient implements Runnable{
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    new ConcurrentSocketChannelUser(Thread.currentThread().getName()+ "[" + i + "]").init();
                } catch (IOException e) {
                    System.out.println(Thread.currentThread().getName() + "--------------" + i );
                    //e.printStackTrace();
                }
            }
        }
    }

    public ConcurrentSocketChannelUser(String name) {
        this.name = name;
    }

    public void init() throws IOException {
        selector = Selector.open();

        InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 9876);
        SocketChannel socketChannel = SocketChannel.open(isa);
        socketChannel.configureBlocking(false);

        socketChannel.register(selector, SelectionKey.OP_READ);

        while (!socketChannel.finishConnect()) {
            System.out.println(" connecting ...");
        }

        //------------ 注册自己的存在------------------
        String newData = name;
        ByteBuffer buf = ByteBuffer.allocate(480);
        buf.clear();
        buf.put(newData.getBytes());
        buf.flip();
        socketChannel.write(buf);
        buf.clear();
        socketChannel.write(UTF8Utils.encode("lisi:nihao"));

        //-----------打开自己的读取功能----------------
        new Thread(new ReadThread()).start();

    }

    public class ReadThread implements Runnable{

        @Override
        public void run() {
            try {
                while (selector.select() > 0) {
                    for (SelectionKey sk : selector.selectedKeys()) {
                        selector.selectedKeys().remove(sk);
                        // 如果该SelectionKey对应的Channel中有可读的数据
                        if (sk.isReadable()) {
                            SocketChannel sc = (SocketChannel) sk.channel();
                            ByteBuffer buff = ByteBuffer.allocate(1024);
                            String content = "";
                            while (sc.read(buff) > 0) {
                                buff.flip();
                                content += UTF8Utils.decode(buff);
                            }
                            System.out.println("聊天信息：" + content);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("错误：" + e.getMessage());
            }
        }
    }
}
