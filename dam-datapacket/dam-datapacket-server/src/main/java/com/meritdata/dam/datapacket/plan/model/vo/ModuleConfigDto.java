package com.meritdata.dam.datapacket.plan.model.vo;

import java.io.Serializable;

public class ModuleConfigDto implements Serializable {

    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;

    /**
     * 模板编码
     */
    private String code;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 成包是否包含
     */

    private String isPackage;

    //moduleInfo
    private String modelInfo;

    /**
     * 模板颜色
     **/
    private String modelColor;

    /**
     * 是否允许配置匹配关系
     */
    private String isEnableMatch;

    public String getIsEnableMatch() {
        return isEnableMatch;
    }

    public void setIsEnableMatch(String isEnableMatch) {
        this.isEnableMatch = isEnableMatch;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIsPackage() {
        return isPackage;
    }

    public void setIsPackage(String isPackage) {
        this.isPackage = isPackage;
    }

    public String getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(String modelInfo) {
        this.modelInfo = modelInfo;
    }

    public String getModelColor() {
        return modelColor;
    }

    public void setModelColor(String modelColor) {
        this.modelColor = modelColor;
    }
}
