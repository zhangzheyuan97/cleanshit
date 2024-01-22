package com.meritdata.dam.datapacket.plan.manage.dao;

import com.meritdata.dam.datapacket.plan.manage.entity.FlowApproveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 流程创建
 */
//,
//
public interface IFlowApproveDao extends JpaRepository<FlowApproveEntity, String>,
                                         JpaSpecificationExecutor<FlowApproveEntity>,
        QuerydslPredicateExecutor<FlowApproveEntity> {

}
