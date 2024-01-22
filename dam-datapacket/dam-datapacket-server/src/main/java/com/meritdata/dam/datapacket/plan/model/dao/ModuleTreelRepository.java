package com.meritdata.dam.datapacket.plan.model.dao;

import com.meritdata.dam.datapacket.plan.model.entity.ModuleTree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ModuleTreelRepository extends JpaRepository<ModuleTree, String>, JpaSpecificationExecutor<ModuleTree>, QuerydslPredicateExecutor<ModuleTree> {
}
