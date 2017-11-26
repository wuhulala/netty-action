package com.wuhulala.newio1;


import com.wuhulala.util.UTF8Utils;

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
        //for (int i = 0; i < 10; i++) {
            new Thread(new TestClient()).start();
        //}
    }

    public static class TestClient implements Runnable{
        @Override
        public void run() {
            for (int i = 0; i < 1; i++) {
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
        String newData = "";
        //测试 粘包问题,可以通过/r/n后台拆分
        for (int i = 0; i < 50; i++) {
            newData = name + "\r\n";
            socketChannel.write(UTF8Utils.encode(newData));

        }
        //这样服务器此时收到了50条命令

        // 模拟拆包  完整的一条命令分为50次发送和一次分割符的发送
        for (int i = 0; i < 50; i++) {
            newData = name;
            socketChannel.write(UTF8Utils.encode(newData));
        }
        socketChannel.write(UTF8Utils.encode("\r\n"));
        //这样服务器此时应该又收到一条命令，加上上面的总共有51条命令
//        ByteBuffer buf = ByteBuffer.allocate(5000 * name.getBytes().length);
//        buf.clear();
//        buf.put(newData.getBytes());
//        buf.flip();
        try {
            System.out.println("我先休息十秒再发");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //buf.clear();
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
                                buff.clear();
                            }
                            System.out.println("聊天信息大小：" + content.length());
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
