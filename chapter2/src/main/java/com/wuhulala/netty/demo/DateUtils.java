package com.wuhulala.netty.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wuhulala
 * @version 1.0
 * @description 作甚的
 * @since 17-11-11
 */
public class DateUtils {
    public static String nowDateTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date);
    }
}
