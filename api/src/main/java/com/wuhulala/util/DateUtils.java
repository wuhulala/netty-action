package com.wuhulala.util;

import com.wuhulala.exception.BaseException;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wuhulala
 * @version 1.0
 * @date 2017/11/19
 * @description 作甚的
 */
public class DateUtils {

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";


    public static String nowDateTime(){
        return formatNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static String formatNow(String format) {
        if(StringUtils.isEmpty(format)){
            throw new BaseException("format can not be null !");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Date date = getNowDate();
        return sdf.format(date);
    }


    public static Date getNowDate() {
        return new Date();
    }
}
