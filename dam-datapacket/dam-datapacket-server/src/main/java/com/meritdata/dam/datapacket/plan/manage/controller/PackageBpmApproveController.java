package com.meritdata.dam.datapacket.plan.manage.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.bpm.base.dto.ProcOpinionDTO;
import com.meritdata.cloud.bpm.core.BpmEngine;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.base.log.annotation.OperateLogger;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackGroupService;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IMeritCloudClient;
import com.meritdata.dam.datapacket.plan.manage.dao.IInitFormDao;
import com.meritdata.dam.datapacket.plan.manage.entity.*;
import com.meritdata.dam.datapacket.plan.manage.entity.request.*;
import com.meritdata.dam.datapacket.plan.manage.entity.response.*;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowApproveInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowCreateInter;
import com.meritdata.dam.datapacket.plan.manage.service.impl.BpmService;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.PackageSystemInter;
import com.meritdata.dam.datapacket.plan.utils.CommUtil;
import com.meritdata.dam.datapacket.plan.utils.PageUtil;
import com.meritdata.dam.datapacket.plan.utils.TempleteUtil;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowColorEnum;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStateEnum;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description: 包的流程审批
 **/
@RestController
@RequestMapping("/api/datapacket/bpmapprove")
@Api(tags = {"数据包管理"})
@Validated
@Slf4j
public class PackageBpmApproveController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageBpmApproveController.class);

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

    @Autowired
    IDataPackGroupService iDataPackGroupService;


    /**
     * sql查询
     */
    @Autowired
    IMaintainService maintainService;

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
    ILogPostService logPostService;
    @Autowired
    IModuleManageService moduleManageService;
    @Autowired
    PackageSystemInter packageSystemInter;

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    @Autowired
    IInitFormDao iInitFormDao;


    @ApiOperation(value = "流程发起-bpm-审批发起校验", notes = "根据审批方式按照表单审批及实体BOM审批两种方式，及对应节点的审核人来发起审批流程，成功返回success=true")
    @RequestMapping(value = "/bpm/startprocessValidate", method = RequestMethod.POST)
    public ResultBody<List<String>> startProcessValidate(@Valid @RequestBody StartFlowRequest startFlowRequest) throws Exception {
        try {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.valid"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.valid.message"), JSON.toJSONString(startFlowRequest)),

                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);

            // 优化开始
            List<String> templateList = startFlowRequest.getTemplate();
            List<String> bomList = startFlowRequest.getBom();

//            try {
////                WebSocketClientUtil.sendMessage("{ \"status\": \"数据准备中\" }","111");
////                webSocketHandler.sendProgress("{ \"status\": \"数据准备中\" }",sessionUtils.getUserId());
//            } catch (Exception e){
//                e.printStackTrace();
//            } finally {
////                WebSocketClientUtil.closeClientConnect();
//            }
            /**
             * 查出公共数据后续方法直接使用
             */
//            List<ModuleManageDto> templeteList = moduleManageService
//                    .list("1", "1000000", "", "", ""); // 添加条件 in templateList
            List<ModuleManageDto> templeteList = moduleManageService
                    .listInTempleteList(templateList); //
//            List<ModuleManageDto> collect = templeteList.stream().filter(templeteListmodel -> templeteListmodel.getModelInfo().equals(template)).collect(Collectors.toList());

            QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
            Predicate predicate = qFlowBomSheetEntity.bomName.in(bomList);
            predicate = ExpressionUtils.or(predicate, qFlowBomSheetEntity.template.in(templateList));

            List<FlowBomSheetEntity> flowBomSheetEntityList = jpaQueryFactory.selectFrom(qFlowBomSheetEntity)
                    .where(predicate)
                    .fetchResults().getResults();

            //

//            Map<String,Boolean>  isNotOverMap = flowCreateInter.findEntityNotOverResult(flowBomSheetEntityList);
//            ResultBody<List<String>> validateBody = flowBomSheetInter.validate(startFlowRequest, list);
//            ResultBody<List<String>> validateBody = flowBomSheetInter.validate(startFlowRequest, flowBomSheetEntityList,templeteList,isNotOverMap);
            Map<String, List<Map<String, Object>>> bomTemMap = new HashMap<>();
            for (String temlateId : templateList) {
                bomTemMap = templeteUtil.findTemplateByBomList(temlateId, bomList, bomTemMap);
            }
            ResultBody<List<String>> body = flowCreateInter.validateAuthority(bomTemMap, "authority", flowBomSheetEntityList, templeteList);


            /**
             * 数据校验
             */
