package com.meritdata.dam.datapacket.plan.manage.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.bpm.base.dto.MainFormQueryDTO;
import com.meritdata.cloud.bpm.base.dto.ProcOpinionDTO;
import com.meritdata.cloud.bpm.base.dto.ProcTrackDTO;
import com.meritdata.cloud.bpm.base.rule.RuleEngine;
import com.meritdata.cloud.bpm.core.BpmEngine;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStateEnum;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStepNodeEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author： lt.liu
 * 时间：2023/3/8
 * @description:
 **/
@Service
public class BpmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BpmService.class);
    @Autowired
    BpmEngine bpmEngine;
    @Autowired
    SessionUtils sessionUtils;

    /**
     * 根据流程key获取流程实例
     *
     * @param procDefKey
     * @return
     */
    public List<MainFormDTO> queryMainForms(String procDefKey) {
        MainFormQueryDTO formQuery = new MainFormQueryDTO();
        // 流程状态
        List<String> systemAdmin = sessionUtils.getRoleCodes().stream().filter(value -> value.toUpperCase().equals("XTGLY")).collect(Collectors.toList());
        //我发起界面
//         if (systemAdmin.size()>0){
        //系统管理员查询全部的
        formQuery.setBpmStatus("all");
//         }else{
//             //普通人查看我发起的流程
//             formQuery.setBpmStatus("myAllDraft");
//         }
        formQuery.setOrderFields(Lists.newArrayList(formQuery.new Field(MainFormQueryDTO.SORT_BY_UPDATE_TIME, true)));
        String mainFormQueryDtoJson = JSON.toJSONString(formQuery);
        Long page = Long.parseLong("0");
        Long rows = Long.parseLong("20000");
        // 调用查询流程实例
        ResultBody<GridView> processInstanceVos = bpmEngine.queryMainForms(mainFormQueryDtoJson, procDefKey, page, rows);
        //实现对象的拷贝
        List<Map<String, Object>> rows1 = processInstanceVos.getData().getRows();
        List<MainFormDTO> mainFormDtoList = new ArrayList<>();
        rows1.forEach(source -> {
            MainFormDTO target = JSONObject.parseObject(JSONObject.toJSONString(source), MainFormDTO.class);
            mainFormDtoList.add(target);
        });
        return mainFormDtoList;
    }

    /**
     * 查询流程状态是结束的流程
     *
     * @param procDefKey
     * @return
     */
    public List<MainFormDTO> queryMainFormsOver(String procDefKey) {
        MainFormQueryDTO formQuery = new MainFormQueryDTO();
        // 流程状态
        boolean xtgly = sessionUtils.getRoleCodes().stream().anyMatch(item -> item.toUpperCase().equals("XTGLY"));
//        List<String> systemAdmin = sessionUtils.getRoleCodes().stream().filter(value -> value.toUpperCase().equals("XTGLY")).collect(Collectors.toList());
        if (xtgly) {
            //系统管理员查询全部的
            formQuery.setBpmStatus("allover");
        } else {
            //普通人查看我发起的流程
            formQuery.setBpmStatus("myover");
        }

        ArrayList<MainFormQueryDTO.Field> fields = Lists
                .newArrayList(formQuery
                        .new Field(MainFormQueryDTO.SORT_BY_UPDATE_TIME, true));
        formQuery.setOrderFields(fields);
        String mainFormQueryDtoJson = JSON.toJSONString(formQuery);
        Long page = Long.parseLong("1");
        Long rows = Long.parseLong("10000000");
        // 调用查询流程实例
        List<MainFormDTO> processInstanceVos = bpmEngine.queryMyApply(mainFormQueryDtoJson, procDefKey, page, rows).getData().getRows();
        return processInstanceVos;
    }


    /**
     * 根据流程key获取流程实例
     *
     * @param procDefKey
     * @return
     */
    public List<MainFormDTO> queryMyApprove(String procDefKey) {
        MainFormQueryDTO formQuery = new MainFormQueryDTO();
        // 流程状态
        formQuery.setBpmStatus("all");
        formQuery.setOrderFields(Lists.newArrayList(formQuery.new Field(MainFormQueryDTO.SORT_BY_UPDATE_TIME, true)));
        String mainFormQueryDtoJson = JSON.toJSONString(formQuery);
        Long page = Long.parseLong("0");
        Long rows = Long.parseLong(Constants.ROWS + "");
        // 调用查询流程实例
        List<MainFormDTO> processInstanceVos = bpmEngine.queryMyApprove(mainFormQueryDtoJson, procDefKey, page, rows).getData().getRows();
        return processInstanceVos;
    }

    /**
     * 启动流程
     *
     * @param procDefKey
     * @param bussinessId
     * @return
     * @throws Exception
     */
    public ResultBody<MainFormDTO> startProcess(String procDefKey, Long bussinessId) throws Exception {
        /**
         * 发起流程
         */
        Map variables = new HashMap();
        variables.put("procDefkey", procDefKey);
        variables.put("businessId", bussinessId);
        ResultBody<MainFormDTO> mainForm = bpmEngine.startProcess(procDefKey, variables);
        LOGGER.info("启动接口返回:" + JSONObject.toJSONString(mainForm));
        complete(mainForm.getData().getId(), mainForm.getData().getBusinessId(), procDefKey);
        return mainForm;
    }

    public ResultBody<MainFormDTO> complete(String id, String businessId, String procDefkey) throws Exception {
        // 任务id

        Map variablesRun = new HashMap();
        variablesRun.put("procDefkey", procDefkey);
        variablesRun.put("businessId", businessId);
        ResultBody<RuleEngine> ruleEngineResultBody = bpmEngine.ruleEngineRun(variablesRun);

        Map variables = new HashMap();
        variables.put("businessId", businessId);
        variables.put("runtime[taskDefKey]", ruleEngineResultBody.getData().getRuntime().getTaskDefKey());
        variables.put("approve[type]", "onlyApprove");
        variables.put("bpmOperValue", "bpmSendBtn");
        variables.put("bpmOperName", "发起审批");
        // 调用完成任务
        ResultBody<MainFormDTO> mainFormVo = bpmEngine.complete(ruleEngineResultBody.getData().getRuntime().getTaskId(), variables);
        return ResultBody.success(mainFormVo);
    }


    /**
     * 获取最近一条审批意见
     *
     * @param procInstId
     * @return
     */
    public ProcOpinionDTO getContent(String procInstId) {
        //
        Map map = new HashMap();
        map.put("procInstId", procInstId);
        List<ProcOpinionDTO> procOpinion = bpmEngine.getOpinions(map).getData();
        List<ProcOpinionDTO> collect = procOpinion.stream()
                .sorted(Comparator.comparing(ProcOpinionDTO::getUpdateTime))
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            return collect.get(collect.size() - 1);
        } else {
            return new ProcOpinionDTO();
        }

    }

    /**
     * 通过业务id获取实例
     */
    public ResultBody<MainFormDTO> queryFlowInstanceIdByBussinessID(String businessId) {
        // 调用查询流程实例
        ResultBody<MainFormDTO> mainformDTO = null;
        try {
            mainformDTO = bpmEngine.queryMainFormsByBusinessId(businessId);
        } catch (Exception ex) {
        }
        return mainformDTO;
    }


    /**
     * 文字追踪,图表追踪,审批意见
     *
     * @param businessId 流程实例编号
     * @return
     */
    public List<ProcTrackDTO> getContentByBussinessID(String businessId) {
        // 调用查询流程实例
        ResultBody<MainFormDTO> mainformDTO = bpmEngine.queryMainFormsByBusinessId(businessId);
        //第一步：获取流程实例id
        String procInstId = mainformDTO.getData().getProcInstId();
        //第二步：根据流程实例获取审核意见

        // 调用 任务跟踪-查询某个流程实例的文字追踪数据
        List<ProcTrackDTO> procOpinion = bpmEngine.getPage(procInstId, null, null).getData();
        return procOpinion;
    }


    /**
     * 判断当前流程是一个咋样的状态
     *
     * @param businessId
     * @return
     */
    public FlowStateEnum JudgeFlowStep(String businessId, String procDefKey, List<MainFormDTO> allFlows) {
        if (ObjectUtils.isEmpty(allFlows)) {
            allFlows = queryMainForms(procDefKey);
        }
        List<ProcTrackDTO> procTrackDto = getContentByBussinessID(businessId);
        /**
         * 返回流程是正常结束？，手动结束，还是正在运行
         */
        for (int i = 0; i < procTrackDto.size(); i++) {
            //正常结束
            if (procTrackDto.get(i).getNodeName().equals(FlowStepNodeEnum.APPROVE.getValue()) && procTrackDto.get(i).getOperate().equals("发送")) {
                return FlowStateEnum.REVIEWED;
            } else
                //异常终止
                if (procTrackDto.get(i).getOperate().equals("流程终止")) {
                    return FlowStateEnum.STOPOVER;
                }
        }

        //草稿状态流程
        long draft = allFlows.stream().filter(flow -> flow.getProcDealStatus().toLowerCase().equals("draft") && flow.getBusinessId().equals(businessId)).count();
        if (draft > 0) {
            //草稿
            return FlowStateEnum.DRAFT;
        } else {
            //处理中
            return FlowStateEnum.COUNTERSIGNER;
        }
    }

    public MainFormDTO queryMainForm(String bussinessId) {
        ResultBody<MainFormDTO> mainFormDTOResultBody = bpmEngine.queryMainFormsByBusinessId(bussinessId);
        if (mainFormDTOResultBody.isSuccess()){
            return mainFormDTOResultBody.getData();
        }
        return null;
    }
}
