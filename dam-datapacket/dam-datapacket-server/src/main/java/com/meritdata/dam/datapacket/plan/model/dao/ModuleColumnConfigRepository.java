package com.meritdata.dam.datapacket.plan.model.dao;


import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 模型策划配置
 *
 * @author
 */
public interface ModuleColumnConfigRepository extends JpaRepository<ModuleColumnConfig, String>, JpaSpecificationExecutor<ModuleColumnConfig>, QuerydslPredicateExecutor<ModuleColumnConfig> {
}
