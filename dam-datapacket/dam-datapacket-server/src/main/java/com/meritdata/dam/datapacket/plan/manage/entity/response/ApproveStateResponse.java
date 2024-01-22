package com.meritdata.dam.datapacket.plan.manage.entity.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author： lt.liu
 * 时间：2023/3/9
 * @description:
 **/

@ApiModel(description = "数据包管理-我的发起-当前流程的审批状态")
@Data
@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveStateResponse {
    @ApiModelProperty(value = "流程状态")
    private String flowState;

    @ApiModelProperty(value = "审核意见")
    private String content;

    @ApiModelProperty(value = "流程编码")
    private String flowStateCode;

    @ApiModelProperty(value = "当前节点")
    private String currentNodeName;

}
