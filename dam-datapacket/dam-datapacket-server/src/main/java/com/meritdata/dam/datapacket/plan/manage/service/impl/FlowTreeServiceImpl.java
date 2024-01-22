package com.meritdata.dam.datapacket.plan.manage.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.properties.MeritdataCloudProperties;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.QueryNodeDTO;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowBomSheetDao;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowBomSheetDataDao;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowCreateDao;
import com.meritdata.dam.datapacket.plan.manage.dao.IInitFormDao;
import com.meritdata.dam.datapacket.plan.manage.entity.*;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowBatchRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.ApproveBatch;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowApproveInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowCreateInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowTreeService;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleTree;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.service.IDataPacketCommonService;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.impl.PackageSystemImpl;
import com.meritdata.dam.datapacket.plan.utils.CommUtil;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStateEnum;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStructureEnum;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @Author fanpeng
 * @Date 2023/7/5
 * @Describe 我的审批树结构
 */

@Service
public class FlowTreeServiceImpl implements IFlowTreeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowTreeServiceImpl.class);

    @Autowired
    private IDatamationsClient iDatamationsClient;

    @Autowired
    private IModulePlanService iModulePlanService;

    @Autowired
    private PackageSystemImpl packageSystem;

    @Autowired
    private MeritdataCloudProperties meritdataCloudProperties;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private IFlowCreateInter flowCreateInter;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private IFlowCreateDao flowCreateDao;

    @Autowired
    private IFlowApproveInter flowApproveInter;

    @Autowired
    private BpmService bpmService;

    @Autowired
    private IInitFormDao iInitFormDao;

    @Autowired
    private IFlowBomSheetDao flowBomSheetDao;

    @Autowired
    private IFlowBomSheetDataDao flowBomSheetDataDao;

    @Autowired
    private IDataPacketCommonService dataPacketCommonService;

    @Autowired
    private IStandAloneService standAloneService;

    @Override
    public List<TreeDto> exhibitionTree(String userId) {
        try {
            List<TreeDto> treeDtoList = iModulePlanService.tree("-1", userId);
            //查询单机与总装直属件的信息，挂在单机模块下，构建单机树结构
            //挂载单机
            List<Map<String, Object>> maps = iDatamationsClient.exhibitionTree(new HashMap<>(), "PHYSICAL_OBJECT_SINGLE_MACHINE");
            //获取权限表信息
            PackageSystemEntity packageSystemEntity = new PackageSystemEntity();
            packageSystemEntity.setSystemId(userId);
            packageSystemEntity.setType("person");
            List<PackageSystemEntity> byEntity = packageSystem.findAuthorityDataByEntity(packageSystemEntity);
            List<String> textList = byEntity.stream().map(PackageSystemEntity::getResourceId).collect(Collectors.toList());
            LOGGER.info("当前用户权限信息:" + textList.toString());
            //获取单机的数据
            List<TreeDto> DJListTree = treeDtoList.stream().filter(item -> item.getText().equals(FlowStructureEnum.STANDALONE.getValue())).collect(Collectors.toList());
            DJListTree.forEach(item -> {
                item.getChildren().forEach(child -> {
                    List<TreeDto> treeDtoListTH = new ArrayList<>();
                    for (Map<String, Object> stringObjectMap : maps) {
                        if (stringObjectMap.get("F_CLASSIFICATION") != null && child.getText().equals(stringObjectMap.get("F_CLASSIFICATION")) && stringObjectMap.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED") != null && stringObjectMap.get("F_STAND_ALONE_IDENTIFICATION") != null && "1".equals(stringObjectMap.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED")) && "1".equals(stringObjectMap.get("F_STAND_ALONE_IDENTIFICATION"))) {
                            QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                            queryNodeDTO.setFirstNode(item.getText());
                            queryNodeDTO.setSecondNode(stringObjectMap.get("F_CLASSIFICATION").toString());
                            queryNodeDTO.setThirdlyNode(stringObjectMap.get("F_DRAWING_NO").toString());
                            queryNodeDTO.setNodeLevel("3");
                            queryNodeDTO.setNodeType("3");
                            if (stringObjectMap.get("F_DRAWING_NO") != null
                                    && textList.contains(stringObjectMap.get("F_DRAWING_NO").toString())) {
                                TreeDto treeDto = new TreeDto();
                                //单机第三级，前端node需要根据单机、发动机、图号信息展示图号下面的所有所有批次信息
                                treeDto.setId(stringObjectMap.get("F_M_SYS_ID").toString());
                                treeDto.setPid(child.getId());
                                treeDto.setText(stringObjectMap.get("F_DRAWING_NO").toString());
                                treeDto.setAttributes(queryNodeDTO);
                                treeDtoListTH.add(treeDto);
                            }
                        }
                    }
                    ArrayList<TreeDto> treeDtoListTHTemp = treeDtoListTH.stream().collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(
                                    Comparator.comparing(ModuleTree::getText)
                            )), ArrayList::new
                    ));
                    child.setChildren(treeDtoListTHTemp);
                });
                standAloneService.formatAloneTree(item.getChildren());
            });
            return treeDtoList;
        } catch (Exception e) {
            LOGGER.error("树构建错误日志：", e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<ApproveBatch> getBatchNoByModeNo(String classification, String modeNo, String type) {
        List<ApproveBatch> result = new ArrayList<>();
        ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
        JSONObject filterMap = new JSONObject();
        filterMap.put("F_CLASSIFICATION", classification);
        filterMap.put("F_MODEL", modeNo);
        paramVO.setQueryFilter(filterMap.toJSONString());
        List<Map<String, Object>> fxtInfo = iDatamationsClient.getFXTInfo(paramVO);
        if (CollectionUtils.isEmpty(fxtInfo)) {
            return result;
        }
        //获取发次号集合(不重复的)
        List<String> issueNoList = fxtInfo.stream().map(item -> item.get("F_ISSUE_NO").toString()).distinct().collect(Collectors.toList());
        //增加发次本级
        for (String item : issueNoList) {
            ApproveBatch ownIssueNo = new ApproveBatch();
            ownIssueNo.setType(type);
            ownIssueNo.setBatchNo(Constants.OWN_ISSUE);
            ownIssueNo.setIssueNo(item);
            ownIssueNo.setDrawingNo("");
            ownIssueNo.setModel(modeNo);
            result.add(ownIssueNo);
        }
        //查询配套清单表，查询发次
        ModelDataQueryParamVO fcParam = new ModelDataQueryParamVO();
        JSONObject fcMap = new JSONObject();
        //根据数据id查询
        JSONObject issueNoJson = new JSONObject();
        issueNoJson.put("$in", issueNoList.toArray());
        fcMap.put("F_ISSUE_NO", issueNoJson);
        fcMap.put("F_MODEL", modeNo);
        fcParam.setQueryFilter(fcMap.toJSONString());
        List<Map<String, Object>> qdInfo = iDatamationsClient.getQDInfo(fcParam);
        if (CollectionUtils.isNotEmpty(qdInfo)) {
            //获取批次号集合(不重复的,根据图号和批次号加起来去重)
            List<Map<String, Object>> batchNoList = qdInfo.stream().collect(
                    Collectors.collectingAndThen(
                            Collectors.toCollection(
                                    () -> new TreeSet<>(
                                            Comparator.comparing(
                                                    item -> item.get("F_DRAWING_NO") + ";" + item.get("F_BATCH_NO")
                                            )
                                    )
                            ), ArrayList::new
                    )
            );
            result.addAll(batchNoList.stream().map(item -> {
                String batchNo = item.get("F_BATCH_NO").toString();
                String drawingNo = item.get("F_DRAWING_NO").toString();
                ApproveBatch jsonObject = new ApproveBatch();
                jsonObject.setBatchNo(batchNo);
                jsonObject.setDrawingNo(drawingNo);
                jsonObject.setIssueNo(item.get("F_ISSUE_NO").toString());
                jsonObject.setModel(modeNo);
                // TODO: 2023/7/7 为了区分是总装直属件还是单机，需要再查询数据库
                List<Map<String, Object>> djInfoSearch = getDJInfo(drawingNo, batchNo);
                if (CollectionUtils.isNotEmpty(djInfoSearch)) {
                    //判断是单机还是总装直属件
                    String classType = djInfoSearch.get(0).get("F_STAND_ALONE_IDENTIFICATION").toString().equals("1") ? FlowStructureEnum.STANDALONE.getValue() : FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue();
                    jsonObject.setType(classType);
                } else {
                    LOGGER.error("未根据批次号、图号获取到单机信息:图号【" + drawingNo + "】批次号【" + batchNo + "】");
                }
                return jsonObject;
            }).collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public List<ApproveBatch> getBatchNoByDrawingNo(String classification, String drawingNo) {
        ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
        JSONObject filterMap = new JSONObject();
        filterMap.put("F_CLASSIFICATION", classification);
        filterMap.put("F_DRAWING_NO", drawingNo);
        paramVO.setQueryFilter(filterMap.toJSONString());
        List<Map<String, Object>> djInfoSearch = iDatamationsClient.getDJInfoSearch(paramVO);
        //获取批次号集合(不重复的)
        Set<String> batchNoList = djInfoSearch.stream().map(item -> item.get("F_BATCH_NO").toString()).collect(Collectors.toSet());
        //封装返回数据
        return batchNoList.stream().map(item -> {
            ApproveBatch jsonObject = new ApproveBatch();
            jsonObject.setType(FlowStructureEnum.STANDALONE.getValue());
            jsonObject.setBatchNo(item);
            jsonObject.setDrawingNo(drawingNo);
            jsonObject.setIssueNo("");
            jsonObject.setModel("");
            return jsonObject;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDJInfo(String drawingNo, String batchNo) {
        //查询条件封装
        JSONObject filterMap = new JSONObject();
        //如果实物号不为null，则根据实物号，批次号和图号，则一定获取的是一条信息
        filterMap.put("F_DRAWING_NO", drawingNo);
        filterMap.put("F_BATCH_NO", batchNo);
        ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
        paramVO.setQueryFilter(filterMap.toJSONString());
        return iDatamationsClient.getDJInfoSearch(paramVO);
    }


    @Override
    public List<ApproveBatch> getPhysicalNo(List<JSONObject> jsonObjects) {
        List<ApproveBatch> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(jsonObjects)) {
            LOGGER.error("未选择批次/发次");
            return result;
        }
        //循环获取
        for (JSONObject jsonObject : jsonObjects) {
            ApproveBatch approveBatch = new ApproveBatch();
            //单机/总装直属件或者分系统/模块
//            String type = jsonObject.getString("type");
            //发次
            String issueNo = jsonObject.getString("issueNo");
            //图号
            String drawingNo = jsonObject.getString("drawingNo");
            approveBatch.setDrawingNo(drawingNo);
            //批次号
            String batchNo = jsonObject.getString("batchNo");
            approveBatch.setBatchNo(batchNo);
            //发次号不为空，则一定为分系统或者模块
            if (StringUtils.isNotEmpty(issueNo)) {
                //如果是发次本级，则必定有发次号，实物号为发次号
                if (batchNo.equals(Constants.OWN_ISSUE)) {
                    List<String> physicalNoList = new ArrayList<>();
                    physicalNoList.add(issueNo);
                    approveBatch.setPhysicals(physicalNoList);
                } else {
                    //单机或者总装直属件--查询配套清单表
                    //查询配套清单表，查询发次 根据发次，批次，图号查询
                    ModelDataQueryParamVO fcParam = new ModelDataQueryParamVO();
                    JSONObject fcMap = new JSONObject();
                    fcMap.put("F_ISSUE_NO", issueNo);
                    fcMap.put("F_DRAWING_NO", drawingNo);
                    fcMap.put("F_BATCH_NO", batchNo);
                    fcParam.setQueryFilter(fcMap.toJSONString());
                    List<Map<String, Object>> qdInfo = iDatamationsClient.getQDInfo(fcParam);
                    //实物号为null则为未管理到实物的总装直属件。实物号为批次号
                    List<String> physicals = qdInfo.stream().map(item -> item.get("F_PHYSICAL_NO") == null ? batchNo : item.get("F_PHYSICAL_NO").toString()).collect(Collectors.toList());
                    approveBatch.setPhysicals(physicals);
                }
            } else {
                //单机则根据批次号和图号查询实物号，单机一定有实物号
                List<Map<String, Object>> djInfo = getDJInfo(drawingNo, batchNo);
                List<String> physicals = djInfo.stream().map(item -> item.get("F_PHYSICAL_NO").toString()).collect(Collectors.toList());
                approveBatch.setPhysicals(physicals);
            }
            result.add(approveBatch);
        }
        return result;
    }

    @Override
    public List<String> validateAuthority(Map<String, List<Map<String, Object>>> bomTemMap, String type, List<FlowBomSheetEntity> flowBomSheetEntityList, List<ModuleManageDto> templateList) {
        List<String> message = new ArrayList<>();
        flowBomSheetEntityList.forEach(bomSheet -> {
            List<Map<String, Object>> edit = bomTemMap.get(bomSheet.getTemplate() + "_" + bomSheet.getBomName());
            if ("data".equals(type)) {//如果没有查询到编辑中的数据
                if (CollectionUtils.isEmpty(edit)) {
                    List<ModuleManageDto> collect = templateList.stream().filter(model -> model.getModelInfo().equals(bomSheet.getTemplate())).collect(Collectors.toList());
                    if (CollectionUtil.isNotEmpty(collect)) {
                        message.add(String.format("【%s】【%s】无需进行审核！", bomSheet.getBomName(), collect.get(0).getName()));
                    }
                }
            } else {//计算密集
                int number = 0;
                String grade = StringUtils.isEmpty(sessionUtils.getEmp().getGrade()) ? "0" : sessionUtils.getEmp().getGrade();
                if (CollectionUtil.isNotEmpty(edit)) {
                    for (Map<String, Object> map : edit) {
                        String m_sys_secretlev = map.get("S_M_SYS_SECRETLEVEL") == null ? "0" : map.get("S_M_SYS_SECRETLEVEL").toString();
                        //    密集要求
                        //密集是按照数字在排序：人员密集要大于资源密集
                        if (meritdataCloudProperties.getUsePlatformSecret() && (Integer.parseInt(grade)) < (Integer.parseInt(m_sys_secretlev))) {
                            number++;
                        }
                    }
                    if (number > 0) {
                        List<ModuleManageDto> collect = templateList.stream().filter(model -> model.getModelInfo().equals(bomSheet.getTemplate())).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(collect)) {
                            message.add(String.format("【%s】【%s】存在无权限查看的数据【%s】条！", bomSheet.getBomName(), collect.get(0).getName(), number));
                        }
                    }
                }
            }
        });
        return message;
    }

    @Override
    public List<String> validateApprove() {
        return null;
    }

    /**
     * 创建流程
     *
     * @param startFlowBatchRequest 流程创建请求对象
     * @param businessId            流程实例id
     */
    @Override
    public void createProcess(StartFlowBatchRequest startFlowBatchRequest, String businessId) {
        FlowCreateEntity createEntity = new FlowCreateEntity();
        createEntity.setBussinessId(businessId);
        createEntity.setUserId(sessionUtils.getEmp().getId());
        createEntity.setApprovalType(startFlowBatchRequest.getApprovalType());
        createEntity.setFlowTime(CommUtil.getTimestamp());
        //改为 型号/图号+发次/批次展示
        List<ApproveBatch> batchNoList = startFlowBatchRequest.getBatchNoList();
        List<String> batchStr = getBatchStr(batchNoList);
        createEntity.setBomName(String.join(",", batchStr));
        createEntity.setTemplateName(String.join(",", startFlowBatchRequest.getTemplate()));
        flowCreateInter.save(createEntity);
    }

    /**
     * 将批次属性封装为（型号/发次或者图号/批次）
     *
     * @param batchNoList
     * @return
     */
    private List<String> getBatchStr(List<ApproveBatch> batchNoList) {
        return batchNoList.stream().map(approveBatch -> {
            String result;
            //批次号
            String batchNo = approveBatch.getBatchNo();
            //发次号
            String issueNo = approveBatch.getIssueNo();
            //图号
            String drawingNo = approveBatch.getDrawingNo();
            //型号
            String model = approveBatch.getModel();
            //类型 --单机--分系统 -- 模块- 总装直属件
            String type = approveBatch.getType();
            if (batchNo.equals(Constants.OWN_ISSUE)) {
                result = model + "/" + issueNo;
            } else {
                result = drawingNo + "/" + batchNo;
            }
            return result;
        }).collect(Collectors.toList());
    }


    @Override
    public void updateProcess(StartFlowBatchRequest startFlowBatchRequest, String businessId) {
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        FlowCreateEntity createEntity = jpaQueryFactory.selectFrom(qFlowCreateEntity).from(qFlowCreateEntity)
                .where(qFlowCreateEntity.bussinessId.eq(businessId + "")).fetchFirst();
//        createEntity.setBussinessId(bussinessId);
        createEntity.setUserId(sessionUtils.getEmp().getId());
        createEntity.setApprovalType(startFlowBatchRequest.getApprovalType());
        createEntity.setFlowTime(CommUtil.getTimestamp());
        //默认是流程发起状态
        createEntity.setFlowState(FlowStateEnum.COUNTERSIGNER.getCode());
        FlowCreateEntity saveEntity = flowCreateDao.saveAndFlush(createEntity);
    }

    @Override
    public boolean startProcess(long businessId, String procDefKey) {
        ResultBody<MainFormDTO> mainFormDTOResultBody;
        try {
            mainFormDTOResultBody = bpmService.startProcess(procDefKey, businessId);
            if (!mainFormDTOResultBody.isSuccess()) {
                LOGGER.error(String.format("%s=%s%s", procDefKey, businessId, "发起失败"));
                LOGGER.error(JSON.toJSONString(mainFormDTOResultBody));
                //流程发起失败，流程创建记录及审批记录要被删除
                flowCreateInter.deleteByBussinessId(businessId);
                flowApproveInter.deleteByBussinessId(businessId);
                return false;
            }
            LOGGER.info(String.format("%s=%s%s", procDefKey, businessId, "发起成功"));
            return true;
        } catch (Exception ex) {
            LOGGER.error("流程启动失败" + ex.toString());
            LOGGER.error(ex.getMessage());
            //流程发起失败，流程创建记录及审批记录要被删除
            flowCreateInter.deleteByBussinessId(businessId);
            flowApproveInter.deleteByBussinessId(businessId);
            return false;
        }
    }

    @Override
    public void saveFormData(StartFlowBatchRequest startFlowBatchRequest, String id) {
        InitFormEntity initData = new InitFormEntity();
        initData.setId(id);
        initData.setFormString(JSONObject.toJSONString(startFlowBatchRequest));
        iInitFormDao.save(initData);
    }


    @Override
    public List<FlowBomSheetEntity> save(StartFlowBatchRequest startFlowBatchRequest, long businessId) {
        //获取表单id集合
        List<String> templateList = startFlowBatchRequest.getTemplate();
        //选择的批次号集合
        List<ApproveBatch> batchNoList = startFlowBatchRequest.getBatchNoList();
        List<FlowBomSheetEntity> flowBomSheetEntityList = new ArrayList<>();
        batchNoList.forEach(bom -> {
            templateList.forEach(template -> {
                FlowBomSheetEntity build = FlowBomSheetEntity.builder()
                        .id(CommUtil.getUUID())
                        .bussinessId(businessId)
                        .template(template)
                        .batchNo(bom.getBatchNo())
                        .model(bom.getModel())
                        .drawingNo(bom.getDrawingNo())
                        .issueNo(bom.getIssueNo())
                        .build();
                flowBomSheetEntityList.add(build);
            });
        });
        flowBomSheetDao.saveAllAndFlush(flowBomSheetEntityList);
        return flowBomSheetEntityList;
    }

    @Override
    public List<FlowBomSheetDataEntity> saveRelationDataAndProcess(StartFlowBatchRequest startFlowBatchRequest, long businessId) {
        //选中的模板id
        List<String> templateList = startFlowBatchRequest.getTemplate();
        //选中的批次号
        List<ApproveBatch> batchNoList = startFlowBatchRequest.getBatchNoList();
        List<FlowBomSheetDataEntity> flowBomSheetDataEntityList = new ArrayList<>();

        batchNoList.forEach(batchDTO -> {
            //批次号
            String batchNo = batchDTO.getBatchNo();
            //发次号
            String issueNo = batchDTO.getIssueNo();
            //图号
            String drawingNo = batchDTO.getDrawingNo();
            //型号
            String model = batchDTO.getModel();
            templateList.forEach(templateId -> {
                //获取要审批的数据
                List<Map<String, Object>> dataListAll = batchDTO.getApproveData().get(templateId);
                if (!CollectionUtils.isEmpty(dataListAll)) {
                    dataListAll.forEach(data -> {
                        FlowBomSheetDataEntity build = FlowBomSheetDataEntity.builder()
                                .id(CommUtil.getUUID())
                                .bussinessId(businessId)
                                .template(templateId)
                                .dataId(data.get("F_M_SYS_ID").toString())
                                .type(0)
                                .batchNo(batchNo)
                                .model(model)
                                .drawingNo(drawingNo)
                                .issueNo(issueNo)
                                .build();
                        flowBomSheetDataEntityList.add(build);
                    });
                }
            });
        });
        return flowBomSheetDataDao.saveAllAndFlush(flowBomSheetDataEntityList);
    }

    public List<Map<String, Object>> getApproveDataByBatch(ApproveBatch approveBatch, String templateId) {
        //批次号
        String batchNo = approveBatch.getBatchNo();
        //发次号
        String issueNo = approveBatch.getIssueNo();
        //图号
        String drawingNo = approveBatch.getDrawingNo();
        //型号
        String model = approveBatch.getModel();
        //类型 --单机--分系统 -- 模块- 总装直属件
        String type = approveBatch.getType();
        //分系统，发次本级的业务数据
        ModelDataQueryParamVO modelDataQueryParamVO = new ModelDataQueryParamVO();
        JSONObject jsonObject = new JSONObject();
        //最高版本
        jsonObject.put("S_M_SYS_MAXVERSION", "1");
        //编辑中
        jsonObject.put("S_M_SYS_VERSIONSTATUS", "2");
        //拼接or参数
        //col:{$or:[{$eq:val},{$eq:val}]}
        JSONObject orparam = new JSONObject();
        List<JSONObject> orList = new ArrayList<>();
        JSONObject jsonNull = new JSONObject();
        jsonNull.put("$null", "null");
        orList.add(jsonNull);
        JSONObject jsonOr = new JSONObject();
        jsonOr.put("$eq", "");
        orList.add(jsonOr);
        orparam.put("$or", orList);
        jsonObject.put("F_IsApproval", orparam);
        if (batchNo.equals(Constants.OWN_ISSUE)) {
            //根据型号和发次查询数据 产品代号为型号  产品编号为发次号
            jsonObject.put("F_BatchCode", model);
            jsonObject.put("F_PhysicalCode", issueNo);
            modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
        } else {
            //根据型号和发次查询数据 产品代号为图号  产品批次为批次号
            jsonObject.put("F_BatchCode", drawingNo);
            jsonObject.put("F_BatchNo", batchNo);
            modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
        }
        return iDatamationsClient.packetDataListAll("", templateId, modelDataQueryParamVO);
    }

    @Override
    public void saveDraftData(StartFlowBatchRequest startFlowBatchRequest, String userId, long bussinessId) {
        FlowCreateEntity createEntity = new FlowCreateEntity();
        createEntity.setBussinessId(bussinessId + "");
        createEntity.setUserId(userId);
        createEntity.setApprovalType(startFlowBatchRequest.getApprovalType());
//        createEntity.setId(UUID.randomUUID().toString());
        //默认是流程发起状态
        createEntity.setFlowState(FlowStateEnum.DRAFT.getCode());
        //改为 型号/图号+发次/批次展示
        List<ApproveBatch> batchNoList = startFlowBatchRequest.getBatchNoList();
        List<String> batchStr = getBatchStr(batchNoList);
        createEntity.setBomName(String.join(",", batchStr));
//        createEntity.setBomName(JSONObject.toJSONString(startFlowBatchRequest));
        createEntity.setTemplateName(String.join(",", startFlowBatchRequest.getTemplate()));
        FlowCreateEntity saveEntity = flowCreateDao.saveAndFlush(createEntity);
    }

    @Override
    public void updateDraftData(StartFlowBatchRequest startFlowBatchRequest, long bussinessId) {
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        FlowCreateEntity createEntity = jpaQueryFactory.selectFrom(qFlowCreateEntity).where(qFlowCreateEntity.bussinessId.eq(bussinessId + "")).fetchFirst();
        createEntity.setApprovalType(startFlowBatchRequest.getApprovalType());
        //默认是流程发起状态
        createEntity.setFlowState(FlowStateEnum.DRAFT.getCode());
        createEntity.setBomName(JSONObject.toJSONString(startFlowBatchRequest));
        createEntity.setTemplateName(String.join(",", startFlowBatchRequest.getTemplate()));
        //改为 型号/图号+发次/批次展示
        List<ApproveBatch> batchNoList = startFlowBatchRequest.getBatchNoList();
        List<String> batchStr = getBatchStr(batchNoList);
        createEntity.setBomName(String.join(",", batchStr));
        createEntity.setTemplateName(String.join(",", startFlowBatchRequest.getTemplate()));
        FlowCreateEntity saveEntity = flowCreateDao.saveAndFlush(createEntity);
    }

    @Override
    public List<FlowBomSheetEntity> getFlowTemplate(StartFlowBatchRequest startFlowBatchRequest) {
        //选中的模板id
        List<String> templateList = startFlowBatchRequest.getTemplate();
        //选中的批次号
        List<ApproveBatch> batchNoList = startFlowBatchRequest.getBatchNoList();
        //发次集合
        List<String> issueNos = batchNoList.stream().filter(item -> item.getIssueNo() != null).map(ApproveBatch::getIssueNo).collect(Collectors.toList());
        //不是发次本级的批次集合
        List<String> batchNos = batchNoList.stream().filter(item -> item.getBatchNo() != null && !item.getBatchNo().equals(Constants.OWN_ISSUE)).map(ApproveBatch::getBatchNo).collect(Collectors.toList());
        //不为null的图号集合
        List<String> drawingNos = batchNoList.stream().filter(item -> item.getDrawingNo() != null).map(ApproveBatch::getDrawingNo).collect(Collectors.toList());
        //不为null的型号集合
        List<String> models = batchNoList.stream().filter(item -> item.getModel() != null).map(ApproveBatch::getModel).collect(Collectors.toList());
        QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        Predicate predicate = qFlowBomSheetEntity.template.in(templateList);
        //参数拼接(BOM参数)
        if (CollectionUtils.isNotEmpty(issueNos)) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.issueNo.in(issueNos));
        }
        if (CollectionUtils.isNotEmpty(batchNos)) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.batchNo.in(issueNos));
        }
        if (CollectionUtils.isNotEmpty(drawingNos)) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.drawingNo.in(issueNos));
        }
        if (CollectionUtils.isNotEmpty(models)) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.model.in(issueNos));
        }
        return jpaQueryFactory.selectFrom(qFlowBomSheetEntity)
                .where(predicate)
                .fetchResults().getResults();
    }

    @Override
    public List<FlowCreateEntity> findNotOverProcess() {
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        //正常终止，已办结
        List<String> overList = Arrays.asList(FlowStateEnum.REVIEWED.getCode(), FlowStateEnum.STOPOVER.getCode(), FlowStateEnum.DRAFT.getCode());
        Predicate predicate = qFlowCreateEntity.flowState.notIn(overList);
        return jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .fetch();
    }

    @Override
    public List<String> validateTrackProcess(List<ApproveBatch> batchNoList, List<ModuleManageDto> templateList, List<FlowCreateEntity> flowCreateEntityList) {
        //求交集，将templateList转为map。key为modelId
        Map<String, ModuleManageDto> map = new HashMap<>();
        templateList.forEach(item -> map.put(item.getModelInfo(), item));
        List<String> message = new ArrayList<>();
        flowCreateEntityList.forEach(item -> {
            List<String> boms = Arrays.asList(item.getBomName().split(","));
            //使用map求交集效率最高
            List<String> templateIds = Arrays.asList(item.getTemplateName().split(","));
            List<ModuleManageDto> templateNotOver = new ArrayList<>();
            templateIds.forEach(template -> {
                if (map.containsKey(template)) {
                    templateNotOver.add(map.get(template));
                }
            });
            //转为型号/发次或者图号/批次
            List<String> batchStr = getBatchStr(batchNoList);
            //求交集
            List<String> intersectionForBatch = intersectionForList(batchStr, boms);
            //如果都有交集，则校验不通过
            if (CollectionUtils.isNotEmpty(intersectionForBatch) && CollectionUtils.isNotEmpty(templateNotOver)) {
                intersectionForBatch.forEach(batchItem -> {
                    templateNotOver.forEach(templateItem -> {
                        message.add(String.format("批次号[%s]模板名称[%s]存在未完成的流程", batchItem, templateItem.getName()));
                    });
                });
            }
        });
        return message;
    }

    /**
     * 求两个结合的交集
     *
     * @param arry1
     * @param arry2
     * @return
     */
    private List<String> intersectionForList(List<String> arry1, List<String> arry2) {
        List<String> result = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        arry1.forEach(item -> map.put(item, item));
        arry2.forEach(item -> {
            if (map.containsKey(item)) {
                result.add(map.get(item));
            }
        });
        return result;
    }

    @Override
    public StartFlowBatchRequest formatApproveBatch(StartFlowBatchRequest startFlowBatchRequest) {
        //选中的模板id
        List<String> templateList = startFlowBatchRequest.getTemplate();
        //选中的批次号
        List<ApproveBatch> batchNoList = startFlowBatchRequest.getBatchNoList();
        List<FlowBomSheetDataEntity> flowBomSheetDataEntityList = new ArrayList<>();
        List<ApproveBatch> newBatchNoList = new ArrayList<>();
        batchNoList.forEach(batchDTO -> {
            Map<String, List<Map<String, Object>>> approveData = new HashMap<>();
            templateList.forEach(templateId -> {
                //获取要审批的数据
                List<Map<String, Object>> dataListAll = getApproveDataByBatch(batchDTO, templateId);
                if (CollectionUtils.isNotEmpty(dataListAll)) {
                    approveData.put(templateId, dataListAll);
                }
            });
            batchDTO.setApproveData(approveData);
            newBatchNoList.add(batchDTO);
        });
        startFlowBatchRequest.setBatchNoList(newBatchNoList);
        return startFlowBatchRequest;
    }

    @Override
    public Map<String, Object> validateData(List<ApproveBatch> batchNoList, List<ModuleManageDto> templateList) {
        Map<String, Object> result = new HashMap<>();

        List<String> message = new ArrayList<>();
        AtomicBoolean flag = new AtomicBoolean(false);
        List<String> finalMessage = message;
        batchNoList.forEach(batch -> {
            //批次号
            String batchNo = batch.getBatchNo();
            //发次号
            String issueNo = batch.getIssueNo();
            //图号
            String drawingNo = batch.getDrawingNo();
            //型号
            String model = batch.getModel();

            templateList.forEach(template -> {
                Map<String, List<Map<String, Object>>> approveData = batch.getApproveData();
                //如果没有编辑中的数据，则校验不通过
                if (!approveData.containsKey(template.getModelInfo()) || CollectionUtils.isEmpty(approveData.get(template.getModelInfo()))) {
                    //发次本级
                    if (batchNo.equals(Constants.OWN_ISSUE)) {
                        finalMessage.add(String.format("【%s】模板【%s】无需进行审核！", model + "/" + issueNo, template.getName()));
                    } else {
                        finalMessage.add(String.format("【%s】模板【%s】无需进行审核！", drawingNo + "/" + batchNo, template.getName()));
                    }
                } else {
                    flag.set(true);
                }
            });
        });
        if (!flag.get()) {
            //全部无数据
            result.put("code", "0");
            message = Collections.singletonList("您选择的数据都无可审批数据，请重新选择！");
            result.put("message", message);
        } else {
            //部分无数据
            if (CollectionUtils.isNotEmpty(message)) {
                result.put("code", "1");
                result.put("message", message);
            } else {
                //校验通过
                result.put("code", "2");
                result.put("message", "校验通过！");
            }
        }
        return result;
    }

    @Override
    public void changeDataStatus(Long businessId, List<String> modelIds) {
        Map<String, List<String>> collect = getSysIdList(businessId);
        collect.keySet().forEach(item -> {
            DataOperateDTO dataOperateDTO = new DataOperateDTO();
            dataOperateDTO.setModelId(item);
            List<String> sysIds = collect.get(item);
            //where条件
            Map<String, String> primaryData = new HashMap<>();
            JSONObject json = new JSONObject();
            json.put("M_SYS_ID", sysIds);
            primaryData.put("$in", json.toJSONString());
            dataOperateDTO.setPrimaryData(primaryData);
            //修改的数据
            Map<String, String> modifyData = new HashMap<>();
            modifyData.put("IsApproval", "是");
            dataOperateDTO.setData(modifyData);
            //批量修改接口
            dataPacketCommonService.updateBatch(dataOperateDTO);
        });

    }

    @Override
    public void setFlowOver(String businessId) {
        Map<String, List<String>> collect = getSysIdList(Long.parseLong(businessId));
        collect.keySet().forEach(item -> {
            DataOperateDTO dataOperateDTO = new DataOperateDTO();
            dataOperateDTO.setModelId(item);
            List<String> sysIds = collect.get(item);
            //where条件
            Map<String, String> primaryData = new HashMap<>();
            JSONObject json = new JSONObject();
            json.put("M_SYS_ID", sysIds);
            primaryData.put("$in", json.toJSONString());
            //给编辑中的数据生效
            primaryData.put("M_SYS_VERSIONSTATUS", "2");
            //
            dataOperateDTO.setPrimaryData(primaryData);
            //修改的数据
            Map<String, String> modifyData = new HashMap<>();
            //是否审批中置空
            modifyData.put("IsApproval", "");
            //数据生效
            modifyData.put("M_SYS_VERSIONSTATUS", "1");
            dataOperateDTO.setData(modifyData);
            //批量修改接口
            dataPacketCommonService.updateBatch(dataOperateDTO);
            //生效完成后，其他版本置为历史
            setHistoryDataStatus(item, sysIds);
        });
    }

    /**
     * 获取 sys_id集合，根据modelId分组
     *
     * @param businessId
     * @return
     */
    private Map<String, List<String>> getSysIdList(long businessId) {
        QFlowBomSheetDataEntity flowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
        Predicate predicate = flowBomSheetDataEntity.bussinessId.eq(businessId);
        List<FlowBomSheetDataEntity> fetch = jpaQueryFactory.select(flowBomSheetDataEntity)
                .from(flowBomSheetDataEntity)
                .where(predicate)
                .fetch();
        //根据模型编码分组
        return fetch.stream().collect(Collectors.groupingBy(FlowBomSheetDataEntity::getTemplate, Collectors.mapping(FlowBomSheetDataEntity::getDataId, Collectors.toList())));
    }

    /**
     * 修改历史状态
     *
     * @param templateId
     * @param sysIds
     */
    private void setHistoryDataStatus(String templateId, List<String> sysIds) {
        //获取dataId集合
        ModelDataQueryParamVO modelDataQueryParamVO = new ModelDataQueryParamVO();
        JSONObject jsonObject = new JSONObject();
        JSONObject sysIdJson = new JSONObject();
        sysIdJson.put("$in", sysIds.toArray());
        //根据数据id
        jsonObject.put("F_M_SYS_ID", sysIdJson);
        modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
        List<Map<String, Object>> dataListAll = iDatamationsClient.packetDataListAll("", templateId, modelDataQueryParamVO);
        //获取dataid集合
        List<String> dataIds = dataListAll.stream().map(item -> item.get("S_M_SYS_DATAID").toString()).distinct().collect(Collectors.toList());
        batchUpdateHistory(templateId, sysIds, dataIds);
    }

    /**
     * 执行修改
     *
     * @param sysIds
     * @param dataIds
     */
    private void batchUpdateHistory(String modelId, List<String> sysIds, List<String> dataIds) {
        //批量修改条件
        DataOperateDTO dataOperateDTO = new DataOperateDTO();
        dataOperateDTO.setModelId(modelId);
        //where条件 根据dataid查其他版本
        Map<String, String> primaryData = new HashMap<>();
        JSONObject json = new JSONObject();
        json.put("M_SYS_DATAID", dataIds);
        primaryData.put("$in", json.toJSONString());
        //不是最高版本的
        primaryData.put("M_SYS_MAXVERSION", "0");
        //排除自己的
        JSONObject sysIdsJson = new JSONObject();
        sysIdsJson.put("M_SYS_ID", sysIds);
        primaryData.put("$notIn", sysIdsJson.toJSONString());
        dataOperateDTO.setPrimaryData(primaryData);
        //修改的数据
        Map<String, String> modifyData = new HashMap<>();
        //是否审批中置空
        modifyData.put("IsApproval", "");
        //置为历史数据
        modifyData.put("M_SYS_VERSIONSTATUS", "4");
        dataOperateDTO.setData(modifyData);
        //批量修改接口
        dataPacketCommonService.updateBatch(dataOperateDTO);
    }

    @Override
    public void recoverDataState(String businessId) {
        Map<String, List<String>> sysIdList = getSysIdList(Long.parseLong(businessId));
        sysIdList.keySet().forEach(item -> {
            List<String> sysIds = sysIdList.get(item);
            //批量修改条件
            DataOperateDTO dataOperateDTO = new DataOperateDTO();
            dataOperateDTO.setModelId(item);
            //where条件 根据dataid查其他版本
            Map<String, String> primaryData = new HashMap<>();
            JSONObject json = new JSONObject();
            json.put("M_SYS_ID", sysIds);
            primaryData.put("$in", json.toJSONString());
            dataOperateDTO.setPrimaryData(primaryData);
            //修改的数据
            Map<String, String> modifyData = new HashMap<>();
            //是否审批中置空
            modifyData.put("IsApproval", "");
            dataOperateDTO.setData(modifyData);
            //批量修改接口
            dataPacketCommonService.updateBatch(dataOperateDTO);
        });
    }
}