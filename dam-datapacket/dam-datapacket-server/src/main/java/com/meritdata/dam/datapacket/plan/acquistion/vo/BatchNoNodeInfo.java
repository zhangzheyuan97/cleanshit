package com.meritdata.dam.datapacket.plan.acquistion.vo;

import java.io.Serializable;

/**
 * @Author fanpeng
 * @Date 2023/7/17
 * @Describe 按批次号审批需要的参数对象
 */
public class BatchNoNodeInfo implements Serializable {

    /**
     * 第一层 单机-分系统-模块
     */
    private String firstNode;
    /**
     * 类型 单机-分系统-模块-总装直属件
     */
    private String type;
    /**
     * 型号
     */
    private String model;
    /**
     * 发次
     */
    private String issueNo;
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 图号
     */
    private String drawingNo;

    /**
     * 实物号
     */
    private String physicalNo;

    public String getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(String firstNode) {
        this.firstNode = firstNode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getIssueNo() {
        return issueNo;
    }

    public void setIssueNo(String issueNo) {
        this.issueNo = issueNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getDrawingNo() {
        return drawingNo;
    }

    public void setDrawingNo(String drawingNo) {
        this.drawingNo = drawingNo;
    }

    public String getPhysicalNo() {
        return physicalNo;
    }

    public void setPhysicalNo(String physicalNo) {
        this.physicalNo = physicalNo;
    }
}
