package com.meritdata.dam.datapacket.plan.application.dao;

import com.meritdata.dam.datapacket.plan.application.entity.ModuleAllLevelSuppliers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ModuleAllLevelSuppliersRepository extends JpaRepository<ModuleAllLevelSuppliers, String>, JpaSpecificationExecutor<ModuleAllLevelSuppliers>, QuerydslPredicateExecutor<ModuleAllLevelSuppliers> {
}
