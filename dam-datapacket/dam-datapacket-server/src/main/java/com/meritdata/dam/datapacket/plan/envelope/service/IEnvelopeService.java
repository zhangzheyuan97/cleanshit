package com.meritdata.dam.datapacket.plan.envelope.service;

import com.alibaba.fastjson.JSONArray;
import com.meritdata.cloud.resultmodel.ResultBody;

import java.util.List;
import java.util.Map;

/**
 * @Author fanpeng
 * @Date 2023/5/18
 * @Describe 包络分析接口
 */
public interface IEnvelopeService {

    /**
     * 包络分析
     *
     * @param modelId 模板id
     * @param tableName 表名称
     * @param data 要做包络分析的数据
     * @param sysId 修改才传入
     * @return
     */
    Map<String, String> analysis(String modelId, String tableName, Map<String, String> data,String sysId);


    /**
     * 是否合格判断
     *
     * @param tableName 模型表名
     * @param newData   模型新数据
     * @param oldData   模型旧数据
     * @return
     */
    Boolean analysisConclusion(String tableName, Map<String, String> newData, Map<String, String> oldData);

    /**
     * 是否包络判断
     *
     * @param tableName 模型表名
     * @param newData   模型新数据
     * @param oldData   模型旧数据
     * @return
     */
    Boolean analysisEnvelope(String tableName, Map<String, String> newData, Map<String, String> oldData);

    /**
     * 根据表名修改数据
     *
     * @param modelId     模型id
     * @param primaryData 根据某个字段的某个值来修改
     * @param modifyData  修改的数据
     * @param sysId  要排除不修改的数据（不排除则填""）
     */
    void updateData(String modelId, Map<String, String> primaryData, Map<String, String> modifyData,String sysId);

    /**
     * 获取隐藏的字段
     * @return
     */
    List<String> getHideField();


    /**
     * 导入数据批量包络分析
     * @param modelId 模型id
     * @param tableName 表名
     * @param dataArray 数据集合
     * @return
     */
    JSONArray analysisArray(String modelId, String tableName,JSONArray dataArray);

    /**
     * 四舍五入计算方法
     *
     * @param data 数据
     * @param reg  分隔符
     * @return
     */
    String roundHalfUp(String data, String reg);

}
