package com.meritdata.dam.datapacket.plan.model.entity;

import com.meritdata.cloud.base.DisableEncrypt;
import com.meritdata.dam.base.model.DamBaseEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 模型汇总信息表
 */
@Entity
@Table(name = "[TM_MODEL_POOL]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@DisableEncrypt
public class ModulePool extends DamBaseEntity {

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
    @Column(name = "[MODULE_CODE]")
    private String moduleCode;

    /**
     * 是否向上汇总，0为否，1为是
     **/
    @Column(name = "[IS_POOL]")
    private String isPool;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getIsPool() {
        return isPool;
    }

    public void setIsPool(String isPool) {
        this.isPool = isPool;
    }
}
