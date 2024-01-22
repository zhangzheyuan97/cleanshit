package com.meritdata.dam.datapacket.plan.model.entity;

import com.meritdata.cloud.base.DisableEncrypt;
import com.meritdata.dam.base.model.DamBaseEntity;
import com.meritdata.dam.entity.datamanage.FormFieldVO;
import com.meritdata.dam.entity.datamanage.FormGroupVO;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.EAN;

import javax.persistence.*;
import java.util.List;

/**
 * 型号策划配置信息表
 */
@Entity
@Table(name = "[TM_MODEL_COLUMN_CONFIG]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@DisableEncrypt
public class ModuleColumnConfig extends DamBaseEntity {


    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;
    /**
     * ID
     **/
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "[ID]", length = 32)
    private String id;

    /**
     * 模型编码
     **/
    @Column(name = "[CODE]")
    private String code;

    /**
     * 树节点id
     **/
    @Column(name = "[NODE_ID]")
    private String nodeId;

    /**
     * 属性名称
     **/
    @Column(name = "[COLUMN_NAME]")
    private String columnName;

    /**
     * 所需字段
     **/
    @Column(name = "[NEED_COLUMN]")
    private String needColumn;

    /**
     * 是否查询
     **/
    @Column(name = "[IS_SEARCH]")
    private String isSearch;

    /**
     * 数据字典
     **/
    @Column(name = "[LOOK_UP]", length = 2000)
    private String lookup;

    /**
     * 数据字典选中code
     **/
    @Column(name = "[LOOK_UP_CODE]", length = 2000)
    private String lookupCode;

    /**
     * 英文名称
     **/
    @Column(name = "[FIELD_NAME]")
    private String fieldName;

    /**
     * 数据类型
     **/
    @Column(name = "[DATA_TYPE]")
    private String dataType;

    @Column(name = "[LENGTH]")
    private Long length;

    @Column(name = "[MODEL_FIELD_ID]")
    private String modelFieldId;

    @Column(name = "[MODEL_COLUMN_ID]")
    private String modelColumnId;

    /**
     * 排序号
     */
    @Column(name = "[SORT_NUMBER]")
    private Integer sortNumber;

    private FormFieldVO formFieldVO;

    public Integer getSortNumber() {
        return sortNumber;
    }

    public void setSortNumber(Integer sortNumber) {
        this.sortNumber = sortNumber;
    }

    public String getModelColumnId() {
        return modelColumnId;
    }

    public FormFieldVO getFormFieldVO() {
        return formFieldVO;
    }

    public void setFormFieldVO(FormFieldVO formFieldVO) {
        this.formFieldVO = formFieldVO;
    }

    public void setModelColumnId(String modelColumnId) {
        this.modelColumnId = modelColumnId;
    }

    public String getModelFieldId() {
        return modelFieldId;
    }

    public void setModelFieldId(String modelFieldId) {
        this.modelFieldId = modelFieldId;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getLookupCode() {
        return lookupCode;
    }

    public void setLookupCode(String lookupCode) {
        this.lookupCode = lookupCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getNeedColumn() {
        return needColumn;
    }

    public void setNeedColumn(String needColumn) {
        this.needColumn = needColumn;
    }

    public String getIsSearch() {
        return isSearch;
    }

    public void setIsSearch(String isSearch) {
        this.isSearch = isSearch;
    }

    public String getLookup() {
        return lookup;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
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
}
