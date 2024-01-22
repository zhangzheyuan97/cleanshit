package com.meritdata.dam.datapacket.plan.utils;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.utils.LogPattenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.meritdata.cloud.log.service.ILogPostService;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description: 打入系统日志的方法
 **/

public class LogUtils {

    @Autowired
    static ILogPostService logPostService;

    /**
     * @param message 错误日志信息
     * @param type    Message.STATUS_SUCESS
     * @author： lt.liu
     * 时间：2023/3/6
     * @description: 打入系统管理日志的方法
     **/

    public static void manageLog(String message, String type) {
        Message msg = new Message(Message.TYPE_OPT,
                LogPattenUtils.getProperty("manage.workbench.bmodule"),
                LogPattenUtils.getProperty("manage.workbench.fmodule"),
                LogPattenUtils.getProperty("manage.workbench.update"),
                message,
                type);
        logPostService.postLog(msg);
    }
}
