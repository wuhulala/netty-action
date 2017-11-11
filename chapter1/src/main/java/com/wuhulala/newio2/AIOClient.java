package com.wuhulala.newio2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * Created by xueah20964 on 2017/5/22.
 */
public class AIOClient {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
        client.connect(new InetSocketAddress("127.0.0.1", 9888));

        if(client.getRemoteAddress() != null) {
            client.write(ByteBuffer.wrap("test".getBytes())).get();
        }
    }


}
