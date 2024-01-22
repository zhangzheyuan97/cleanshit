package com.meritdata.dam.datapacket.plan.utils.enumutil;

import java.util.HashMap;

/**
 * @Author fanpeng
 * @Date 2023/5/18
 * @Describe 包络结论枚举
 */
public enum ConclusionAndEnvelopeEnum {

    PASS_SUCCESS("1", "合格包络"),
    PASS_FAIL("2", "合格不包络"),
    NOPASS_SUCCESS("3", "不合格包络"),
    NOPASS_FAIL("4", "不合格不包络");


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
    ConclusionAndEnvelopeEnum(String code, String value) {
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
    public static ConclusionAndEnvelopeEnum getEnumExampleByCode(String code) {
        //遍历当前的枚举类
        for (ConclusionAndEnvelopeEnum enumExample : ConclusionAndEnvelopeEnum.values()) {
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
    public static ConclusionAndEnvelopeEnum getEnumExampleByValue(String value) {
        //遍历当前的枚举类
        for (ConclusionAndEnvelopeEnum enumExample : ConclusionAndEnvelopeEnum.values()) {
            if (value.equals(enumExample.getValue())) {
                return enumExample;
            }
        }
        return null;
    }

    public static HashMap<String, String> getEnumValueAndCode() {
        //遍历当前的枚举类
        HashMap<String, String> map = new HashMap<>();
        for (ConclusionAndEnvelopeEnum enumExample : ConclusionAndEnvelopeEnum.values()) {
            map.put(enumExample.getCode(), enumExample.getValue());
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
        ConclusionAndEnvelopeEnum enumExample = getEnumExampleByCode(code);
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
        ConclusionAndEnvelopeEnum enumExample = getEnumExampleByValue(value);
        if (enumExample != null) {
            return enumExample.getCode();
        } else {
            return null;
        }
    }
}
