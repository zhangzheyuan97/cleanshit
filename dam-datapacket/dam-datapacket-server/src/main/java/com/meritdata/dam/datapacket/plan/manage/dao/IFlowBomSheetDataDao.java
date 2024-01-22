package com.meritdata.dam.datapacket.plan.manage.dao;

import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetDataEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 流程bom表单
 */

public interface IFlowBomSheetDataDao extends JpaRepository<FlowBomSheetDataEntity, String>,
        JpaSpecificationExecutor<FlowBomSheetDataEntity>, QuerydslPredicateExecutor<FlowBomSheetDataEntity>
       {

}
