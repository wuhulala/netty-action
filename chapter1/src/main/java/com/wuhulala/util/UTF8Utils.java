package com.wuhulala.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by xueah20964 on 2017/5/23.
 */
public class UTF8Utils {
    public static ByteBuffer encode(String str){
        return StandardCharsets.UTF_8.encode(str);
    }

    public static String decode(ByteBuffer buff) {
        return String.valueOf(StandardCharsets.UTF_8.decode(buff));
    }
}
