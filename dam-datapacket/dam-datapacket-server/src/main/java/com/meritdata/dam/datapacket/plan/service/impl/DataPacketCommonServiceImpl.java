package com.meritdata.dam.datapacket.plan.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.client.IDataProcessClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.service.IDataPacketCommonService;
import com.meritdata.dam.dataprocess.model.bo.Database;
import com.meritdata.dam.dataprocess.model.bo.PreparedSql;
import com.meritdata.dam.dataprocess.model.dto.BatchDataOperationDTO;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.metamanage.ModelVersionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author fanpeng
 * @Date 2023/7/6
 * @Describe 公共service
 */
@Service
@Slf4j
public class DataPacketCommonServiceImpl implements IDataPacketCommonService {

    @Autowired
    private IDatamationsClient datamationsClient;

    @Autowired
    private IDataProcessClient dataProcessClient;

    @Override
    public ResultBody<Boolean> updateBatch(DataOperateDTO dataOperateDTO) {
        ModelVersionDTO modelInfo = getModelInfoById(dataOperateDTO.getModelId());
        //数据源信息
        Database database = JSONObject.parseObject(JSON.toJSONString(modelInfo.getDatabase()), Database.class);
        //主键数据集
        Map<String, String> primaryData = dataOperateDTO.getPrimaryData();
        //数据集
        Map<String, String> data = dataOperateDTO.getData();
        //封装updatesql语句
        String sql = installSql(modelInfo, primaryData, data);
        BatchDataOperationDTO batchDataOperationDTO = new BatchDataOperationDTO();
        //数据库参数
        batchDataOperationDTO.setDatabase(database);
        //sql参数  todo 这里可以传多个sql（数据源相同的情况下）
        List<PreparedSql> preparedSqlList = new ArrayList<>();
        PreparedSql sqlParam = new PreparedSql();
        sqlParam.setSql(sql);
        preparedSqlList.add(sqlParam);
        batchDataOperationDTO.setPreparedSqlList(preparedSqlList);
        return dataProcessClient.batchUpdate(batchDataOperationDTO);
    }

    /**
     * 装载sql
     *
     * @param modelInfo
     * @param primaryData
     * @param data
     * @return
     */
    private String installSql(ModelVersionDTO modelInfo, Map<String, String> primaryData, Map<String, String> data) {
        StringBuilder updateSql = new StringBuilder();
        updateSql.append("UPDATE \"PUBLIC\".\"");
        updateSql.append(modelInfo.getTableName());
        updateSql.append("\" SET ");
        updateSql = new StringBuilder(resolveUpdateData(updateSql, data));
        updateSql.append(" WHERE ");
        return resolvePrimary(updateSql, primaryData);
    }


    private ModelVersionDTO getModelInfoById(String modelId) {
        JSONObject jsonObject = datamationsClient.getModelInfoById(modelId);
        return JSONObject.parseObject(jsonObject.toJSONString(), ModelVersionDTO.class);
    }

    /**
     * 构造where条件
     *
     * @param updateSql
     * @param primaryData
     * @return
     */
    private String resolvePrimary(StringBuilder updateSql, Map<String, String> primaryData) {
        for (String key : primaryData.keySet()) {
            //增加in条件
            if (key.equals("$in")) {
                String s = primaryData.get(key);
                JSONObject jsonObject = JSONObject.parseObject(s);
                for (String keyIn : jsonObject.keySet()) {
                    List object = jsonObject.getObject(keyIn, List.class);
                    updateSql.append("\"");
                    updateSql.append(keyIn);
                    updateSql.append("\"in ");
                    updateSql.append("(");
                    for (int i = 0; i < object.size(); i++) {
                        updateSql.append("'");
                        updateSql.append(object.get(i));
                        if (object.size() - 1 == i) {
                            updateSql.append("'");
                        } else {
                            updateSql.append("',");
                        }
                    }
                    updateSql.append(")");
                    updateSql.append(" AND");
                }
                //增加notin条件
            } else if (key.equals("$notIn")) {
                String s = primaryData.get(key);
                JSONObject jsonObject = JSONObject.parseObject(s);
                for (String keyNotIn : jsonObject.keySet()) {
                    List object = jsonObject.getObject(keyNotIn, List.class);
                    updateSql.append("\"");
                    updateSql.append(keyNotIn);
                    updateSql.append("\"not in ");
                    updateSql.append("(");
                    for (int i = 0; i < object.size(); i++) {
                        updateSql.append("'");
                        updateSql.append(object.get(i));
                        if (object.size() - 1 == i) {
                            updateSql.append("'");
                        } else {
                            updateSql.append("',");
                        }
                    }
                    updateSql.append(")");
                    updateSql.append(" AND");
                }
            } else {
                updateSql.append("\"");
                updateSql.append(key);
                updateSql.append("\"=");
                updateSql.append("'");
                updateSql.append(primaryData.get(key));
                updateSql.append("' AND");
            }
        }
        return updateSql.substring(0, updateSql.lastIndexOf("AND"));
    }

    /**
     * 构造修改数据
     *
     * @param updateSql
     * @param data
     * @return
     */
    private String resolveUpdateData(StringBuilder updateSql, Map<String, String> data) {
        for (String key : data.keySet()) {
            updateSql.append("\"");
            updateSql.append(key);
            updateSql.append("\"=");
            updateSql.append("'");
            updateSql.append(data.get(key));
            updateSql.append("' ,");
        }
        return updateSql.substring(0, updateSql.lastIndexOf(","));
    }
}
