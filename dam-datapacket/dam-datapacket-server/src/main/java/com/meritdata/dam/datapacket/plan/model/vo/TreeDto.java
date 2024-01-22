package com.meritdata.dam.datapacket.plan.model.vo;


import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.acquistion.vo.QueryNodeDTO;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleTree;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.List;

public class TreeDto extends ModuleTree implements Serializable {
    /**
     * 序列化版本标识
     */
    private static final long serialVersionUID = 1L;

    //区分分系统/模块或者单机,0为单机，1为分系统
    private QueryNodeDTO attributes;
    //按批次号审批需要的参数封装
    private BatchNoNodeInfo batchNoNodeInfo;

    private List<TreeDto> children;

    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<TreeDto> getChildren() {
        return children;
    }

    public void setChildren(List<TreeDto> children) {
        this.children = children;
    }

    public QueryNodeDTO getAttributes() {
        return attributes;
    }

    public void setAttributes(QueryNodeDTO attributes) {
        this.attributes = attributes;
    }

    public BatchNoNodeInfo getBatchNoNodeInfo() {
        return batchNoNodeInfo;
    }

    public void setBatchNoNodeInfo(BatchNoNodeInfo batchNoNodeInfo) {
        this.batchNoNodeInfo = batchNoNodeInfo;
    }
}
