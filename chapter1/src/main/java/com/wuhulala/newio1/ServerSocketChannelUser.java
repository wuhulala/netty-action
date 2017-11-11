package com.wuhulala.newio1;


import com.wuhulala.utils.UTF8Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xueah20964 on 2017/5/23.
 */
public class ServerSocketChannelUser {

    private static Map<String, SocketAddress> nameAddress = new ConcurrentHashMap<>();
    private static Map<SocketAddress, SocketChannel> addressChannel = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ServerSocketChannel ssc = null;
        Selector selector = null;
        try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            //设置为异步
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(9876));

            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.out.println("e 1" + e.getMessage());
        }

        try {
            assert selector != null;
            int count = 0;
            while (selector.select() > 0) {
                System.out.println("-------------" + ++count + "------------------");
                // Get an iterator over the set of selected keys
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                System.out.println("本次收取到了 -------------【" + selector.selectedKeys().size() + "】个客户端------------------");

                //目前这种处理是同步处理的问题，应该对于每一个channel用线程池去处理
                while (it.hasNext()) {
                    SelectionKey sk = it.next();
                    System.out.println("此key感兴趣的事件:" + translate(sk.interestOps()));
                    if (sk.isAcceptable()) {
                        // 如果这个key是可以接收客户端请求的
                        ssc = (ServerSocketChannel) sk.channel();
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        try {
                            sc.register(selector, SelectionKey.OP_READ);
                        } catch (ClosedChannelException e) {
                            System.out.println("客户端关闭了：" + e.getMessage());
                        }
                    }
                    if (sk.isReadable()) {
                        SocketChannel sc = null;
                        try {
                            sc = (SocketChannel) sk.channel();
                            parse(sc);
                        } catch (InterruptedException | IOException e) {
                            addressChannel.remove(sc.getRemoteAddress());
                            nameAddress.remove(getKey(nameAddress, sc.getRemoteAddress()));
                            sk.cancel();
                            System.out.println("e 3" + e.getMessage());
                        }
                    }
                    it.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("e while" + e.getMessage());
        }


    }

    private static String translate(int i) {
        return null;
    }

    private static void parse(SocketChannel sc) throws IOException, InterruptedException {
        System.out.println("start 处理客户端请求."+ sc +".." + Thread.currentThread().getName());
        Thread.sleep(1000);
        ByteBuffer bb = ByteBuffer.allocate(1024);
        StringBuilder sb = new StringBuilder();
        String username;
        synchronized (sc) {
            while (sc.read(bb) > 0) {
                bb.flip();
                while (bb.hasRemaining()) {
                    sb.append((char) bb.get());
                }
                bb.clear();
            }
            username = sb.toString();
            //System.out.println(username);
            if (!username.contains(":")) {
                SocketAddress sa = sc.getRemoteAddress();
                nameAddress.put(username, sa);
                addressChannel.put(sa, sc);
                System.out.println("进入聊天室的是：[" + username + "]-：地址" + sc.getRemoteAddress());
                sc.write(UTF8Utils.encode("进入聊天室成功！！！你好" + username));
            } else {
                String[] users = username.split(":");
                if (users.length == 3) {
                    SocketAddress address = nameAddress.get(users[1]);
                    if (address != null) {
                        SocketChannel socketChannel = addressChannel.get(address);
                        if (socketChannel != null) {
                            socketChannel.write(UTF8Utils.encode(users[0] + "说:【" + users[2] + "】"));
                        } else {
                            sc.write(UTF8Utils.encode("他不在线。。"));

                        }
                    } else {
                        sc.write(UTF8Utils.encode("他不在线。。"));
                    }
                } else {
                    sc.write(UTF8Utils.encode("输入信息无效。。"));
                }
            }
        }
        System.out.println("end 处理客户端请求..." + Thread.currentThread().getName());

    }


    //根据value值获取到对应的一个key值
    private static <K, V> K getKey(Map<K, V> map, V value) {
        for (K getKey : map.keySet()) {
            if (map.get(getKey) == value) {
                return getKey;
            }
        }
        return null;
    }

    public void main1() {
        Map<String, InetSocketAddress> map = new HashMap<>();
        InetSocketAddress i1 = new InetSocketAddress(9631);
        InetSocketAddress i2 = new InetSocketAddress(9632);

        map.put("1", i1);
        map.put("2", i2);

        System.out.println(getKey(map, i2));
    }

}
