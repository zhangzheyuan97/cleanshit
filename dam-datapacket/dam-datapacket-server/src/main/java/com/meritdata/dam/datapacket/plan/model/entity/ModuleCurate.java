package com.meritdata.dam.datapacket.plan.model.entity;

import com.meritdata.cloud.base.DisableEncrypt;
import com.meritdata.dam.base.model.DamBaseEntity;
import org.hibernate.annotations.GenericGenerator;


import javax.persistence.*;


/**
 * 模型配置表
 */
@Entity
@Table(name = "[TM_MODEL_CURATE]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@DisableEncrypt
public class ModuleCurate extends DamBaseEntity {


    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;
    /**
     * ID
     **/
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "[ID]", length = 32)
    private String id;
    /**
     * 模板编码
     **/
    @Column(name = "[CODE]")
    private String code;

    /**
     * 成包是否包含
     **/
    @Column(name = "[IS_PACKAGE]")
    private String isPackage;

    /**
     * 树节点id
     **/
    @Column(name = "[NODE_ID]")
    private String nodeId;

    /**
     * 模板信息
     **/
    @Column(name = "[MODEL_INFO]")
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

    public String getIsPackage() {
        return isPackage;
    }

    public void setIsPackage(String isPackage) {
        this.isPackage = isPackage;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(String modelInfo) {
        this.modelInfo = modelInfo;
    }
}
