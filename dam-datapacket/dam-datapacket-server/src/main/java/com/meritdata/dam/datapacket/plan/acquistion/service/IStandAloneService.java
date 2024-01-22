package com.meritdata.dam.datapacket.plan.acquistion.service;


import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;

import java.util.List;
import java.util.Map;

public interface IStandAloneService {
    /**
     * 根据当前登陆人id获取当前登陆人有权限的树结构
     * @param userId
     * @return
     */
    List<TreeDto> getTree(String userId);

    ResultBody<GridView> getModelList(Map map);

    List<ModuleColumnConfig> getdynamicList(Map map);

    //单机树增加两个节点
    void formatAloneTree(List<TreeDto> mapList);

}
