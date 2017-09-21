package com.tmall.util;

/**
 * 用于java.util.Date类与java.sql.Timestamp 类的互相转换
 */
public class DateUtil {
    public static java.sql.Timestamp d2t(java.util.Date d){
        if (null == d) {
            return null;
        }
        return new java.sql.Timestamp(d.getTime());
    }

    public static java.util.Date t2d(java.sql.Timestamp timestamp) {
        if (null == timestamp) {
            return null;
        }
        return new java.util.Date(timestamp.getTime());
    }
}
