package com.meritdata.dam.datapacket.plan.manage.entity.response;

import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowListRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/

@ApiModel(description = "数据包管理-我的发起-列表响应")
@Data
@Component
public class StartFlowListResponse{

    @ApiModelProperty(value = "发起时间")
    private Timestamp flowTime;

    @ApiModelProperty(value = "实体BOM")
    private String bom;

    @ApiModelProperty(value = "审批节点")
    private String appriovaNode;

    @ApiModelProperty(value = "节点处理人")
    private String nodeAppriovaName;

    @ApiModelProperty(value = "流程编码")
    private String bussinessId;

    @ApiModelProperty(value = "发起人",notes = "系统管理员角色显示,分系统设计师和单机设计师不显示")
    private String  userId;;

    @ApiModelProperty(value = "模板名称")
    private String template;

    @ApiModelProperty(value = "审批类型",notes = "0:BOM审批；1：表单审批；")
    private Integer ApprovalType;


    @ApiModelProperty(value = "流程状态", required = true)
    private String flowState;
}
