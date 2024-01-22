package com.meritdata.dam.datapacket.plan.acquistion.vo;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

@ApiModel(value = "图号下的实物传输对象", description = "图号下的实物传输对象")
public class MatterConfigDTO implements Serializable {

    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 序号
     */
    private Integer num;

    /**
     * 类型
     */
    private String type;

    /**
     * 类型二
     */
    private String secondType;
    /**
     * 类型三
     */
    private String thirdType;

    /**
     * 图号
     */
    private String figure;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 实物号
     */
    private String physicalNo;

    /**
     * 是否被引用 0:无引用  1：被引用
     */
    private String isUse;

    /**
     * 型号
     */
    private String model;

    /**
     * 发次
     */
    private String lity;

    private String FMSysId;

    private String uuid;

    private String createTime;

    private String isUpdate;

    public String getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(String isUpdate) {
        this.isUpdate = isUpdate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFMSysId() {
        return FMSysId;
    }

    public void setFMSysId(String FMSysId) {
        this.FMSysId = FMSysId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLity() {
        return lity;
    }

    public void setLity(String lity) {
        this.lity = lity;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFigure() {
        return figure;
    }

    public void setFigure(String figure) {
        this.figure = figure;
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

    public String getIsUse() {
        return isUse;
    }

    public void setIsUse(String isUse) {
        this.isUse = isUse;
    }

    public String getSecondType() {
        return secondType;
    }

    public void setSecondType(String secondType) {
        this.secondType = secondType;
    }

    public String getThirdType() {
        return thirdType;
    }

    public void setThirdType(String thirdType) {
        this.thirdType = thirdType;
    }
}
