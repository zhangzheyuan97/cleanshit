package com.meritdata.dam.datapacket.plan.model.vo;

import java.io.Serializable;

public class ModuleInfoDTO implements Serializable {

    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;

    private String name;

    private String code;

    private String tableName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
