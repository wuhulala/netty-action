package com.wuhulala.newio1;


import com.wuhulala.util.UTF8Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/26
 */
public class NioServerSocket {

    private static Map<String, SocketAddress> nameAddress = new ConcurrentHashMap<>();
    private static Map<SocketAddress, SocketChannel> addressChannel = new ConcurrentHashMap<>();
    private static Map<SocketAddress, Integer> addressReqCount = new ConcurrentHashMap<>();
    /**
     * 用来存取本次多读取了多余的半包
     */
    private static Map<SocketAddress, Object> addressOldReq = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ServerSocketChannel ssc = null;
        final Selector selector;
        try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            //设置为异步
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(9876));

            ssc.register(selector, SelectionKey.OP_ACCEPT);
            for (int i = 0; i < 2; i++) {
                new Thread(() -> {
                    System.out.println(Thread.currentThread().getName() + " 启动成功");
                    doHandler(selector);
                }).start();
            }
        } catch (IOException e) {
            System.out.println("e 1" + e.getMessage());
        }

    }

    private static void doHandler(Selector selector) {
        ServerSocketChannel ssc;
        try {
            System.out.println(Thread.currentThread().getName() + " 初始化处理器");

            assert selector != null;
            int count = 0;
            while (selector.select() > 0) {
                System.out.println(Thread.currentThread().getName() + " 处理");

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
                            System.out.println("注册地址为[" + sc.getRemoteAddress() + "]的客户端");
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
        if (i == OP_ACCEPT) {
            return "服务端可以接收这个客户端连接了";
        } else if (i == OP_CONNECT) {
            return "客户端已经与服务器连接成功";
        } else if (i == OP_READ) {
            return "可以从通道中读取数据了";
        } else if (i == OP_WRITE) {
            return "可以向通道中写入数据了";
        } else {
            return "未知事件";
        }
    }

    public static final int OP_READ = 1 << 0;

    public static final int OP_WRITE = 1 << 2;

    public static final int OP_CONNECT = 1 << 3;

    public static final int OP_ACCEPT = 1 << 4;

    private static void parse(SocketChannel sc) throws IOException, InterruptedException {
        System.out.println("start 处理客户端请求." + sc + ".." + Thread.currentThread().getName());
        ByteBuffer bb = ByteBuffer.allocate(1024);
        String req = "";

        //对于粘包问题 通过\r\n拆分数据包
        //对于拆包问题 可以保存这个半包数据和客户端地址对应起来，最后再拼起来
        while (sc.read(bb) > 0) {
            bb.flip();
            req += UTF8Utils.decode(bb);
            bb.clear();
        }

        //先读取本次所有的数据，然后在根据分隔符分割
        char[] reqChars = req.toCharArray();
        // 在此默认命令大小只有20000
        char[] onceReq = new char[20000];
        // 读取对于本客户端上次遗留的半包
        char[] oldReq = getSocketAddressOldReq(sc);
        // 拷贝半包
        System.arraycopy(oldReq, 0, onceReq, 0, oldReq.length);

        int count = 0;
        int index = oldReq.length;
        for (int i = 0, len = reqChars.length; i < len; i++) {
            if (reqChars[i] == '\r' && reqChars[i + 1] == '\n') {
                System.out.println("单次请求是：[" + String.valueOf(onceReq) + "]");

                //重置所有
                ++count;
                index = 0;
                i++;
            } else {
                onceReq[index++] = reqChars[i];
            }
        }

        // 如果这次存在多余的半包比如xxx/r/nxx，那么xx就是多余的两个字符
        if (index != 0) {
            char[] newOldReq = new char[index];
            System.arraycopy(onceReq, 0, newOldReq, 0, index);
            updateSocketAddressOldReq(sc, newOldReq);
        }

        System.out.println("此次接收到的消息大小为：" + req.length() + "，命令条数：" + count);

        updateSocketAddressReqCount(sc, count);

        System.out.println("end 处理客户端请求..." + Thread.currentThread().getName());

    }

    private static void updateSocketAddressOldReq(SocketChannel sc, char[] oldReq) {
        try {
            SocketAddress sa = sc.getRemoteAddress();
            addressOldReq.put(sa, oldReq);
            System.out.println(sa.toString() + "此次有半包======" + String.valueOf(oldReq));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static char[] getSocketAddressOldReq(SocketChannel sc) {
        char[] oldReq = new char[0];
        try {
            SocketAddress sa = sc.getRemoteAddress();
            if (addressOldReq.get(sc) != null) {
                System.out.println(sa.toString() + "此次存在历史遗留的包======" + String.valueOf(oldReq));
                oldReq = (char[]) addressOldReq.get(sc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return oldReq;
    }

    private static void updateSocketAddressReqCount(SocketChannel sc, int count) {
        try {
            SocketAddress sa = sc.getRemoteAddress();
            Integer oldCount = addressReqCount.get(sa);
            addressReqCount.put(sa, oldCount == null ? count : oldCount + count);
            System.out.println(sa.toString() + "当前已经请求了======" + addressReqCount.get(sa) + "次！！！");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static <K, V> K getKey(Map<K, V> map, V value) {
        for (K getKey : map.keySet()) {
            if (map.get(getKey) == value) {
                return getKey;
            }
        }
        return null;
    }

}
