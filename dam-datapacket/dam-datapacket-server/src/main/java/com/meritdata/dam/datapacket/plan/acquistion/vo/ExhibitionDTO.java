package com.meritdata.dam.datapacket.plan.acquistion.vo;


import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * 实物维护类型实体
 *
 * @author weijh
 * @date 2023/2/9 11:32
 * @company 美林数据
 */
@ApiModel(value = "业务传输对象", description = "业务传输对象")
public class ExhibitionDTO implements Serializable {
    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 分类
     */
    private String classIfication;
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
    private String drawingNo;
    /**
     * 名称
     */
    private String name;
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 实物号
     */
    private String physicalNo;

    /**
     * 是否管理到实物标识
     */
    private String isManageObject;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassIfication() {
        return classIfication;
    }

    public void setClassIfication(String classIfication) {
        this.classIfication = classIfication;
    }

    public String getDrawingNo() {
        return drawingNo;
    }

    public void setDrawingNo(String drawingNo) {
        this.drawingNo = drawingNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getIsManageObject() {
        return isManageObject;
    }

    public void setIsManageObject(String isManageObject) {
        this.isManageObject = isManageObject;
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


    @Override
    public String toString() {
        return "ExhibitionDTO{" +
                "classIfication='" + classIfication + '\'' +
                ", secondType='" + secondType + '\'' +
                ", thirdType='" + thirdType + '\'' +
                ", drawingNo='" + drawingNo + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", physicalNo='" + physicalNo + '\'' +
                '}';
    }
}
