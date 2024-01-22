package com.meritdata.dam.datapacket.plan.manage.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.bpm.core.BpmEngine;
import com.meritdata.cloud.properties.MeritdataCloudProperties;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IMeritCloudClient;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowApproveDao;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowCreateDao;
import com.meritdata.dam.datapacket.plan.manage.dao.IInitFormDao;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.QFlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowListRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowVerifyRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.StartFlowListResponse;
import com.meritdata.dam.datapacket.plan.manage.entity.response.VerifyFlowListResponse;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowCreateInter;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.utils.CommUtil;
import com.meritdata.dam.datapacket.plan.utils.MeritCloudUtil;
import com.meritdata.dam.datapacket.plan.utils.PageUtil;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStateEnum;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/
@Service
@Slf4j
public class FlowCreateService implements IFlowCreateInter {

    @Autowired
    IFlowApproveDao flowApproveDao;

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    @Autowired
    IFlowCreateDao flowCreateDao;
    @Autowired
    IInitFormDao iInitFormDao;

    @Autowired
    IFlowBomSheetInter flowBomSheetInter;

    @Autowired
    IMeritCloudClient iMeritCloudClient;

    @Autowired
    BpmEngine bpmEngine;

    @Autowired
    IDataPacketClient dataPacketClient;

    @Autowired
    IMaintainService iMaintainService;

    @Autowired
    private MeritdataCloudProperties meritdataCloudProperties;

    @Autowired
    SessionUtils sessionUtils;


    @Autowired
    MeritCloudUtil meritCloudUtil;


    @Autowired
    IModuleManageService moduleManageService;

    @Autowired
    com.meritdata.dam.datapacket.plan.utils.TempleteUtil templeteUtil;


