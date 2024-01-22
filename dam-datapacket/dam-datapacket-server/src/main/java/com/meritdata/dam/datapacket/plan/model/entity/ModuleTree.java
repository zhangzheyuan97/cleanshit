package com.meritdata.dam.datapacket.plan.model.entity;

import com.meritdata.cloud.base.DisableEncrypt;
import com.meritdata.dam.base.model.DamBaseEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 型号策划左侧树表
 */
@Entity
@Table(name = "[TM_MODEL_TREE]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@DisableEncrypt
public class ModuleTree extends DamBaseEntity {

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
     * 节点名称
     **/
    @Column(name = "[NODE_NAME]")
    private String text;

    /**
     * 父节点id
     **/
    @Column(name = "[PID]")
    private String pid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