//            ResultBody<List<String>> body = flowCreateInter.validateAuthority(startFlowRequest, "authority");
//        //找到所有的实例
//        List<MainFormDTO> list = bpmService.queryMainFormsOver(procDefKey);
//        flowBomSheetInter.chageDataState(startFlowRequest,list);

            return ResultBody.success(body);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("审批发起校验失败!")));
        }
    }

    /**
     * @param startFlowRequest 表单数据
     * @param id               流程实例id
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "流程发起-bpm-发起审批", notes = "根据审批方式按照表单审批及实体BOM审批两种方式，及对应节点的审核人来发起审批流程，成功返回success=true")
    @RequestMapping(value = "/bpm/startprocess", method = RequestMethod.POST)
    public ResultBody startProcess(@Valid @RequestBody StartFlowRequest startFlowRequest, @RequestParam(required = false) String id) {
        System.out.println("开始" + System.currentTimeMillis());
        try {
            // 优化开始
            List<String> templateList = startFlowRequest.getTemplate();
            List<String> bomList = startFlowRequest.getBom();
            /**
             * 查出公共数据后续方法直接使用
             */
            // 优化前方法
//            List<ModuleManageDto> templeteList = moduleManageService
//                    .list("1", "1000000", "", "", ""); // 添加条件 in templateList
//            List<ModuleManageDto> collect = templeteList.stream().filter(templeteListmodel -> templeteListmodel.getModelInfo().equals(template)).collect(Collectors.toList());
            // 优化后
            List<ModuleManageDto> templeteList = moduleManageService
                    .listInTempleteList(templateList); //

            QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
            Predicate predicate = qFlowBomSheetEntity.bomName.in(bomList);
            predicate = ExpressionUtils.or(predicate, qFlowBomSheetEntity.template.in(templateList));
            List<FlowBomSheetEntity> flowBomSheetEntityList = jpaQueryFactory.selectFrom(qFlowBomSheetEntity)
                    .where(predicate)
                    .fetchResults().getResults();

            Map<String, Boolean> isNotOverMap = flowCreateInter.findEntityNotOverResult(flowBomSheetEntityList);
            /**
             * 优化前
             * ResultBody<List<String>> validateBody = flowBomSheetInter.validate(startFlowRequest, list);
             *
             */

            /**
             * 优化后
             */
            ResultBody<List<String>> validateBody = flowBomSheetInter.validate(startFlowRequest, flowBomSheetEntityList, templeteList, isNotOverMap);
            //有数据在流程中
            if (validateBody.isSuccess() && validateBody.getData().size() > 0) {
                return ResultBody.failure(JSON.toJSONString(validateBody.getData()));
            }

            /**
             * 数据存在没有编辑状态
             */
            // 优化前
