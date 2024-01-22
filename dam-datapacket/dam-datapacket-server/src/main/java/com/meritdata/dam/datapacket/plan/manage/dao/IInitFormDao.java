package com.meritdata.dam.datapacket.plan.manage.dao;

import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.InitFormEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * 流程创建
 */

public interface IInitFormDao extends JpaRepository<InitFormEntity, String>,
        JpaSpecificationExecutor<InitFormEntity>, QuerydslPredicateExecutor<InitFormEntity>,
        QueryByExampleExecutor<InitFormEntity>

{


}
