package com.meritdata.dam.datapacket.plan.manage.entity.request;

import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
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
public class ApproveVersionRequrst {
    @ApiModelProperty(value = "实做BOM")
    @NotEmpty(message = "实体BOM必填!")
    private String bom;

    @ApiModelProperty(value = "模板")
    @NotEmpty(message = "模板必填!")
    private String template;

    @ApiModelProperty(value = "审签时间")
//    @DateTimeFormat(pattern = "yyyy-MM-dd 00:00:0")
//    @JsonFormat(pattern = "yyyy-MM-dd 00:00:00")
    private Date dateStart;

    @ApiModelProperty(value = "审签时间")
//    @DateTimeFormat(pattern = "yyyy-MM-dd 23:59:59")
//    @JsonFormat(pattern = "yyyy-MM-dd 23:59:59")
    private Date dateEnd;


    @ApiModelProperty(value = "发起人")
//    @Pattern(regexp = "^\\s*|[\u4e00-\u9fa5a-zA-Z0-9]*$",message = "发起人只能是汉字、数字、英文字母！")
    private String start;

    @ApiModelProperty(value = "批准人")
//    @Pattern(regexp = "^\\s*|[\u4e00-\u9fa5a-zA-Z0-9]*$",message = "批准人只能是汉字、数字、英文字母！")
    private String approve;


    @ApiModelProperty(value = "页面大小")
    @Min(value = 0,message = "页面大小值无效")
    int pageSize;

    @ApiModelProperty(value = "页码")
    @Min(value = 0,message = "页码值无效")
    int PageNumber;

    @ApiModelProperty(value = "树节点属性")
    private BatchNoNodeInfo batchNoNodeInfo;



}