//           ResultBody<List<String>> validateData = flowCreateInter.validateAuthority(startFlowRequest, "data");
            /**
             * 把数据组装成为 temlate 对应 List<bom> 将数据组装为 map key 为模板编码+'_' + bom编码  value 为对应的list
             * 若要使用时使用 map.get(bomSheet.getTemplate()+ "_" + bomSheet.getBomName()) 来获取
             */
            // 优化后
            Map<String, List<Map<String, Object>>> bomTemMap = new HashMap<>();
            for (String temlateId : templateList) {
                bomTemMap = templeteUtil.findTemplateByBomList(temlateId, bomList, bomTemMap);
            }

            List<FlowBomSheetEntity> tempList = new ArrayList<>();
            bomList.forEach(bom -> {
                templateList.forEach(template -> {
                    FlowBomSheetEntity build = FlowBomSheetEntity.builder()
                            .id(CommUtil.getUUID())
//                            .bussinessId(bussinessId)
                            .template(template)
//                            .nodeId(nodIds.get(bom))
//                            .nodeName(null == nodNames ? "" : nodNames.get(bom))
                            .bomName(bom).build();
                    tempList.add(build);
                });
            });
            ResultBody<List<String>> validateData = flowCreateInter.validateAuthority(bomTemMap, "data", tempList, templeteList);
            if (validateData.isSuccess() && validateData.getData().size() > 0) {
                return ResultBody.failure(JSON.toJSONString(validateData.getData()));
            }
            long bussinessId;
            if (StrUtil.isNotBlank(id)) {
                bussinessId = Long.parseLong(id);
            } else {
                bussinessId = flowCreateInter.findMaxBussinessId();
            }
            /**
             * 初始化流程创建表
             */
            if (StrUtil.isBlank(id)) {
                /**
                 * 这个方法中涉及发起流程的初始状态 没有优化的点
                 */
                flowCreateInter.save(startFlowRequest, sessionUtils.getEmp().getId(), bussinessId);
                // 同时在初始化表中保存一份数据
                // 保存表单数据
                InitFormEntity initData = new InitFormEntity();
                initData.setId(bussinessId + "");
                initData.setFormString(JSONObject.toJSONString(startFlowRequest));
                iInitFormDao.save(initData);
            } else {
                // 更新状态
                flowCreateInter.update(startFlowRequest, sessionUtils.getEmp().getId(), id);
                // 更新下数据
                // 保存表单数据
                InitFormEntity initData = new InitFormEntity();
                initData.setId(id);
                initData.setFormString(JSONObject.toJSONString(startFlowRequest));
                iInitFormDao.save(initData);
            }

            /**
             * 初始化流程审核表   里面优化了人员查询的方法
             */
            flowApproveInter.save(startFlowRequest.getAppriovaNode(), bussinessId);

            /**
             * 发起流程
             */
            ResultBody<MainFormDTO> mainFormDTOResultBody;
            try {
                mainFormDTOResultBody = bpmService.startProcess(procDefKey, bussinessId);
                if (!mainFormDTOResultBody.isSuccess()) {
                    log.error(String.format("%s=%s%s", procDefKey, bussinessId, "发起失败"));
                    log.error(JSON.toJSONString(mainFormDTOResultBody));
                    //流程发起失败，流程创建记录及审批记录要被删除
                    flowCreateInter.deleteByBussinessId(bussinessId);
                    flowApproveInter.deleteByBussinessId(bussinessId);
                    return ResultBody.failure(JSON.toJSONString(Arrays.asList("流程发起审批失败!")));
                } else {
                    log.error(String.format("%s=%s%s", procDefKey, bussinessId, "发起成功"));
                }
            } catch (Exception ex) {
                log.error("流程启动失败" + ex.toString());
                log.error(ex.getMessage());
                //流程发起失败，流程创建记录及审批记录要被删除
                flowCreateInter.deleteByBussinessId(bussinessId);
                flowApproveInter.deleteByBussinessId(bussinessId);
                return ResultBody.failure(JSON.toJSONString(Arrays.asList("流程发起审批失败!")));
            }
            /**
             * 初始化bom和表单的关系 只有保存方法无优化点
             */
            List<FlowBomSheetEntity> boomsheetList = flowBomSheetInter.save(startFlowRequest.getBom(), startFlowRequest.getTemplate(), startFlowRequest.getNodIds(), bussinessId, startFlowRequest.getNodNames());
            /**
             * 初始化bom和表单及data  里面涉及循环查询
             */
            // todo 待优化 查询全部不好优化
            flowBomSheetDataInter.initDate(startFlowRequest.getBom(), startFlowRequest.getTemplate(), bussinessId);
            /**
             * 修改数据的状态
             */
            //找到所有的实例
//            List<MainFormDTO> list = bpmService.queryMainFormsOver(procDefKey);

