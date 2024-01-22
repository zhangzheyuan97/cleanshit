package com.meritdata.dam.datapacket.plan.client;

import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.manage.entity.client.ModelDataExportParam;
import com.meritdata.dam.datapacket.plan.model.vo.ModelDataExportParamDto;
import com.meritdata.dam.datapacket.plan.utils.PageResult;
import com.meritdata.cloud.config.CloudFeignConfiguration;
import com.meritdata.dam.entity.datamanage.*;
import com.meritdata.dam.entity.metamanage.ModelVerFieldDTO;
import com.meritdata.dam.entity.metamanage.ModelVersionHiveDTO;
import feign.Response;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 远程调用接口
 */
@FeignClient(value = "dam-datawarehouse", configuration = CloudFeignConfiguration.class, contextId = "IDatawarehouseClient")
public interface IDatamationsClient {
    /**
     * 查询ＥＢＯＭ信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/exhibition-tree", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> exhibitionTree(@RequestParam Map<String, Object> map, @RequestParam String tableName);

    /**
     * 分页查询ＥＢＯＭ信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/getEbomInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getEbomInfo(@RequestParam Map<String, Object> map, @RequestParam String tableName);

    /**
     * 分页查询ＥＢＯＭ信息总数
     */
    @PostMapping(value = "/api/datapacket/exhibition/getEbomInfoCount", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Integer getEbomInfoCount(@RequestParam Map<String, Object> map, @RequestParam String tableName);

    /**
     * 根据分类和图号查询单机表的批次号，和实物号
     */
    @PostMapping(value = "/api/datapacket/exhibition/getPcAndSwInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Map<String, Object> getPcAndSwInfo(@RequestParam Map<String, Object> map, @RequestParam String tableName);

    @PostMapping(value = "/api/datapacket/exhibition/getPcAndSwInfoByPhysicalNo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getPcAndSwInfoByPhysicalNo(@RequestParam Map<String, Object> map, @RequestParam String tableName);


    @PostMapping(value = "/api/datapacket/exhibition/getPcAndSwInfoByDrawing", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getPcAndSwInfoByDrawing(@RequestParam Map<String, Object> map, @RequestParam String tableName);

    /**
     * 根据分类和图号查询单机表的批次号，和实物号
     */
    @PostMapping(value = "/api/datapacket/exhibition/getPcAndSwInfoByBatchNo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getPcAndSwInfoByBatchNo(@RequestParam Map<String, Object> map, @RequestParam String tableName);

    /**
     * 配套清单是否包含该发次
     */
    @PostMapping(value = "/api/datapacket/exhibition/getFcInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    long getFcInfo(@RequestParam String issueNo);

    /**
     * 分页获取发次信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/getFCInfoByMsysId", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Map<String, Object> getFCInfoByMsysId(@RequestParam String msysId);


    /**
     * 分页获取发次信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/getFCInfoList", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    PageResult<Map<String, Object>> getFCInfoList(@RequestParam Map<String, Object> map);

    /**
     * 获取发次信息总数
     */
    @PostMapping(value = "/api/datapacket/exhibition/getFCInfoListCount", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    long getFCInfoListCount(@RequestParam Map<String, Object> map);

    /**
     * 获取发次信息总数
     */
    @PostMapping(value = "/api/datapacket/exhibition/getPhysicalNo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getPhysicalNo(@RequestParam Map<String, Object> map);

    /**
     * 获取发次信息，用于组装树
     */
    @PostMapping(value = "/api/datapacket/exhibition/getFCInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getFCInfo(@RequestParam Map<String, Object> map);

    @PostMapping(value = "/api/datapacket/exhibition/batchNoExist", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Long batchNoExist(@RequestParam String batchNo);

    /**
     * 获取单机表信息，用于组装树
     */
    @PostMapping(value = "/api/datapacket/exhibition/getDJInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getDJInfo(@RequestParam Map<String, Object> map);


    /**
     * 获取列表信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/dataList", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    PageResult<Map<String, Object>> dataList(@RequestParam Map<String, Object> map);

    /**
     * 获取列表信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/dataCount", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Long dataCount(@RequestParam Map<String, Object> map);


    /**
     * 新增发次
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/api/datamanage/data/manage/data/add", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody addLity(@RequestBody DataOperateDTO param, @RequestParam String flag);

    /**
     * 编辑发次
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/api/datamanage/data/manage/data/update", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody editLity(@RequestBody DataOperateDTO param);

    /**
     * 新增单机/分系统数据
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/api/collection/data/add", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody addAloneData(@RequestBody DataOperateDTO param, @RequestParam String flag);


//    /**
//     * 同步单机/分系统数据
//     *
//     * @param
//     * @return
//     */
//    @RequestMapping(value = "/api/collection/data/sync", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
//    ResultBody syncAloneData(@RequestBody DataOperateDTO param, @RequestParam String flag);

    /**
     * 更新模型数据
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/api/datamanage/data/manage/data/update", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody updateLity(@RequestBody DataOperateDTO param);

    /**
     * 删除模型数据
     *
     * @param
     * @return
     */


    @RequestMapping(value = "/api/datamanage/data/manage/deletedata", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody deletedata(@RequestParam String dataManageType, @RequestBody ModelDataExportParamDto modelDataExportParam);

    @RequestMapping(value = "/api/collection/deletedata", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody deletedataByIds(@RequestParam String dataManageType, @RequestBody ModelDataExportParamDto modelDataExportParam);

    @PostMapping(value = "/api/datapacket/exhibition/getModuleInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String getModuleInfo(@RequestParam Map<String, String> map);

    @PostMapping(value = "/api/datapacket/exhibition/getModelVerId", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String getModelVerId(@RequestParam Map<String, String> map);


    /**
     * 获取采集分系统中间列表信息
     */
    @PostMapping(value = "/api/collection/datacollection/rightDataList", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    PageResult<Map<String, Object>> rightDataList(@RequestParam List<String> dataCodeList, @RequestParam String name);


    /**
     * 获取采集分系统中间列表信息
     */
    @PostMapping(value = "/api/collection/exhibition/querySupportingListByBomAndTemplete", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> querySupportingListByBomAndTemplete(@RequestParam Map<String, String> paramMap);


    /**
     * 查询配套清单表信息
     */
    @PostMapping(value = "/api/collection/exhibition/querySupportingList", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> querySupportingList(@RequestParam Map<String, Object> map);

    /**
     * 根据图号查询配套清单表信息
     */
    @PostMapping(value = "/api/collection/exhibition/querySupportingListByBatchNo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> querySupportingListByBatchNo(@RequestParam List<String> figureRemove, @RequestParam String issneno);

    /**
     * @param physicalCode
     * @param modelId
     * @param ids          数据id
     * @param param
     * @return
     */
    @PostMapping(value = "/api/datapacket/exhibition/dataCollectionListManageByIds", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    GridView<Map<String, Object>> dataCollectionListManageByIds(@RequestParam(name = "physicalCode") String physicalCode,
                                                                @RequestParam(name = "modelId") String modelId,
                                                                @RequestParam(name = "ids") List<String> ids,
                                                                @RequestBody ModelDataQueryParamVO param);

    /**
     * @param ids          数据id
     * @param param
     * @return
     */
    @PostMapping(value = "/api/datapacket/exhibition/dataCollectionListManageByDataIds", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    GridView<Map<String, Object>> dataCollectionListManageByDataIds(@RequestParam(name = "modelId") String modelId,
                                                                @RequestParam(name = "param") String param,
                                                                    @RequestBody List<String> ids);

    /**
     * 主要来批量查询
     *
     * @param physicalCodes
     * @param modelId
     * @param ids           数据id
     * @param param
     * @return
     */
    @PostMapping(value = "/api/datapacket/exhibition/dataListManageByIds", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    GridView<Map<String, Object>> dataListManageByIds(@RequestParam(name = "physicalCodes") List<String> physicalCodes,
                                                      @RequestParam(name = "modelId") String modelId,
                                                      @RequestParam(name = "ids") List<String> ids,
                                                      @RequestBody ModelDataQueryParamVO param);

    @PostMapping(value = "/api/datapacket/exhibition/dataCollectionListManage", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    GridView<Map<String, Object>> dataCollectionListManage(@RequestParam String physicalCode, @RequestParam String modelId, @RequestBody ModelDataQueryParamVO param);

    /**
     * 根据modelid获取对应的模型表名
     *
     * @param modelId
     * @return
     */
    @PostMapping(value = "/api/datapacket/exhibition/TableNameByModelId", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String TableNameByModelId(@RequestParam String modelId);

    /**
     * 根据模型id查询模型信息
     *
     * @param modelId
     * @return
     */
    @PostMapping(value = "/api/datapacket/exhibition/getModelInfoById", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    JSONObject getModelInfoById(@RequestParam String modelId);

    @PostMapping(value = "/api/datamanage/data/manage/show/model-info", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResultBody<ModelVersionHiveDTO> showModelInfoByModelId(@RequestParam String modelId);

    @PostMapping(value = "/api/datapacket/exhibition/dataListCollectionManageCount", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Long dataCollectionListManageCount(@RequestParam String physicalCode, @RequestParam String modelId, @RequestBody ModelDataQueryParamVO param);


    @PostMapping(value = "/api/datapacket/exhibition/dataCollectionListManageByEffecive", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    GridView<Map<String, Object>> dataCollectionListManageByEffecive(@RequestParam String physicalCode, @RequestParam String modelId,
                                                                     @RequestBody ModelDataQueryParamVO param,@RequestParam String batchNoNodeInfo);

    @PostMapping(value = "/api/datapacket/exhibition/dataListCollectionManageCountByEffective", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Long dataListCollectionManageCountByEffective(@RequestParam String physicalCode, @RequestParam String modelId,
                                                  @RequestBody ModelDataQueryParamVO param,@RequestParam String batchNoNodeInfo);

    /**
     * 编辑一条模板信息
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/api/datamanage/data/manage/data/update", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody updateModelData(@RequestBody DataOperateDTO param);

    @RequestMapping(value = "/api/collection/exhibition/find-primary", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody<Map<String, Object>> getModelDataByPrimary(DataQueryDTO queryDTO);

    /**
     * 修订功能
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/api/collection/exhibition/revise", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    ResultBody reviseModelData(@RequestBody DataOperateDTO param);

    /**
     * 查询历史数据
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/api/collection/exhibition/queryDataForList", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    List<Map<String, Object>> queryDataForList(@RequestParam String modelId, @RequestBody ModelDataQueryParamVO param);

    /**
     * 获取新增列中的属性
     */
    @RequestMapping(value = "/api/collection/exhibition/field/form", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    List<FormGroupVO> queryFormFields(@RequestParam String modelId, @RequestParam boolean createData);

    @RequestMapping(value = "/api/datamanage/file/manage/search-sys", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    @ApiOperation(value = "根据ID批量查询文件系统属性信息", notes = "根据ID批量查询文件系统属性信息")
    ResultBody getSysInfoByIds(@RequestParam(required = true) String[] ids);

    @PostMapping(value = "/api/datamanage/file/manage/file/exist")
    @ApiOperation(value = "按照ID列表判断文件是否存在", notes = "按照ID列表判断文件是否存在")
    ResultBody fileExist(@ApiParam(value = "文件IDS") @RequestBody List<String> ids);

    @PostMapping(value = "/api/datamanage/file/manage/download/diskspace-use-message", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "文件下载时获取磁盘空间信息", notes = "文件下载时获取磁盘空间信息")
    ResultBody diskspaceUseMessage(@ApiParam(value = "文件IDS") @RequestBody List<String> ids,
                                   @ApiParam(value = "是否打包") @RequestParam(value = "compression", required = false) Boolean compression);

    @PostMapping(value = "/api/datamanage/file/manage/download/chunks-to-local", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResultBody chunksDownloadTolocal(@ApiParam(value = "文件IDS") @RequestBody List<String> ids,
                                     @ApiParam(value = "是否打包") @RequestParam(value = "compression", required = false) Boolean compression,
                                     @ApiParam(value = "选中的分类ID") @RequestParam(value = "selectId", required = false) String selectId);

    @RequestMapping(value = "/api/datamanage/file/manage/local-file-exist")
    ResultBody loaclFileExist(@ApiParam(value = "文件路径") @RequestParam(value = "path") String path);

    @RequestMapping(value = "/api/datamanage/file/manage/download/local-path", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void localFileDownload(@ApiParam(value = "文件路径") @RequestParam(value = "path") String path);

    @RequestMapping(value = "/api/datamanage/data/manage/data/page-ref", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResultBody<GridView<Map<String, Object>>> pageRefModelData(@RequestBody DataQueryDTO queryDTO);

    @RequestMapping(value = "/api/datamanage/data/manage/dic/validData", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResultBody getDicValidDataById(@ApiParam(name = "字典ID", value = "字典ID") @RequestParam(name = "id") String id,
                                   @ApiParam(name = "模型ID", value = "模型ID") @RequestParam(name = "modelId") String modelId,
                                   @ApiParam(name = "模型绑定字段ID", value = "模型绑定字段ID") @RequestParam(name = "fieldId") String fieldId);

    @RequestMapping(value = "/api/datamanage/data/manage/field/base-tpl", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResultBody<List<ModelVerFieldDTO>> getBaseTemplateField(@ApiParam(name = "模型ID", value = "模型ID", required =
            true, type = "String") @RequestParam(name = "modelId") String modelId);


//    /**
//     * 数据状态从编辑中修改为审批中
//     * @param dataIds
//     * @return
//     */
//    @RequestMapping(value = "/api/datamanage/data/manage/effectdata",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    ResultBody changeEditDataToVerify(List<String> dataIds);


    @RequestMapping(value = "/api/collection/effectdata", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultBody effectData(@RequestBody ModelDataExportParam modelDataExportParam);


    /**
     * @param modelInfo
     * @param filed      模糊查询的字段名称
     * @param filedValue 模糊查询的字段值
     * @return
     */
    @PostMapping(value = "/api/collection/dataListManage", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Map<String, Object>> dataListManage(@RequestParam(name = "modelInfo") String modelInfo,
                                                    @RequestParam(name = "filed") String filed,
                                                    @RequestParam(name = "filedValue") String filedValue);


    /**
     * 根据属性默认值配置生成默认值
     *
     * @param valueType 默认值类型
     * @param value     默认值
     * @return
     */
    @RequestMapping(value = "/api/datamanage/data/manage/field/default-value", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "根据属性默认值配置生成默认值", notes = "根据属性默认值配置生成默认值")
    public ResultBody getFieldDefaultVal(
            @RequestParam(name = "valueType") String valueType,
            @RequestParam(name = "value") String value);

    /**
     * 获取分系统表信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/getFXTInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getFXTInfo(@RequestBody ModelDataQueryParamVO param);


    /**
     * 获取单机表信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/getDJInfoSearch", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getDJInfoSearch(@RequestBody ModelDataQueryParamVO param);


    /**
     * 获取配套清单信息
     */
    @PostMapping(value = "/api/datapacket/exhibition/getQDInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> getQDInfo(@RequestBody ModelDataQueryParamVO param);


    /**
     * 根据条件获取已生效数据数量
     */
    @PostMapping(value = "/api/datapacket/exhibition/dataCountByParam", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    long dataCountByParam(@RequestParam String physicalCode, @RequestParam String modelId, @RequestBody ModelDataQueryParamVO param);

    /**
     * 根据文件路径，获取该服务器的文件
     *
     * @param path
     * @return
     */
    @PostMapping(value = "/api/datapacket/exhibition/getFileByPath", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Response getFileByPath(@RequestParam(value = "path") String path);

    /**
     * 根据文件路径删除缓存文件
     *
     * @param path
     * @return
     */
    @PostMapping(value = "/api/datapacket/exhibition/delTempFile", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    boolean delTempFile(@RequestParam(value = "path") String path);


    /**
     * 获取组包数据列表
     */
    @PostMapping(value = "/api/datapacket/exhibition/packetDataList", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    GridView<Map<String, Object>> packetDataList(@RequestParam String physicalCode, @RequestParam String modelId, @RequestBody ModelDataQueryParamVO param);

    /**
     * 获取组包数据列表-不分页
     */
    @PostMapping(value = "/api/datapacket/exhibition/packetDataListAll", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<Map<String, Object>> packetDataListAll(@RequestParam String physicalCode, @RequestParam String modelId, @RequestBody ModelDataQueryParamVO param);

    /**
     * 获取组包数据总数
     */
    @PostMapping(value = "/api/datapacket/exhibition/packetDataListCount", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    long packetDataListCount(@RequestParam String physicalCode, @RequestParam String modelId, @RequestBody ModelDataQueryParamVO param);

    /**
     * 添加模板表头
     */
    @PostMapping(value = "/api/datapacket/exhibition/listLookupsEnable", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<TreeModel<Object>> listLookupsEnable(@RequestParam String secretlevel);

    /**
     * 导入数据
     */
    @PostMapping(value = "/api/datapacket/exhibition/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResultBody importData(@RequestPart("formFile") MultipartFile file, @RequestParam String modelId, @RequestParam Boolean isInit, @RequestParam String physicalCode, @RequestParam String attributes);

    /**
     * 导出数据
     */
    @PostMapping(value = "/api/datapacket/exhibition/exportData", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String exportData(@RequestBody ModelDataExportParam requestData, @RequestParam String modelId,
                      @RequestParam String nodeId, @RequestParam String attributes,
                      @RequestParam String physicalCode);
}
