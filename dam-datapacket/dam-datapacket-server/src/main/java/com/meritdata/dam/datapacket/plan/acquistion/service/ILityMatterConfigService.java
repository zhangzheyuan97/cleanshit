package com.meritdata.dam.datapacket.plan.acquistion.service;

import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.vo.MatterConfigDTO;

import java.util.List;

public interface ILityMatterConfigService {

    ResultBody getMatterByMatter(String page, String rows, String matter, String physicalNo, String queryCriteria);

    ResultBody addMatterConfig(List<MatterConfigDTO> matterList,String lity);
}
