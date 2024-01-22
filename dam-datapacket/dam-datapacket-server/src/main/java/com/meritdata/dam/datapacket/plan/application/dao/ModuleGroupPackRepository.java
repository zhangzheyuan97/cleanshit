package com.meritdata.dam.datapacket.plan.application.dao;

import com.meritdata.dam.datapacket.plan.application.entity.ModuleGroupPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface ModuleGroupPackRepository extends JpaRepository<ModuleGroupPack, String>, JpaSpecificationExecutor<ModuleGroupPack>, QuerydslPredicateExecutor<ModuleGroupPack> {

}
