package com.meritdata.dam.datapacket.plan.manage.entity.response;

import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;

/**
 * @author： lt.liu
 * 时间：2023/3/10
 * @description:
 **/
@SuperBuilder
@NoArgsConstructor
@Data
public class FlowBomSheetEntityResponse extends FlowBomSheetEntity {

    @ApiModelProperty(value = "模板名称")
    private String templateName;

    @ApiModelProperty(value = "表名称")
    private String moduleCode;

    /**
     * nodeId
     **/
    @ApiModelProperty(value = "节点id")
    private String nodeId;

    @ApiModelProperty(value = "表名称2")
    private String tableName;

    @ApiModelProperty(value = "模板的    modelinfo")
    private String modelId;

    @ApiModelProperty(value = "是否有权限")
    private Boolean hasAuthority;

    @ApiModelProperty(value = "图号")
    private String drawingNo;

    private String batchNoOrIssueNo;

    private String drawingNoOrModel;

//    moduleCode: product_matching_list
//    nodeId: 0bc9d3ed1e0644fd997fdcc2639de03e
//    tableName: product_matching_list
//    modelId: f88f38c7d5de45d995d7925beb658de0



}
