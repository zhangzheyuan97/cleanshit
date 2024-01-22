package com.meritdata.dam.datapacket.plan.manage.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.meritdata.dam.datapacket.plan.client.IMeritCloudClient;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowApproveDao;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowApproveEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.QFlowApproveEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.request.AppriovaNode;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowApproveInter;
import com.meritdata.dam.datapacket.plan.utils.MeritCloudUtil;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStepEnum;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStepNodeEnum;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/
@Service
@Slf4j
public class FlowApproveService implements IFlowApproveInter {


    @Autowired
    IFlowApproveDao flowApproveDao;

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    @Autowired
    MeritCloudUtil meritCloudUtil;


    @Autowired
    com.meritdata.dam.datapacket.plan.client.IMeritCloudClient imeritCloudClient;


    /**
     * 批量保存
     *
     * @param entityList
     * @return
     */
    @Override
    public List<FlowApproveEntity> saveAllAndFlush(List<FlowApproveEntity> entityList) {
        return flowApproveDao.saveAllAndFlush(entityList);
    }

    @Override
    public void save(AppriovaNode appriovaNode, long bussinessId) {

        List<String> approve = appriovaNode.getApprove();
        List<String> countersigner = appriovaNode.getCountersigner();
        List<String> proofreader = appriovaNode.getProofreader();
        List<String> reviewed = appriovaNode.getReviewed();
        List<String> queryUserIds = new ArrayList<>();
        queryUserIds.addAll(approve);
        queryUserIds.addAll(proofreader);
        queryUserIds.addAll(countersigner);
        queryUserIds.addAll(reviewed);
        // 去重
        queryUserIds = queryUserIds.stream().distinct().collect(Collectors.toList());

        /**
         * 入库：批准人和流程节点关系
         */
        List<FlowApproveEntity> approveList = new ArrayList<>();
        Map<String,JSONObject> userMap = meritCloudUtil.getUserObjectByUserIdList(queryUserIds);
        approve.stream().distinct().forEach(userId -> {

//            JSONObject dept = imeritCloudClient.getEmpById(userId);
//            String userCode = dept.get("code").toString();
            // todo 待优化
//            String userCode = meritCloudUtil.getUserCodeByUserId(userId);
            String userCode = meritCloudUtil.getUserCodeByUserInfo(userMap.get(userId));
            FlowApproveEntity flowApproveEntity = FlowApproveEntity.builder()
                    .bussinessId(bussinessId).id(getUUID()).step(FlowStepEnum.APPROVE.getCode())
                    .userId(userId).userCode(userCode).build();
            approveList.add(flowApproveEntity);
        });
        saveAllAndFlush(approveList);

        /**
         * 入库：校对人和流程节点关系
         */
        List<FlowApproveEntity> proofreaderList = new ArrayList<>();
        proofreader.stream().distinct().forEach(userId -> {

//            String userCode = meritCloudUtil.getUserCodeByUserId(userId);
            String userCode = meritCloudUtil.getUserCodeByUserInfo(userMap.get(userId));
            FlowApproveEntity flowApproveEntity = FlowApproveEntity.builder()
                    .bussinessId(bussinessId).id(getUUID()).step(FlowStepEnum.PROOFREADER.getCode()).userId(userId).userCode(userCode).build();
            proofreaderList.add(flowApproveEntity);
        });
        saveAllAndFlush(proofreaderList);

        /**
         * 入库：会签人和流程节点关系
         */
        List<FlowApproveEntity> countersignerList = new ArrayList<>();
        countersigner.stream().distinct().forEach(userId -> {
//            String userCode = meritCloudUtil.getUserCodeByUserId(userId);
            String userCode = meritCloudUtil.getUserCodeByUserInfo(userMap.get(userId));
            FlowApproveEntity flowApproveEntity = FlowApproveEntity.builder()
                    .bussinessId(bussinessId).id(getUUID()).step(FlowStepEnum.COUNTERSIGNER.getCode()).userId(userId).userCode(userCode).build();
            countersignerList.add(flowApproveEntity);
        });
        saveAllAndFlush(countersignerList);

        /**
         * 入库：审核人和流程节点关系
         */
        List<FlowApproveEntity> reviewedList = new ArrayList<>();
        reviewed.stream().distinct().forEach(userId -> {
//            String userCode = meritCloudUtil.getUserCodeByUserId(userId);
            String userCode = meritCloudUtil.getUserCodeByUserInfo(userMap.get(userId));
            FlowApproveEntity proofreaderEntity = FlowApproveEntity.builder()
                    .bussinessId(bussinessId).id(getUUID()).step(FlowStepEnum.REVIEWED.getCode()).userId(userId).userCode(userCode).build();
            reviewedList.add(proofreaderEntity);
        });
        saveAllAndFlush(reviewedList);
    }

