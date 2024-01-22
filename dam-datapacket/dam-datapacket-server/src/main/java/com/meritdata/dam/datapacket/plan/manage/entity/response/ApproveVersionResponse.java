package com.meritdata.dam.datapacket.plan.manage.entity.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author： lt.liu
 * 时间：2023/3/9
 * @description:
 **/

@ApiModel(description = "数据包管理-数据包版本-响应对象")
@Data
@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveVersionResponse {
    @ApiModelProperty(value = "流程实例id")
    private String businessId;

    @ApiModelProperty(value = "审签时间")
    private Date date;

    @ApiModelProperty(value = "发起人")
    private String start;

    @ApiModelProperty(value = "批准人")
    private String approve;

    @ApiModelProperty(value = "版本")
    private Integer version;

}
