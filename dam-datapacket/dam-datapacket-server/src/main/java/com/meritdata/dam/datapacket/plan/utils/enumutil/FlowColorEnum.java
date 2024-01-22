package com.meritdata.dam.datapacket.plan.utils.enumutil;

import java.util.HashMap;

/**
 * 枚举测试类
 *
 * @author lt.liu
 */
public enum FlowColorEnum {

    //生效为绿色
    YELLOW("yellow", "黄色"),
    //编辑者红色
    RED("red", "红色"),
    //黄色
    GREE("gree", "绿色");


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
    FlowColorEnum(String code, String value) {
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

    /**
     * 根据编码返回对应的EnumExample
     * 如果不存在对应的EnumExample，则返回null
     *
     * @param code 编码
     * @return EnumExample
     */
    public static FlowColorEnum getEnumExampleByCode(String code) {
        //遍历当前的枚举类
        for (FlowColorEnum enumExample : FlowColorEnum.values()) {
            if (enumExample.getCode().equals(code)) {
                return enumExample;
            }
        }
        return null;
    }

    /**
     * 根据描述信息返回对应的EnumExample
     * 如果不存在对应的EnumExample，则返回null
     *
     * @param value 描述信息
     * @return EnumExample
     */
    public static FlowColorEnum getEnumExampleByValue(String value) {
        //遍历当前的枚举类
        for (FlowColorEnum enumExample : FlowColorEnum.values()) {
            if (value.equals(enumExample.getValue())) {
                return enumExample;
            }
        }
        return null;
    }

    public static HashMap<String,String> getEnumValueAndCode() {
        //遍历当前的枚举类
        HashMap<String,String> map=new HashMap<>();
        for (FlowColorEnum enumExample : FlowColorEnum.values()) {
            map.put(enumExample.getCode(),enumExample.getValue());
        }
        return map;
    }




    /**
     * 根据编码返回对应的EnumExample的描述信息
     * 如果不存在对应的EnumExample，则返回null
     *
     * @param code 编码
     * @return 描述信息
     */
    public static String getValueByCode(String code) {
        FlowColorEnum enumExample = getEnumExampleByCode(code);
        if (enumExample != null) {
            return enumExample.getValue();
        } else {
            return null;
        }
    }

    /**
     * 根据描述信息返回对应的EnumExample的编码
     * 如果不存在对应的EnumExample，则返回null
     *
     * @param value 描述信息
     * @return 编码
     */
    public static String getCodeByValue(String value) {
        FlowColorEnum enumExample = getEnumExampleByValue(value);
        if (enumExample != null) {
            return enumExample.getCode();
        } else {
            return null;
        }
    }
}
