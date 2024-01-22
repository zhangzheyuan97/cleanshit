package com.meritdata.dam.datapacket.plan.acquistion.service;

import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;

import java.util.List;
import java.util.Map;

public interface ILityMatterService {

    ResultBody addLity(String attributes, String model, String lity);

    ResultBody addPhysical(String attributes, String classIfication, String secondType, String thirdType, String drawingNo, String name, String batchNo, String physicalNo, String isManageObject);

    ResultBody deletedata(List<Map<String, Object>> selectGridData, String tableName);

    List<TreeDto> addTreeNode(String userId,List<TreeDto> treeDtoList);

    List<TreeDto> addlicenseTreeNode(List<TreeDto> treeDtoList);

    ResultBody editLity(String lityShadow,String mSysId, String attributes, String model, String lity);
}
