package com.meritdata.dam.datapacket.plan.manage.entity.request;

import com.alibaba.fastjson.JSONObject;
import com.meritdata.dam.datapacket.plan.manage.entity.response.ApproveBatch;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 *
 */
@ApiModel(description = "数据包管理-我的发起-发起审批-请求参数-批次发次")
@Data
@Component
public class StartFlowBatchRequest {

    @ApiModelProperty(value = "审批类型  0:BOM审批；1：表单审批；")
    @Min(value = 0, message = "流程节点值非法")
    @Max(value = 1, message = "流程节点值非法")
    private int ApprovalType;

    @ApiModelProperty(value = "表单", required = true)
    @NotEmpty(message = "表单必填!")
    private List<String> template;

    @ApiModelProperty(value = "发次和批次", required = true)
    private List<String> issueNoOrBatchNo;

    @ApiModelProperty(value = "审批节点", required = true)
    @Valid
    private AppriovaNode AppriovaNode;

    @ApiModelProperty(value = "发次和实物关系", required = true)
    private JSONObject chosenNodeIdsAndBoms;

    /**
     * 按批次号审批需要的条件
     */
    @ApiModelProperty(value = "批次号属性集合", required = true)
    private List<ApproveBatch> batchNoList;
}
