package com.meritdata.dam.datapacket.plan.model.service;


import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleCurate;

import java.util.List;
import java.util.Map;


/**
 * 型号策划-模型配置
 */
public interface IModuleInfoConfigService {

    /**
     * 查询模型的具体配置
     *
     * @return
     */
    List<ModuleColumnConfig> getModuleCurate(String nodeId, String moduleCode,  String tableName,String modelId);

    List<ModuleColumnConfig> saveColumnConfig(List<ModuleColumnConfig> columnConfigList);

    ModuleCurate saveModuleCurate(ModuleCurate moduleCurate);

    List<Map<String, Object>> formatMapStringToJson(List<String> fieldByModelInfo);
}
