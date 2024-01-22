package com.meritdata.dam.datapacket.plan.model.vo;

import java.io.Serializable;

/**
 * 模板字段对应DTO
 */
public class ModuleVerFieldDto implements Serializable {
    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;
    /**
     * 模板编码
     */
    private String code;
    /**
     * 属性名称
     */
    private String busiName;
    /**
     * 英文名称
     */
    private String fieldName;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 长度
     */
    private Long length;
    /**
     * 排序
     */
    private Integer sortNumber;
    /**
     * 状态，枚举值，1启用0停用
     */
    private Integer status;

    /**
     * 精度
     */
    private Integer definition;

    private String modelFieldId;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModelFieldId() {
        return modelFieldId;
    }

    public void setModelFieldId(String modelFieldId) {
        this.modelFieldId = modelFieldId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBusiName() {
        return busiName;
    }

    public void setBusiName(String busiName) {
        this.busiName = busiName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Integer getSortNumber() {
        return sortNumber;
    }

    public void setSortNumber(Integer sortNumber) {
        this.sortNumber = sortNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDefinition() {
        return definition;
    }

    public void setDefinition(Integer definition) {
        this.definition = definition;
    }
}
