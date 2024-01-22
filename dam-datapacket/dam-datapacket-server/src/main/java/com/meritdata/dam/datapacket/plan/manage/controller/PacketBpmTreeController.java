package com.meritdata.dam.datapacket.plan.manage.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.properties.MeritdataCloudProperties;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.base.exception.ParamNotBlankException;
import com.meritdata.dam.base.log.annotation.OperateLogger;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.LityInfoVo;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.factory.ExecutorProcessPool;
import com.meritdata.dam.datapacket.plan.manage.dao.IInitFormDao;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetDataEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.InitFormEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowBatchRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.ApproveBatch;
import com.meritdata.dam.datapacket.plan.manage.entity.response.FlowBomSheetEntityResponse;
import com.meritdata.dam.datapacket.plan.manage.service.*;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowApproveInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowCreateInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowTreeService;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.service.ITreeService;
import com.meritdata.dam.datapacket.plan.service.impl.TreeServiceImpl;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.PackageSystemInter;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.PageUtil;
import com.meritdata.dam.datapacket.plan.utils.RedisTemplateService;
import com.meritdata.dam.datapacket.plan.utils.TempleteUtil;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import io.swagger.annotations.*;
import com.meritdata.dam.datapacket.plan.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigInteger;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author fanpeng
 * @Date 2023/7/5
 * @Describe 按批次号审批
 */

@RestController
@RequestMapping("/api/datapacket/bpm")
@Api(tags = {"数据包流程", "发起审批"})
@Slf4j
public class PacketBpmTreeController {

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private RedisTemplateService redisTemplateService;

    @Autowired
    private ITreeService treeService;

    @Autowired
    private TreeServiceImpl treeServiceImpl;

    @Autowired
    private IFlowTreeService flowTreeService;
    /**
     * 流程bom和表单的关系
     */
    @Autowired
    IFlowBomSheetInter flowBomSheetInter;
    @Autowired
    PackageSystemInter packageSystemInter;
    @Autowired
    TempleteUtil templeteUtil;
    @Autowired
    IFlowBomSheetDataInter iFlowBomSheetDataInter;
    /**
     * 判断密集
     */
    @Autowired
    private MeritdataCloudProperties meritdataCloudProperties;
    @Autowired
    IMaintainService iMaintainService;

    @Autowired
    private IFlowCreateInter flowCreateInter;

    @Autowired
    private IFlowApproveInter flowApproveInter;

    @Autowired
    private IInitFormDao iInitFormDao;

    @Autowired
    ILogPostService logPostService;

    @Autowired
    IModuleManageService moduleManageService;

    //流程id
    private static String PROCDEFKEY = "dam_form_approval";