//            flowBomSheetInter.chageDataState(startFlowRequest, new ArrayList<>(), bussinessId + "");
            flowBomSheetInter.chageDataStateNew(bomTemMap, boomsheetList);

            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.start"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.start.message"), JSON.toJSONString(startFlowRequest)),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
            System.out.println("结束" + System.currentTimeMillis());

            return ResultBody.success();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("流程发起审批失败!")));
        }
    }


    @ApiOperation(value = "保存", notes = "保存草稿数据成功返回success=true")
    @RequestMapping(value = "/bpm/save", method = RequestMethod.POST)
    public ResultBody saveProcess(@Valid @RequestBody StartFlowRequest startFlowRequest, @RequestParam(required = false) String id) throws Exception {
        System.out.println("开始" + System.currentTimeMillis());
        try {
            // 第一次保存
            if (StrUtil.isBlank(id)) {
                /**
                 * 初始化流程创建表
                 */
                long bussinessId = flowCreateInter.findMaxBussinessId();
                /**
                 * 这个方法中涉及发起流程的初始状态 没有优化的点
                 */
                flowCreateInter.saveDraftData(startFlowRequest, sessionUtils.getEmp().getId(), bussinessId);
                // 保存表单数据
                InitFormEntity initData = new InitFormEntity();
                initData.setId(bussinessId + "");
                initData.setFormString(JSONObject.toJSONString(startFlowRequest));
                iInitFormDao.save(initData);
            } else {
                // 已有数据

                flowCreateInter.updateDraftData(startFlowRequest, Long.parseLong(id));
                // 更新数据
                Optional<InitFormEntity> byId = iInitFormDao.findById(id);
                if (byId.isPresent()) {
                    InitFormEntity initFormEntity = byId.get();
                    initFormEntity.setFormString(JSONObject.toJSONString(startFlowRequest));
                    iInitFormDao.save(initFormEntity);
                }
            }

            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.save"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.save.message"), JSON.toJSONString(startFlowRequest)),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
            return ResultBody.success();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResultBody.failure("流程表单数据保存失败");
        }
    }


    @ApiOperation(value = "编辑查看详情", notes = "编辑查看详情")
    @RequestMapping(value = "/bpm/detail", method = RequestMethod.POST)
    public ResultBody saveProcess(@RequestParam String id) {
        Optional<InitFormEntity> byId = iInitFormDao.findById(id);
        if (byId.isPresent()) {
            return ResultBody.success(byId.get());
        } else {
            return ResultBody.failure("未找到对应的数据");
        }
    }


    @ApiOperation(value = "流程发起-列表查询", notes = "1：模糊搜索我发起的流程-分系统设计师或单机设计师。2：模糊搜索全部流程-系统管理员。")
    @RequestMapping(value = "/startprocess/list", method = RequestMethod.POST)
    public ResultBody<List<StartFlowListResponse>> startProcessList(@Valid @RequestBody StartFlowListRequest startFlowListRequest) throws Exception {
        try {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.list"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.list.message"), JSON.toJSONString(startFlowListRequest)),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);


            //添加角色查询
            GridView<StartFlowListResponse> byPage = flowCreateInter.findByPage(startFlowListRequest);
            List<StartFlowListResponse> flowCreateList = byPage.getRows();
            /**
             * 设置流程状态
             */
            List<MainFormDTO> bpmList = null;
            switch (startFlowListRequest.getInferfaceState()) {
                case "all": {
                    bpmList = bpmService.queryMainForms(procDefKey);
                }
                break;
                default: {
                    bpmList = bpmService.queryMyApprove(procDefKey);
                }
            }
            for (int i = 0; i < flowCreateList.size(); i++) {
                StartFlowListResponse flowCreateEntity = flowCreateList.get(i);
                //找到相同业务id的流程
                List<MainFormDTO> collect = bpmList.stream().filter(model -> model.getBusinessId().equals(flowCreateEntity.getBussinessId()))
                        .collect(Collectors.toList());//
                if (collect.size() == 0) {
                    continue;
                }
                //解决基础平台BUG-下面代码不可修改
                if (null != collect && collect.size() > 0 && "draft".equals(collect.get(0).getProcDealStatus())) {
                    flowCreateEntity.setNodeAppriovaName(collect.get(0).getDrafterName());   //接口不存在
                } else {
                    //设置流程的状态信息
                    flowCreateEntity.setNodeAppriovaName(flowApproveInter.setNodeAppriovaName(collect.get(0).getNodeNames(),
                            Long.parseLong(flowCreateEntity.getBussinessId())));   //接口不存在
                }
                flowCreateEntity.setAppriovaNode(collect.get(0).getNodeNames());  //数据存在

            }
            byPage.setRows(flowCreateList);
            return ResultBody.success(byPage);
        } catch (Exception ex) {
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("查询失败!")));
        }
    }

    @ApiOperation(value = "流程发起-bpm--查看详情-审批状态", notes = "")
    @RequestMapping(value = "/bpm/detial/content", method = RequestMethod.POST)
    @ApiImplicitParams(
            @ApiImplicitParam(name = "bussinessId", value = "流程实例ID", required = true)
    )
    public ResultBody<ApproveStateResponse> getContent(@Valid @NotEmpty(message = "流程实例ID不能为空!")
                                                       @RequestParam(required = true, value = "bussinessId") String bussinessId) throws Exception {
        try {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.revise"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.revise.message"), bussinessId),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);


//            List<MainFormDTO> bpmList = bpmService.queryMainForms(procDefKey);
            MainFormDTO mainFormDTO = bpmService.queryMainForm(bussinessId);
//            List<MainFormDTO> collect = bpmList.stream()
//                    .filter(model -> model.getBusinessId()
//                            .equals(bussinessId.trim()))
//                    .sorted(Comparator.comparing(MainFormDTO::getUpdateTime))
//                    .collect(Collectors.toList());
            if (mainFormDTO == null) {
                return ResultBody.failure("流程未发起！");
            }
            FlowCreateEntity modelByBussinessId = flowCreateInter.findModelByBussinessId(bussinessId.trim());
            if (FlowStateEnum.DRAFT.getCode().equals(modelByBussinessId.getFlowState())) {
                return ResultBody.failure("流程未发起！");
            }
            // draft草稿箱、track跟踪、over办结
            String flowState = "流程发起";
            String flowStateCode = FlowStateEnum.DRAFT.getCode();
            ProcOpinionDTO content = bpmService.getContent(mainFormDTO.getProcInstId());
            String flowContent = content.getContent();
