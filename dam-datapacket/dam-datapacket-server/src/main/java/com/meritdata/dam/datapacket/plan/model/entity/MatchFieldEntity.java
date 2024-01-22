package com.meritdata.dam.datapacket.plan.model.entity;

import com.meritdata.cloud.base.DisableEncrypt;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

/**
 * 设置匹配字段关系实体类
 */
@Entity
@Table(name = "[TM_MATCH_FIELD]")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@DisableEncrypt
public class MatchFieldEntity {

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
     * 产品代号
     **/
    @Column(name = "[PRO_CODE]", length = 2000)
    private String procode;

    /**
     * 检查测试项目
     **/
    @Column(name = "[TESTING_ITEM]", length = 2000)
    private String testitem;

    /**
     * 检查测试要求
     **/
    @Column(name = "[TESTING_REQUIREMENT]", length = 2000)
    private String testrequire;


    /**
     * 所属表名称
     **/
    @Column(name = "[TABLE_NAME]")
    private String tablename;

    /**
     * 所属型号
     **/
    @Column(name = "[MODEL_TREE]")
    private String modeltree;

    /**
     * 备用字段1
     **/
    @Column(name = "[PROP1]")
    private String prop1;

    /**
     * 备用字段2
     **/
    @Column(name = "[PROP2]")
    private String prop2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcode() {
        return procode;
    }

    public void setProcode(String procode) {
        this.procode = procode;
    }

    public String getTestitem() {
        return testitem;
    }

    public void setTestitem(String testitem) {
        this.testitem = testitem;
    }

    public String getTestrequire() {
        return testrequire;
    }

    public void setTestrequire(String testrequire) {
        this.testrequire = testrequire;
    }

    public String getProp1() {
        return prop1;
    }

    public void setProp1(String prop1) {
        this.prop1 = prop1;
    }

    public String getProp2() {
        return prop2;
    }

    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getModeltree() {
        return modeltree;
    }

    public void setModeltree(String modeltree) {
        this.modeltree = modeltree;
    }
}