    /**
     * 数据维护模型树查询
     *
     * @return
     */
    @RequestMapping(value = "/getTree", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "发起审批左侧树查询", notes = "发起审批左侧树查询")
    public ResultBody getTree(@ApiParam(name = "keywords", value = "过滤条件", required = false, type = "string") @RequestParam(required = false) String keywords) {
        try {
            String userId = sessionUtils.getEmpId();
            //判断是否存在key
            boolean hasKey = redisTemplateService.hasKey(userId, Constants.PageFlagEnum.MY_APPROVE.getCode());
            //如果redis存在该缓存
            if (hasKey) {
                JSONArray treeData = redisTemplateService.getTreeData(userId, Constants.PageFlagEnum.MY_APPROVE.getCode());
                List<TreeDto> treeDtoList = JSONArray.parseArray(treeData.toString(), TreeDto.class);
                //启动获取最新数据线程并更新redis
                Runnable task = () -> {
                    log.info("开始更新发起审批页面redis树结构数据！");
                    getTreeAndSetRedis(userId);
                    log.info("更新发起审批页面redis树结构数据完成！");
                };
                //执行线程
                ExecutorProcessPool.getInstance().execute(task);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtoList));
            } else {
                //redis数据不存在，则需要查询，并更新至redis
                List<TreeDto> exhibitionTree = getTreeAndSetRedis(userId);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, exhibitionTree));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取左侧树信息失败", e);
            return ResultBody.failure("获取左侧树信息失败");
        }
    }


    private List<TreeDto> getTreeAndSetRedis(String userId) {
        List<TreeDto> treeDtos = flowTreeService.exhibitionTree(userId);
        //排序
        try {
            treeServiceImpl.sortTreeDtoByKeyWords(treeDtos, Constants.TREE_THREE);
        } catch (Exception e) {
            log.error(Constants.PageFlagEnum.MY_APPROVE + " sort is error", e);
        }
        //存入redis
        redisTemplateService.setTreeData(userId, Constants.PageFlagEnum.MY_APPROVE.getCode(), JSON.toJSONString(treeDtos));
        return treeDtos;
    }

    /**
     * 根据树节点查询发次/批次
     *
     * @return
     */
    @RequestMapping(value = "/getBatchNo", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "根据型号/图号查询批次", notes = "根据型号/图号查询批次")
    public ResultBody getBatchNo(
            @ApiParam(name = "page", value = "页数", required = true, type = "string") @RequestParam String page,
            @ApiParam(name = "rows", value = "行数", required = true, type = "string") @RequestParam String rows,
            @ApiParam(name = "batchName", value = "批次号", required = false, type = "string") @RequestParam(value = "batchName", required = false, defaultValue = "") String batchName,
            @ApiParam(name = "attributes", value = "节点属性", required = true, type = "string") @RequestParam(value = "attributes", required = false, defaultValue = "") String attributes) {
        try {
            if (StringUtils.isEmpty(attributes)) {
                return ResultBody.failure("根据型号/图号查询批次失败,请选择正确的树节点");
            }
            List<ApproveBatch> result;
            JSONObject jsonObject = JSONObject.parseObject(attributes);
            //单机或者分系统或者模块
            String firstNode = jsonObject.getString("firstNode");
            //如果是分系统和模块，thirdlyNode为型号，单机thirdlyNode为图号
            String drawingNo = jsonObject.getString("thirdlyNode");
            //如果是分系统和模块，secondNode为分系统类型，单机secondNode为单机类型
            String classification = jsonObject.getString("secondNode");
            //单机
            if (firstNode.equals(Constants.SINGLE_NAME)) {
                result = flowTreeService.getBatchNoByDrawingNo(classification, drawingNo);
            } else {
                result = flowTreeService.getBatchNoByModeNo(classification, drawingNo, firstNode);
            }
            if (CollectionUtils.isEmpty(result)){
                return ResultBody.success(new GridView<>(result,
                        0));
            }
            if (StringUtils.isNotEmpty(batchName) && CollectionUtils.isNotEmpty(result)) {
                result = result.stream().filter(item -> {
                    if (item.getBatchNo().equals(Constants.OWN_ISSUE) && item.getIssueNo().contains(batchName)) {
                        return true;
                    }
                    return item.getBatchNo().contains(batchName);
                }).collect(Collectors.toList());
            }
            //排序
            result = result.stream().sorted(customComparator).collect(Collectors.toList());
            int total = result.size();
            //分页条件
            int pageNo = Integer.parseInt(page);
            int pageSize = Integer.parseInt(rows);
            result = result.stream().skip((pageNo - 1) * pageSize).limit(pageSize).
                    collect(Collectors.toList());
            return ResultBody.success(new GridView<>(result,
                    total));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("根据型号/图号查询批次失败", e);
            return ResultBody.failure("根据型号/图号查询批次失败");
        }
    }


    /**
     * 对返回数据的排序
     */
    private Comparator<ApproveBatch> customComparator = new Comparator<ApproveBatch>() {
        @Override
        public int compare(ApproveBatch o1, ApproveBatch o2) {
            String s1 = o1.getIssueNo();
            String s2 = o2.getIssueNo();

            //判断s1和s2是否为纯数字或者数字开头
            boolean isNumeric1 = s1.matches("\\d+");
            boolean isNumeric2 = s2.matches("\\d+");
            if (isNumeric1 && !isNumeric2) {
                return -1; // 数字排在前面
            } else if (!isNumeric1 && isNumeric2) {
                return 1; // 字母排在数字后面
            } else if (isNumeric1 && isNumeric2) {
                //如果都是数字，则按照从小到大排序
                BigInteger num1 = new BigInteger(s1);
                BigInteger num2 = new BigInteger(s2);
                return num1.compareTo(num2);
            } else {
                //其他情况按照字符串比较
                Collator collator = Collator.getInstance(Locale.CHINA);
                return collator.compare(s1,s2);
            }
        }
    };

    @ApiOperation(value = "流程发起-bpm-审批发起校验", notes = "校验是否存在编辑中数据，成功返回success=true")
    @RequestMapping(value = "/bpm/startprocessValidate", method = RequestMethod.POST)
    public ResultBody startProcessValidate(@Valid @RequestBody StartFlowBatchRequest startFlowBatchRequest) {
        try {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.valid"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.valid.message"), JSON.toJSONString(startFlowBatchRequest)),

                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
            //选中的模板id
            List<ModuleManageDto> templateList = moduleManageService.listInTempleteList(startFlowBatchRequest.getTemplate());
            //先获取数据，根据发次/批次信息获取数据并封装至对象中，后续会用
            startFlowBatchRequest = flowTreeService.formatApproveBatch(startFlowBatchRequest);
            //校验是否存在编辑中的数据
            Map<String, Object> result = flowTreeService.validateData(startFlowBatchRequest.getBatchNoList(), templateList);
            return ResultBody.success(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("审批发起校验失败!", ex);
            return ResultBody.failure(JSON.toJSONString(Collections.singletonList("审批发起校验失败!")));
        }
    }

    /**
     * @param startFlowBatchRequest 表单数据
     * @param id                    流程实例id
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "流程发起-bpm-发起审批", notes = "根据审批方式按照表单审批及实体BOM审批两种方式，及对应节点的审核人来发起审批流程，成功返回success=true")
    @RequestMapping(value = "/bpm/startprocess", method = RequestMethod.POST)
    public synchronized ResultBody startProcess(@Valid @RequestBody StartFlowBatchRequest startFlowBatchRequest, @RequestParam(required = false) String id) {
        try {
            //选中的模板id
            List<ModuleManageDto> templateList = moduleManageService.listInTempleteList(startFlowBatchRequest.getTemplate());
            //选中的批次号
            List<ApproveBatch> batchNoList = startFlowBatchRequest.getBatchNoList();
            //校验是否存在正在审批中的数据
            //查询到未办理的流程
            List<FlowCreateEntity> notOverProcess = flowTreeService.findNotOverProcess();
            //校验是否有未办理的流程
            List<String> messages = flowTreeService.validateTrackProcess(batchNoList, templateList, notOverProcess);
            if (CollectionUtils.isNotEmpty(messages)) {
                return ResultBody.failure(JSON.toJSONString(messages));
            }
            //先获取数据，根据发次/批次信息获取数据并封装至对象中，后续会用
            startFlowBatchRequest = flowTreeService.formatApproveBatch(startFlowBatchRequest);
            //校验是否存在编辑中的数据
//            List<String> editMessage = flowTreeService.validateData(startFlowBatchRequest.getBatchNoList(), templateList);
//            if (CollectionUtils.isNotEmpty(editMessage)) {
//                return ResultBody.failure(JSON.toJSONString(editMessage));
//            }
            //获取流程id
            long businessId;
            if (StrUtil.isNotBlank(id)) {
                businessId = Long.parseLong(id);
            } else {
                businessId = Long.parseLong(DateUtils.getCurrentDateTime());
            }
            //初始化流程创建表
            if (StrUtil.isBlank(id)) {
                flowTreeService.createProcess(startFlowBatchRequest, businessId + "");
                // 同时在初始化表中保存一份数据
                // 保存表单数据
                flowTreeService.saveFormData(startFlowBatchRequest, businessId + "");
            } else {
                // 更新状态
                flowTreeService.updateProcess(startFlowBatchRequest, id);
                // 更新下数据
                // 保存表单数据
                flowTreeService.saveFormData(startFlowBatchRequest, id);
            }
            //初始化流程审核表
            flowApproveInter.save(startFlowBatchRequest.getAppriovaNode(), businessId);
            //发起流程
            if (!flowTreeService.startProcess(businessId, PROCDEFKEY)) {
                return ResultBody.failure(JSON.toJSONString(Collections.singletonList("流程发起审批失败!")));
            }
            // 保存数据与模型发次关系
            flowTreeService.saveRelationDataAndProcess(startFlowBatchRequest, businessId);
            //保存模型和实物关系
            flowTreeService.save(startFlowBatchRequest, businessId);


            flowTreeService.changeDataStatus(businessId, startFlowBatchRequest.getTemplate());

            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.start"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.start.message"), JSON.toJSONString(startFlowBatchRequest)),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
            return ResultBody.success();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("流程发起审批失败!", e);
            return ResultBody.failure(JSON.toJSONString(Collections.singletonList("流程发起审批失败!")));
        }
    }


    @ApiOperation(value = "保存", notes = "保存草稿数据成功返回success=true")
    @RequestMapping(value = "/bpm/save", method = RequestMethod.POST)
    public ResultBody saveProcess(@Valid @RequestBody StartFlowBatchRequest startFlowBatchRequest, @RequestParam(required = false) String id) throws Exception {
        System.out.println("开始" + System.currentTimeMillis());
        try {
            // 第一次保存
            if (StrUtil.isBlank(id)) {
                /**
                 * 初始化流程创建表
                 */
                long bussinessId = Long.parseLong(DateUtils.getCurrentDateTime());
                /**
                 * 这个方法中涉及发起流程的初始状态 没有优化的点
                 */
                flowTreeService.saveDraftData(startFlowBatchRequest, sessionUtils.getEmp().getId(), bussinessId);
                // 保存表单数据
                InitFormEntity initData = new InitFormEntity();
                initData.setId(bussinessId + "");
                initData.setFormString(JSONObject.toJSONString(startFlowBatchRequest));
                iInitFormDao.save(initData);
            } else {
                // 已有数据
                flowTreeService.updateDraftData(startFlowBatchRequest, Long.parseLong(id));
                // 更新数据
                Optional<InitFormEntity> byId = iInitFormDao.findById(id);
                if (byId.isPresent()) {
                    InitFormEntity initFormEntity = byId.get();
                    initFormEntity.setFormString(JSONObject.toJSONString(startFlowBatchRequest));
                    iInitFormDao.save(initFormEntity);
                }
            }

            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.config.bmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.fmodule"),
                    LogPattenUtils.getProperty("model.manage.flow.save"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.flow.save.message"), JSON.toJSONString(startFlowBatchRequest)),
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

            //根据bussinessId查询模板和型号、批次号的关联关系
            List<FlowBomSheetEntity> bomSheetList = flowBomSheetInter.getBomSheetList(bussinessId, bom);
            //根据bussinessId查询关联关系
            List<FlowBomSheetDataEntity> bomSheetDataList = flowBomSheetInter.getBomSheetDataList(bussinessId, bom);
            List<FlowBomSheetEntityResponse> responseList = new ArrayList<>();

            //查询所有的权限
            PackageSystemEntity auth = new PackageSystemEntity();
            auth.setType("person");
            auth.setSystemId(sessionUtils.getEmp().getId());
            List<PackageSystemEntity> authorityDataByEntity = packageSystemInter.findAuthorityDataByEntity(auth);
            List<ModuleManageDto> allTempLete = templeteUtil.getALLNewTempLete("", "", "");
            //遍历批次号数据
            for(FlowBomSheetEntity source : bomSheetList){
                FlowBomSheetEntityResponse target = FlowBomSheetEntityResponse.builder().build();
                BeanUtils.copyProperties(source, target);
                if (allTempLete.size() > 0) {
                    List<ModuleManageDto> collect = allTempLete.stream().filter(templete -> templete.getModelInfo().equals(source.getTemplate())).collect(Collectors.toList());
                    target.setTableName(collect.get(0).getTableName());
                    target.setModuleCode(collect.get(0).getCode());
                    target.setModelId(collect.get(0).getModelInfo());
                    target.setTemplateName(collect.get(0).getName());
                    target.setDrawingNo(source.getDrawingNo());
                    if (StringUtils.isNotEmpty(source.getBatchNo()) && "发次本级".equals(source.getBatchNo())) {
                        target.setBatchNoOrIssueNo(source.getIssueNo());
                    } else {
                        target.setBatchNoOrIssueNo(source.getBatchNo());
                    }
                    target.setDrawingNoOrModel(StringUtils.isNotEmpty(source.getDrawingNo()) ? source.getDrawingNo() : source.getModel());
                    target.setBomName(StringUtils.isNotEmpty(source.getBatchNo()) ? source.getBatchNo() : source.getIssueNo());
                    //分系统使用model型号来区分数据权限，单机使用图号来区分数据权限
                    long count = authorityDataByEntity.stream().filter(model -> model.getResourceId().equals(StringUtils.isNotEmpty(source.getModel()) ? source.getModel() : source.getDrawingNo())).count();
                    target.setHasAuthority(count > 0 ? true : false);
                }
                List<FlowBomSheetDataEntity> temp = bomSheetDataList.stream().filter(r->{
                    if(StringUtils.isNotEmpty(target.getBatchNo()) && !target.getBatchNo().equals(r.getBatchNo())){
                        return  false;
                    }
                    if(StringUtils.isNotEmpty(target.getDrawingNo()) && !target.getDrawingNo().equals(r.getDrawingNo())){
                        return  false;
                    }
                    if(StringUtils.isNotEmpty(target.getIssueNo()) && !target.getIssueNo().equals(r.getIssueNo())){
                        return  false;
                    }
                    if(StringUtils.isNotEmpty(target.getModel()) && !target.getModel().equals(r.getModel())){
                        return  false;
                    }
                    if(!target.getTemplate().equals(r.getTemplate())){
                        return  false;
                    }
                    return true;
                }).collect(Collectors.toList());
                if(CollectionUtil.isNotEmpty(temp)){
                    responseList.add(target);
                }
            }
            responseList = responseList.stream().sorted(Comparator.comparing(FlowBomSheetEntityResponse::getDrawingNoOrModel)
                    .thenComparing(FlowBomSheetEntityResponse::getBatchNoOrIssueNo)
                    .thenComparing(FlowBomSheetEntityResponse::getTemplateName))
                    .collect(Collectors.toList());
            return ResultBody.success(responseList);
        } catch (Exception ex) {
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("查询列表失败!")));
        }
    }


    /**
     * 数据包审核-根据id查询
     */
    @RequestMapping(value = "/dataPageByIds", method = RequestMethod.POST)
    @ResponseBody
    @OperateLogger(operation = "查询数据")
    public ResultBody<GridView> dataListManageByIds(@ApiParam(name = "实物编码", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String physicalCode,
                                                    @ApiParam(name = "模型名称", value = "模型名称", required = true, type = "string") @RequestParam(required = false) String modelId,
                                                    @ApiParam(name = "bussinessId", value = "流程实例编号", required = true, type = "string") @RequestParam(required = false) String bussinessId,
                                                    @ApiParam(name = "批次号", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String batchNo,
                                                    @ApiParam(name = "发次号", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String issueNo,
                                                    @ApiParam(name = "图号", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String drawingNo,
                                                    @ApiParam(name = "型号", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String model,
                                                    @RequestBody ModelDataQueryParamVO param) {

//        List<String> sysIds = iFlowBomSheetDataInter.findListByBomAndTempleteAndbusinessId(physicalCode, modelId, Long.parseLong(bussinessId));
        List<String> sysIds = iFlowBomSheetDataInter.findListBybatchNoAndIssueNoAndDrawingNoAndModel(modelId, Long.parseLong(bussinessId), batchNo, issueNo, drawingNo, model);
        //如果关系表中没有查到模板数据id，直接返回空数据，避免不拼id in（）导致全查
        if(CollectionUtil.isEmpty(sysIds)){
            List nullList = new ArrayList();
            GridView<Map<String, Object>> nullResult = new GridView<>(new PageUtil<Map<String, Object>>(nullList, 10, 1).getList(), nullList.size(), 10, 1);
            return ResultBody.success(nullResult);
        }
        final String regex = "\\d+";
        if (modelId == null) {
            throw new ParamNotBlankException("模型id");
        }
        String page = param.getPage().toString();
        String rows = param.getRows().toString();
        int pageInt = Integer.parseInt(page);
        int rowsInt = Integer.parseInt(rows);

        if (StringUtils.isBlank(page) && !page.matches(regex)) {
            String message = "分页参数page 传值有误，page:" + page;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        if (StringUtils.isBlank(rows) && !rows.matches(regex)) {
            String message = "分页参数rows 传值有误，rows:" + rows;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        param.setPage(Integer.parseInt("1"));
        param.setRows(Integer.parseInt("100000"));
//        List<Map<String, Object>> data = iMaintainService.dataListManageByIds(physicalCode, modelId, param, sysIds).getData().getRows();
        List<Map<String, Object>> data = iMaintainService.dataListManageByIds(modelId, param, sysIds).getData().getRows();
        int number = 0;
        List<Map<String, Object>> result = new ArrayList<>();

        String grade = StringUtils.isEmpty(sessionUtils.getEmp().getGrade()) ? "0" : sessionUtils.getEmp().getGrade();
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> map = data.get(i);
            String m_sys_secretlev = map.get("S_M_SYS_SECRETLEVEL") == null ? "0" : map.get("S_M_SYS_SECRETLEVEL").toString();
            //密集要求
            //密集是按照数字在排序：人员密集要大于资源密集
            if (meritdataCloudProperties.getUsePlatformSecret() && (Integer.parseInt(grade)) < Integer.parseInt(m_sys_secretlev)) {
                number++;
            } else {
                result.add(map);
            }
        }
//        不知道是干啥用的，不注掉影响根据序号排序
//        result.sort(Comparator.comparing(o -> Integer.parseInt(o.get("S_M_SYS_VERSIONSTATUS").toString())));
//        Collections.reverse(result);

        GridView<Map<String, Object>> mapGridView = new GridView<>(new PageUtil<Map<String, Object>>(result, rowsInt, pageInt).getList(), result.size(), rowsInt, pageInt);
        mapGridView.setRecords(number);
        return ResultBody.success(mapGridView);
    }

    /**
     * @param startFlowBatchRequest 审批人员信息
     * @param id                    流程实例id
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "流程退回后修改审批人", notes = "流程退回后修改审批人")
    @RequestMapping(value = "/bpm/update/user", method = RequestMethod.POST)
    @Transactional
    public ResultBody modifyApproveUsers(@Valid @RequestBody StartFlowBatchRequest startFlowBatchRequest, @RequestParam String id) {
        try {
            /**
             * 删除旧的人员
             */
            flowApproveInter.delete(Long.parseLong(id));
            // 插入新的人员
            flowApproveInter.save(startFlowBatchRequest.getAppriovaNode(), Long.parseLong(id));
            // 更新详情表
            Optional<InitFormEntity> byId = iInitFormDao.findById(id);
            if (byId.isPresent()) {
                InitFormEntity initFormEntity = byId.get();
                initFormEntity.setFormString(JSONObject.toJSONString(startFlowBatchRequest));
                iInitFormDao.save(initFormEntity);
            }

            return ResultBody.success();
        } catch (Exception ex) {
            return ResultBody.failure(JSON.toJSONString(Arrays.asList("修改流程审批人员失败!")));
        }
    }


}
