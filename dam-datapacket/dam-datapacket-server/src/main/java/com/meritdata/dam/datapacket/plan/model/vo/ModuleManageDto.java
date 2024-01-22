package com.meritdata.dam.datapacket.plan.model.vo;

import java.io.Serializable;

/*
模型管理dto
 */
public class ModuleManageDto implements Serializable {
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
     * 是否向上汇总
     */
    private String isPool;

    //模型id
    private String id;

    //模板信息
    private String modelInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getIsPool() {
        return isPool;
    }

    public void setIsPool(String isPool) {
        this.isPool = isPool;
    }

    public String getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(String modelInfo) {
        this.modelInfo = modelInfo;
    }
}