    @Override
    public void delete(Long bussinessId) {
        QFlowApproveEntity qFlowApproveEntity = QFlowApproveEntity.flowApproveEntity;
        // 删除掉所有原来的数据
        Predicate predicate = qFlowApproveEntity.bussinessId.eq(bussinessId);
        long delCount = jpaQueryFactory.delete(qFlowApproveEntity).where(predicate).execute();
    }
    @Override
    public void update(AppriovaNode appriovaNode, Long bussinessId) {
        QFlowApproveEntity qFlowApproveEntity = QFlowApproveEntity.flowApproveEntity;
        // 删除掉所有原来的数据
        Predicate predicate = qFlowApproveEntity.bussinessId.eq(bussinessId);
        long delCount = jpaQueryFactory.delete(qFlowApproveEntity).where(predicate).execute();
        List<String> approve = appriovaNode.getApprove();
        List<String> countersigner = appriovaNode.getCountersigner();
        List<String> proofreader = appriovaNode.getProofreader();
        List<String> reviewed = appriovaNode.getReviewed();
        List<String> queryUserIds = new ArrayList<>();
        queryUserIds.addAll(approve);
        queryUserIds.addAll(proofreader);
        queryUserIds.addAll(countersigner);
        queryUserIds.addAll(reviewed);
        // 去重
        queryUserIds = queryUserIds.stream().distinct().collect(Collectors.toList());

        /**
         * 入库：批准人和流程节点关系
         */
        List<FlowApproveEntity> approveList = new ArrayList<>();
        Map<String,JSONObject> userMap = meritCloudUtil.getUserObjectByUserIdList(queryUserIds);
        approve.forEach(userId -> {


            String userCode = meritCloudUtil.getUserCodeByUserInfo(userMap.get(userId));
            FlowApproveEntity flowApproveEntity = FlowApproveEntity.builder()
                    .bussinessId(bussinessId).id(getUUID()).step(FlowStepEnum.APPROVE.getCode())
                    .userId(userId).userCode(userCode).build();
            approveList.add(flowApproveEntity);
        });
        saveAllAndFlush(approveList);

        /**
         * 入库：校对人和流程节点关系
         */
        List<FlowApproveEntity> proofreaderList = new ArrayList<>();
        proofreader.forEach(userId -> {

            String userCode = meritCloudUtil.getUserCodeByUserInfo(userMap.get(userId));
            FlowApproveEntity flowApproveEntity = FlowApproveEntity.builder()
                    .bussinessId(bussinessId).id(getUUID()).step(FlowStepEnum.PROOFREADER.getCode()).userId(userId).userCode(userCode).build();
            proofreaderList.add(flowApproveEntity);
        });
        saveAllAndFlush(proofreaderList);

        /**
         * 入库：会签人和流程节点关系
         */
        List<FlowApproveEntity> countersignerList = new ArrayList<>();
        countersigner.forEach(userId -> {
            String userCode = meritCloudUtil.getUserCodeByUserInfo(userMap.get(userId));
            FlowApproveEntity flowApproveEntity = FlowApproveEntity.builder()
                    .bussinessId(bussinessId).id(getUUID()).step(FlowStepEnum.COUNTERSIGNER.getCode()).userId(userId).userCode(userCode).build();
            countersignerList.add(flowApproveEntity);
        });
        saveAllAndFlush(countersignerList);

        /**
         * 入库：审核人和流程节点关系
         */
        List<FlowApproveEntity> reviewedList = new ArrayList<>();
        reviewed.forEach(userId -> {
            String userCode = meritCloudUtil.getUserCodeByUserInfo(userMap.get(userId));
            FlowApproveEntity proofreaderEntity = FlowApproveEntity.builder()
                    .bussinessId(bussinessId).id(getUUID()).step(FlowStepEnum.REVIEWED.getCode()).userId(userId).userCode(userCode).build();
            reviewedList.add(proofreaderEntity);
        });
        saveAllAndFlush(reviewedList);
    }

