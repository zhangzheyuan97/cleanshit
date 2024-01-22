package com.meritdata.dam.datapacket.plan.acquistion.vo;

import java.util.Date;

/**
 * @author merit
 */
public class LityInfoVo {

    private String id;

    /**
     * 型号
     */
    private String modelName;

    /**
     * 发次
     */
    private String lity;

    /**
     * 编号
     */
    private Integer num;

    /**
     * 图号
     */
    private String figure;

    /**
     * 实物号
     */
    private String physicalNo;

    private String batchNo;

    private String FMSysId;

    private String createTime;

    private String expandNum;

    private String isUpdate;

    /**
     * 是否可以编辑,1表示可以编辑，0表示不可编辑
     */
    private String editAbleOrNot;

    public String getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(String isUpdate) {
        this.isUpdate = isUpdate;
    }

    public String getExpandNum() {
        return expandNum;
    }

    public void setExpandNum(String expandNum) {
        this.expandNum = expandNum;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getFMSysId() {
        return FMSysId;
    }

    public void setFMSysId(String FMSysId) {
        this.FMSysId = FMSysId;
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

    public String getFigure() {
        return figure;
    }

    public void setFigure(String figure) {
        this.figure = figure;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getLity() {
        return lity;
    }

    public void setLity(String lity) {
        this.lity = lity;
    }

    public String getEditAbleOrNot() {
        return editAbleOrNot;
    }

    public void setEditAbleOrNot(String editAbleOrNot) {
        this.editAbleOrNot = editAbleOrNot;
    }
}
