package com.meritdata.dam.datapacket.plan.manage.entity.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

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
public class ApproveRequrst {
    @ApiModelProperty(value = "实做BOM")
    @NotEmpty(message = "实体BOM必填!")
    private String bom;

    @ApiModelProperty(value = "模板")
    @NotEmpty(message = "模板必填!")
    private String template;


}
