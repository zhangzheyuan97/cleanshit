package com.meritdata.dam.datapacket.plan.model.dao;

import com.meritdata.dam.datapacket.plan.model.entity.ModuleCurate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ModuleCurateRepository extends JpaRepository<ModuleCurate, String>, JpaSpecificationExecutor<ModuleCurate>, QuerydslPredicateExecutor<ModuleCurate> {
}