    @Override
    public GridView<StartFlowListResponse> findByPage(StartFlowListRequest startFlowListRequest) {

        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        Predicate predicate = null;
        if (StringUtils.isNotEmpty(startFlowListRequest.getBussinessId())) {
            predicate = qFlowCreateEntity.bussinessId.like(String.format("%s%s%s", "%", startFlowListRequest.getBussinessId(), "%"));
        }

        /**
         * 构造查询条件-开始
         */
        if (StringUtils.isNotEmpty(startFlowListRequest.getFlowState())) {
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.flowState.eq(startFlowListRequest.getFlowState()));
            } else {
                predicate = qFlowCreateEntity.flowState.eq(startFlowListRequest.getFlowState());
            }
        }


        //2表示全部
        if (2 != startFlowListRequest.getApprovalType()) {
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.approvalType.eq(startFlowListRequest.getApprovalType()));
            } else {
                predicate = qFlowCreateEntity.approvalType.eq(startFlowListRequest.getApprovalType());
            }
        }

        if (StringUtils.isNotEmpty(startFlowListRequest.getInitator())) {
            List<String> userIdList = meritCloudUtil.getUserInfoListByUserName(startFlowListRequest.getInitator().trim());
            if (predicate != null && userIdList.size() > 0) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.userId.in(userIdList));
            } else {
                predicate = qFlowCreateEntity.userId.in(userIdList);
            }
        }


        if (StringUtils.isNotEmpty(startFlowListRequest.getTemplate())) {

            Map<String, String> map = new HashMap();
            map.put("nodeId", "");
            map.put("tempId", "");
            map.put("code", "");
            map.put("name", startFlowListRequest.getTemplate());
            map.put("tableName", "");
            //查询出所有模板信息
            List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(map);
            //查询所有的modelInfo
            List<String> modelInfoList = moduleManageInfoList.stream().map(ModuleManageDto::getModelInfo).collect(Collectors.toList());
            //根据modelInfo 查询到所有的bussinessid

            //流程实例列表
            List<Long> businessIdListByTemplate = flowBomSheetInter.findBusinessIdListByTemplate(modelInfoList);
            List<String> collect = businessIdListByTemplate.stream().map(longData -> longData + "").collect(Collectors.toList());

            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.bussinessId.in(collect));
            } else {
                predicate = qFlowCreateEntity.bussinessId.in(collect);
            }
        }


        //根据角色校验系统管理员显示全部数据，其它角色只显示他自己的数据

        List<String> sysRoleCodes = sessionUtils.getSysRoleCodes();
        //系统管理查询全部数据
        List<String> adminList = sysRoleCodes.stream().filter(code -> code.equals("admin")).collect(Collectors.toList());
        //分系统设计师、单机设计师 只查询自己的数据
        if (adminList.size() == 0) {
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.userId.in(Arrays.asList(sessionUtils.getEmp().getId())));
            } else {
                predicate = qFlowCreateEntity.userId.in(Arrays.asList(sessionUtils.getEmp().getId()));
            }
        }
        //排序
        NumberExpression<Integer> otherwise = getSortExpression(qFlowCreateEntity);

        /**
         * 构造查询条件-结束
         */

        Pageable pageable = PageRequest.of(startFlowListRequest.getPageNum() - 1, startFlowListRequest.getPageSize());
        QueryResults<FlowCreateEntity> flowCreateQueryResults = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .orderBy(otherwise.asc()).orderBy(qFlowCreateEntity.bussinessId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        List<FlowCreateEntity> list = flowCreateQueryResults.getResults();
        if (null == list) {
            list = new ArrayList<>();
        }

        List<StartFlowListResponse> responseList = new ArrayList<>();
        list.stream().forEach(source -> {
            StartFlowListResponse response = new StartFlowListResponse();
            BeanUtils.copyProperties(source, response);

            //设置用户名称
            String userName = meritCloudUtil.getUserNameByUserId(response.getUserId());
            response.setUserId(userName);

            //这里设置流程状态
            response.setFlowState(FlowStateEnum.getValueByCode(response.getFlowState()));

            if (StrUtil.isBlank(response.getTemplate())){
                response.setTemplate(source.getTemplateName());
            }
            if (StrUtil.isBlank(response.getBom())){
                response.setBom(source.getBomName());
            }

            responseList.add(response);
        });
        flowBomSheetInter.setBomAndTemplate(responseList);

        /**
         * 在这里处理节点的流程状态
         */
        GridView<StartFlowListResponse> startFlowListResponseGridView = new GridView<>(responseList,
                flowCreateQueryResults.getTotal(), startFlowListRequest.getPageSize(), startFlowListRequest.getPageNum());
        return startFlowListResponseGridView;
    }

    private NumberExpression<Integer> getSortExpression(QFlowCreateEntity qFlowCreateEntity) {

        NumberExpression<Integer> otherwise;

        otherwise = new CaseBuilder().when(qFlowCreateEntity.flowState.eq(FlowStateEnum.DRAFT.getCode())).then(1)
                    .when(qFlowCreateEntity.flowState.eq(FlowStateEnum.SENDBACK.getCode())).then(2)
                    .when(qFlowCreateEntity.flowState.eq(FlowStateEnum.COUNTERSIGNER.getCode())).then(3)
                    .when(qFlowCreateEntity.flowState.eq(FlowStateEnum.REVIEWED.getCode())).then(4)
                    .when(qFlowCreateEntity.flowState.eq(FlowStateEnum.STOPOVER.getCode())).then(5)
                    .otherwise(6);

        return otherwise;
    }

    /**
     * 在流程表里面找最大的流程实例ID
     *
     * @return
     */
    @Override
    public long findMaxBussinessId() {
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        LocalDate localDate = LocalDate.now();
        String localDateValue = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Predicate predicate = qFlowCreateEntity.bussinessId.like(String.format("%s%s", localDateValue, "%"));

        List<FlowCreateEntity> result = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .orderBy(qFlowCreateEntity.bussinessId.desc()).limit(1).fetch();
        if (ObjectUtils.isNotEmpty(result)) {
            return Long.parseLong(result.get(0).getBussinessId()) + 1;
        } else {
            long maxBussinessId = Long.parseLong(localDateValue) * 1000;
            return maxBussinessId;
        }
    }

    /**
     * 保存数据
     *
     * @return
     */
    @Override
    public FlowCreateEntity save(FlowCreateEntity entity) {
//        if (StringUtils.isNotEmpty(entity.getId())) {
//            flowCreateDao.deleteById(entity.getId());
//        }
//        entity.setId(UUID.randomUUID().toString());
        //默认是流程发起状态
        entity.setFlowState(FlowStateEnum.COUNTERSIGNER.getCode());
        FlowCreateEntity saveEntity = flowCreateDao.save(entity);
        return saveEntity;
    }

    public FlowCreateEntity save(FlowCreateEntity entity, String code) {
//        if (StringUtils.isNotEmpty(entity.getId())) {
//            flowCreateDao.deleteById(entity.getId());
//        }
//        entity.setId(UUID.randomUUID().toString());
        //默认是流程发起状态
        entity.setFlowState(code);
        FlowCreateEntity saveEntity = flowCreateDao.save(entity);
        return saveEntity;
    }

    @Override
    public FlowCreateEntity update(FlowCreateEntity entity) {
        FlowCreateEntity saveEntity = flowCreateDao.save(entity);
        return saveEntity;
    }

    @Override
    public List<FlowCreateEntity> saveAllAndFlush(List<FlowCreateEntity> entityList) {
        return flowCreateDao.saveAll(entityList);
    }

    @Override
    public boolean existsById(String id) {
        return flowCreateDao.existsById(id);
    }

    @Override
    public boolean existsByBussinessId(long bussinessId) {
        //bussinessId是一个有序的数据
        return findMaxBussinessId() > bussinessId;
    }

    @Override
    public void deleteByBussinessId(long bussinessId) {
        if (existsByBussinessId(bussinessId)) {
            FlowCreateEntity entity = new FlowCreateEntity();
            entity.setBussinessId(bussinessId + "");
            Example<FlowCreateEntity> one = Example.of(entity);
            FlowCreateEntity flowCreateEntity = flowCreateDao.findOne(one).get();
            deleteById(flowCreateEntity.getId());
        }
    }

    @Override
    public void deleteById(String id) {
        flowCreateDao.deleteById(id);
    }

    /**
     * 创建流程
     *
     * @param startFlowRequest 流程创建请求对象
     * @param userId           用户id
     * @param bussinessId      流程实例id
     */
    @Override
    public void save(StartFlowRequest startFlowRequest, String userId, long bussinessId) {
        FlowCreateEntity createEntity = new FlowCreateEntity();
        createEntity.setBussinessId(bussinessId + "");
        createEntity.setUserId(userId);
        createEntity.setApprovalType(startFlowRequest.getApprovalType());
        createEntity.setFlowTime(CommUtil.getTimestamp());
        createEntity.setBomName(String.join(",",startFlowRequest.getBom()));
        createEntity.setTemplateName(String.join(",",startFlowRequest.getTemplate()));
        save(createEntity);
    }

    @Override
    public void update(StartFlowRequest startFlowRequest, String userId, String bussinessId) {
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        FlowCreateEntity createEntity = jpaQueryFactory.selectFrom(qFlowCreateEntity).from(qFlowCreateEntity)
                .where(qFlowCreateEntity.bussinessId.eq(bussinessId + "")).fetchFirst();
//        createEntity.setBussinessId(bussinessId);
        createEntity.setUserId(userId);
        createEntity.setApprovalType(startFlowRequest.getApprovalType());
        createEntity.setFlowTime(CommUtil.getTimestamp());
        //默认是流程发起状态
        createEntity.setFlowState(FlowStateEnum.COUNTERSIGNER.getCode());
        FlowCreateEntity saveEntity = flowCreateDao.saveAndFlush(createEntity);
    }

    @Override
    public void setFlowOver(String bussinessId) {
        FlowCreateEntity flowCreateEntity = findModelByBussinessId(bussinessId);
        if (null != flowCreateEntity) {
            save(flowCreateEntity, FlowStateEnum.STOPOVER.getCode());
        }
    }


    @Override
    public void setFlowPass(String bussinessId) {
        FlowCreateEntity flowCreateEntity = findModelByBussinessId(bussinessId);
        if (null != flowCreateEntity) {
            flowCreateEntity.setFlowState(FlowStateEnum.REVIEWED.getCode());
            save(flowCreateEntity, FlowStateEnum.REVIEWED.getCode());
        }
    }

    @Override
    public void setFlowDraft(String bussinessId) {
        FlowCreateEntity flowCreateEntity = findModelByBussinessId(bussinessId);
        if (null != flowCreateEntity) {
            flowCreateEntity.setFlowState(FlowStateEnum.DRAFT.getCode());
            save(flowCreateEntity, FlowStateEnum.DRAFT.getCode());
        }
    }
    @Override
    public void setFlowBack(String bussinessId) {
        FlowCreateEntity flowCreateEntity = findModelByBussinessId(bussinessId);
        if (null != flowCreateEntity) {
            flowCreateEntity.setFlowState(FlowStateEnum.SENDBACK.getCode());
            flowCreateDao.saveAndFlush(flowCreateEntity);
        }
    }

    @Override
    public void setFlowTrack(String bussinessId) {
        FlowCreateEntity flowCreateEntity = findModelByBussinessId(bussinessId);
        if (null != flowCreateEntity) {
            flowCreateEntity.setFlowState(FlowStateEnum.COUNTERSIGNER.getCode());
            save(flowCreateEntity);
        }
    }

    @Override
    public FlowCreateEntity findModelByBussinessId(String bussinessId) {

        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        Predicate predicate = null;
        if (StringUtils.isNotEmpty(bussinessId)) {
            predicate = qFlowCreateEntity.bussinessId.eq(bussinessId);
        }

        List<FlowCreateEntity> flowApproveEntityQueryResults = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .fetchResults().getResults();
        if (flowApproveEntityQueryResults.size() > 0) {
            return flowApproveEntityQueryResults.get(0);
        }
        return null;
    }


    @Override
    public ResultBody<List<String>> validateAuthority(StartFlowRequest startFlowRequest, String type) {
        //   /dam-datapacket/api/datapacket/maintain/dataPage

        List<String> message = new ArrayList<>();
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

        List<ModuleManageDto> templeteList = moduleManageService
                .list("1", "1000000", "", "", "");

        ModelDataQueryParamVO param = new ModelDataQueryParamVO();
        param.setPage(1);
        param.setRows(100000);

        flowBomSheetEntityList.forEach(bomSheet -> {
//            List<Map<String, Object>> rows = iMaintainService.dataListManage(bomSheet.getBomName(), bomSheet.getTemplate(), param).getData().getRows();
            //最高版本数据
//            List<Map<String, Object>> maxVersion = rows.stream().filter(mode -> mode.get("S_M_SYS_MAXVERSION").toString().equals("1")).collect(Collectors.toList());
            List<Map<String, Object>> maxVersion = templeteUtil.getMaxVersion(bomSheet.getTemplate(), bomSheet.getBomName());
            List<Map<String, Object>> edit = templeteUtil.getMaxVersionAndEdit(maxVersion);
            //最高版本编辑中数据
//            List<Map<String, Object>> edit = maxVersion.stream().filter(mode -> mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("2")).collect(Collectors.toList());
            switch (type) {
                case "data": {
                    //如果没有查询到编辑中的数据
                    if (edit.size() == 0) {
                        List<ModuleManageDto> collect = templeteList.stream().filter(model -> model.getModelInfo().equals(bomSheet.getTemplate())).collect(Collectors.toList());
                        message.add(String.format("【%s】【%s】无需进行审核，请检查！", bomSheet.getBomName(), collect.get(0).getName()));
                    }
                }
                break;
                default: {
                    //计算密集
                    int number = 0;
                    String grade = StringUtils.isEmpty(sessionUtils.getEmp().getGrade()) ? "0" : sessionUtils.getEmp().getGrade();
                    for (int i = 0; i < edit.size(); i++) {
                        Map<String, Object> map = edit.get(i);
                        String m_sys_secretlev = map.get("S_M_SYS_SECRETLEVEL") == null ? "0" : map.get("S_M_SYS_SECRETLEVEL").toString();
                        //    密集要求
                        //密集是按照数字在排序：人员密集要大于资源密集
                        if (meritdataCloudProperties.getUsePlatformSecret() && (Integer.parseInt(grade)) < (Integer.parseInt(m_sys_secretlev))) {
                            number++;
                        }
                    }
                    if (number > 0) {
                        List<ModuleManageDto> collect = templeteList.stream().filter(model -> model.getModelInfo().equals(bomSheet.getTemplate())).collect(Collectors.toList());
                        message.add(String.format("【%s】【%s】存在未完成的流程，请检查！", bomSheet.getBomName(), collect.get(0).getName(), number));
                    }
                }
            }
        });
        return ResultBody.success(message);
    }


    @Override
    public List<FlowCreateEntity> findEntityNotOver() {
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        Predicate predicate = null;
        //正常终止，异常终止  把一些状态改到了监听中
        List<String> overList = Arrays.asList(FlowStateEnum.COUNTERSIGNER.getCode());
        predicate = qFlowCreateEntity.flowState.in(overList);
        List<FlowCreateEntity> flowApproveEntityQueryResults = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .fetchResults().getResults();
        return flowApproveEntityQueryResults;
    }

    @Override
    public boolean findEntityNotOver(long bussinessId) {

        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        Predicate predicate = null;
        //正常终止，异常终止
        List<String> overList = Arrays.asList(FlowStateEnum.COUNTERSIGNER.getCode(), FlowStateEnum.DRAFT.getCode());
        predicate = qFlowCreateEntity.flowState.in(overList);
        predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.bussinessId.eq(bussinessId + ""));
        List<FlowCreateEntity> flowApproveEntityQueryResults = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .fetchResults().getResults();
        if (flowApproveEntityQueryResults.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ResultBody<List<String>> validateAuthority(Map<String,List<Map<String, Object>>> bomTemMap, String type, List<FlowBomSheetEntity> flowBomSheetEntityList, List<ModuleManageDto> templateList) {
        List<String> message = new ArrayList<>();
        flowBomSheetEntityList.forEach(bomSheet -> {
            List<Map<String, Object>> edit=  bomTemMap.get( bomSheet.getTemplate()+"_"+bomSheet.getBomName());
            switch (type) {
                case "data": {
                    //如果没有查询到编辑中的数据
                    if (edit== null || edit.size() == 0) {
                        List<ModuleManageDto> collect = templateList.stream().filter(model -> model.getModelInfo().equals(bomSheet.getTemplate())).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(collect)){
                            message.add(String.format("【%s】【%s】无需进行审核，请检查！", bomSheet.getBomName(), collect.get(0).getName()));
                        }
                    }
                }
                break;
                default: {
                    //计算密集
                    int number = 0;
                    String grade = StringUtils.isEmpty(sessionUtils.getEmp().getGrade()) ? "0" : sessionUtils.getEmp().getGrade();
                    if (CollectionUtil.isNotEmpty(edit)){

                        for (int i = 0; i < edit.size(); i++) {
                            Map<String, Object> map = edit.get(i);
                            String m_sys_secretlev = map.get("S_M_SYS_SECRETLEVEL") == null ? "0" : map.get("S_M_SYS_SECRETLEVEL").toString();
                            //    密集要求
                            //密集是按照数字在排序：人员密集要大于资源密集
                            if (meritdataCloudProperties.getUsePlatformSecret() && (Integer.parseInt(grade)) < (Integer.parseInt(m_sys_secretlev))) {
                                number++;
                            }
                        }
                        if (number > 0) {
                            List<ModuleManageDto> collect = templateList.stream().filter(model -> model.getModelInfo().equals(bomSheet.getTemplate())).collect(Collectors.toList());
                            if (CollectionUtil.isNotEmpty(collect)){
                                message.add(String.format("【%s】【%s】存在未完成的流程，请检查！", bomSheet.getBomName(), collect.get(0).getName(), number));
                            }
                        }
                    }
                }
            }
        });
        return ResultBody.success(message);
    }

    @Override
    public Map<String, Boolean> findEntityNotOverResult(List<FlowBomSheetEntity> flowBomSheetEntityList) {
        Map<String, Boolean> result = new HashMap<>();
        List<String> businessIds = flowBomSheetEntityList.stream().map(FlowBomSheetEntity::getBussinessId).distinct().map(Object::toString).collect(Collectors.toList());
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        //正常终止，异常终止
        List<String> overList = Arrays.asList(FlowStateEnum.COUNTERSIGNER.getCode(), FlowStateEnum.DRAFT.getCode());
        Predicate predicate = qFlowCreateEntity.flowState.in(overList);
        predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.bussinessId.in(businessIds));
        List<FlowCreateEntity> flowApproveEntityQueryResults = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .fetchResults().getResults();
        businessIds.forEach(id ->{
            boolean isOver = flowApproveEntityQueryResults.stream().anyMatch(item -> item.getBussinessId().equals(id));
                result.put(id,isOver);

        });
        return result;

    }

    @Override
    public void saveDraftData(StartFlowRequest startFlowRequest, String userId, long bussinessId) {
        FlowCreateEntity createEntity = new FlowCreateEntity();
        createEntity.setBussinessId(bussinessId + "");
        createEntity.setUserId(userId);
        createEntity.setApprovalType(startFlowRequest.getApprovalType());
//        createEntity.setId(UUID.randomUUID().toString());
        //默认是流程发起状态
        createEntity.setFlowState(FlowStateEnum.DRAFT.getCode());
        createEntity.setBomName(String.join(",",startFlowRequest.getBom()));
        createEntity.setTemplateName(String.join(",",startFlowRequest.getTemplate()));
        FlowCreateEntity saveEntity = flowCreateDao.saveAndFlush(createEntity);
    }
    @Override
    public void updateDraftData(StartFlowRequest startFlowRequest,long bussinessId) {
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        FlowCreateEntity createEntity = jpaQueryFactory.selectFrom(qFlowCreateEntity).where(qFlowCreateEntity.bussinessId.eq(bussinessId+"")).fetchFirst();
        createEntity.setApprovalType(startFlowRequest.getApprovalType());
        //默认是流程发起状态
        createEntity.setFlowState(FlowStateEnum.DRAFT.getCode());
        createEntity.setBomName(String.join(",",startFlowRequest.getBom()));
        createEntity.setTemplateName(String.join(",",startFlowRequest.getTemplate()));
        FlowCreateEntity saveEntity = flowCreateDao.saveAndFlush(createEntity);
    }


    @Override
    public boolean flowIsOver(String bussinessId) {

        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        Predicate predicate = null;
        //正常终止，异常终止
        List<String> overList = Arrays.asList(FlowStateEnum.REVIEWED.getCode(), FlowStateEnum.STOPOVER.getCode());
        predicate = qFlowCreateEntity.flowState.in(overList);
        predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.bussinessId.eq(bussinessId));
        List<FlowCreateEntity> results = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .fetchResults().getResults();
        return results.size() > 0 ? true : false;
    }

    @Override
    public GridView<VerifyFlowListResponse> findByPage(StartFlowVerifyRequest startFlowListRequest, List<MainFormDTO> bpmList) {

        //第一步：找到所有的流程-我未处理的
        List<String> code = new ArrayList<>();
        code.add(sessionUtils.getEmpCode());
        List<String> bussinesssIdNoList = new ArrayList<>();
        for (int i = 0; i < bpmList.size(); i++) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(bpmList.get(i)));
            log.info(String.format("%s%s", "jsonObject=", jsonObject.toJSONString()));
            if (null != jsonObject.get("flowAuthorIds") && null != jsonObject.get("procDealStatus") &&
                    jsonObject.get("flowAuthorIds").toString().contains(code.get(0))
                    && jsonObject.get("procDealStatus").toString().equals("track")) {
                if (startFlowListRequest.getDealState().equals("all") || startFlowListRequest.getDealState().equals("no")) {
                    bussinesssIdNoList.add(jsonObject.get("businessId").toString());
                }
            }
        }

        //第二部：找到所有的流程-我已处理的
        List<String> bussinesssIdyesList = new ArrayList<>();
        for (int i = 0; i < bpmList.size(); i++) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(bpmList.get(i)));
            if (!(null != jsonObject.get("flowAuthorIds") && null != jsonObject.get("procDealStatus") &&
                    jsonObject.get("flowAuthorIds").toString().contains(code.get(0))
                    && jsonObject.get("procDealStatus").toString().equals("track"))) {

                if (startFlowListRequest.getDealState().equals("all") || startFlowListRequest.getDealState().equals("yes")) {
                    bussinesssIdyesList.add(jsonObject.get("businessId").toString());
                }
            }
        }

        //找到所有的业务id
        List<String> bussinesssIdAllList = new ArrayList<>();
        for (int i = 0; i < bpmList.size(); i++) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(bpmList.get(i)));
            bussinesssIdAllList.add(jsonObject.get("businessId").toString());
        }

        //根据所有的业务id查询到数据
        List<StartFlowListResponse> dataAll = findData(startFlowListRequest, bussinesssIdAllList);
        List<VerifyFlowListResponse> dataAllSort = new ArrayList<>();
        //把未审核的添加到数据列中
        bussinesssIdNoList.forEach(bussinessId -> {
            List<StartFlowListResponse> collect = dataAll.stream().filter(model -> model.getBussinessId().equals(bussinessId)).collect(Collectors.toList());
            if (collect.size() > 0) {
                StartFlowListResponse startFlowListResponse = collect.get(0);
                VerifyFlowListResponse verify = new VerifyFlowListResponse();
                BeanUtils.copyProperties(startFlowListResponse, verify);
                verify.setDealState("no");
                dataAllSort.add(verify);
            }

        });
        //把已审核的添加到数据列中
        bussinesssIdyesList.forEach(bussinessId -> {
            List<StartFlowListResponse> collect = dataAll.stream().filter(model -> model.getBussinessId().equals(bussinessId)).collect(Collectors.toList());
            if (collect.size() > 0) {
                StartFlowListResponse startFlowListResponse = collect.get(0);
                VerifyFlowListResponse verify = new VerifyFlowListResponse();
                BeanUtils.copyProperties(startFlowListResponse, verify);
                verify.setDealState("yes");
                dataAllSort.add(verify);
            }
        });

        List list = new PageUtil(dataAllSort, startFlowListRequest.getPageSize(), startFlowListRequest.getPageNum()).getList();
        GridView<VerifyFlowListResponse> listGridView = new GridView<VerifyFlowListResponse>(list,
                dataAllSort.size(), startFlowListRequest.getPageSize(), startFlowListRequest.getPageNum());
        return listGridView;
    }


    public List<StartFlowListResponse> findData(StartFlowVerifyRequest startFlowListRequest, List<String> bussinesssIdList) {


        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        Predicate predicate = null;
        //我的审批 bussinesssIdList不是空的
        if (null != bussinesssIdList && bussinesssIdList.size() > 0) {
            predicate = qFlowCreateEntity.bussinessId.in(bussinesssIdList);
        } else {
            return new ArrayList<StartFlowListResponse>();
        }

        /**
         * 构造查询条件-开始
         */
        if (StringUtils.isNotEmpty(startFlowListRequest.getBussinessId())) {
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.bussinessId.like(String.format("%s%s%s", "%", startFlowListRequest.getBussinessId(), "%")));
            } else {
                predicate = qFlowCreateEntity.bussinessId.like(String.format("%s%s%s", "%", startFlowListRequest.getBussinessId(), "%"));
            }
        }

        //2表示全部
        if (2 != startFlowListRequest.getApprovalType()) {
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.approvalType.eq(startFlowListRequest.getApprovalType()));
            } else {
                predicate = qFlowCreateEntity.approvalType.eq(startFlowListRequest.getApprovalType());
            }
        }

        if (StringUtils.isNotEmpty(startFlowListRequest.getInitator())) {
            List<String> userIdList = meritCloudUtil.getUserInfoListByUserName(startFlowListRequest.getInitator().trim());
            if (predicate != null && userIdList.size() > 0) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.userId.in(userIdList));
            } else {
                predicate = qFlowCreateEntity.userId.in(userIdList);
            }
        }


        if (StringUtils.isNotEmpty(startFlowListRequest.getTemplate())) {


            List<String> modelInfoList = templeteUtil.getALLDataByTempleteName(startFlowListRequest.getTemplate());

            //流程实例列表
            List<Long> businessIdListByTemplate = flowBomSheetInter.findBusinessIdListByTemplate(modelInfoList);
            List<String> collect = businessIdListByTemplate.stream().map(longData -> longData + "").collect(Collectors.toList());
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.bussinessId.in(collect));
            } else {
                predicate = qFlowCreateEntity.bussinessId.in(collect);
            }
        }
        //根据角色校验系统管理员显示全部数据，其它角色只显示他自己的数据
