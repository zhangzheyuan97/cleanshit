package com.meritdata.dam.datapacket.plan.application.service;

import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;

import java.util.List;
import java.util.Map;

public interface IDataPackShowService {
    /**
     * 查询模板数据
     * @param map
     * @param attributes
     * @param batchNoNodeInfo
     * @return
     */
    ResultBody<GridView> queryTemplateList(Map map, String attributes, BatchNoNodeInfo batchNoNodeInfo);

    /**
     * 查询右侧列表数据
     * @param physicalCode
     * @param modelId
     * @param param
     * @return
     */
    ResultBody<GridView> queryDataList(String physicalCode, String modelId, ModelDataQueryParamVO param,String tempID,String batchNoNodeInfo);

    /**
     * 红绿灯展示判断逻辑方法
     * @param batchNoNodeInfo
     * @param template
     * @return
     */
    String getColor(BatchNoNodeInfo batchNoNodeInfo, String template);

    /**
     * 校验数据中是否有型号策划中配置的数据字典
     * @param lookupByPlan
     * @param rows
     * @param i
     * @param stringObjectMap
     */
    void checkLookUp(Map<String, String> lookupByPlan, List<Map<String, Object>> rows, int i, Map<String, Object> stringObjectMap);
}
