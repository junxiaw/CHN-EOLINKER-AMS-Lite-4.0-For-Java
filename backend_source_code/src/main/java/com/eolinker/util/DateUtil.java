package com.eolinker.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wangjunxia
 * @date 2019-01-16
 */
public class DateUtil {
    public static final String yyyy_MM_dd = "yyyy-MM-dd";

    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";

    public static final String yyyy_MM_dd_plus_HH_mm_ss = "yyyy/MM/dd+HH:mm:ss";

    public static final String yyyy_MM_dd_blank_HH_mm_ss = "yyyy/MM/dd HH:mm:ss";

    public static String formatAll(Date date){
        if ( date == null){
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss);
        return dateFormat.format(date);
    }

    public static String format(Date date, String format){
        if ( date == null){
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }
}
