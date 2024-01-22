package com.meritdata.dam.datapacket.plan.client;

import com.meritdata.cloud.config.CloudFeignConfiguration;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleInfoDTO;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleVerFieldDto;
import com.meritdata.dam.entity.metamanage.ModelFieldAssignConfigDTO;
import com.meritdata.dam.entity.metamanage.ModelVerFieldDTO;
import com.meritdata.dam.entity.metamanage.SelectFieldConditionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 远程调用client
 */
@FeignClient(value = "dam-metamanage", configuration = CloudFeignConfiguration.class, contextId = "IDataPacketClient")
public interface IDataPacketClient {


    /**
     * 生效模板分页查询
     */
    @PostMapping(value = "/api/datapacket/modulePage", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<ModuleManageDto> modulePage(@RequestParam Map<String, String> map);

    /**
     * 生效模板分页查询 按照表单id
     */
    @PostMapping(value = "/api/datapacket/modulePageByIds", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<ModuleManageDto> modulePageByIds(@RequestParam Map<String, String> map);


    /**
     * 根据id获取模板
     */
    @PostMapping(value = "/api/datapacket/findModuleVerById", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ModuleManageDto moduleVerById(@RequestParam(value = "id", required = true) String id);

    /**
     * 生效模板分页查询
     */
    @PostMapping(value = "/api/datapacket/moduleAllPage", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<ModuleManageDto> moduleAllPage(@RequestParam Map<String, String> map);

    /**
     * 生效模板总数查询
     */
    @PostMapping(value = "/api/datapacket/moduleCount", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Long moduleCount(@RequestParam Map<String, String> map);

    /**
     * 根据模板编码查询模型字段
     */
    @PostMapping(value = "/api/datapacket/ModuleVerFieldList", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<ModuleVerFieldDto> ModuleVerFieldList(@RequestParam Map<String, String> map);

    /**
     * 根据模板编码查询模型字段
     */
    @PostMapping(value = "/api/datapacket/moduleVerFieldCount", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Long moduleVerFieldCount(@RequestParam Map<String, String> map);

    /**
     * 根据模板编码查询
     */
    @PostMapping(value = "/api/datapacket/moduleVerFieldById", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModuleManageDto moduleVerFieldById(@RequestParam(value = "code", required = true) String code);

    /**
     * 根据modelInfo查询数据字典
     */
    @PostMapping(value = "/api/datapacket/getFieldByModelInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> getFieldByModelInfo(@RequestParam(value = "modelInfo", required = true) String modelField);

    /**
     * 根据modelId查询tm_model_ver_field中的model_field
     */
    @PostMapping(value = "/api/datapacket/getModelField", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getModelFieldByModelId(@RequestParam(value = "modelId", required = true) String modelId);

    /**
     * 获取采集分系统中间列表信息
     */
    @PostMapping(value = "/api/collection/datacollection/centerDataList", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<ModuleManageDto> centerDataList(@RequestParam List<String> dataCodeList, @RequestParam Map<String, Object> map);

    /**
     * 获取采集分系统中间列表信息统计数
     */
    @PostMapping(value = "/api/collection/datacollection/centerDataCount", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Long centerDataCount(@RequestParam List<String> dataCodeList, @RequestParam Map<String, Object> map);

    @PostMapping(value = "/api/datamanage/getModuleInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ModuleVerFieldDto getModuleInfo(@RequestParam Map<String, String> map);

    @PostMapping(value = "/api/datapacket/getModuleVerFieldById", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ModuleVerFieldDto getModuleVerFieldById(@RequestParam String id);

    /**
     * 获取采集分系统中间列表信息统计数
     */
    @PostMapping(value = "/api/collection/datacollection/getModuleInfo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ModuleInfoDTO getModuleInfo(@RequestParam String moduleId);

    /**
     * 根据模板的编号获取模板的名称
     */
    @PostMapping(value = "/api/collection/datacollection/getModuleInfoListByIds", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<ModuleInfoDTO> getModuleInfoListByIds(@RequestParam List<String> moduleIdList);


    /**
     * 查询所有模板分页查询
     */
    @PostMapping(value = "/api/datapacket/moduleAllTemplete", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<ModuleManageDto> moduleAllTemplete(Map<String, String> map);

    /**
     * 根据模型名称获取匹配的模型ID集合
     */
    @PostMapping(value = "/api/metamanage/model/manage/getModelIdByModelName", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    ResultBody<List<String>> getModelIdByModelName(@RequestParam String modelName);

    /**
     * 根据条件获取字段信息（包含字段配置信息）
     *
     * @param selectFieldCondition 查询模型字段信息相关参数信息
     * @return
     */
    @RequestMapping(value = "/api/collection/datacollection/getFieldByCondition", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<ModelVerFieldDTO>> getFieldByCondition(@RequestBody SelectFieldConditionDTO selectFieldCondition);

    /**
     * 根据模型ID获取该模型所有单值赋值信息
     *
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/api/collection/datacollection/getModelFieldAssignByModelId", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<ModelFieldAssignConfigDTO>> getModelFieldAssignByModelId(@RequestParam String modelId);

}
