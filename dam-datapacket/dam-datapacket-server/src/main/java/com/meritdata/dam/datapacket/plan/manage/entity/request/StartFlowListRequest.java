package com.meritdata.dam.datapacket.plan.manage.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;
import java.util.List;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/

@ApiModel(description = "数据包管理-我的发起-列表请求对象")
@Data
@Component
public class StartFlowListRequest extends PageRequestParam{

    @ApiModelProperty(value = "流程编码",required = true)
    @Pattern(regexp = "^\\s*|[0-9]*$",message = "流程编码只能是数字！")
    private String bussinessId;

    @ApiModelProperty(value = "发起人",notes = "系统管理员角色显示,分系统设计师和单机设计师不显示")
    @Pattern(regexp = "^\\s*|[\u4e00-\u9fa5a-zA-Z0-9]*$",message = "发起人只能是汉字、数字、英文字母！")
    private String initator;

    @ApiModelProperty(value = "模板名称")
    @Pattern(regexp = "^\\s*|[\u4e00-\u9fa5a-zA-Z0-9]*$",message = "模板名称 只能是汉字、数字、英文字母！")
    private String template;

    @ApiModelProperty(value = "审批类型",notes = "0:BOM审批；1：表单审批；2：全部")
    @Max(value = 2,message = "审批类型参数无效！")
    @Min(value = 0,message = "审批类型参数无效！")
    private int ApprovalType=2;

    @ApiModelProperty(value = "流程状态 draft=草稿，pass=已通过，processing=处理中，stop=已终止", name = "flowState",notes = "draft=草稿，pass=已通过，processing=处理中，stop=已终止")
    @Pattern(regexp = "^\\s*|draft|pass|track|stop|back$",message = "流程状态不合法！")
    private String flowState;

    @ApiModelProperty(value = "流程状态 approves=未处理  all=全部流程状态")
    @Pattern(regexp = "^all|approves$",message = "流程状态不合法！")
    private String inferfaceState;

}
