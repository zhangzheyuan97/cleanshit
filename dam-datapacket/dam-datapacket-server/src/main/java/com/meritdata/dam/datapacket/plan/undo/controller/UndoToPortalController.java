package com.meritdata.dam.datapacket.plan.undo.controller;

import com.alibaba.fastjson.JSON;
import com.meritdata.cloud.bpm.base.dto.UndoDTO;
import com.meritdata.cloud.utils.RSAUtils;
import com.meritdata.dam.datapacket.plan.undo.service.IPacketToPortalService;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.DESUtils;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Author fanpeng
 * @Date 2023/5/5
 * @Describe 待办同步至门户controller
 */
@RestController
@RequestMapping("/api/datapacket/bpm/synundo")
@Api(value = "同步门户待办API接口", tags = {"同步门户待办API接口"})
public class UndoToPortalController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UndoToPortalController.class);

    @Value("${CLOUD_HOME:cloud_home}")
    private String CLOUD_HOME;

    @Value("${datapacket.redirectUrl:}")
    private String redirectUrl;

    @Value("${datapacket.callBackUrl:}")
    private String callBackUrl;

    @Autowired
    private IPacketToPortalService packetToPortalService;

    /**
     * 用于跳转页面
     *
     * @return skipPath 具体的跳转路径
     */
    @RequestMapping("/view")
    public void doView(@RequestParam("userId") String userId, @RequestParam("businessId") String businessId,
                       HttpServletResponse response) throws Exception {
        //先解密
        String userCode = DESUtils.getDecryptString(userId);
        //存储路径(公钥地址)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String token = RSAUtils.encrypt(sdf.format(new Date()) + userCode, CLOUD_HOME + File.separator + "key");
        response.sendRedirect(redirectUrl + "?uid=" + businessId + "&userId=" + userCode + "&token=" + token);
    }


    @ApiOperation(value = "同步门户待办接口")
    @RequestMapping(value = "/to-portal/undo", method = RequestMethod.POST)
    public Boolean undo(@RequestBody String undos) {
        try {
            LOGGER.info("调用到了门户待办");
            List<UndoDTO> undoList = JSON.parseArray(undos, UndoDTO.class);
            packetToPortalService.syncUndo(undoList, Constants.UNDO_TYPE);

            // 后续处理逻辑
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("同步门户待办失败;", e);
            return false;
        }
    }

    @ApiOperation(value = "获取门户回调地址")
    @RequestMapping(value = "/getPortalCallBackUrl", method = RequestMethod.GET)
    public String getPortalCallBackUrl() {
       return callBackUrl;
    }

    @ApiOperation(value = "同步门户已办接口")
    @RequestMapping(value = "/to-portal/completed", method = RequestMethod.POST)
    public Boolean completed(@RequestBody String undos) {
        try {
            LOGGER.info("调用到了门户待办");
            List<UndoDTO> undoList = JSON.parseArray(undos, UndoDTO.class);
            // 后续处理逻辑
//            packetToPortalService.syncUndo(undoList, Constants.COMPLETE_TYPE);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("同步门户已办失败;", e);
            return false;
        }
    }

    /**
     * 根据userId获取token
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "获取用户token")
    @RequestMapping(value = "/getToken", method = RequestMethod.POST)
    public String getToken(@ApiParam(name = "userId", value = "用户加密字符串", required = true, type = "string") @RequestParam(value = "userId") String userId) {
        try {
            //先解密
            String userCode = DESUtils.getDecryptString(userId);
            //存储路径(公钥地址)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return RSAUtils.encrypt(sdf.format(new Date()) + userCode, CLOUD_HOME + File.separator + "key");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("获取" + userId + "的token失败", e);
            return "";
        }
    }

//    public static void main(String[] args) {
//        String admin = DESUtils.getEncryptString("fanpeng");
//        System.out.println("加密" + admin);
//        String decryptString = DESUtils.getDecryptString(admin);
//        System.out.println("解密" + decryptString);
//    }
}