//        List<String> sysRoleCodes = sessionUtils.getSysRoleCodes();
//        //系统管理查询全部数据
//        List<String> adminList = sysRoleCodes.stream().filter(code -> code.equals("admin")).collect(Collectors.toList());
//        //分系统设计师、单机设计师 只查询自己的数据
//        if (adminList.size() == 0) {
//            if (predicate != null) {
//                predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.userId.in(Arrays.asList(sessionUtils.getEmp().getId())));
//            } else {
//                predicate = qFlowCreateEntity.userId.in(Arrays.asList(sessionUtils.getEmp().getId()));
//            }
//        }
        //排序
        NumberExpression<Integer> otherwise = getSortExpression(qFlowCreateEntity);

        /**
         * 构造查询条件-结束
         */
        QueryResults<FlowCreateEntity> flowCreateQueryResults = jpaQueryFactory.selectFrom(qFlowCreateEntity)
                .where(predicate)
                .orderBy(otherwise.asc()).orderBy(qFlowCreateEntity.bussinessId.desc())
                .fetchResults();
        List<FlowCreateEntity> list = flowCreateQueryResults.getResults();
        if (null == list) {
            list = new ArrayList<>();
        }

        List<StartFlowListResponse> responseList = new ArrayList<>();
        list.stream().forEach(source -> {
            StartFlowListResponse response = new StartFlowListResponse();
            BeanUtils.copyProperties(source, response);

            //设置用户名称
            String userName = meritCloudUtil.getUserNameByUserId(response.getUserId());
            response.setUserId(userName);
            //解决我的审批发次null值问题
            response.setBom(source.getBomName());


            responseList.add(response);
        });
        flowBomSheetInter.setBomAndTemplate(responseList);
        /**
         * 在这里处理节点的流程状态
         */

        return responseList;
    }


}
