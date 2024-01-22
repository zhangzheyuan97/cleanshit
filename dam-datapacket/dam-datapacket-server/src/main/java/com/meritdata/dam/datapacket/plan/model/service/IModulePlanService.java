package com.meritdata.dam.datapacket.plan.model.service;


import com.meritdata.dam.datapacket.plan.model.entity.MatchFieldEntity;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleCurate;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;

import java.util.List;


/**
 * 型号策划
 */
public interface IModulePlanService {

    /**
     * 数据分类树查询
     *
     * @return
     */
    List<TreeDto> tree(String pid, String userId);

    List<TreeDto> addTreeNode(String userId, List<TreeDto> treeDtoList);

    List<TreeDto> licenseTree(String pid);


    /**
     * nodeId:树节点id
     * <p>
     * 根据树节点查询配置过的模板信息
     */
    List<ModuleCurate> getModuleCurateByNodeId(String nodeId);

    List<ModuleCurate> getModuleCurateByNodeId(String nodeId, String code);

    boolean isEndNode(String nodeId);

    /**
     * 查询当前树节点的当前模型的匹配字段关系数据
     *
     * @param tableName
     * @param modeltree
     * @return
     */
    List<MatchFieldEntity> getMatchList(String tableName, String modeltree);

    /**
     * 查询当前树节点的当前模型的匹配字段关系数据的数量
     * @param Id
     * @return
     */
    long getMatchListCount(String Id);

    /**
     * 新增匹配字段关系数据
     *
     * @param matchFieldEntity
     * @return
     */
    boolean addMatchData(MatchFieldEntity matchFieldEntity);

    /**
     * 删除匹配字段关系数据
     * @param matchFieldEntity
     * @return
     */
    boolean delMatchData(MatchFieldEntity matchFieldEntity);

    /**
     * 获取当前树节点下该模型配置的字典值
     * @param tableName
     * @param nodeId
     * @return
     */
    List<String> getTestItemLookup(String tableName, String nodeId,  String cloumnName);
}
