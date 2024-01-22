package com.meritdata.dam.datapacket.plan.application.entity;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.meritdata.cloud.base.DisableEncrypt;
import com.meritdata.dam.base.model.DamBaseEntity;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * 组包信息表
 */
@Entity
@Table(name = "[TM_MODEL_GROUP_PACK]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@DisableEncrypt
public class ModuleGroupPack extends DamBaseEntity {
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
    @Column(name = "[NODE_ID]")
    private String nodeId;

    /**
     * 父节点id
     **/
    @Column(name = "[PID]")
    private String pid;

    /**
     * 组包时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "[GROUPPACK_DATE]")
    private Date groupPackDate;

    /**
     * 实物号
     */
    @Column(name = "[PHYSICAL_NO]")
    private  String physicalNo;

    /**
     * 组包人
     */
    @Column(name = "[PACKAGER]")
    private  String packager;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Date getGroupPackDate() {
        return groupPackDate;
    }

    public void setGroupPackDate(Date groupPackDate) {
        this.groupPackDate = groupPackDate;
    }

    public String getPhysicalNo() {
        return physicalNo;
    }

    public void setPhysicalNo(String physicalNo) {
        this.physicalNo = physicalNo;
    }

    public String getPackager() {
        return packager;
    }

    public void setPackager(String packager) {
        this.packager = packager;
    }
}
