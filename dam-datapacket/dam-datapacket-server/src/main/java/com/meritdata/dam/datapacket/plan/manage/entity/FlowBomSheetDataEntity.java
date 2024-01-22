package com.meritdata.dam.datapacket.plan.manage.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/
@Entity
@Table(name = "[TM_FLOW_BOM_SHEET_DATA]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@SuperBuilder
@Data
@NoArgsConstructor
@ApiModel(description = "数据包管理-我的发起-模板及表单数据关联关系")
public class FlowBomSheetDataEntity {
    private static final long serialVersionUID = 4209907977490540276L;

    /**
     * ID
     **/
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "[ID]", length = 32)
    @ApiModelProperty(value = "主键ID")
    private String id;

    /**
     * BUSSINESS_ID 流程实例id
     **/
    @Column(name = "[BUSSINESS_ID]", length = 32)
    @ApiModelProperty(value = "流程示例编号")
    private long bussinessId;

    /**
     * 流程bom名称
     **/
    @Column(name = "[BOM_NAME]", length =128)
    @ApiModelProperty(value = "流程BOM名称")
    private String bomName;

    /**
     * 模板名称
     **/
    @Column(name = "[TEMPLATE]", length = 128)
    @ApiModelProperty(value = "模板名称")
    private String template;


    /**
     * 数据
     **/
    @Column(name = "[DATA_ID]", length = 128)
    @ApiModelProperty(value = "数据")
    private String dataId;


    /**
     * 数据
     **/
    @Column(name = "[type]", length = 32)
    @ApiModelProperty(value = "类别")
    private Integer type;


    /**
     * 批次号
     **/
    @Column(name = "[BATCH_NO]", length = 128)
    @ApiModelProperty(value = "批次号")
    private String batchNo;

    /**
     * 发次号
     **/
    @Column(name = "[ISSUE_NO]", length = 128)
    @ApiModelProperty(value = "发次号")
    private String issueNo;

    /**
     * 图号
     **/
    @Column(name = "[DRAWING_NO]", length = 128)
    @ApiModelProperty(value = "图号")
    private String drawingNo;

    /**
     * 型号
     **/
    @Column(name = "[MODEL]", length = 128)
    @ApiModelProperty(value = "型号")
    private String model;
}
