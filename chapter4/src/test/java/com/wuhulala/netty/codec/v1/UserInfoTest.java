package com.wuhulala.netty.codec.v1;

import com.wuhulala.util.StopWatcher;
import org.junit.Test;
import org.msgpack.MessagePack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description 作甚的
 */
public class UserInfoTest {
    private int count = 0;

    @Test
    public void codeC() throws Exception {
        UserInfo info = new UserInfo(20, "wuhulala");

        byte[] b = jdkCodeC(info);

        System.out.println("jdk serializable length is : " + b.length);

        printSeparator();

        System.out.println("custom serializable length is : " + info.codeC().length);

        //jdk serializable length is : 113
        //--------------------------------------------------------
        //custom serializable length is : 16
    }

    @Test
    public void testCodeCSpeed() throws IOException {
        UserInfo info = new UserInfo(20, "wuhulala");

        int loop = 1000000;

        StopWatcher w1 = new StopWatcher();
        byte[] b1 = new byte[0];
        for (int i = 0; i < loop; i++) {
            b1 = jdkCodeC(info);
        }

        long elapsed1 = w1.elapsedTime();
        System.out.println("jdk cost time is :" + elapsed1 + "ms" + ", cost space is : " + b1.length);


        printSeparator();
        printSeparator();


        StopWatcher w2 = new StopWatcher();
        byte[] b2  = new byte[0];
        for (int i = 0; i < loop; i++) {
             b2 = info.codeC();
        }

        long elapsed2 = w2.elapsedTime();
        System.out.println("custom cost time is :" + elapsed2 + "ms" + ", cost space is : " + b2.length);

        printSeparator();
        printSeparator();


        StopWatcher w3 = new StopWatcher();
        byte[] b3 = new byte[0];
        MessagePack msgPack = new MessagePack();

        for (int i = 0; i < loop; i++) {
            b3 = msgPack(msgPack, info);
        }

        long elapsed3 = w3.elapsedTime();
        System.out.println("messagePack cost time is :" + elapsed3 + "ms"+ ", cost space is : " + b3.length);

        //jdk cost time is :1682ms
        //--------------------------------------------------------
        //--------------------------------------------------------
        //jdk cost time is :181ms
    }

    private byte[] jdkCodeC(UserInfo info) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(info);
        os.flush();
        os.close();

        byte[] b = bos.toByteArray();
        bos.close();
        return b;
    }

    private byte[] msgPack(MessagePack msgPack, UserInfo info) throws IOException {
        //System.out.println(++count);
        return msgPack.write(info);
    }



    private void printSeparator() {
        printBlankLine();
        System.out.println("--------------------------------------------------------");
    }

    private void printBlankLine() {
        System.out.println("");
    }

}