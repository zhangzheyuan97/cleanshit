package com.meritdata.dam.datapacket.plan.manage.entity.client;

import io.swagger.annotations.ApiParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 中台的接口对象
 * @author： lt.liu
 * 时间：2023/3/22
 * @description:
 **/
public class ModelDataExportParam implements Serializable {


    /**
     * 模型id
     */
    @ApiParam(name = "模型ID", value = "模型ID", required = true, type = "String")
    private String modelId;

    @ApiParam(name = "参数集合", value = "参数集合", required = true, type = "map")
    private Map<String, Object> param;

    @ApiParam(name = "选中的数据表格中的数据", value = "选中的数据表格中的数据", required = false, type = "list")
    private List<Map<String, Object>> selectGridData;

    @ApiParam(name = "选中的是否为多表模型", value = "选中的是否为多表模型", required = false, type = "boolean")
    private Boolean multiFlag;

    @ApiParam(name = "uuid", value = "uuid", required = false, type = "String")
    private String uuid;

    @ApiParam(name = "类型，管理(dataManage)、管理初始化(dataManageInit)、查看(dataView)",
            value = "类型，管理(dataManage)、管理初始化(dataManageInit)、查看(dataView)", required = false, type = "String")
    private String operType;

    @ApiParam(name = "是否对导出数据进行脱敏处理, 默认false",
            value = "true/false", required = false, type = "boolean")
    private Boolean maskFlag;

    /**
     * 加密属性的别名list，用于数据脱敏时，主键属性脱敏
     */
    @ApiParam(name = "加密属性", value = "加密属性", required = false, type = "list")
    private List<String> encryptField;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

    public List<Map<String, Object>> getSelectGridData() {
        return selectGridData;
    }

    public void setSelectGridData(List<Map<String, Object>> selectGridData) {
        this.selectGridData = selectGridData;
    }

    public Boolean getMultiFlag() {
        return multiFlag;
    }

    public void setMultiFlag(Boolean multiFlag) {
        this.multiFlag = multiFlag;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    public Boolean getMaskFlag() {
        if (maskFlag == null) {
            return Boolean.FALSE;
        }
        return maskFlag;
    }

    public void setMaskFlag(Boolean maskFlag) {
        this.maskFlag = maskFlag;
    }

    public List<String> getEncryptField() {
        return encryptField;
    }

    public void setEncryptField(List<String> encryptField) {
        this.encryptField = encryptField;
    }
}
