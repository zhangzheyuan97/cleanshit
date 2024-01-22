package com.meritdata.dam.datapacket.plan.service;

import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;

import java.util.List;

/**
 * @Author fanpeng
 * @Date 2023/4/24
 * @Describe 树service
 */
public interface ITreeService {


    /**
     * 根据关键字模糊匹配对应的树结构
     *
     * @param keywords
     * @param treeList
     * @return
     */
    List<TreeDto> getTreeListByKeyWords(String keywords, List<TreeDto> treeList);
}
