package com.meritdata.dam.datapacket.plan.acquistion.controller;

import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.service.ILityMatterConfigService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.MatterConfigDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/datapacket/lityMatter/config")
@Api(value = "发次实物api接口", tags = {"发次实物api接口"})
public class LityMatterConfigController {

    private static final Logger logger = LoggerFactory.getLogger(LityMatterConfigController.class);

    @Autowired
    ILityMatterConfigService lityMatterConfigService;


    /**
     * 根据图号获取实物信息
     */
    @ApiOperation(value = "根据图号获取实物信息", notes = "根据图号获取实物信息")
    @RequestMapping(value = {"getMatter"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody getMatter(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                @ApiParam(name = "图号", value = "图号", required = true, type = "string") @RequestParam String matter,
                                @ApiParam(name = "实物号", value = "实物号", required = false, type = "string") @RequestParam String physicalNo,
                                @ApiParam(name = "查询过滤条件", value = "0(查所有),1(查实物),2(查批次)", required = false, type = "string") @RequestParam String queryCriteria
    ) {
        try {
            return lityMatterConfigService.getMatterByMatter(page, rows, matter, physicalNo, queryCriteria);
        } catch (Exception e) {
            return ResultBody.failure("根据图号获取实物信息失败!");
        }
    }

    /**
     * 配置清单保存配置信息
     */
    @ApiOperation(value = "保存配置信息", notes = "保存配置信息")
    @RequestMapping(value = {"addMatterConfig"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody addMatterConfig(@RequestBody List<MatterConfigDTO> matterList, @RequestParam String lity) {
        try {
            return lityMatterConfigService.addMatterConfig(matterList, lity);
        } catch (Exception e) {
            return ResultBody.failure("保存配置信息失败!");
        }
    }

}
