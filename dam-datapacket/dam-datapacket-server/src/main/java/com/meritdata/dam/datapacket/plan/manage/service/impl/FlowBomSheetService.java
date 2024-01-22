package com.meritdata.dam.datapacket.plan.manage.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.client.IMeritCloudClient;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowBomSheetDao;
import com.meritdata.dam.datapacket.plan.manage.entity.*;
import com.meritdata.dam.datapacket.plan.manage.entity.client.ModelDataExportParam;
import com.meritdata.dam.datapacket.plan.manage.entity.request.ApproveVersionRequrst;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.ApproveVersionResponse;
import com.meritdata.dam.datapacket.plan.manage.entity.response.StartFlowListResponse;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowApproveInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowCreateInter;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.service.IDataPacketCommonService;
import com.meritdata.dam.datapacket.plan.utils.CommUtil;
import com.meritdata.dam.datapacket.plan.utils.MeritCloudUtil;
import com.meritdata.dam.datapacket.plan.utils.TempleteUtil;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import com.meritdata.dam.entity.metamanage.ModelVersionDTO;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/
@Service
@Slf4j
public class FlowBomSheetService implements IFlowBomSheetInter {

    @Autowired
    IFlowBomSheetDao flowBomSheetDao;
    @Autowired
    JPAQueryFactory jpaQueryFactory;
    @Autowired
    IFlowCreateInter flowCreateInter;
    @Autowired
    IMeritCloudClient iMeritCloudClient;
    @Autowired
    IFlowApproveInter iFlowApproveInter;

    @Autowired
    IDataPacketClient dataPacketClient;
    @Autowired
    IMaintainService iMaintainService;

    @Autowired
    IFlowBomSheetDataInter flowBomSheetDataInter;

    @Autowired
    IModuleManageService moduleManageService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IDataPacketCommonService iDataPacketCommonService;

    @Autowired
    TempleteUtil templeteUtil;


    @Override
    public void save(List<String> bomNameList, List<String> templateList, Map<String, String> nodIds, long bussinessId) {
        List<FlowBomSheetEntity> flowBomSheetEntityList = new ArrayList<>();
        bomNameList.forEach(bom -> {
            templateList.forEach(template -> {
                FlowBomSheetEntity build = FlowBomSheetEntity.builder()
                        .id(CommUtil.getUUID())
                        .bussinessId(bussinessId)
                        .template(template).nodeId(nodIds.get(bom))
                        .bomName(bom).build();
                flowBomSheetEntityList.add(build);
            });
        });
        saveAllAndFlush(flowBomSheetEntityList);
    }

    @Override
    public List<FlowBomSheetEntity> save(List<String> bomNameList, List<String> templateList, Map<String, String> nodIds,
                                         long bussinessId, Map<String, String> nodNames) {
        List<FlowBomSheetEntity> flowBomSheetEntityList = new ArrayList<>();
        bomNameList.forEach(bom -> {
            templateList.forEach(template -> {
                FlowBomSheetEntity build = FlowBomSheetEntity.builder()
                        .id(CommUtil.getUUID())
                        .bussinessId(bussinessId)
                        .template(template)
                        .nodeId(nodIds.get(bom))
                        .nodeName(null == nodNames ? "" : nodNames.get(bom))
                        .bomName(bom).build();
                flowBomSheetEntityList.add(build);
            });
        });
        saveAllAndFlush(flowBomSheetEntityList);
        return flowBomSheetEntityList;
    }

    @Override
    public void saveAllAndFlush(List<FlowBomSheetEntity> flowBomSheetEntityList) {
        flowBomSheetDao.saveAllAndFlush(flowBomSheetEntityList);
    }

    @Override
    public List<Long> findBusinessIdListByTemplate(String templateKey) {

        QFlowBomSheetEntity qFlowCreateEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        List<FlowBomSheetEntity> flowBomSheetEntityQueryResults = findListByTemplate(templateKey);
        //去重
        List<Long> collect = flowBomSheetEntityQueryResults.stream().map(FlowBomSheetEntity::getBussinessId).collect(Collectors.toList());
        HashSet set = new HashSet<Long>(collect);
        List<Long> templateIdList = new ArrayList(set);
        return templateIdList;
    }

    @Override
    public List<Long> findBusinessIdListByTemplate(List<String> modelInfo) {

        QFlowBomSheetEntity qFlowCreateEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        List<Long> businessIds = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(qFlowCreateEntity.template.in(modelInfo))
                .orderBy(qFlowCreateEntity.bussinessId.desc())
                .fetchResults().getResults().stream().map(FlowBomSheetEntity::getBussinessId).collect(Collectors.toList());
        return businessIds;
    }


    @Override
    public List<FlowBomSheetEntity> findListByTemplate(String templateKey) {
        QFlowBomSheetEntity qFlowCreateEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        List<FlowBomSheetEntity> flowBomSheetEntityQueryResults = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(qFlowCreateEntity.template.like(templateKey.trim()))
                .orderBy(qFlowCreateEntity.bussinessId.desc())
                .fetchResults().getResults();
        return flowBomSheetEntityQueryResults;
    }


