package com.meritdata.dam.datapacket.plan.model.dao;

import com.meritdata.dam.datapacket.plan.model.entity.MatchFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MatchFieldRepository extends JpaRepository<MatchFieldEntity, String>, JpaSpecificationExecutor<MatchFieldEntity>, QuerydslPredicateExecutor<MatchFieldEntity> {
}
