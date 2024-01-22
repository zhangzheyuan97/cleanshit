package com.meritdata.dam.datapacket.plan.manage.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author： lt.liu
 * 时间：2023/3/7
 * @description:
 **/
@ApiModel(description = "分页请求请求")
@Data
@Component
public class PageRequestParam {

    @ApiModelProperty(value = "当前页码")
    private int pageNum;

    @ApiModelProperty(value = "每页数量")
    private int pageSize;
}


