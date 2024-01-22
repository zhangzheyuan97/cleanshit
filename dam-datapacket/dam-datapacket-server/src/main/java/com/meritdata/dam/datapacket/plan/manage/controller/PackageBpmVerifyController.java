package com.meritdata.dam.datapacket.plan.manage.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.bpm.core.BpmEngine;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowListRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowVerifyRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.StartFlowListResponse;
import com.meritdata.dam.datapacket.plan.manage.entity.response.VerifyFlowListResponse;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowApproveInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowCreateInter;
import com.meritdata.dam.datapacket.plan.manage.service.impl.BpmService;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.utils.TempleteUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程审核的接口
 *
 * @author： lt.liu
 * 时间：2023/4/6
 * @description:
 **/


@RestController
@RequestMapping("/api/datapacket/verify")
@Api(tags = {"数据包管理-审核"})
@Validated
@Slf4j
public class PackageBpmVerifyController {

    //流程id
    private String procDefKey = "dam_form_approval";

    /**
     * 流程引擎
     */
    @Autowired
    BpmEngine bpmEngine;

    /**
     * 流程创建表
     */
    @Autowired
    IFlowCreateInter flowCreateInter;

    /**
     * 流程审核表
     */
    @Autowired
    IFlowApproveInter flowApproveInter;

    /**
     * 流程bom和表单的关系
     */
    @Autowired
    IFlowBomSheetInter flowBomSheetInter;

    @Autowired
    SessionUtils sessionUtils;

    @Autowired
    BpmService bpmService;
    @Autowired
    IDataPacketClient iDataPacketClient;
    @Autowired
    IFlowBomSheetDataInter flowBomSheetDataInter;
    @Autowired
    IDataPacketClient dataPacketClient;
    @Autowired
    TempleteUtil templeteUtil;
    @Autowired
    IModuleManageService moduleManageService;


    @ApiOperation(value = "流程发起-列表查询", notes = "1：模糊搜索我发起的流程-分系统设计师或单机设计师。2：模糊搜索全部流程-系统管理员。")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public ResultBody<List<StartFlowListResponse>> startProcessList(@Valid @RequestBody StartFlowVerifyRequest startFlowListRequest) throws Exception {
        //添加角色查询
        try {
            /**
             * 设置流程状态
             */
            List<MainFormDTO> bpmList = bpmService.queryMyApprove(procDefKey);
            log.info(String.format("%s%s%s", "我的审批查询到数据", bpmList.size(), "条"));

            //根据bussinessid 查询数据
            GridView<VerifyFlowListResponse> byPage = flowCreateInter.findByPage(startFlowListRequest, bpmList);
            List<VerifyFlowListResponse> flowCreateList = byPage.getRows();

            for (int i = 0; i < flowCreateList.size(); i++) {
                VerifyFlowListResponse flowCreateEntity = flowCreateList.get(i);
                //找到相同业务id的流程
                List<JSONObject> collect = new ArrayList<>();
                for (int s = 0; s < bpmList.size(); s++) {
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(bpmList.get(s)));
                    if (jsonObject.get("businessId").toString().equals(flowCreateEntity.getBussinessId())) {
                        collect.add(jsonObject);
                    }
                }

                if (collect.size() == 0) {
                    continue;
                }
                //设置流程的状态信息
                flowCreateEntity.setNodeAppriovaName(
                        flowApproveInter.setNodeAppriovaName(collect.get(0).get("nodeNames").toString(),
                                Long.parseLong(flowCreateEntity.getBussinessId())));   //接口不存在
                flowCreateEntity.setAppriovaNode(collect.get(0).get("nodeNames").toString());  //数据存在

            }
            byPage.setRows(flowCreateList);
            return ResultBody.success(byPage);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error("获取流程列表失败！");
            return ResultBody.failure("查询失败！");
        }
    }

}