//            String status = collect.get(collect.size() - 1).getProcDealStatus();
//            if (StrUtil.isNotBlank(collect.get(collect.size() - 1).getOperationType())){
//                status = "back";
//            }
            String status = modelByBussinessId.getFlowState();
            switch (status) {
                case "track": {
                    flowState = "处理中";
                    flowContent = "您发起的流程正在审批中，请耐心等待......";
                    flowStateCode = FlowStateEnum.COUNTERSIGNER.getCode();
                }
                break;
                case "pass": {
                    //正常结束
                    flowState = "已通过";
                    flowContent = "您发起的流程已审批通过。";
                    flowStateCode = FlowStateEnum.REVIEWED.getCode();
                }
                break;
                case "stop": {
                    flowState = "已终止";
                    flowContent = "您发起的流程已被终止。";
                    flowStateCode = FlowStateEnum.STOPOVER.getCode();
                }
                break;
                case "back": {
                    flowState = "被退回";
                    flowContent = "您发起的流程已被退回。";
                    flowStateCode = FlowStateEnum.SENDBACK.getCode();

                }

                break;
//                case "draft":
                default: {
//                    flowState = "草稿";
//                    flowStateCode = FlowStateEnum.DRAFT.getCode();
                }
            }
            return ResultBody.success(
                    ApproveStateResponse
                            .builder()
                            .content(flowContent)
                            .flowState(flowState)
                            .flowStateCode(flowStateCode)
                            .currentNodeName(mainFormDTO.getNodeNames())
                            .build());
        } catch (Exception ex) {
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("获取流程状态失败!")));
        }
    }


    @ApiOperation(value = "流程发起-bpm-手动结束", notes = "此流程不是正常全部流转完成的结束")
    @RequestMapping(value = "/bpm/flowOver", method = RequestMethod.POST)
    @ApiImplicitParams(
            @ApiImplicitParam(name = "bussinessId", value = "流程实例ID", required = true)
    )
    public ResultBody<ApproveStateResponse> setFlowOver(@Valid @NotEmpty(message = "流程实例ID不能为空!")
                                                        @RequestParam(required = true, value = "bussinessId") String bussinessId) throws Exception {

        Message msg = new Message(Message.TYPE_OPT,
                LogPattenUtils.getProperty("model.manage.config.bmodule"),
                LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                LogPattenUtils.getProperty("model.manage.flow.over"),
                StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.over.message"), bussinessId),
                Message.STATUS_SUCESS);
        logPostService.postLog(msg);
        /**
         * 流程手动结束-设置数据状态
         */
        flowBomSheetInter.setFlowOver(bussinessId);
        return ResultBody.success();
    }


    @ApiOperation(value = "流程发起-bpm-流程正常流转完成结束", notes = "此流程是正常全部流转完成的结束")
    @RequestMapping(value = "/bpm/flowPass", method = RequestMethod.POST)
    @ApiImplicitParams(
            @ApiImplicitParam(name = "bussinessId", value = "流程实例ID", required = true)
    )
    public ResultBody<ApproveStateResponse> setFlowPass(@Valid @NotEmpty(message = "流程实例ID不能为空!")
                                                        @RequestParam(required = true, value = "bussinessId") String bussinessId) throws Exception {
        flowCreateInter.setFlowPass(bussinessId);
        return ResultBody.success();
    }


    @ApiOperation(value = "流程发起-查看详情-实做bomlist接口", notes = "实做BOM编号的清单")
    @RequestMapping(value = "/process/detial/bomList", method = RequestMethod.POST)
    @ApiImplicitParams(
            @ApiImplicitParam(name = "bussinessId", value = "流程实例ID", required = true)
    )
    public ResultBody<List<String>> getBomList(@Valid @NotEmpty(message = "流程实例ID不能为空!")
                                               @RequestParam(required = true, value = "bussinessId") String bussinessId) {
        try {

            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.bom"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.bom.message"), bussinessId),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
            return ResultBody.success(flowBomSheetInter.findBomListByBussinessId(bussinessId));
        } catch (Exception ex) {
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("查询bom列表失败!")));
        }
    }


    @ApiOperation(value = "流程发起-查看详情-实做bom与表单list", notes = "实做bom与表单列表数据")
    @RequestMapping(value = "/process/detial/bomsheet", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bussinessId", value = "流程实例ID", required = true),
            @ApiImplicitParam(name = "bom", value = "实做BOM", required = false)
    })
    public ResultBody<List<FlowBomSheetEntityResponse>> getBomSheetList(@Valid @NotEmpty(message = "流程实例ID不能为空!")
                                                                        @RequestParam(required = true, value = "bussinessId") String bussinessId,
                                                                        @RequestParam(required = false, value = "bom") String bom
    ) {
        try {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.bomsheet"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.bomsheet.message"), bussinessId, bom),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);

            List<FlowBomSheetEntity> bomSheetList = flowBomSheetInter.getBomSheetList(bussinessId, bom);
            List<FlowBomSheetEntityResponse> responseList = new ArrayList<>();

            //查询所有的权限
            PackageSystemEntity auth = new PackageSystemEntity();
            auth.setType("person");
            auth.setSystemId(sessionUtils.getEmp().getId());
            List<PackageSystemEntity> authorityDataByEntity = packageSystemInter.findAuthorityDataByEntity(auth);

            bomSheetList.forEach(source -> {
                FlowBomSheetEntityResponse target = FlowBomSheetEntityResponse.builder().build();
                BeanUtils.copyProperties(source, target);
                List<ModuleManageDto> allTempLete = templeteUtil.getALLNewTempLete(source.getTemplate(), "", "");
                if (allTempLete.size() > 0) {
                    List<ModuleManageDto> collect = allTempLete.stream().filter(templete -> templete.getModelInfo().equals(source.getTemplate())).collect(Collectors.toList());
                    target.setTableName(collect.get(0).getTableName());
                    target.setModuleCode(collect.get(0).getCode());
                    target.setModelId(collect.get(0).getModelInfo());
                    target.setTemplateName(collect.get(0).getName());
                    long count = authorityDataByEntity.stream().filter(model -> model.getResourceId().equals(source.getNodeName())).count();
                    target.setHasAuthority(count > 0 ? true : false);
                }
                responseList.add(target);
            });
            return ResultBody.success(responseList);
        } catch (Exception ex) {
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("查询列表失败!")));
        }
    }


    @ApiOperation(value = "数据包管理-流程-流程key", notes = "返回数据包流程审批的KEY")
    @RequestMapping(value = "/process/key", method = RequestMethod.POST)
    public ResultBody<String> processKey() {
        return ResultBody.success(procDefKey);
    }


    @ApiOperation(value = "数据包管理-流程-根据businessid及审核节点获取审核的人员", notes = "给基础平台的BPM提供")
    @RequestMapping(value = "/getUserCode/getNodeUserList", method = RequestMethod.POST)
    public List<String> getNodeUserList(@RequestParam Map<String, Object> params) {
        String businessId = "";
        int node = 1;
        return flowApproveInter.getCustomUserCode(businessId, node);
    }


    @ApiOperation(value = "数据包管理-审批类型", notes = "给数据包管理提供")
    @RequestMapping(value = "/getapprovalType", method = RequestMethod.POST)
    public List<KeyValueResponse> getApprovalType() {
        List<KeyValueResponse> list = new ArrayList<>();
        for (int i = 0; i < FlowStateEnum.values().length; i++) {
            list.add(KeyValueResponse.builder()
                    .key(FlowStateEnum.values()[i].getCode())
                    .value(FlowStateEnum.values()[i].getValue()).build());
        }
        return list;
    }

    @ApiOperation(value = "数据包管理-流程状态", notes = "获取流程状态")
    @RequestMapping(value = "/getBpmState", method = RequestMethod.POST)
    public List<KeyValueResponse> getBpmState() {
        List<KeyValueResponse> list = new ArrayList<>();
        list.add(KeyValueResponse.builder().key("0").value("按照实做BOM审批").build());
        list.add(KeyValueResponse.builder().key("1").value("按照表单审批").build());

        return list;
    }


    @Autowired
    com.meritdata.dam.datapacket.plan.client.IDatamationsClient idatamationsClient;

    @ApiOperation(value = "数据包管理-getBean", notes = "获取流程状态-测试接口")
    @RequestMapping(value = "/getBean", method = RequestMethod.POST)
    public List<Map<String, Object>> getBean() {
        Map<String, String> map = new HashMap<>();
        map.put("bom", "阀门");
        map.put("templete", "element");
        List<Map<String, Object>> mapPageResult = idatamationsClient.querySupportingListByBomAndTemplete(map);
        int a = 1;
        return mapPageResult;
    }


    @ApiOperation(value = "数据包管理-数据包版本", notes = "根据实物bom和模板名称获取流程信息")
    @RequestMapping(value = "/getFlowNumberByBomAndTemplete", method = RequestMethod.POST)
    public ResultBody<ApproveVersionResponse> getFlowNumberByBomAndTemplete(@Valid @RequestBody ApproveVersionRequrst request) {
        List<ApproveVersionResponse> approveVersionResponsesList = new ArrayList<>();
        //找到所有已经通过的流程
        List<Long> bussinessIds = iDataPackGroupService.getBusinessId(request.getBatchNoNodeInfo(), request.getTemplate());
        if (bussinessIds.size() == 0) {
            GridView<ApproveVersionResponse> gridView = new GridView(approveVersionResponsesList,
                    Long.parseLong(approveVersionResponsesList.size() + ""), request.getPageSize(), request.getPageNumber());
            return ResultBody.success(gridView);
        }
        //找到所有的实例
        List<MainFormDTO> list = bpmService.queryMainFormsOver(procDefKey);
        if (list.size() == 0) {//list如果为空调用微服务平台
            for (Long bussinessId : bussinessIds) {
                MainFormDTO mainFormDTOResultBody = bpmService.queryFlowInstanceIdByBussinessID(bussinessId.toString()).getData();
                list.add(mainFormDTOResultBody);
            }
        }
        //按审签时间降序排列和条件筛选
        List<ApproveVersionResponse> approveVersionResponse = flowBomSheetInter.getApproveVersionResponse(bussinessIds, list, approveVersionResponsesList, request);
        for (int i = 0; i < approveVersionResponse.size(); i++) {
            approveVersionResponse.get(i).setVersion(approveVersionResponse.size() - i);
        }
        //分页
        PageUtil<ApproveVersionResponse> approveVersionResponsePage = new PageUtil<>(approveVersionResponse, request.getPageSize(), request.getPageNumber());

        GridView<ApproveVersionResponse> gridView = new GridView(approveVersionResponsePage.getList(),
                Long.parseLong(approveVersionResponsesList.size() + ""), request.getPageSize(), request.getPageNumber());
        return ResultBody.success(gridView);
    }


    /**
     * 数据包版本流程excl下载
     *
     * @param response
     * @param attributes
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/download/dataVersion")
    @ResponseBody
    @ApiOperation(value = "下载excl文件", notes = "下载excl文件")
    @OperateLogger(operation = "下载excl文件")
    public void dataVersionDownload(HttpServletResponse response,
                                    @ApiParam(name = "attributes", value = "树节点属性", required = true, type = "HashMap") @RequestBody HashMap attributes) {
//                                    @ApiParam(name = "batchNoNodeInfo", value = "按批次号审批时需要的树节点属性", required = true) @RequestBody BatchNoNodeInfo batchNoNodeInfo) {
        String status = Message.STATUS_FAIL;
        try {
            status = Message.STATUS_SUCESS;
            if (!attributes.containsKey("batchNoNodeInfo")) {
                LOGGER.error("数据包版本流程excl下载失败,未传入按批次号审批的树节点属性信息");
            }
            BatchNoNodeInfo batchNoNodeInfo = JSONObject.parseObject(JSON.toJSONString(attributes.get("batchNoNodeInfo")), BatchNoNodeInfo.class);
            flowBomSheetDataInter.dataVersionDownload(response, attributes, batchNoNodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("数据包版本流程excl下载失败", e);
            status = Message.STATUS_FAIL;
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.search"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.search.message"), "数据包版本流程excl下载"),
                    status);
            logPostService.postLog(msg);
        }
    }


    @ApiOperation(value = "数据包管理-数据包版本-获取状态", notes = "根据实物bom和模板名称获取模板状态")
    @RequestMapping(value = "/getFlowStateByBomAndTemplete", method = RequestMethod.POST)
    public ResultBody<List<ApproveResponse>> getFlowStateByBomAndTemplete(@Valid @RequestBody List<ApproveRequrst> request) {

        Message msg = new Message(Message.TYPE_OPT,
                LogPattenUtils.getProperty("model.manage.config.bmodule"),
                LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                LogPattenUtils.getProperty("model.manage.flow.state"),
                StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.state.message"), JSON.toJSONString(request)),
                Message.STATUS_SUCESS);
        logPostService.postLog(msg);

        List<ApproveResponse> response = new ArrayList<>();
        request.forEach(requestModel -> {
            FlowBomSheetEntity lastModelByBomAndTemplete = flowBomSheetInter.findLastModelByBomAndTemplete(requestModel.getBom(),
                    requestModel.getBom());
            String color = "";
            if (null == lastModelByBomAndTemplete) {
                color = FlowColorEnum.YELLOW.getCode();
            } else {
                FlowCreateEntity flowCreateEntity = flowCreateInter.findModelByBussinessId(lastModelByBomAndTemplete.getBussinessId() + "");
                if (flowCreateEntity.getFlowState().equals(FlowStateEnum.REVIEWED.getCode())) {
                    color = FlowColorEnum.GREE.getCode();
                } else {
                    color = FlowColorEnum.RED.getCode();
                }
            }
            response.add(ApproveResponse.builder()
                    .bom(requestModel.getBom())
                    .template(requestModel.getBom())
                    .color(color)
                    .build());
        });
        return ResultBody.success(response);
    }


    @Autowired
    IMeritCloudClient iMeritCloudClient;

    @ApiOperation(value = "数据包管理-测试", notes = "测试")
    @RequestMapping(value = "/test", method = RequestMethod.GET)

    @ApiImplicitParams({
            @ApiImplicitParam(name = "S_M_SYS_DATAID", value = "S_M_SYS_DATAID", required = true),
            @ApiImplicitParam(name = "S_M_SYS_VERSION", value = "S_M_SYS_VERSION", required = false),
            @ApiImplicitParam(name = "ModelId", value = "ModelId", required = false),
            @ApiImplicitParam(name = "F_M_SYS_ID", value = "F_M_SYS_ID", required = false)
    })
    public ResultBody test(@RequestParam(required = true, value = "S_M_SYS_DATAID") String S_M_SYS_DATAID,
                           @RequestParam(required = true, value = "S_M_SYS_VERSION") String S_M_SYS_VERSION,
                           @RequestParam(required = true, value = "ModelId") String ModelId,
                           @RequestParam(required = true, value = "F_M_SYS_ID") String F_M_SYS_ID) {
        ResultBody body = templeteUtil.updateModelDataStateCenter(
                S_M_SYS_DATAID,
                S_M_SYS_VERSION,
                ModelId,
                F_M_SYS_ID);
        return ResultBody.success(body);
    }


    /**
     * @param startFlowRequest 审批人员信息
     * @param id               流程实例id
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "流程退回后修改审批人", notes = "流程退回后修改审批人")
    @RequestMapping(value = "/bpm/update/user", method = RequestMethod.POST)
    @Transactional
    public ResultBody modifyApproveUsers(@Valid @RequestBody StartFlowRequest startFlowRequest, @RequestParam String id) {
        try {
            /**
             * 删除旧的人员
             */
            flowApproveInter.delete(Long.parseLong(id));
            // 插入新的人员
            flowApproveInter.save(startFlowRequest.getAppriovaNode(), Long.parseLong(id));
            // 更新详情表
            Optional<InitFormEntity> byId = iInitFormDao.findById(id);
            if (byId.isPresent()) {
                InitFormEntity initFormEntity = byId.get();
                initFormEntity.setFormString(JSONObject.toJSONString(startFlowRequest));
                iInitFormDao.save(initFormEntity);
            }
