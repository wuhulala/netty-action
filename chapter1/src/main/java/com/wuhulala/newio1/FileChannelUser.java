package com.wuhulala.newio1;


import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by xueah20964 on 2017/5/22.
 */
public class FileChannelUser {

    private static final int SIZE = 1024;

    public static void main(String[] args) throws IOException {
//        // 获取通道，该通道允许写操作
//        FileChannel fc = new FileOutputStream("D://data.txt").getChannel();
//        // 将字节数组包装到缓冲区中
//        fc.write(ByteBuffer.wrap("Some text".getBytes()));
//        // 关闭通道
//        fc.close();
//
//        // 随机读写文件流创建的管道
//        fc = new RandomAccessFile("D://data.txt", "rw").getChannel();
//        // fc.position()计算从文件的开始到当前位置之间的字节数
//        System.out.println("此通道的文件位置：" + fc.position());
//        // 设置此通道的文件位置,fc.size()此通道的文件的当前大小,该条语句执行后，通道位置处于文件的末尾
//        fc.position(fc.size());
//        System.out.println("此通道的文件位置：" + fc.position());
//
//        // 在文件末尾写入字节
//        fc.write(ByteBuffer.wrap("Some more".getBytes()));
//        fc.close();

        // 用通道读取文件
        FileChannel fc = new FileInputStream("D://data.txt").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(10);
        System.out.println("初始化buffer:【" + buffer.mark() + "】");

        // 将文件内容读到指定的缓冲区中
        fc.read(buffer);
        System.out.println("从通道读取至buffer后 :【" + buffer.mark() + "】");
        buffer.flip();//语句表示把 读取指针指向0的位置
        System.out.println("反转后 buffer后:【" + buffer.mark() + "】");

        while (buffer.hasRemaining()) {
            System.out.print((char) buffer.get());
        }
        System.out.println("从buffer读取读取完后:【" + buffer.mark() + "】");

        buffer.flip();//语句表示把 读取指针指向0的位置

        int count = 0;
        while (buffer.hasRemaining()) {
            if (++count == 3) {
                break;
            }

            System.out.print((char) buffer.get());
        }
        System.out.println("第二次从buffer读取读取完后:【" + buffer.mark() + "】");
        //buffer.flip();//语句表示把 读取指针指向0的位置

        buffer.compact(); //compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。limit属性依然像clear()方法一样，设置成capacity。现在Buffer准备好写数据了，但是不会覆盖未读的数据。
        System.out.println("设置buffer compact后:【" + buffer.mark() + "】");

        //--初始化buffer:【java.nio.HeapByteBuffer[pos=0 lim=1024 cap=1024]】
        //--从通道读取至buffer后 :【java.nio.HeapByteBuffer[pos=18 lim=1024 cap=1024]】
        //--反转后 buffer后:【java.nio.HeapByteBuffer[pos=0 lim=18 cap=1024]】
        //--Some textSome more从buffer读取读取完后:【java.nio.HeapByteBuffer[pos=18 lim=18 cap=1024]】
        //buffer 的容量cap没有改变是肯定的
        //buffer 的位置pos 0->18->0->18 这个是buffer的开始下标，类似为指针这种概念
        //buffer 的限制lim 1024->1024->18->18 在反转后lim变为18就是说明pos最多到这里
        fc.close();
    }

    @Test
    public void testFileChannel2FileChannel() throws IOException {
        long start = System.currentTimeMillis();

        try(RandomAccessFile fromFile = new RandomAccessFile("D://a.txt", "rw");
        FileChannel fromChannel = fromFile.getChannel();
        RandomAccessFile toFile = new RandomAccessFile("D://b.txt", "rw");
        FileChannel toChannel = toFile.getChannel();) {

            long position = 0;
            long count = fromChannel.size();
            System.out.println(position + ":" + count);
            toChannel.transferFrom(fromChannel, position, count);

        }

        long end = System.currentTimeMillis();
        System.out.println("花费时间 【" + (end - start) + "ms】");
    }

    @Test
    public void testFileChannel2BuffertoFileChannel() throws IOException {
        try(RandomAccessFile fromFile = new RandomAccessFile("D://data.txt", "rw");
        FileChannel fromChannel = fromFile.getChannel();
        RandomAccessFile toFile = new RandomAccessFile("D://data2.txt", "rw");
        FileChannel toChannel = toFile.getChannel();) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            long size = fromChannel.size();
            long count = 0;

            while (fromChannel.read(buffer) > 0) {
                buffer.flip();
                toChannel.write(buffer);
                count += buffer.limit();
                buffer.clear();
            }
        }
    }


    @Test
    public void test10000F2B2F() throws IOException {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 1; i++) {
            testFileChannel2BuffertoFileChannel();
        }
        long end = System.currentTimeMillis();
        System.out.println("花费时间 【" + (end - start) + "ms】");
    }
}
