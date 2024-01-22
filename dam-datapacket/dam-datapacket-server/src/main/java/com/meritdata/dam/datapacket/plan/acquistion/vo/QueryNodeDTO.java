package com.meritdata.dam.datapacket.plan.acquistion.vo;

/**
 * 树节点传递信息
 */
public class QueryNodeDTO {
    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;

    private String firstNode;
    private String secondNode;
    private String thirdlyNode;
    private String fourthlyNode;
    private String fifthNode;
    //是否单机
    private String isAlone;
    //类型二
    private String secondType;
    //类型三
    private String thirdType;

    //单机层级 todo
    private String aloneLevel;

    //1关联到实物，0未关联到实物
    private String linkPhysical;

    public String getLinkPhysical() {
        return linkPhysical;
    }

    public void setLinkPhysical(String linkPhysical) {
        this.linkPhysical = linkPhysical;
    }

    public String getAloneLevel() {
        return aloneLevel;
    }

    public void setAloneLevel(String aloneLevel) {
        this.aloneLevel = aloneLevel;
    }

    public String getIsAlone() {
        return isAlone;
    }

    public void setIsAlone(String isAlone) {
        this.isAlone = isAlone;
    }

    /**
     * 0模块 1分系统  2单机 3总装直属件
     */
    private String nodeType;

    private String nodeLevel;

    /**
     * 用于解决数据采集查询中间列表数据
     */
    private String TempID;

    /**
     * 名称
     */
    private String name;

    private String UUID;

    private String authType;

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeLevel() {
        return nodeLevel;
    }

    public void setNodeLevel(String nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    public String getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(String firstNode) {
        this.firstNode = firstNode;
    }

    public String getSecondNode() {
        return secondNode;
    }

    public void setSecondNode(String secondNode) {
        this.secondNode = secondNode;
    }

    public String getThirdlyNode() {
        return thirdlyNode;
    }

    public void setThirdlyNode(String thirdlyNode) {
        this.thirdlyNode = thirdlyNode;
    }

    public String getFourthlyNode() {
        return fourthlyNode;
    }

    public void setFourthlyNode(String fourthlyNode) {
        this.fourthlyNode = fourthlyNode;
    }

    public String getFifthNode() {
        return fifthNode;
    }

    public void setFifthNode(String fifthNode) {
        this.fifthNode = fifthNode;
    }


    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getTempID() {
        return TempID;
    }

    public void setTempID(String tempID) {
        TempID = tempID;
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
