package com.meritdata.dam.datapacket.plan.application.vo;


import com.meritdata.dam.datapacket.plan.acquistion.vo.PackGroupFileVO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GroupPackDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String templateName;
    private String code;
    private String tableName;
    private String modelInfo;
    private String classify;
    private String drawingNo;
    private String batchNo;
    private String physicalNo;
    //无权限的提示信息
    private String message;
    private List<Map<String, Object>> tableList;
    private List<PackGroupFileVO> files;
    /**
     * 模型数据中的最大密级
     */
    private String maxSecretLevel;

    public String getMaxSecretLevel() {
        return maxSecretLevel;
    }

    public void setMaxSecretLevel(String maxSecretLevel) {
        this.maxSecretLevel = maxSecretLevel;
    }

    public List<PackGroupFileVO> getFiles() {
        return files;
    }
    public void setFiles(List<PackGroupFileVO> files) {
        this.files = files;
    }
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(String modelInfo) {
        this.modelInfo = modelInfo;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public String getDrawingNo() {
        return drawingNo;
    }

    public void setDrawingNo(String drawingNo) {
        this.drawingNo = drawingNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getPhysicalNo() {
        return physicalNo;
    }

    public void setPhysicalNo(String physicalNo) {
        this.physicalNo = physicalNo;
    }

    public List<Map<String, Object>> getTableList() {
        return tableList;
    }

    public void setTableList(List<Map<String, Object>> tableList) {
        this.tableList = tableList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
