package com.meritdata.dam.datapacket.plan.application.service;

import com.meritdata.cloud.base.entity.Emp;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.acquistion.vo.PackGroupFileVO;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface IDataPackGroupService {
    /**
     * 查询分系统树数据
     * @param userId 当前登陆人empId
     * @return
     */
    List<TreeDto> groupPackTree(String userId);

    /**
     * 组包
     * @param id
     * @param pid
     * @param text
     * @param attributes
     * @param emp
     * @return
     * @throws IOException
     */
    ResultBody groupPack(String id, String pid, String text, String attributes, Emp emp) throws IOException;

    /**
     * 单行下载
     * @param text
     * @param response
     * @return
     */
    ResultBody singleDownload(String text, String id, String attributes, HttpServletResponse response,String groupPackDate) throws ParseException;

    /**
     * 清除缓存
     * @param text
     * @return
     */
    ResultBody clearCache(String text);

    /**
     * 查询组包列表数据
     * @param nodeId
     * @param physicalNo
     * @param packager
     * @param startTime
     * @param endTime
     * @param map
     * @return
     * @throws ParseException
     */
    ResultBody<GridView> dataListGroupPack(String nodeId, String physicalNo, String packager, String startTime, String endTime, Map<String, Object> map) throws ParseException;

//    /**
//     * 根据实物号和模板id查询已经审批结束的数据
//     * @param bom
//     * @param modelId
//     * @return
//     */
//    List<Map<String, Object>> getTableDataListByBomAndModelId(String bom,String modelId);


    /**
     * 校验密集数据
     * @return
     */
    List<String> validSecretData(String id, String pid, String text, String attributes, Emp emp) throws Exception;


    /**
     * 根据字段属性判断是否是数据字典项
     * @param moduleColumnConfig 需要判断的字段属性
     * @param cellVal 数据字典值
     * @return
     */
    String setLookUpValue(ModuleColumnConfig moduleColumnConfig, String cellVal);

    /**
     * 判断是否是文件,并设置包含文件路径的文件集合
     * @param files 已有文件集合
     * @param mapList 数据集合
     * @param moduleColumnConfig 字段属性
     * @param cellVal
     * @return
     */
    String setFiles(List<PackGroupFileVO> files, Map<String, Object> mapList, ModuleColumnConfig moduleColumnConfig, String cellVal);

    /**
     * 获取所有生效的businessId
     * @param batchNoNodeInfo
     * @param modelId
     * @return
     */
    List<Long> getBusinessId(BatchNoNodeInfo batchNoNodeInfo, String modelId);

    public List<PackGroupFileVO> setPackGroupFile(Map<String, Object> mapList, String key);
}

