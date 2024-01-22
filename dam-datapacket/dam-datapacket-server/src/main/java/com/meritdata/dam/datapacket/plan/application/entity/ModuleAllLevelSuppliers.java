package com.meritdata.dam.datapacket.plan.application.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 全级次供应商清单表信息
 */
@Entity
@Table(name = "[ALL_LEVEL_SUPPLIERS]")
public class ModuleAllLevelSuppliers implements Serializable {
    /**
     * 序列化版本标识
     */
    //private static final long serialVersionUID = 1L;

    /**
     * ID
     **/
    @Id
    @Column(name = "[M_SYS_ID]")
    private String id;

    /**
     * 产品层级
     **/
    @Column(name = "[PRODUCT_HIERARCHY]")
    private String productHierarchy;

    /**
     * 产品统计编码
     **/
    @Column(name = "[PRODUCT_CODE]")
    private String productCode;

    /**
     * 产品名称
     */
    @Column(name = "[PRODUCT_NAME]")
    private String productName;

    /**
     * 类别
     */
    @Column(name = "[CLASSIFICATION]")
    private String classification;

    /**
     * 重要程度
     */
    @Column(name = "[IMPORTANCE]")
    private String importance;

    /**
     * 图号
     */
    @Column(name = "[DRAWING_CODE]")
    private String drawingCode;

    /**
     * 上一级输入要求
     */
    @Column(name = "[REQUIREMENT]")
    private String requirement;

    /**
     * 产品通用规范
     */
    @Column(name = "[PRODUCT_SPECIFICATION]")
    private String productSpecification;

    /**
     * 相关专用规范
     */
    @Column(name = "[SPECIAL_SPECIFICATIONS]")
    private String specialSpecifications;

    /**
     * 外包方
     */
    @Column(name = "[FIRST_PARTY]")
    private String firstParty;

    /**
     * 供应商单位名称
     */
    @Column(name = "[PARTY_B]")
    private String partyB;

    /**
     * 企业统一社会信用代码
     */
    @Column(name = "[UNIFIED_CORPORATE_SOCIAL_CREDIT_CODE]")
    private String unifiedCorporateSocialCreditCode;

    /**
     * 风险要素
     */
    @Column(name = "[RISK_ELEMENTS]")
    private String riskElements;

    /**
     * 供应商次
     */
    @Column(name = "[SUPPLIER]")
    private String supplier;

    /**
     * 特殊说明
     */
    @Column(name = "[SPECIAL_INSTRUCTIONS]")
    private String specialInstructions;


    /**
     * 特殊说明
     */
    @Column(name = "[M_SYS_CREATETIME]")
    private Date createtime;

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }


    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getProductHierarchy() {
        return productHierarchy;
    }

    public void setProductHierarchy(String productHierarchy) {
        this.productHierarchy = productHierarchy;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getDrawingCode() {
        return drawingCode;
    }

    public void setDrawingCode(String drawingCode) {
        this.drawingCode = drawingCode;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getProudctSpecification() {
        return productSpecification;
    }

    public void setProudctSpecification(String proudctSpecification) {
        this.productSpecification = proudctSpecification;
    }

    public String getSpecialSpecifications() {
        return specialSpecifications;
    }

    public void setSpecialSpecifications(String specialSpecifications) {
        this.specialSpecifications = specialSpecifications;
    }

    public String getFirstParty() {
        return firstParty;
    }

    public void setFirstParty(String firstParty) {
        this.firstParty = firstParty;
    }

    public String getPartyB() {
        return partyB;
    }

    public void setPartyB(String partyB) {
        this.partyB = partyB;
    }

    public String getUnifiedCorporateSocialCreditCode() {
        return unifiedCorporateSocialCreditCode;
    }

    public void setUnifiedCorporateSocialCreditCode(String unifiedCorporateSocialCreditCode) {
        this.unifiedCorporateSocialCreditCode = unifiedCorporateSocialCreditCode;
    }

    public String getRiskElements() {
        return riskElements;
    }

    public void setRiskElements(String riskElements) {
        this.riskElements = riskElements;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
}
