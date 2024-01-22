package com.meritdata.dam.datapacket.plan.system.service;


import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import java.util.List;

public interface PackageSystemInter
{

    /**
     *
     * @param entity
     */
    void save(PackageSystemEntity   entity);


    /**
     *
     * @param entity
     */
    void saveList(List<PackageSystemEntity>   entity);


     List<PackageSystemEntity> findByEntity(PackageSystemEntity   entity);


    /**
     * 按照权限查询所有的数据
     * @param entity
     * @return
     */
    List<PackageSystemEntity> findAuthorityDataByEntity(PackageSystemEntity entity);



     void  deleteById(String id);

    void  deleteByIds(List<String> ids);

}
