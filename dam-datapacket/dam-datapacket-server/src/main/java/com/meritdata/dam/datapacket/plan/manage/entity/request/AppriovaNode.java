package com.meritdata.dam.datapacket.plan.manage.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/
@ApiModel(description = "审批节点")
@Data
@Component
public class AppriovaNode {

    @ApiModelProperty(value = "校对人", required = true)
    @NotEmpty(message = "校对人必填!")
    @Valid
    private List<String> proofreader;

    @ApiModelProperty(value = "审核人", required = true)
    @NotEmpty(message = "审核人必填!")
    @Valid
    private List<String> reviewed;

    @ApiModelProperty(value = "会签人", required = true)
    @NotEmpty(message = "会签人必填!")
    @Valid
    private List<String> countersigner;

    @ApiModelProperty(value = "批准人", required = true)
    @NotEmpty(message = "批准人必填!")
    @Valid
    private List<String> approve;
}