//
//            Message msg = new Message(Message.TYPE_OPT,
//                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
//                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
//                    LogPattenUtils.getProperty("model.manage.flow.start"),
//                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.start.message"), JSON.toJSONString(startFlowRequest)),
//                    Message.STATUS_SUCESS);
//            logPostService.postLog(msg);
            return ResultBody.success();
        } catch (Exception ex) {
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("修改流程审批人员失败!")));
        }
    }


    /**
     * 前端控制只能删除草稿状态下数据
     *
     * @param id 流程实例id
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "草稿状态数据删除", notes = "草稿状态数据删除")
    @RequestMapping(value = "/bpm/delete/draft", method = RequestMethod.POST)
    public ResultBody deleteDraftData(@RequestParam String id) {
        try {
            /**
             * 删除流程数据
             */
            flowCreateInter.deleteByBussinessId(Long.parseLong(id));
            // 删除表单数据
            iInitFormDao.deleteById(id + "");
//
//            Message msg = new Message(Message.TYPE_OPT,
//                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
//                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
//                    LogPattenUtils.getProperty("model.manage.flow.start"),
//                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.start.message"), JSON.toJSONString(startFlowRequest)),
//                    Message.STATUS_SUCESS);
//            logPostService.postLog(msg);
            return ResultBody.success();
        } catch (Exception ex) {
            return ResultBody.failure("删除草稿失败!");
        }
    }


}
