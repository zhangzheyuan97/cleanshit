package com.meritdata.dam.datapacket.plan.model.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.model.dao.ModuleColumnConfigRepository;
import com.meritdata.dam.datapacket.plan.model.dao.ModuleCurateRepository;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleCurate;
import com.meritdata.dam.datapacket.plan.model.entity.QModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.entity.QModuleCurate;
import com.meritdata.dam.datapacket.plan.model.service.IModuleInfoConfigService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleLookupDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleVerFieldDto;
import com.meritdata.dam.entity.datamanage.FormFieldVO;
import com.meritdata.dam.entity.datamanage.FormGroupVO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class ModuleInfoConfigServiceImpl implements IModuleInfoConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ModuleInfoConfigServiceImpl.class);

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private IDataPacketClient dataPacketClient;

    @Autowired
    private ModuleColumnConfigRepository moduleColumnConfigRepository;

    @Autowired
    private ModuleCurateRepository moduleCurateRepository;

    @Autowired
    private IMaintainService maintainService;

    @Autowired
    private IDatamationsClient datamationsClient;


    @Override
    public List<ModuleColumnConfig> getModuleCurate(String nodeId, String moduleCode, String tableName, String modelId) {
        QModuleColumnConfig qModuleColumnConfig = QModuleColumnConfig.moduleColumnConfig;
        List<ModuleColumnConfig> moduleColumnConfigList = jpaQueryFactory.selectFrom(qModuleColumnConfig)
                .where(qModuleColumnConfig.code.eq(moduleCode).and(qModuleColumnConfig.nodeId.eq(nodeId)))
                .orderBy(qModuleColumnConfig.sortNumber.asc())
                .fetch();
        if (moduleColumnConfigList.isEmpty()) {
            Map<String, String> map = new HashMap();
            map.put("code", moduleCode);
            List<ModuleVerFieldDto> moduleVerFieldDtos = dataPacketClient.ModuleVerFieldList(map);
            List<ModuleColumnConfig> moduleColumnConfigs = new ArrayList<>();
            moduleVerFieldDtos.stream().forEach(moduleVerFieldDto -> {
                ModuleColumnConfig columnConfig = new ModuleColumnConfig();
                columnConfig.setCode(moduleCode);
                columnConfig.setColumnName(moduleVerFieldDto.getBusiName());
                columnConfig.setNeedColumn("1");
                columnConfig.setIsSearch("0");
                columnConfig.setLookupCode("");
                columnConfig.setSortNumber(moduleVerFieldDto.getSortNumber());
                columnConfig.setFieldName(moduleVerFieldDto.getFieldName());
                columnConfig.setDataType(moduleVerFieldDto.getDataType());
                columnConfig.setLength(moduleVerFieldDto.getLength());
                columnConfig.setModelFieldId(moduleVerFieldDto.getModelFieldId());
                columnConfig.setModelColumnId(moduleVerFieldDto.getId());
                //查询每个字段的数据字典
                List<String> fieldByModelInfo = dataPacketClient.getFieldByModelInfo(moduleVerFieldDto.getModelFieldId());

                if (!CollectionUtils.isEmpty(fieldByModelInfo)) {
                    //将返回的map字符串转为listMap类型
                    List<Map<String, Object>> lookUpString = formatMapStringToJson(fieldByModelInfo);
                    if (!CollectionUtils.isEmpty(lookUpString)) {
                        columnConfig.setLookup(JSON.toJSONString(lookUpString));
                    }
                    List<String> codeList = lookUpString.stream().map(item -> item.get("code").toString()).collect(Collectors.toList());
                    String lookupCode = String.join(",", codeList);
                    columnConfig.setLookupCode(lookupCode);
                }
                moduleColumnConfigs.add(columnConfig);
            });
            List<FormGroupVO> fields = maintainService.getFields(modelId, true);
            moduleColumnConfigs.stream().forEach(item -> {
                fields.stream().forEach(field -> {
                    if ("默认组".equals(field.getGroupName())) {
                        List<FormFieldVO> fields1 = field.getFields();
                        for (int i = 0; i < fields1.size(); i++) {
                            FormFieldVO fie = fields1.get(i);
                            String result = "";
                            if (item.getModelFieldId().equals(fie.getModelFieldId())) {
                                if (fie.getModelFieldConfigDTO() != null && "1".equals(fie.getModelFieldConfigDTO().getDefaultValType())) {
                                    ResultBody fieldDefaultVal = datamationsClient.getFieldDefaultVal("1", fie.getModelFieldConfigDTO().getDefaultVal());
                                    if (fieldDefaultVal.isSuccess()) {
                                        fie.getModelFieldConfigDTO().setDefaultVal(fieldDefaultVal.getData().toString());
                                    }
                                }
                                if (fie.getModelFieldConfigDTO() != null && StringUtils.isNotBlank(fie.getModelFieldConfigDTO().getExpression())) {
                                    AtomicReference<String> expression = new AtomicReference<>("");
                                    expression.set(fie.getModelFieldConfigDTO().getExpression());
                                    FormGroupVO formGroupVO = new FormGroupVO();
                                    BeanUtils.copyProperties(field, formGroupVO);
                                    result = forMuale(expression.get(), formGroupVO);
                                }
                                if (StringUtils.isNotBlank(result)) {
                                    fie.getModelFieldConfigDTO().setExpression(result);
                                }
                                item.setFormFieldVO(fie);
                            }
                        }
                    }
                    if ("文件信息".equals(field.getGroupName())) {
                        field.getFields().stream().forEach(fie -> {
                            if (item.getModelFieldId().equals(fie.getModelFieldId())) {
                                item.setFormFieldVO(fie);
                            }
                        });
                    }
                });
            });
            return moduleColumnConfigs;
        } else {
            moduleColumnConfigList.stream().forEach(moduleColumnConfig -> {
                //获取数据字典
                List<String> fieldByModelInfo = dataPacketClient.getFieldByModelInfo(moduleColumnConfig.getModelFieldId());
                if (!CollectionUtils.isEmpty(fieldByModelInfo)) {
                    //将返回的map字符串转为listMap类型
                    List<Map<String, Object>> lookUpString = formatMapStringToJson(fieldByModelInfo);
                    if (!CollectionUtils.isEmpty(lookUpString)) {
                        moduleColumnConfig.setLookup(JSON.toJSONString(lookUpString));
                    }
                }
                //根据行id获取行信息
                ModuleVerFieldDto moduleVerFieldById = dataPacketClient.getModuleVerFieldById(moduleColumnConfig.getModelColumnId());
                if (moduleVerFieldById != null) {
                    moduleColumnConfig.setColumnName(moduleVerFieldById.getBusiName());
                    moduleColumnConfig.setFieldName(moduleVerFieldById.getFieldName());
                    moduleColumnConfig.setLength(moduleVerFieldById.getLength());
                }
            });
            List<FormGroupVO> fields = maintainService.getFields(modelId, true);
            moduleColumnConfigList.stream().forEach(item -> {
                fields.stream().forEach(field -> {
                    if ("默认组".equals(field.getGroupName())) {
                        List<FormFieldVO> fields1 = field.getFields();
                        for (int i = 0; i < fields1.size(); i++) {
                            FormFieldVO fie = fields1.get(i);
                            String result = "";
                            if (item.getModelFieldId().equals(fie.getModelFieldId())) {
                                if (fie.getModelFieldConfigDTO() != null && "1".equals(fie.getModelFieldConfigDTO().getDefaultValType())) {
                                    ResultBody fieldDefaultVal = datamationsClient.getFieldDefaultVal("1", fie.getModelFieldConfigDTO().getDefaultVal());
                                    if (fieldDefaultVal.isSuccess()) {
                                        fie.getModelFieldConfigDTO().setDefaultVal(fieldDefaultVal.getData().toString());
                                    }
                                }
                                if (fie.getModelFieldConfigDTO() != null && StringUtils.isNotBlank(fie.getModelFieldConfigDTO().getExpression())) {
                                    AtomicReference<String> expression = new AtomicReference<>("");
                                    expression.set(fie.getModelFieldConfigDTO().getExpression());
                                    FormGroupVO formGroupVO = new FormGroupVO();
                                    BeanUtils.copyProperties(field, formGroupVO);
                                    result = forMuale(expression.get(), formGroupVO);
                                }
                                if (StringUtils.isNotBlank(result)) {
                                    fie.getModelFieldConfigDTO().setExpression(result);
                                }
                                item.setFormFieldVO(fie);
                            }
                        }
                    }
                    if ("文件信息".equals(field.getGroupName())) {
                        field.getFields().stream().forEach(fie -> {
                            if (item.getModelFieldId().equals(fie.getModelFieldId())) {
                                item.setFormFieldVO(fie);
                            }
                        });
                    }
                });
            });
            return moduleColumnConfigList;
        }
    }

    private String forMuale(String str, FormGroupVO field) {
        AtomicReference<String> result = new AtomicReference<>(str);
        AtomicReference<String> string = new AtomicReference<>("");
        for (FormFieldVO item : field.getFields()) {
            if (result.get().contains(item.getModelFieldId())) {
                if (StringUtils.isNotBlank(string.get()) && string.get().contains(item.getModelFieldId())) {
                    string.set(string.get().replaceAll(item.getModelFieldId(), item.getAliasName()));
                } else {
                    string.set(result.get().replaceAll(item.getModelFieldId(), item.getAliasName()));
                }
            }
        }
        return string.get();
    }

    @Override
    public List<ModuleColumnConfig> saveColumnConfig(List<ModuleColumnConfig> columnConfigList) {
        return moduleColumnConfigRepository.saveAll(columnConfigList);
    }

    @Override
    public ModuleCurate saveModuleCurate(ModuleCurate moduleCurate) {
        QModuleCurate qModuleCurate = QModuleCurate.moduleCurate;
        List<ModuleCurate> moduleOld = jpaQueryFactory.selectFrom(qModuleCurate)
                .where(qModuleCurate.code.eq(moduleCurate.getCode()).and(
                        qModuleCurate.nodeId.eq(moduleCurate.getNodeId()))).fetch();
        if (moduleOld.size() > 0) {
            moduleCurate.setId(moduleOld.get(0).getId());
        }
        return moduleCurateRepository.save(moduleCurate);
    }

    /**
     * 将map类型的字符串集合转为json
     *
     * @param fieldByModelInfo
     * @return
     */
    @Override
    public List<Map<String, Object>> formatMapStringToJson(List<String> fieldByModelInfo) {
        List<Map<String, Object>> result = new ArrayList<>();
        fieldByModelInfo.forEach(item -> {
            String itemString = item.substring(1, item.length() - 1);
            String[] mapList = itemString.split(",");
            Map<String, Object> map = new HashMap<>();
            for (String mapStr : mapList) {
                String[] mapArr = mapStr.split("=");
                map.put(mapArr[0].trim(), mapArr[1].trim());
            }
//            map.put("checked", "1");
            result.add(map);
        });
        return result;
    }
}
