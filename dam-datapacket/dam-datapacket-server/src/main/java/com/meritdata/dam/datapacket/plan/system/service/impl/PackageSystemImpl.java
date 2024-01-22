package com.meritdata.dam.datapacket.plan.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.system.dao.PackageSystemDao;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.entity.QPackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.PackageSystemInter;
import com.meritdata.dam.datapacket.plan.utils.CommUtil;
import com.meritdata.dam.datapacket.plan.utils.MeritCloudUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PackageSystemImpl implements PackageSystemInter {

    @Autowired
    PackageSystemDao dao;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;


    @Autowired
    MeritCloudUtil meritCloudUtil;

    @Override
    public void save(PackageSystemEntity entity) {

        QPackageSystemEntity qPackageSystemEntity = QPackageSystemEntity.packageSystemEntity;
        Predicate predicate = null;
        predicate = qPackageSystemEntity.systemId.eq(entity.getSystemId());
        predicate = ExpressionUtils.and(predicate, qPackageSystemEntity.type.eq(entity.getType()));

        List<PackageSystemEntity> flowCreateQueryResults = jpaQueryFactory.
                selectFrom(qPackageSystemEntity)
                .where(predicate)
                .fetchResults().getResults();
        dao.deleteAll(flowCreateQueryResults);

        if (!StringUtils.isEmpty(entity.getResourceId())) {
            entity.setId(CommUtil.getUUID());
            dao.saveAndFlush(entity);
        }
    }

    @Override
    public void saveList(List<PackageSystemEntity> entity) {


        //第一步：先删除数据
        entity.forEach(model -> {
            QPackageSystemEntity qPackageSystemEntity = QPackageSystemEntity.packageSystemEntity;
            Predicate predicate = null;
            predicate = qPackageSystemEntity.systemId.eq(model.getSystemId());
            predicate = ExpressionUtils.and(predicate, qPackageSystemEntity.type.eq(model.getType()));

            List<PackageSystemEntity> flowCreateQueryResults = jpaQueryFactory.
                    selectFrom(qPackageSystemEntity)
                    .where(predicate)
                    .fetchResults().getResults();
            dao.deleteAll(flowCreateQueryResults);
        });

        //第二步：新录入数据
        for (int i = 0; i < entity.size(); i++) {
            if (StringUtils.isEmpty(entity.get(i).getResourceId())) {
                entity.remove(i);
                i--;
            } else {
                PackageSystemEntity packageSystemEntity = entity.get(i);
                packageSystemEntity.setId(CommUtil.getUUID());
                entity.set(i, packageSystemEntity);
            }
        }
        //第三步：resourceid为nll，就步入库，是删除操作

        if (entity.size() > 0) {
            dao.saveAllAndFlush(entity);
        }
    }

    @Override
    public List<PackageSystemEntity> findByEntity(PackageSystemEntity entity) {
        QPackageSystemEntity qPackageSystemEntity = QPackageSystemEntity.packageSystemEntity;
        Predicate predicate = null;
        if (StringUtils.isNotEmpty(entity.getId())) {
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qPackageSystemEntity.id.eq(entity.getId()));
            } else {
                predicate = qPackageSystemEntity.id.eq(entity.getId());
            }
        }

        if (StringUtils.isNotEmpty(entity.getResourceId())) {
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qPackageSystemEntity.resourceId.eq(entity.getResourceId()));
            } else {
                predicate = qPackageSystemEntity.resourceId.eq(entity.getResourceId());
            }
        }

        if (StringUtils.isNotEmpty(entity.getSystemId())) {
            if (predicate != null) {
                predicate = ExpressionUtils.and(predicate, qPackageSystemEntity.systemId.eq(entity.getSystemId()));
            } else {
                predicate = qPackageSystemEntity.systemId.eq(entity.getSystemId());
            }
        }

        QueryResults<PackageSystemEntity> results = jpaQueryFactory.selectFrom(qPackageSystemEntity)
                .where(predicate)
                .fetchResults();

        List<PackageSystemEntity> moduleColumnConfigList = results.getResults();
        return moduleColumnConfigList;


    }


    public List<PackageSystemEntity> findListByEntity(String type, List<String> systemIds) {
        QPackageSystemEntity qPackageSystemEntity = QPackageSystemEntity.packageSystemEntity;
        Predicate predicate = null;
        predicate = qPackageSystemEntity.systemId.in(systemIds);
        predicate = ExpressionUtils.and(predicate, qPackageSystemEntity.type.eq(type));
        QueryResults<PackageSystemEntity> results = jpaQueryFactory.selectFrom(qPackageSystemEntity)
                .where(predicate)
                .fetchResults();
        List<PackageSystemEntity> moduleColumnConfigList = results.getResults();
        return moduleColumnConfigList;
    }


    @Override
    public List<PackageSystemEntity> findAuthorityDataByEntity(PackageSystemEntity entity) {
        //TODO:后期这里是要按照权限进行数据的筛选
        switch (entity.getType()) {
            case "person": {
                //查询部门的id
                List<String> allEmptIds = findAllEmptIds();
                List<String> hasEmptIds = meritCloudUtil.getEmpIdsByUserId(allEmptIds, entity.getSystemId());
                List<PackageSystemEntity> entityList = new ArrayList<>();
                List<PackageSystemEntity> deptSystemEntity = findListByEntity("dept", hasEmptIds);
                if (deptSystemEntity.size() > 0) {
                    entityList.addAll(deptSystemEntity);
                }
                //查询角色的id
                List<String> allRoleIds = findAllRoleIds();
                List<String> hasRoleIds = meritCloudUtil.getRoleIdsByUserId(allRoleIds, entity.getSystemId());
                List<PackageSystemEntity> roleSystemEntity = findListByEntity("role", hasRoleIds);
                List<PackageSystemEntity> personSystemEntity = findByEntity(entity);
                if (roleSystemEntity.size() > 0) {
                    entityList.addAll(roleSystemEntity);
                }
                if (personSystemEntity.size() > 0) {
                    entityList.addAll(personSystemEntity);
                }
                return entityList;
            }
            default: {
                return findByEntity(entity);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        dao.deleteById(id);
    }

    @Override
    public void deleteByIds(List<String> ids) {
        dao.deleteAllById(ids);
    }

    public void deleteByParams(List<String> ids) {

    }

    /**
     * 查找所有的组织的id
     *
     * @return
     */
    public List<String> findAllEmptIds() {
        QPackageSystemEntity qPackageSystemEntity = QPackageSystemEntity.packageSystemEntity;
        Predicate predicate = qPackageSystemEntity.type.eq("dept");
        QueryResults<PackageSystemEntity> results = jpaQueryFactory.selectFrom(qPackageSystemEntity)
                .where(predicate)
                .fetchResults();
        List<PackageSystemEntity> moduleColumnConfigList = results.getResults();
        List<String> collect = moduleColumnConfigList.stream().map(PackageSystemEntity::getSystemId).distinct().collect(Collectors.toList());
        return collect;
    }

    public List<String> findAllRoleIds() {
        QPackageSystemEntity qPackageSystemEntity = QPackageSystemEntity.packageSystemEntity;
        Predicate predicate = qPackageSystemEntity.type.eq("role");
        QueryResults<PackageSystemEntity> results = jpaQueryFactory.selectFrom(qPackageSystemEntity)
                .where(predicate)
                .fetchResults();
        List<PackageSystemEntity> moduleColumnConfigList = results.getResults();
        List<String> collect = moduleColumnConfigList.stream().map(PackageSystemEntity::getSystemId).distinct().collect(Collectors.toList());
        return collect;
    }
}
