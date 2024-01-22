package com.meritdata.dam.datapacket.plan.acquistion.service;


import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.common.enums.VersionStatusEnum;
import com.meritdata.dam.datapacket.plan.manage.entity.client.ModelDataExportParam;
import com.meritdata.dam.datapacket.plan.model.vo.ModelDataExportParamDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.entity.datamanage.*;
import com.meritdata.dam.entity.metamanage.ModelVerFieldDTO;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface IMaintainService {

    /**
     * 数据维护模型树查询
     *
     * @return
     */
    List<TreeDto> maintainTree(String userId);

    ResultBody<GridView> centerDataList(Map<String, String> map);

    ResultBody addModelData(DataOperateDTO param, VersionStatusEnum versionStatus, Map<String, String> mapData);

    ResultBody<GridView> dataListManage(String physicalCode, String modelId, ModelDataQueryParamVO param, String nodeId, String attributes);

    ResultBody<GridView> dataListManage(String physicalCode, String modelId, ModelDataQueryParamVO param, String nodeId);

    ResultBody<GridView> dataListManageByIds(String physicalCode, String modelId, ModelDataQueryParamVO param, List<String> ids);

    ResultBody<GridView> dataListManageByIds(String modelId,ModelDataQueryParamVO param, List<String> ids);

    ResultBody<GridView> listManageByBomIds(List<String> physicalCodes, String modelId, ModelDataQueryParamVO param, List<Object> ids);


    /**
     * 根据ids删除
     *
     * @param dataManageType
     * @param modelDataExportParam
     * @return
     */
    ResultBody deleteModelDatas(String dataManageType, ModelDataExportParamDto modelDataExportParam);

    ResultBody updateModelData(DataOperateDTO param);

    ResultBody<Map<String, Object>> getModelDataByPrimary(DataQueryDTO queryDTO);

    ResultBody reviseModelData(DataOperateDTO param);

    List<Map<String, Object>> queryDataForList(String modelId, ModelDataQueryParamVO param);

    List<Map<String, Object>> getGradeList();

    List<FormGroupVO> getFields(String modelId, boolean createData);

    ResultBody getSysInfoByIds(String[] ids);

    ResultBody fileExist(List<String> ids, String selectId, HttpServletResponse httpServletResponse, long fileSize);

    ResultBody diskspaceUseMessage(List<String> ids, Boolean compression);

    ResultBody chunksDownloadTolocal(List<String> ids, Boolean compression, String selectId);

    ResultBody loaclFileExist(String path);

    void localFileDownload(String path, HttpServletResponse response);

    ResultBody<GridView<Map<String, Object>>> pageRefModelData(DataQueryDTO queryDTO);

    ResultBody getDicValidDataById(String id, String modelId, String fieldId);

    ResultBody<List<ModelVerFieldDTO>> getBaseTemplateField(String modelId);

    /**
     * 修改数据状态接口
     *
     * @param modelDataExportParam
     * @return
     */
    ResultBody effectData(ModelDataExportParam modelDataExportParam);

    /**
     * 数据同步接口
     *
     * @param physicalCode
     * @param modelId
     */
    ResultBody syncData(String physicalCode, String modelId, String attributes);

    List<TreeDto> treeList(List<TreeDto> treeDtoList);

    List<TreeDto> hitPathList(List<TreeDto> treeDtoList, String keywords);

    List<TreeDto> tree(String pid, List<TreeDto> treeDtoList);

    /**
     * jpa无法满足动态表名查询数据公共方法
     *
     * @param tableName
     * @param sqlType
     * @return
     */
    List<?> QuerySql(String tableName, String sqlType, String s);

    /**
     * Quer查询List<Object>转List<Map<String,Object>>公共方法
     *
     * @param qList
     * @param cloList
     * @return
     */
    List<Map<String, String>> ListForMap(List<Object[]> qList, List<Object[]> cloList);

    /**
     * 获取当前发次下的模型是否配置了字典
     *
     * @param modelId
     * @param nodeId
     * @return
     */
    Map<String, String> getLookupByPlan(String modelId, String nodeId);

    /**
     * 获取序号字段名的公共方法
     *
     * @param modelId
     * @param nodeId
     * @return
     */
    String queryClomName(String modelId, String nodeId, String columnName);

    /**
     * 导出所有模板
     *
     * @param request
     * @param res
     * @return
     */
    Boolean exportAllModel(HttpServletRequest request, HttpServletResponse res, String nodeId, String attributes, String treeName, String batchNoNodeInfo);

    /**
     * 获取小于等于当前用户的密级集
     *
     * @return
     */
    String[] getCurrentUserSecret();

    /**
     * excl头导出
     *
     * @param wb
     * @param modelName
     * @param editFields
     * @param ruleList
     */
    void creatExcelHeader(HSSFWorkbook wb, String modelName, List<ModelVerFieldDTO> editFields, List<String> ruleList, JSONObject jsonObject, String treeName, Map<String, List<String>> stringListMap, List<String> lookUpForFegin,  String batchNoNodeInfo);

    /**
     * 打包zip
     *
     * @param response
     * @param byteList
     * @param filename
     */
    void zipFile(HttpServletResponse response, Map<String, byte[]> byteList, String filename);

    /**
     * 去掉所有空格、回车符
     *
     * @param str
     * @return
     */
    String replaceAllBlank(String str);

    /**
     * 获取模型下所有字段名
     *
     * @param nodeId
     * @param moduleCode
     * @param tableName
     * @param modelId
     * @return
     */
    String getAllFiled(String nodeId, String moduleCode, String tableName, String modelId);

    /**
     * 批量导出数据
     *
     * @param request
     * @param res
     * @return
     */
    void exportAllData(HttpServletRequest request, HttpServletResponse res, String physicalCode, String attributes, String treeName);

    /**
     * 获取用户有管理权限的模板中的编辑字段，过滤掉停用的字段
     *
     * @param modelId
     * @return
     * @throws Exception
     */
    List<ModelVerFieldDTO> getModelEditFields(String modelId);

    ModelVerFieldDTO setModelFieldAlias(ModelVerFieldDTO modelVerField);

    String getTextByCodeLookUp(List<TreeModel<Object>> list, String code);

}
