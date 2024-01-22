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
 * 时间：2023/3/13
 * @description:
 **/


@ApiModel(description = "数据包管理-key-value返回对象")
@Data
@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyValueResponse {
    @ApiModelProperty(value = "流程状态")
    private String key;

    @ApiModelProperty(value = "审核意见")
    private String value;

}
