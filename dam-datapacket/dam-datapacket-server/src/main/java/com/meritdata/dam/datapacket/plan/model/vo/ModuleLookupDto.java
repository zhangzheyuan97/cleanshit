package com.meritdata.dam.datapacket.plan.model.vo;

import java.io.Serializable;

public class ModuleLookupDto implements Serializable {

    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;

    private String code;

    private String name;

    private String checked;

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

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }


}
