package com.meritdata.dam.datapacket.plan.system.dao;


import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PackageSystemDao  extends JpaRepository<PackageSystemEntity, String>,
        JpaSpecificationExecutor<PackageSystemEntity>, QuerydslPredicateExecutor<PackageSystemEntity>
{

}
