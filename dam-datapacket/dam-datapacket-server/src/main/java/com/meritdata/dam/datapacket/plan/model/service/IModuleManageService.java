package com.meritdata.dam.datapacket.plan.model.service;


import com.meritdata.dam.datapacket.plan.model.entity.ModulePool;
import com.meritdata.dam.datapacket.plan.model.vo.BaseTypeDTO;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleVerFieldDto;

import java.util.List;

/**
 * 模板管理
 */
public interface IModuleManageService {

    /**
     * 分页模糊查询
     *
     * @param page
     * @param rows
     * @param moduleName
     * @param moduleCode
     * @param tableName
     * @return
     */
    public List<ModuleManageDto> list(String page, String rows, String moduleName, String moduleCode, String tableName);

    /**
     * 根据模板id以及其他字段信息模糊查询
     *
     * @param code
     * @param busiName
     * @param fieldName
     * @param dataType
     * @param length
     * @param sortNumber
     * @param status
     * @param definition
     * @return
     */
    public List<ModuleVerFieldDto> moduleVerFieldInfo(String code, String busiName, String fieldName, String dataType, String length,
                                                      String sortNumber, String status, String definition);

    /**
     * 根据模板id以及其他字段信息模糊查询
     *
     * @param code
     * @return
     */
    public List<BaseTypeDTO> moduleVerFieldDataType(String code);

    /**
     * 根据模板id信息查询模板详情信息
     *
     * @param code
     * @return
     */
    public List<ModuleManageDto> moduleVerFieldById(String code);

    /**
     * 配置页面查询是否向上汇总
     *
     * @param code
     * @return
     */
    public ModulePool moduleIsPoolByCode(String code);

    /**
     * 模板管理配置
     */
    public ModulePool moduleConfig(String code, String isPool);

    List<ModuleManageDto> listInTempleteList(List<String> templateList);
}
