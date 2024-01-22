package com.meritdata.dam.datapacket.plan.utils.enumutil;

/**
 * 枚举测试类
 * @author lt.liu
 */
public enum FlowStepEnum {

    PROOFREADER(1, "校对人"),
    REVIEWED(2, "审核人"),
    COUNTERSIGNER(3, "会签人"),
    APPROVE(4, "批准人");

    /**
     * 编码
     */
    private int code;

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
    FlowStepEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    public int getCode() {
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
    public static FlowStepEnum getEnumExampleByCode(int code) {
        //遍历当前的枚举类
        for (FlowStepEnum enumExample : FlowStepEnum.values()) {
            if (enumExample.getCode() == code) {
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
    public static FlowStepEnum getEnumExampleByValue(String value) {
        //遍历当前的枚举类
        for (FlowStepEnum enumExample : FlowStepEnum.values()) {
            if (value.equals(enumExample.getValue())) {
                return enumExample;
            }
        }
        return null;
    }

    /**
     * 根据编码返回对应的EnumExample的描述信息
     * 如果不存在对应的EnumExample，则返回null
     *
     * @param code 编码
     * @return 描述信息
     */
    public static String getValueByCode(int code) {
        FlowStepEnum enumExample = getEnumExampleByCode(code);
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
    public static Integer getCodeByValue(String value) {
        FlowStepEnum enumExample = getEnumExampleByValue(value);
        if (enumExample != null) {
            return enumExample.getCode();
        } else {
            return null;
        }
    }
}
