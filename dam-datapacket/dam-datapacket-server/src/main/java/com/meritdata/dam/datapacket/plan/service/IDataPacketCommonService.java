package com.meritdata.dam.datapacket.plan.service;


import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;

/**
 * @Author fanpeng
 * @Date 2023/7/06
 * @Describe 公共service
 */
public interface IDataPacketCommonService {

    /**
     * 批量更新数据
     * @param dataOperateDTO
     * @return
     */
    ResultBody<Boolean> updateBatch(DataOperateDTO dataOperateDTO);
}
