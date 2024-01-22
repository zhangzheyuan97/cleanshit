package com.meritdata.dam.datapacket.plan.manage.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/
@Entity
@Table(name = "[TM_FLOW_CREATE]")
@Data
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class FlowCreateEntity {

    /**
     * ID
     **/
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "[ID]", length = 32)
    private String id;

    /**
     * 流程实例ID
     */
    @Column(name = "[BUSSINESS_ID]", length = 32)
    private String bussinessId;

    /**
     *  draft:草稿，
     *  pass:已通过，
     *  processing:处理中，
     *  stop:已终止"
     **/
    @Column(name = "[FLOW_STATE]",length = 20)
    private String flowState;


    /**
     * 审批类型
     * 1：bom审批
     * 0：表单审批
     */
    @Column(name = "[APPROVAL_TYPE]",length = 32)
    private Integer approvalType;

    /**
     * 流程发起时间
     */
    @Column(name = "[FLOW_TIME]",length = 32)
    private Timestamp flowTime;


    /**
     * 流程发起人
     */
    @Column(name = "[USER_ID]",length = 32)
    private String userId;

    /**
     * bom名称
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "[BOM_NAME]")
    private String bomName;

    /**
     * 表单名称
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "[TEMPLATE_NAME]")
    private String templateName;
}
