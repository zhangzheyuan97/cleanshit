package com.meritdata.dam.datapacket.plan.utils.enumutil;

/**
 * 枚举类
 *
 * @author jiang.lei
 */
public enum FlowStructureEnum {

    //分系统
    SUBSYSTEM("subsystem", "分系统"),

    //模块
    MODULE("module", "模块"),

    //单机
    STANDALONE("standalone", "单机"),

    //总装直属件
    DIRECTLYAFFILIATEDPARTS("directlyaffiliatedparts","总装直属件");


    /**
     * 编码
     */
    private String code;

    /**
     * 描述信息
     */
    private String value;

    /**
     * 构造函数
     *
     * @param code  编码
     * @param value 描述信息
     */
    FlowStructureEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取描述信息
     *
     * @return 描述信息
     */
    public String getValue() {
        return value;
    }

}
