package com.meritdata.dam.datapacket.plan.system.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;



@Entity
@Table(name = "[TM_PACKAGE_SYSTEM]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "数据包管理-数据包版本-响应对象")
public class PackageSystemEntity {

    /**
     * ID
     **/
    @Id
    @ApiModelProperty(value = "唯一标识")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "[ID]", length = 32)
    private String id;


    @ApiModelProperty(value = "类别  role=角色  person=人员  dept=机构 ")
    @Column(name = "[TYPE]", length = 32)
    private String type;


    @ApiModelProperty(value = "系统的编号")
    @Column(name = "[SYSTEM_ID]", length = 32)
    private String systemId;


    @ApiModelProperty(value = "资源"   )
    @Column(name = "[RESOURCE_ID]", length = 128)
    private String resourceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
