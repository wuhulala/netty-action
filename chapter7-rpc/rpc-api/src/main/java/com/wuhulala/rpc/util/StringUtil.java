package com.wuhulala.rpc.util;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by wuhulala on 2016/6/20.
 */
public final class StringUtil {

    public static final String SEPARATOR = String.valueOf((char)29);

    public static  boolean isEmpty(String strValue){
        if(strValue != null){
            strValue = strValue.trim();
        }
        return StringUtils.isEmpty(strValue);
    }

    public static boolean isNotEmpty(String strValue) {
        return !isEmpty(strValue);
    }

    /**
     * 分割固定格式的字符串
     */
    public static String[] splitString(String str, String separator) {
        return StringUtils.splitByWholeSeparator(str, separator);
    }
}
