package com.meritdata.dam.datapacket.plan.manage.dao;

import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * 流程创建
 */

public interface IFlowCreateDao extends JpaRepository<FlowCreateEntity, String>,
        JpaSpecificationExecutor<FlowCreateEntity>, QuerydslPredicateExecutor<FlowCreateEntity>,
        QueryByExampleExecutor<FlowCreateEntity>

{


}
