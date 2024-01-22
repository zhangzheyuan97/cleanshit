package com.meritdata.dam.datapacket.plan.envelope.controller;

import com.alibaba.fastjson.JSONArray;
import com.meritdata.dam.base.log.annotation.OperateLogger;
import com.meritdata.dam.datapacket.plan.envelope.service.IEnvelopeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author fanpeng
 * @Date 2023/5/24
 * @Describe 包络分析controller
 */
@RestController
@RequestMapping("/api/datapacket/envelope")
@Api(value = "包络分析Api接口", tags = {"包络分析Api接口"})
public class EnvelopeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvelopeController.class);

    @Autowired
    private IEnvelopeService envelopeService;


    /**
     * 获取隐藏字段
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/getHideField", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "包络分析-获取隐藏字段", notes = "包络分析-获取隐藏字段")
    public List<String> getHideField() {
        return envelopeService.getHideField();
    }


    /**
     * 四舍五入计算方法
     *
     * @param data 数据
     * @param reg  分隔符
     * @return
     */
    @RequestMapping(value = "/roundHalfUp", method = RequestMethod.POST)
    @ResponseBody
    @OperateLogger(operation = "包络分析-对数字进行四舍五入")
    public String roundHalfUp(@ApiParam(name = "data", value = "要进行四舍五入的数据", required = true, type = "string") @RequestParam(required = false) String data,
                              @ApiParam(name = "reg", value = "分隔符", required = true, type = "string") @RequestParam(required = false) String reg) {
        return envelopeService.roundHalfUp(data, reg);
    }
}
