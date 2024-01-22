package com.meritdata.dam.datapacket.plan.manage.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.bpm.base.dto.ProcOpinionDTO;
import com.meritdata.cloud.bpm.core.BpmEngine;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowListRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.ApproveStateResponse;
import com.meritdata.dam.datapacket.plan.manage.entity.response.FlowBomSheetEntityResponse;
import com.meritdata.dam.datapacket.plan.manage.entity.response.KeyValueResponse;
import com.meritdata.dam.datapacket.plan.manage.service.*;
import com.meritdata.dam.datapacket.plan.manage.service.impl.BpmService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleInfoDTO;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStateEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description: 包的流程审批
 **/
@RestController
@RequestMapping("/api/bpm")
@Api(tags = {"数据包流程", "数据包流程"})
@Validated
@Slf4j
public class PackageBpmController {

    //流程id
    private String procDefKey = "dam_form_approval";

    /**
     * 流程引擎
     */
    @Autowired
    BpmEngine bpmEngine;


    /**
     * 流程审核表
     */
    @Autowired
    IFlowApproveInter flowApproveInter;

    /**
     * 流程创建表
     */
    @Autowired
    IFlowCreateInter flowCreateInter;


    /**
     * 流程bom和表单的关系
     */
    @Autowired
    IFlowBomSheetInter flowBomSheetInter;

    @Autowired
    IFlowBomSheetDataInter flowBomSheetDataInter;

    @Autowired
    private IFlowTreeService flowTreeService;

    static ExecutorService threadPool = Executors.newFixedThreadPool(20);

    @ApiOperation(value = "数据包管理-流程-根据businessid及审核节点获取审核的人员", notes = "给基础平台的BPM提供")
    @RequestMapping(value = "/user/getUserCode/getUserList", method = RequestMethod.POST)
    public List<String> getNodeUserList(@RequestParam Map<String, Object> params) {
        String map = params.get("params").toString();
        JSONObject jsonObject = JSON.parseObject(map);
        String businessId = jsonObject.get("businessId").toString();
        String node = jsonObject.get("customCondition").toString();
        return flowApproveInter.getCustomUserCode(businessId, Integer.parseInt(node));
    }

    @RequestMapping(value = "/flow/over/listen", method = RequestMethod.POST)
    public String listenerService(@RequestParam Map<String, Object> params) {
        Logger listenerService = Logger.getLogger("listenerService");
        listenerService.info(JSON.toJSONString(params));
        String businessId = params.get("businessId").toString();

        Future<Integer> future = null;
        try {
            Callable<Integer> callable = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    //数据状态恢复
                    log.info("更新开始:" +System.currentTimeMillis());
                    // 把修改状态放在第一步 ，为了防止定时任务扫描到平台是over 但是 这边的状态还没改为pass，造成定时任务把状态给改为终止
                    // TODO: 2023/7/14 这块要将数据状态的isapprove改回来
//                    flowBomSheetInter.FlowOverDataState(businessId);
                    flowTreeService.setFlowOver(businessId);
                    flowCreateInter.setFlowPass(businessId);
                    //流程正常结束businessId
                    log.info("线程  执行修改完成!");
                    log.info("更新结束:" +System.currentTimeMillis());
                    return 0;
                }
            };
            future = threadPool.submit(callable);
        } catch (Exception ex) {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }

        }
        log.info("线程执行修改完成!");
        return "";
    }

    @RequestMapping(value = "/flow/firstNode/listen", method = RequestMethod.POST)
    public String taskListenerService(@RequestParam Map<String, Object> params) {
        Logger listenerService = Logger.getLogger("listenerService");
        listenerService.info(JSON.toJSONString(params));
        String businessId = params.get("businessId").toString();


        Future<Integer> future = null;
        try {
            Callable<Integer> callable = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    // 第一个节点会同时进入 要区分 是正常进入还是退回进入 两者更改的状态不一样
                    // 通过businessId 查询mainForm 数据根据 OperationType() 来判断 有值代表退回
                    Thread.sleep(1000);
                    ResultBody<MainFormDTO> mainformDTO = null;
                    try {
                        mainformDTO = bpmEngine.queryMainFormsByBusinessId(businessId);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        log.info("修改状态失败!");
                    }
                    if (null != mainformDTO
                            &&
                            null != mainformDTO.getData()
                    ) {
                        if (null != mainformDTO.getData().getProcDealStatus()){
                            // 区分驳回和第一个节点自己收回
                            if (StrUtil.isNotBlank(mainformDTO.getData().getOperationType())){

                                // 代表驳回
                                flowCreateInter.setFlowBack(businessId);
                            } else {
                                // 代表驳回
//                    flowCreateInter.setFlowBack(businessId);
                            }
                        }else {
                            // 代表正常进入 改为处理中
                            flowCreateInter.setFlowTrack(businessId);
                        }

                    }else {
                        // 第一次正常进入时 此时还是空但是已经到第一个节点了， 801 的特点是第一个节点会直接完成因此可以更改为进行中
                        // 其实这块可以不写因为在startProcess接口中直接设置为了进行中
                        // 代表正常进入 改为处理中
                        flowCreateInter.setFlowTrack(businessId);
                    }
                    return 0;
                }
            };
            future = threadPool.submit(callable);
        } catch (Exception ex) {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }

        }
        return "";
    }


    @RequestMapping(value = "/flow/stop/{id}", method = RequestMethod.POST)
    public void stopFlowById(@PathVariable String id) {
        log.info("终止业务id:" + id);
        Future<Integer> future = null;
        try {
            Callable<Integer> callable = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    //数据状态恢复
                    // TODO: 2023/7/14 这块要将数据状态的isapprove改回来
//                    flowBomSheetInter.recoverDataState(id);
                    flowTreeService.recoverDataState(id);
                    flowCreateInter.setFlowOver(id);
                    //流程正常结束businessId
                    log.info("线程  执行流程终止修改完成!");
                    return 0;
                }
            };
            future = threadPool.submit(callable);
        } catch (Exception ex) {
            if (!future.isDone() && !future.isCancelled()) {
                future.cancel(true);
            }

        }

    }

    @RequestMapping(value = "/flow/track", method = RequestMethod.POST)
    public void stopFlowById(@RequestParam Map<String, Object> params) {
        String id = params.get("businessId").toString();
        log.info("流程进行中:" + id);
        try {
            flowCreateInter.setFlowTrack(id);
        } catch (Exception e){
            log.info("流程进行中 id:" + id);
            e.printStackTrace();
        }
    }
}
