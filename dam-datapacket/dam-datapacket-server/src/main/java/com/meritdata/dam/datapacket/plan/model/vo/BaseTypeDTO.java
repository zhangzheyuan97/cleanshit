package com.meritdata.dam.datapacket.plan.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 系统支持数据库类型实体
 *
 * @author weijh
 * @date 2023/2/9 11:32
 * @company 美林数据
 */
@ApiModel(value = "业务传输对象", description = "业务传输对象")
public class BaseTypeDTO implements Serializable {
    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 4242686183378092023L;

    /**
     * 数据源类型编码
     **/
    @ApiModelProperty(value = "编码")
    private String code;

    /**
     * 数据源类型名称
     **/
    @ApiModelProperty(value = "名称")
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DataBaseTypeDTO{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
