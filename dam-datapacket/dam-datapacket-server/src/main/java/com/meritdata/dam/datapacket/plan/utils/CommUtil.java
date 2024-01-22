package com.meritdata.dam.datapacket.plan.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author： lt.liu
 * 时间：2023/3/8
 * @description:
 **/
public class CommUtil {
    /**
     * 获取uuid
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }


    /**
     * 获取时间戳
     * @return
     */
    public static Timestamp getTimestamp() {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        return  timestamp;
    }
}
