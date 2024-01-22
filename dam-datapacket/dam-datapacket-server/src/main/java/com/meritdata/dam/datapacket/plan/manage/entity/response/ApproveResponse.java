package com.meritdata.dam.datapacket.plan.manage.entity.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

/**
 * @author： lt.liu
 * 时间：2023/3/9
 * @description:
 **/

@ApiModel(description = "数据包管理-数据包版本-请求对象")
@Data
@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveResponse {
    @ApiModelProperty(value = "实做BOM")
    private String bom;

    @ApiModelProperty(value = "模板")
    private String template;

    @ApiModelProperty(value = "模板")
    private String color;



}
