package com.meritdata.dam.datapacket.plan.acquistion.service;


import com.meritdata.dam.datapacket.plan.acquistion.vo.ExhibitionDTO;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.utils.PageResult;

import java.util.List;
import java.util.Map;

public interface IExhibitionService {

    /**
     * 数据维护模型树查询
     *
     * @return
     */
    List<TreeDto> exhibitionTree(String userId);

    List<ExhibitionDTO> dataList(Map<String, Object> map);
}
