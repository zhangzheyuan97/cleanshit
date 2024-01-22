package com.meritdata.dam.datapacket.plan.manage.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author： hh.ou
 * 时间：2023/6/6
 * @description:
 **/
@Entity
@Table(name = "[TM_INIT_FORM_DATA]")
@Data
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class InitFormEntity {


    /**
     * 流程实例ID
     */
    @Id
    @Column(name = "[ID]", length = 32)
    private String id;

    /**
     * 发起审批或者保存时的表单数据方便前端回显数据
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "[FORM_DATA]")
    private String formString;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormString() {
        return formString;
    }

    public void setFormString(String formString) {
        this.formString = formString;
    }
}