    /**
     * 主键id
     *
     * @return
     */
    @Override
    public String getUUID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public List<String> getCustomUserCode(String businessId, int step) {
        QFlowApproveEntity qFlowApproveEntity = QFlowApproveEntity.flowApproveEntity;
        Predicate predicate = qFlowApproveEntity.bussinessId.eq(Long.parseLong(businessId));
        predicate = ExpressionUtils.and(predicate, qFlowApproveEntity.step.eq(step));
        List<FlowApproveEntity> flowApproveEntityResults = jpaQueryFactory.selectFrom(qFlowApproveEntity)
                .where(predicate)
                .fetchResults().getResults();
        List<String> userCodeList = flowApproveEntityResults.stream().map(FlowApproveEntity::getUserCode).collect(Collectors.toList());
        return userCodeList;
    }

    @Override
    public List<String> getCustomUserId(String businessId, int step) {
        QFlowApproveEntity qFlowApproveEntity = QFlowApproveEntity.flowApproveEntity;
        Predicate predicate = qFlowApproveEntity.bussinessId.eq(Long.parseLong(businessId));
        predicate = ExpressionUtils.and(predicate, qFlowApproveEntity.step.eq(step));
        List<FlowApproveEntity> flowApproveEntityResults = jpaQueryFactory.selectFrom(qFlowApproveEntity)
                .where(predicate)
                .fetchResults().getResults();
        List<String> userCodeList = flowApproveEntityResults.stream().map(FlowApproveEntity::getUserId).collect(Collectors.toList());
        return userCodeList;
    }

    @Override
    public String setNodeAppriovaName(String nodeNames, Long bussinessId) {
        int step = 1;
        Integer codeByValue = FlowStepNodeEnum.getCodeByValue(nodeNames);
        if (null != codeByValue){
            step = codeByValue;
        }else{
            return "";
        }
        //找到对应的用户的id
        List<String> list = getCustomUserId(bussinessId + "", step);

        List<String> name = new ArrayList<>();
        list.forEach(userId -> {
            JSONObject dept = imeritCloudClient.getEmpById(userId);
            if (null != dept) {
                name.add(dept.get("name").toString());
            }
        });
        if (CollectionUtils.isEmpty(name)) {
            return "";
        } else {
            return StringUtils.join(name, ",");
        }
    }

    @Override
    public void deleteById(String id) {
        flowApproveDao.deleteById(id);
    }

    @Override
    public void deleteByBussinessId(long bussinessId) {
        FlowApproveEntity entity = new FlowApproveEntity();
        entity.setBussinessId(bussinessId);
        Example<FlowApproveEntity> one = Example.of(entity);
        QFlowApproveEntity qFlowApproveEntity = QFlowApproveEntity.flowApproveEntity;
        List<FlowApproveEntity> fetch = jpaQueryFactory.selectFrom(qFlowApproveEntity).where(qFlowApproveEntity.bussinessId.eq(bussinessId)).fetch();
        FlowApproveEntity flowCreateEntity = new FlowApproveEntity();
        if (fetch.size() > 0) {
            flowCreateEntity = fetch.get(0);
        }
        if (null != flowCreateEntity) {
            deleteById(flowCreateEntity.getId());
        }
    }

}
