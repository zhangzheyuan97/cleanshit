package com.meritdata.dam.datapacket.plan.model.dao;

import com.meritdata.dam.datapacket.plan.model.entity.ModulePool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ModulePoolRepository extends JpaRepository<ModulePool, String>, JpaSpecificationExecutor<ModulePool>, QuerydslPredicateExecutor<ModulePool> {
}
