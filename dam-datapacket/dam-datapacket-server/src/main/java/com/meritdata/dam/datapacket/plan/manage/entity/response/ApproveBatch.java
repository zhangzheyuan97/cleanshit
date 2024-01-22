package com.meritdata.dam.datapacket.plan.manage.entity.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Author fanpeng
 * @Date 2023/7/11
 * @Describe 按批次号审批参数对象封装
 */
@ApiModel(description = "数据包管理-按批次号审批-批次对象")
@Data
@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveBatch {

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "型号")
    private String model;

    @ApiModelProperty(value = "发次号")
    private String issueNo;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "图号")
    private String drawingNo;

    @ApiModelProperty(value = "实物号集合")
    private List<String> physicals;

    /**
     * 审批数据
     * key为模型id
     * value为数据集合
     */
    @ApiModelProperty(value = "审批数据")
    private Map<String, List<Map<String, Object>>> approveData;
}