    @Override
    public List<String> findBomListByBussinessId(String bussinessId) {
        List<FlowBomSheetEntity> entityList = getBomSheetList(bussinessId, "");
        List<String> collect = entityList.stream().map(FlowBomSheetEntity::getBomName).distinct().collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<FlowBomSheetEntity> findListByBussinessId(String bussinessId) {
        List<FlowBomSheetEntity> entityList = getBomSheetList(bussinessId, "");
        return entityList;
    }


    @Override
    public List<FlowBomSheetEntity> getBomSheetList(String bussinessId, String bom) {
        QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        Predicate predicate = qFlowBomSheetEntity.bussinessId.like(String.format("%s%s%s", "%", bussinessId.trim(), "%"));
        if (StringUtils.isNotEmpty(bom) && !"all".equals(bom.trim())) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.bomName.eq(bom));
        }
        List<FlowBomSheetEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetEntity)
                .where(predicate)
                .orderBy(qFlowBomSheetEntity.bussinessId.desc())
                .fetchResults().getResults();
        return entityList;
    }

    @Override
    public List<FlowBomSheetDataEntity> getBomSheetDataList(String bussinessId, String bom) {
        QFlowBomSheetDataEntity qFlowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
        Predicate predicate = qFlowBomSheetDataEntity.bussinessId.like(String.format("%s%s%s", "%", bussinessId.trim(), "%"));
        if (StringUtils.isNotEmpty(bom) && !"all".equals(bom.trim())) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.bomName.eq(bom));
        }
        List<FlowBomSheetDataEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetDataEntity)
                .where(predicate)
                .orderBy(qFlowBomSheetDataEntity.bussinessId.desc())
                .fetchResults().getResults();
        return entityList;
    }

    @Override
    public List<Long> getBussinessId(ApproveVersionRequrst request) {
        List<Long> collect = new ArrayList<>();
        QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        Predicate predicate = qFlowBomSheetEntity.bomName.eq(request.getBom());
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.template.eq(request.getTemplate()));
        List<FlowBomSheetEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetEntity)
                .where(predicate)
                .orderBy(qFlowBomSheetEntity.bussinessId.desc())
                .fetchResults().getResults();
        if (entityList.size() > 0) {
            collect = entityList.stream().map(FlowBomSheetEntity::getBussinessId).collect(Collectors.toList());
            return collect;
        } else {
            return collect;
        }
    }

    @Override
    public List<ApproveVersionResponse> getApproveVersionResponse(List<Long> bussinessIds, List<MainFormDTO> bpmList,

                                                                  List<ApproveVersionResponse> approveVersionResponseList,
                                                                  ApproveVersionRequrst request) {
        for (int i = 0; i < bussinessIds.size(); i++) {
            String business = bussinessIds.get(i).toString();

            List<MainFormDTO> collect = bpmList.stream().filter(model -> model.getBusinessId().equals(business)).collect(Collectors.toList());
            if (collect.size() > 0) {
                //获取发起人
                FlowCreateEntity flowCreateEntity = flowCreateInter.findModelByBussinessId(business + "");
                //发起人
                List<String> startName = getUserName(Arrays.asList(flowCreateEntity.getUserId()));
//                MeritCloudUtil meritCloudUtil = new MeritCloudUtil();
//                meritCloudUtil.getUserObjectByUserId(startName.get(0));
                //按照发起人查询-不包含发起人就不进行下面的操作
                if (StringUtils.isNotEmpty(request.getStart()) && !startName.get(0).contains(request.getStart())) {
                    continue;
                }
                //审批人
                List<String> customUserCode = iFlowApproveInter.getCustomUserId(business + "", 4);
                List<String> approveNameList = getUserName(customUserCode);
                //List<String> approveNameList = meritCloudUtil.getUserCodeByUserIds(customUserCode);
                String approveName = approveNameList.stream()
                        .collect(Collectors.joining(","));
                //按照审批人查询-不包含审批人就不进行下面的操作
                if (StringUtils.isNotEmpty(request.getApprove()) && !approveName.contains(request.getApprove())) {
                    continue;
                }
                //流程时间小于最小的，或者大于最大的，不进行下面的操作
//                if (null != request.getDateStart() && null != request.getDateEnd()
//                        && (request.getDateStart().compareTo(collect.get(0).getUpdateTime()) > 0
//                        || request.getDateEnd().compareTo(collect.get(0).getUpdateTime()) < 0)) {
//                    continue;
//                }

                if (null != request.getDateStart() && request.getDateStart().compareTo(collect.get(0).getUpdateTime()) > 0) {
                    continue;
                }

                if (null != request.getDateEnd() && request.getDateEnd().compareTo(collect.get(0).getUpdateTime()) < 0) {
                    continue;
                }

                approveVersionResponseList.add(ApproveVersionResponse.builder()
                        .approve(approveName)
                        .businessId(business + "")
                        .date(collect.get(0).getUpdateTime()).start(startName.get(0)).build());
            }
        }
        //按照审签时间降序排列
        List<ApproveVersionResponse> reversedSortByDate = approveVersionResponseList.stream()
                .sorted(Comparator.comparing(ApproveVersionResponse::getDate).reversed())
                .collect(Collectors.toList());
        return reversedSortByDate;
    }

    @Override
    public void setBomAndTemplate(List<StartFlowListResponse> responseList) {

        List<ModuleManageDto> templeteModulList = moduleManageService
                .list("1", "1000000", "", "", "");

        for (int i = 0; i < responseList.size(); i++) {
            StartFlowListResponse startFlowListResponse = responseList.get(i);
            List<FlowBomSheetEntity> bomSheetList = getBomSheetList(startFlowListResponse.getBussinessId() + "", "");
            //解决按照发次/批次审批时，页面BOM字段为空值问题
            if (CollectionUtil.isNotEmpty(bomSheetList) && StringUtils.isBlank(startFlowListResponse.getBom())){
                startFlowListResponse.setBom(bomSheetList.stream().map(FlowBomSheetEntity::getBomName).distinct().collect(Collectors.joining(",")));
            }
            List<String> templeteList = new ArrayList<>();

            List<String> collect = bomSheetList.stream().map(FlowBomSheetEntity::getTemplate).distinct().collect(Collectors.toList());

            collect.forEach(template -> {
                //设置模板名称
                //  ModuleInfoDTO moduleInfo = dataPacketClient.getModuleInfo(template);
                //   ModuleManageDto moduleInfo = dataPacketClient.moduleVerById(template);

                List<ModuleManageDto> result = templeteModulList.stream().filter(model -> model.getModelInfo().equals(template)).collect(Collectors.toList());
                if (result.size() > 0) {
                    templeteList.add(result.get(0).getName());
                }

            });
            if (CollectionUtil.isNotEmpty(templeteList)) {
                //根据模板编号查询模板名称
                startFlowListResponse.setTemplate(templeteList.stream().collect(Collectors.joining(",")));
            } else {
                String ids = startFlowListResponse.getTemplate();
                if (StrUtil.isNotBlank(ids)) {
                    List<String> split = Arrays.asList(ids.split(","));
                    List<ModuleManageDto> result = templeteModulList.stream().filter(model -> split.contains(model.getModelInfo())).collect(Collectors.toList());
                    List<String> names = result.stream().map(ModuleManageDto::getName).collect(Collectors.toList());
                    startFlowListResponse.setTemplate(String.join(",", names));
                }
            }
        }
    }

    @Override
    public ResultBody<List<String>> validate(StartFlowRequest startFlowRequest, List<MainFormDTO> mainFormDTO) {

        List<String> message = new ArrayList<>();
        List<String> templateList = startFlowRequest.getTemplate();
        List<String> bomList = startFlowRequest.getBom();
        List<ModuleManageDto> templeteList = moduleManageService
                .list("1", "1000000", "", "", "");

        /**
         * 找到所有的流程实例编号
         */
        QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        bomList.forEach(bom -> {
            templateList.forEach(template -> {
                Predicate predicate = qFlowBomSheetEntity.bomName.eq(bom);
                predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.template.eq(template));
                List<FlowBomSheetEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetEntity)
                        .where(predicate)
                        .orderBy(qFlowBomSheetEntity.bussinessId.desc())
                        .fetchResults().getResults();
                if (entityList.size() > 0) {
//                    LongSummaryStatistics summaryStatistics = entityList.stream().map(FlowBomSheetEntity::getBussinessId).collect(Collectors.summarizingLong(x -> x));
//                   //记录最大的流程实例编号,去查询结束的流程
//                    List<MainFormDTO> collect = mainFormDTO.stream().filter(mainFormDTO1 -> mainFormDTO1.getBusinessId().equals(summaryStatistics.getMax() + "")).collect(Collectors.toList());
                    entityList.forEach(model -> {
                        if (flowCreateInter.findEntityNotOver(model.getBussinessId())) {
                            List<ModuleManageDto> collect = templeteList.stream().filter(templeteListmodel -> templeteListmodel.getModelInfo().equals(template)).collect(Collectors.toList());
                            message.add(String.format("实物号[%s]模板名称[%s]存在未完成的流程", bom, collect.get(0).getName()));
                        }
                    });
                }
                //下面的操作是验证流程是否结束
            });
        });
        return ResultBody.success(message);
    }

    @Override
    public void chageDataState(StartFlowRequest startFlowRequest, List<MainFormDTO> mainFormDTO, String bussinessId) {
        List<String> dataIds = new ArrayList<>();
        List<String> bomNameList = startFlowRequest.getBom();
        List<String> templateList = startFlowRequest.getTemplate();
        List<FlowBomSheetEntity> flowBomSheetEntityList = new ArrayList<>();

        bomNameList.forEach(bom -> {
            templateList.forEach(template -> {
                FlowBomSheetEntity build = FlowBomSheetEntity.builder()
                        .id(CommUtil.getUUID())
                        .template(template)
                        .bomName(bom).build();
                flowBomSheetEntityList.add(build);
            });
        });

//        ModelDataQueryParamVO param = new ModelDataQueryParamVO();
//        param.setPage(1);
//        param.setRows(100000);

        flowBomSheetEntityList.forEach(bomSheet -> {
            /**
             * list传递未空，查询最高版本
             *
             */
            List<Map<String, Object>> maxVersion = templeteUtil.getMaxVersion(bomSheet.getTemplate(), bomSheet.getBomName());
            //最高版本编辑中数据
            List<Map<String, Object>> edit = templeteUtil.getMaxVersionAndEdit(maxVersion);
            edit.forEach(editData -> {
                if (null != editData.get("F_M_SYS_ID")) {
                    templeteUtil.updateModelDataState(editData.get("F_M_SYS_ID").toString(),
                            "是", bomSheet.getTemplate(), "");
                }
            });
        });

    }

    @Override
    public void setFlowOver(String bussinessId) {

        List<String> dataIds = new ArrayList<>();
        List<FlowBomSheetEntity> entityList = getBomSheetList(bussinessId, "");
        //获取bom
        List<String> bomNameList = entityList.stream().map(FlowBomSheetEntity::getBomName).distinct().collect(Collectors.toList());
        //获取template
        List<String> templateList = entityList.stream().map(FlowBomSheetEntity::getTemplate).distinct().collect(Collectors.toList());
        List<FlowBomSheetEntity> flowBomSheetEntityList = new ArrayList<>();
        //第一步：查询bom和templete
        bomNameList.forEach(bom -> {
            templateList.forEach(template -> {
                FlowBomSheetEntity build = FlowBomSheetEntity.builder()
                        .id(CommUtil.getUUID())
                        .template(template)
                        .bomName(bom).build();
                flowBomSheetEntityList.add(build);
            });
        });

        ModelDataQueryParamVO param = new ModelDataQueryParamVO();
        param.setPage(1);
        param.setRows(100000);
        //第二步：查询bussinessid下的数据
        List<FlowBomSheetDataEntity> listByBusinessId = flowBomSheetDataInter.findListByBusinessId(Long.parseLong(bussinessId));
        List<String> dataIdList = listByBusinessId.stream().map(FlowBomSheetDataEntity::getDataId).collect(Collectors.toList());

        //第三步：修改数据状态
        flowBomSheetEntityList.forEach(bomSheet -> {
            List<Map<String, Object>> rows = iMaintainService.dataListManageByIds(bomSheet.getBomName(), bomSheet.getTemplate(), param, dataIdList).getData().getRows();
            //最高版本数据
            // List<Map<String, Object>> maxVersion = rows.stream().filter(mode -> mode.get("S_M_SYS_MAXVERSION").toString().equals("1")).collect(Collectors.toList());
            //最高版本编辑中数据
            List<Map<String, Object>> edit = rows.stream().filter(mode -> mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("3")).collect(Collectors.toList());

            edit.forEach(editData -> {
                dataIds.add(editData.get("S_M_SYS_DATAID").toString());
                //这里要修改bom的代码
                ModelDataExportParam modelDataExportParam = new ModelDataExportParam();
                modelDataExportParam.setModelId(bomSheet.getTemplate());
                modelDataExportParam.setOperType("dataManage");

                List<Map<String, Object>> selectGridData = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("S_M_SYS_DATAID", editData.get("S_M_SYS_DATAID").toString());
                map.put("F_M_SYS_ID", editData.get("S_M_SYS_DATAID").toString());
                map.put("S_M_SYS_VERSION", editData.get("S_M_SYS_VERSION").toString()); //数据版本
                //STOP("停用", 0),
                //EFFECT("生效", 1),
                //EDIT("编辑中", 2),
                //AUDIT("审核中", 3),
                //HISTORY("历史", 4);
                map.put("S_M_SYS_VERSIONSTATUS", 2);  //数据版本状态
                selectGridData.add(map);
                modelDataExportParam.setSelectGridData(selectGridData);
                //更新数据仓库数据状态
                iMaintainService.effectData(modelDataExportParam);
            });
        });
    }


    @Override
    public void setFlowSuccessOver(String bussinessId) {

        List<String> dataIds = new ArrayList<>();
        List<FlowBomSheetEntity> entityList = getBomSheetList(bussinessId, "");
        //获取bom
        List<String> bomNameList = entityList.stream().map(FlowBomSheetEntity::getBomName).distinct().collect(Collectors.toList());
        //获取template
        List<String> templateList = entityList.stream().map(FlowBomSheetEntity::getTemplate).distinct().collect(Collectors.toList());
        List<FlowBomSheetEntity> flowBomSheetEntityList = new ArrayList<>();
        bomNameList.forEach(bom -> {
            templateList.forEach(template -> {
                FlowBomSheetEntity build = FlowBomSheetEntity.builder()
                        .id(CommUtil.getUUID())
                        .template(template)
                        .bomName(bom).build();
                flowBomSheetEntityList.add(build);
            });
        });
        ModelDataQueryParamVO param = new ModelDataQueryParamVO();
        param.setPage(1);
        param.setRows(100000);

        //第二步：查询bussinessid下的数据
        List<FlowBomSheetDataEntity> listByBusinessId = flowBomSheetDataInter.findListByBusinessId(Long.parseLong(bussinessId));
        List<String> dataIdList = listByBusinessId.stream().map(FlowBomSheetDataEntity::getDataId).collect(Collectors.toList());

        flowBomSheetEntityList.forEach(bomSheet -> {
            List<Map<String, Object>> rows = iMaintainService.dataListManageByIds(bomSheet.getBomName(), bomSheet.getTemplate(), param, dataIdList).getData().getRows();
            //最高版本数据
//            List<Map<String, Object>> allVersion = rows.stream().filter(mode -> mode.get("S_M_SYS_MAXVERSION").toString().equals("1")).collect(Collectors.toList());
            //最高版本编辑中数据
            List<Map<String, Object>> edit = rows.stream().filter(mode -> mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("3")).collect(Collectors.toList());
            edit.forEach(editData -> {
                dataIds.add(editData.get("S_M_SYS_DATAID").toString());
                //这里要修改bom的代码
                ModelDataExportParam modelDataExportParam = new ModelDataExportParam();
                modelDataExportParam.setModelId(bomSheet.getTemplate());
                modelDataExportParam.setOperType("dataManage");

                List<Map<String, Object>> selectGridData = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("S_M_SYS_DATAID", editData.get("S_M_SYS_DATAID").toString());
                map.put("F_M_SYS_ID", editData.get("S_M_SYS_DATAID").toString());
                map.put("S_M_SYS_VERSION", editData.get("S_M_SYS_VERSION").toString()); //数据版本
                //STOP("停用", 0),
                //EFFECT("生效", 1),
                //EDIT("编辑中", 2),
                //AUDIT("审核中", 3),
                //HISTORY("历史", 4);
                map.put("S_M_SYS_VERSIONSTATUS", 2);  //数据版本状态
                selectGridData.add(map);
                modelDataExportParam.setSelectGridData(selectGridData);
                //更新数据仓库数据状态
                iMaintainService.effectData(modelDataExportParam);
            });
//            if (edit.size()>0){
//            //把小于当前版本的都修改为历史
//            List<Map<String, Object>> history = maxVersion.stream().filter(mode -> mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("3")
//                    && Integer.parseInt(mode.get("S_M_SYS_VERSION").toString()) <Integer.parseInt(edit.get(0).get("S_M_SYS_VERSION").toString())
//            ).collect(Collectors.toList());
//
//            history.forEach(editData -> {
//                dataIds.add(editData.get("S_M_SYS_DATAID").toString());
//                //这里要修改bom的代码
//                ModelDataExportParam modelDataExportParam = new ModelDataExportParam();
//                modelDataExportParam.setModelId(bomSheet.getTemplate());
//                modelDataExportParam.setOperType("dataManage");
//
//                List<Map<String, Object>> selectGridData = new ArrayList<>();
//                Map<String, Object> map = new HashMap<>();
//                map.put("S_M_SYS_DATAID", editData.get("S_M_SYS_DATAID").toString());
//                map.put("F_M_SYS_ID", editData.get("S_M_SYS_DATAID").toString());
//                map.put("S_M_SYS_VERSION",1); //数据版本
//                //STOP("停用", 0),
//                //EFFECT("生效", 1),
//                //EDIT("编辑中", 2),
//                //AUDIT("审核中", 3),
//                //HISTORY("历史", 4);
//                map.put("S_M_SYS_VERSIONSTATUS",2);  //数据版本状态
//                selectGridData.add(map);
//                modelDataExportParam.setSelectGridData(selectGridData);
//                //更新数据仓库数据状态
//                iMaintainService.effectData(modelDataExportParam);
//            });
//            }
        });
    }

    @Override
    public FlowBomSheetEntity findLastModelByBomAndTemplete(String bom, String templete) {

        QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        Predicate predicate = qFlowBomSheetEntity.bomName.eq(bom);
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.template.eq(templete));
        List<FlowBomSheetEntity> flowBomSheetEntityQueryResults = jpaQueryFactory.selectFrom(qFlowBomSheetEntity)
                .where(predicate)
                .orderBy(qFlowBomSheetEntity.bussinessId.desc())
                .fetchResults().getResults();
        //解决接口报错问题，判空
        if (CollectionUtils.isEmpty(flowBomSheetEntityQueryResults)) {
            return null;
        }

        FlowBomSheetEntity max = flowBomSheetEntityQueryResults.stream().max(Comparator.comparingLong(FlowBomSheetEntity::getBussinessId)).orElseGet(null);
        return max;
    }


    private List<String> getUserName(List<String> ids) {
        List<String> userIdList = new ArrayList<>();
        JSONObject listInIds = iMeritCloudClient.getListInIds(ids);
        List<Map<String, String>> l = (List<Map<String, String>>) listInIds.get("data");
        for (int i = 0; i < l.size(); i++) {
            String userName = l.get(i).get("name");
            userIdList.add(userName);
        }
        return userIdList;
    }


    @Override
    public ResultBody dataListManage(String template, String bom) {

//        Map<String, String> maps = new HashMap<>();
//        maps.put("name", "SUPPORTING_LIST");
//        ModelVersionDTO moduleInfo = iMetaManageClient.getModuleInfo(maps);
//        String modelInfo = moduleInfo != null ? moduleInfo.getModelInfo() : "";


        List<Map<String, Object>> f_physical_no = iDatamationsClient.dataListManage(template, "F_PHYSICAL_NO", bom);
        int a = 10;
        return null;
    }

    @Override
    public void deleteBomSheet(String templateId, String bom, long bussinessId) {
        QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        Predicate predicate = qFlowBomSheetEntity.bussinessId.like(String.format("%s%s%s", "%", bussinessId, "%"));
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.bomName.eq(bom));
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.template.eq(templateId));

        List<FlowBomSheetEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetEntity)
                .where(predicate)
                .orderBy(qFlowBomSheetEntity.bussinessId.desc())
                .fetchResults().getResults();
        if (null != entityList) {
            flowBomSheetDao.delete(entityList.get(0));
        }
    }


    @Override
    public void recoverDataState(String bussinessId) {
        //找到所有审核的数据
        // 优化前
//        List<FlowBomSheetDataEntity> bomSheetDataEntityList = flowBomSheetDataInter
//                .findListByBusinessId(Long.parseLong(bussinessId)).stream().filter(model -> model.getType().equals(0)).collect(Collectors.toList());
        // 优化后
        List<FlowBomSheetDataEntity> dataEntityList = flowBomSheetDataInter.findListByBusinessIdAndType(Long.parseLong(bussinessId), 0);

        Map<String, List<FlowBomSheetDataEntity>> empleteIdListMap = dataEntityList.stream().collect(Collectors.groupingBy(FlowBomSheetDataEntity::getTemplate));
        Iterator<Map.Entry<String, List<FlowBomSheetDataEntity>>> iterator = empleteIdListMap.entrySet().iterator();
        /**
         * 优化思路 第一个不修改原来的逻辑 ， 第二个 将需要使用的数据一次查出组装为后续方便使用的数据减少数据库操作
         */
        Map<String, List<Map<String, Object>>> dataMap = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, List<FlowBomSheetDataEntity>> next = iterator.next();
            // key就是模板id
            String key = next.getKey();
            List<FlowBomSheetDataEntity> value = next.getValue();
            // key是模板id
            // 得到bom的集合
            List<String> bomList = value.stream().map(FlowBomSheetDataEntity::getBomName).collect(Collectors.toList());
            List<String> dataIdList = value.stream().map(FlowBomSheetDataEntity::getDataId).collect(Collectors.toList());
            List<Map<String, Object>> templateByBomListAndDataIdList = templeteUtil.findTemplateByBomListAndDataIdList(key + "", bomList, dataIdList);
            bomList.forEach(bom -> {

                List<Map<String, Object>> bomResult = templateByBomListAndDataIdList.stream()
                        .filter(mode ->  bom.equals(mode.get("F_PhysicalCode").toString()))
                        .collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(bomResult)) {
                    dataMap.put(key + "_" + bom, bomResult);
                }
            });

        }

        //修改数据状态
        dataEntityList.forEach(dataEntity -> {
            // 优化前
//            List<Map<String, Object>> allDataByModelInfoAndBomIds = templeteUtil.getALLDataByModelInfoAndBomIds(dataEntity.getTemplate(),
//                    dataEntity.getBomName(), Arrays.asList(dataEntity.getDataId()));

            /**
             * 优化后 从上面的dataMap 中取对应的templet + bom 条件数据 还需要在过滤出 dataId相等的数据
             *
             */
            List<Map<String, Object>> orginalList = dataMap.get(dataEntity.getTemplate() + "_" + dataEntity.getBomName());
            List<Map<String, Object>> allDataByModelInfoAndBomIds = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(orginalList)) {
                String dataId = dataEntity.getDataId();
                allDataByModelInfoAndBomIds = orginalList.stream()
                        .filter(mode -> dataId.equals(mode.get("F_M_SYS_ID").toString()))
                        .collect(Collectors.toList());
            }
            log.info("数据恢复" + allDataByModelInfoAndBomIds.size());
            if (allDataByModelInfoAndBomIds.size() > 0) {
                if (allDataByModelInfoAndBomIds.size() > 0 && null != allDataByModelInfoAndBomIds.get(0)) {
//                        templeteUtil.updateModelDataState(allDataByModelInfoAndBomIds.get(0).get("S_M_SYS_DATAID").toString(),
//                                "", dataEntity.getTemplate(),"");
                    log.info("数据恢复");
                    templeteUtil.updateModelDataState(allDataByModelInfoAndBomIds.get(0).get("F_M_SYS_ID").toString(),
                            "", dataEntity.getTemplate(), allDataByModelInfoAndBomIds.get(0).get("S_M_SYS_VERSIONSTATUS").toString());
                }
            }
//            });
        });
    }

    @Override
    public void FlowOverDataState(String bussinessId) {
        log.info("审批通过后开始修改数据！");
        //找到所有审核的数据
        /**
         * 优化前 存在lamda二次处理
         */
        log.info("审批通过后开始修改数据：查询boomsheet数据");
        List<FlowBomSheetDataEntity> dataEntityList = flowBomSheetDataInter.findListByBusinessIdAndType(Long.parseLong(bussinessId), 0);

        log.info("审批通过后开始修改数据：按照模板进行分组数据");
        Map<String, List<FlowBomSheetDataEntity>> empleteIdListMap = dataEntityList.stream().collect(Collectors.groupingBy(FlowBomSheetDataEntity::getTemplate));
        Iterator<Map.Entry<String, List<FlowBomSheetDataEntity>>> iterator = empleteIdListMap.entrySet().iterator();
        /**
         * 优化思路 第一个不修改原来的逻辑 ， 第二个 将需要使用的数据一次查出组装为后续方便使用的数据减少数据库操作
         */
        Map<String, List<Map<String, Object>>> dataMap = new HashMap<>();
        log.info("审批通过后开始修改数据：组装数据开始步骤1" + empleteIdListMap.size());
        while (iterator.hasNext()) {
            Map.Entry<String, List<FlowBomSheetDataEntity>> next = iterator.next();
            // key就是模板id
            String key = next.getKey();
            List<FlowBomSheetDataEntity> value = next.getValue();
            // key是模板id
            // 得到bom的集合
            try {
                List<String> bomList = value.stream().map(FlowBomSheetDataEntity::getBomName).distinct().collect(Collectors.toList());
                List<String> dataIdList = value.stream().map(FlowBomSheetDataEntity::getDataId).distinct().collect(Collectors.toList());
                log.info("审批通过后开始修改数据：组装数据开始步骤1-1");
                List<Map<String, Object>> templateByBomListAndDataIdList = new ArrayList<>();
                int batchSize = 100;

                int numBatches = (int) Math.ceil((double) bomList.size() / batchSize);
                int dataNumBatches = (int) Math.ceil((double) dataIdList.size() / batchSize);
                for (int i = 0; i < numBatches; i++) {
                    int startIndex = i * batchSize;
                    int endIndex = Math.min((i + 1) * batchSize, bomList.size());
                    log.info("bom开始-结束：" + startIndex + "-" + endIndex);
                    List<String> batch = bomList.subList(startIndex, endIndex);
                    for (int j = 0; j < dataNumBatches; j++) {
                        int startDataIndex = j * batchSize;
                        int endDataIndex = Math.min((j + 1) * batchSize, dataIdList.size());
                        log.info("dataId开始-结束：" + startDataIndex + "-" + endDataIndex);
                        List<String> dataBatch = dataIdList.subList(startDataIndex, endDataIndex);
//                        ArrayList<String> dataBatchArrayList = new ArrayList<>(dataBatch);
                        log.info("dataId开始-结束：11111");
                        List<Map<String, Object>> templateByBomListAndDataIdList1 = templeteUtil.findTemplateByBomListAndDataIdList(key + "", batch, dataBatch);
                        log.info("dataId开始-结束：222222");
                        if (CollectionUtil.isNotEmpty(templateByBomListAndDataIdList1)) {
                            templateByBomListAndDataIdList.addAll(templateByBomListAndDataIdList1);
                        }
                    }
                }
                log.info("审批通过后开始修改数据：组装数据开始步骤1-1结束");
                bomList.forEach(bom -> {

                    List<Map<String, Object>> bomResult = templateByBomListAndDataIdList.stream()
                            .filter(mode -> bom.equals(mode.get("F_PhysicalCode").toString()))
                            .collect(Collectors.toList());
                    if (CollectionUtil.isNotEmpty(bomResult)) {
                        dataMap.put(key + "_" + bom, bomResult);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                log.info("查询异常" + e.toString());
                log.info("查询异常" + e.toString());
            }


        }
        log.info("审批通过后开始修改数据：组装数据开始步骤2");
        //找到分类
        List<FlowBomSheetEntity> sheetEntityList = findListByBussinessId(bussinessId);
        // 新建一个Map 来存储下面model匹配的数据减少每次的filter
        Map<String, List<FlowBomSheetDataEntity>> modelMap = new HashMap<>();
        for (FlowBomSheetDataEntity flowBomSheetDataEntity : dataEntityList) {
            String bomName = flowBomSheetDataEntity.getBomName();
            String templateName = flowBomSheetDataEntity.getTemplate();
            if (modelMap.get(bomName + "_" + templateName) != null) {
                List<FlowBomSheetDataEntity> flowBomSheetDataEntities = modelMap.get(bomName + "_" + templateName);
                flowBomSheetDataEntities.add(flowBomSheetDataEntity);
                modelMap.put(bomName + "_" + templateName, flowBomSheetDataEntities);
            } else {
                List<FlowBomSheetDataEntity> newEmptyList = new ArrayList<>();
                newEmptyList.add(flowBomSheetDataEntity);
                modelMap.put(bomName + "_" + templateName, newEmptyList);
            }
        }
        log.info("审批通过后开始修改数据：循环数据进行更新");
        sheetEntityList.forEach(model -> {

            /**
             * 优化后
             */
            List<FlowBomSheetDataEntity> bomSheetDataEntityList = modelMap.get(model.getBomName() + "_" + model.getTemplate());
            //批量修改中台状态值
            log.info("批量修改中台状态值：更新开始：实物/批次号：" + model.getBomName() + "进入当前方法时间戳：" + System.currentTimeMillis());
            String s_m_sys_version = dataMap.get(model.getTemplate() + "_" + model.getBomName()).get(0).get("S_M_SYS_VERSION").toString();
            DataOperateDTO dataOperateDTO = new DataOperateDTO();
            Map<String, String> PrimaryMap = new HashMap<>();
            Map<String, String> DataMap = new HashMap<>();
            PrimaryMap.put("PhysicalCode", model.getBomName());
            DataMap.put("M_SYS_VERSION", s_m_sys_version);
            DataMap.put("M_SYS_VERSIONSTATUS", "1");
            DataMap.put("IsApproval", "");
            dataOperateDTO.setModelId(model.getTemplate());
            dataOperateDTO.setPrimaryData(PrimaryMap);
            dataOperateDTO.setData(DataMap);
            iDataPacketCommonService.updateBatch(dataOperateDTO);
            log.info("批量修改中台状态值：更新结束，结束方法时间戳：" + System.currentTimeMillis());
            // 循环模板id来
            //修改数据状态
            bomSheetDataEntityList.forEach(dataEntity -> {
                List<Map<String, Object>> orginalList = dataMap.get(dataEntity.getTemplate() + "_" + dataEntity.getBomName());
                List<Map<String, Object>> allDataByModelInfoAndBomIds = new ArrayList<>();
                if (CollectionUtil.isNotEmpty(orginalList)) {
                    String dataId = dataEntity.getDataId();
                    allDataByModelInfoAndBomIds = orginalList.stream()
                            .filter(mode -> dataId.equals(mode.get("F_M_SYS_ID").toString()))
                            .collect(Collectors.toList());
                }
//                /**
//                 * 以下两个更新操作暂无优化思路
//                 */
//                log.info("审批通过后开始修改数据：" + dataEntity.getTemplate() + "_" + dataEntity.getBomName() + "修改数据量为" + allDataByModelInfoAndBomIds.size());
//                if (allDataByModelInfoAndBomIds.size() > 0) {
//                    if (null != allDataByModelInfoAndBomIds.get(0).get("S_M_SYS_DATAID") && null != allDataByModelInfoAndBomIds.get(0).get("F_M_SYS_ID")) {
//                        log.info("审批通过后开始修改数据：更新");
//                        templeteUtil.updateModelDataStateCenter(
//                                allDataByModelInfoAndBomIds.get(0).get("S_M_SYS_DATAID") + "",
//                                allDataByModelInfoAndBomIds.get(0).get("S_M_SYS_VERSION") + "",
//                                dataEntity.getTemplate(),
//                                allDataByModelInfoAndBomIds.get(0).get("F_M_SYS_ID") + "");
//                        templeteUtil.updateModelDataState(allDataByModelInfoAndBomIds.get(0).get("F_M_SYS_ID") + "",
//                                "", dataEntity.getTemplate(), "");
//                        log.info("审批通过后开始修改数据：更新结束");
//                    }
                //审批通过后，修改低于当前版本的数据状态为历史
                log.info("审批通过后开始修改数据：修改低于当前版本数据开始");
                setHistoryData(model, dataEntity, allDataByModelInfoAndBomIds);
                log.info("审批通过后开始修改数据：修改低于当前版本数据结束");
//                }
            });
        });
    }

    @Override
    public ResultBody<List<String>> validate(StartFlowRequest startFlowRequest, List<FlowBomSheetEntity> flowBomSheetEntityList, List<ModuleManageDto> templeteList, Map<String, Boolean> isNotOverMap) {

        List<String> message = new ArrayList<>();
        List<String> templateList = startFlowRequest.getTemplate();
        List<String> bomList = startFlowRequest.getBom();
        /**
         * 找到所有的流程实例编号
         */
        bomList.forEach(bom -> {
            templateList.forEach(template -> {
                List<FlowBomSheetEntity> entityList = flowBomSheetEntityList.stream().filter(item -> item.getBomName().equals(bom) && item.getTemplate().equals(template)).collect(Collectors.toList());
                if (entityList.size() > 0) {
                    entityList.forEach(model -> {
                        if (isNotOverMap.get(String.valueOf(model.getBussinessId()))) {
                            List<ModuleManageDto> collect = templeteList.stream().filter(templeteListmodel -> templeteListmodel.getModelInfo().equals(template)).collect(Collectors.toList());
                            message.add(String.format("实物号[%s]模板名称[%s]存在未完成的流程", bom, collect.get(0).getName()));
                        }
                    });
                }
                //下面的操作是验证流程是否结束
            });
        });
        return ResultBody.success(message);
    }

    @Override
    public void chageDataStateNew(Map<String, List<Map<String, Object>>> bomTemMap, List<FlowBomSheetEntity> flowBomSheetEntityList) {

        flowBomSheetEntityList.forEach(bomSheet -> {
            List<Map<String, Object>> edit = bomTemMap.get(bomSheet.getTemplate() + "_" + bomSheet.getBomName());
            if (CollectionUtil.isNotEmpty(edit)) {
                edit.forEach(editData -> {
                    if (null != editData.get("F_M_SYS_ID")) {
                        templeteUtil.updateModelDataState(editData.get("F_M_SYS_ID").toString(),
                                "是", bomSheet.getTemplate(), "");
                    }
                });
            }
        });
    }


    /**
     * 审批通过后，修改低于当前版本的数据状态为历史
     *
     * @param model
     * @param dataEntity
     * @param allDataByModelInfoAndBomIds
     */
    private void setHistoryData(FlowBomSheetEntity model, FlowBomSheetDataEntity dataEntity, List<Map<String, Object>> allDataByModelInfoAndBomIds) {
        //根据id获取的数据必定为一条数据，如果没获取到则人为删除，为脏数据不用管
        //审批通过的数据，需要查询小于本条数据的版本号的所有数据，将状态改为历史----历史状态为4，中台约定，不可更改
        Map<String, Object> passData = allDataByModelInfoAndBomIds.get(0);
        //dataId
        String dataId = passData.get("S_M_SYS_DATAID").toString();
        //数据版本
        Integer sysVersion = Integer.parseInt(passData.get("S_M_SYS_VERSION").toString());

        if (sysVersion > 1) {
            //根据dataId 获取小于当前版本号的所有数据
            ModelDataQueryParamVO queryParamVO = new ModelDataQueryParamVO();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("S_M_SYS_DATAID", dataId);
            //小于当前版本的条件
            JSONObject versionPredicate = new JSONObject();
            versionPredicate.put("$lt", sysVersion);
            jsonObject.put("S_M_SYS_VERSION", versionPredicate);
            queryParamVO.setQueryFilter(jsonObject.toJSONString());
            //获取到所有小于当前版本的数据
            List<Map<String, Object>> ltPassDatas = iDatamationsClient.packetDataListAll(model.getBomName(), dataEntity.getTemplate(), queryParamVO);
            for (Map<String, Object> item : ltPassDatas) {
                if ("4".equals(item.get("S_M_SYS_VERSIONSTATUS"))) {
                    // 如果状态已经是历史 无需修改
                    continue;
                }
                //主键字段
                Map<String, String> primaryData = new HashMap<>();
                primaryData.put("F_M_SYS_ID", item.get("F_M_SYS_ID").toString());
                //要修改的字段
                Map<String, String> modifyData = new HashMap<>();

                //改为历史
                modifyData.put("S_M_SYS_VERSIONSTATUS", "4");
                DataOperateDTO param = new DataOperateDTO();
                param.setModelId(dataEntity.getTemplate());
                param.setData(modifyData);
                param.setPrimaryData(primaryData);
                //生效数据
                param.setEffect(false);
                ResultBody body = iDatamationsClient.updateModelData(param);
                if (!body.isSuccess()) {
                    log.error("审批通过后，修改低版本数据为历史失败！");
                }
            }
        }
    }
}
