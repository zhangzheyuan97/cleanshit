package com.meritdata.dam.datapacket.plan.model.vo;

import java.io.Serializable;

/**
 * 模板管理查询基础类
 */
public class ModuleSearchBaseDto implements Serializable {
    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;
    /**
     * 页数
     */
    private String page;

    /**
     * 行数
     */
    private String rows;

    /**
     * 模版名称
     */
    private String name;

    /**
     * 模版编码
     */
    private String code;

    /**
     * 表名称
     */
    private String tableName;

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

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
