package com.meritdata.dam.datapacket.plan.model.vo;

public class MatchFieldDTO {


    /**
     * 产品代号
     **/
    private String procode;

    /**
     * 检查测试项目
     **/
    private String testitem;

    /**
     * 检查测试要求
     **/
    private String testrequire;


    /**
     * 所属表名称
     **/
    private String tablename;

    /**
     * 所属型号
     **/
    private String modeltree;

    /**
     * 备用字段1
     **/
    private String prop1;

    /**
     * 备用字段2
     **/
    private String prop2;

    public String getProcode() {
        return procode;
    }

    public void setProcode(String procode) {
        this.procode = procode;
    }

    public String getTestitem() {
        return testitem;
    }

    public void setTestitem(String testitem) {
        this.testitem = testitem;
    }

    public String getTestrequire() {
        return testrequire;
    }

    public void setTestrequire(String testrequire) {
        this.testrequire = testrequire;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getModeltree() {
        return modeltree;
    }

    public void setModeltree(String modeltree) {
        this.modeltree = modeltree;
    }

    public String getProp1() {
        return prop1;
    }

    public void setProp1(String prop1) {
        this.prop1 = prop1;
    }

    public String getProp2() {
        return prop2;
    }

    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }
}
