package com.meritdata.dam.datapacket.plan.manage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/
@Entity
@Table(name = "[TM_FLOW_APPROVE]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowApproveEntity {
    private static final long serialVersionUID = 4209907977490540276L;

    /**
     * ID
     **/
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "[ID]", length = 32)
    private String id;

    /**
     * BUSSINESS_ID 流程实例id
     **/
    @Column(name = "[BUSSINESS_ID]", length = 32)
    private long bussinessId;

    /**
     * 模板编码
     */
    @Column(name = "[CODE]")
    private String code;

    /**
     * 模板编码
     */
    @Column(name = "[USER_ID]")
    private String userId;

    /**
     * 模板编码
     */
    @Column(name = "[USER_CODE]")
    private String userCode;

    /**
     * 审核流程节点
     */
    @Column(name = "[STEP]")
    private Integer step;
}
