package com.meritdata.dam.datapacket.plan.model.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.model.entity.ModulePool;
import com.meritdata.dam.datapacket.plan.model.entity.QModulePool;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.vo.BaseTypeDTO;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleSearchBaseDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleVerFieldDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ModuleManageServiceImpl implements IModuleManageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManageServiceImpl.class);

    @Autowired
    IModuleManageService iModuleManageService;

    /**
     * 数仓建设接口
     */
    @Autowired
    IDataPacketClient dataPacketClient;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    ILogPostService logPostService;


    @Autowired
    com.meritdata.dam.datapacket.plan.model.dao.ModulePoolRepository modulePoolRepository;

    @Override
    public List<ModuleManageDto> list(String page, String rows, String moduleName, String moduleCode, String tableName) {
        //此集合用来过滤是否向上汇总
        List<ModuleManageDto> moduleManages = new ArrayList<>();
        //count记录序号
//        int count = 1;
        try {
            //调用中台接口，分页查询模板管理列表
            //因跨服务掉用接口，实=所以将查询条件转为map传递
            Map<String, String> map = new HashMap<>();
            map.put("page", page);
            map.put("rows", rows);
            map.put("name", moduleName);
            map.put("code", moduleCode);
            map.put("tableName", tableName);
            //获取生效的模板信息（分页）
            List<ModuleManageDto> moduleManageDtos = dataPacketClient.modulePage(map);

            //根据查询的当前页所有模板id匹配是否向上汇总
            QModulePool qModulePool = QModulePool.modulePool;
            List<ModulePool> modulePools = jpaQueryFactory.selectFrom(qModulePool)
                    .from(qModulePool).fetch();
            //遍历查询的所有生效的模板信息，如果ModulePool表中存在此模板信息则屈ModulePool的是否向上汇总信息
            //如果没有则设置为否
            if (modulePools.size() == 0) {
                moduleManageDtos.stream().forEach(item -> {
                    ModuleManageDto moduleManageDto = new ModuleManageDto();
                    moduleManageDto.setCode(item.getCode());
                    moduleManageDto.setName(item.getName());
                    moduleManageDto.setTableName(item.getTableName());
                    moduleManageDto.setIsPool("0");
                    moduleManageDto.setId(item.getId());
                    moduleManageDto.setModelInfo(item.getModelInfo());
                    moduleManages.add(moduleManageDto);

                });
            } else {
                moduleManageDtos.stream().forEach(item -> {
                    ModuleManageDto moduleManageDto = new ModuleManageDto();
                    for (ModulePool p : modulePools) {
                        if (item.getCode().equals(p.getModuleCode())) {
                            moduleManageDto.setIsPool(p.getIsPool());
                            break;
                        } else {
                            moduleManageDto.setIsPool("0");
                        }
                    }
                    moduleManageDto.setCode(item.getCode());
                    moduleManageDto.setName(item.getName());
                    moduleManageDto.setTableName(item.getTableName());
                    moduleManageDto.setId(item.getId());
                    moduleManageDto.setModelInfo(item.getModelInfo());
                    moduleManages.add(moduleManageDto);
                });
            }

            return moduleManages;
        } catch (Exception e) {
            LOGGER.error("查询模板管理列表失败", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ModuleVerFieldDto> moduleVerFieldInfo(String code, String busiName, String fieldName, String dataType, String length,
                                                      String sortNumber, String status, String definition) {
//        List<ModuleVerFieldDto> moduleVerFieldDtoList=new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(code)) {
            String message = "分页参数rows 传值有误，code:" + code;
            return new ArrayList<>();
        }
        try {
            map.put("code", code);
            map.put("busiName", busiName);
            map.put("fieldName", fieldName);
            map.put("dataType", dataType);
            map.put("length", length == null ? "" : length);
            map.put("sortNumber", sortNumber == null ? "" : sortNumber);
            map.put("status", status == null ? "" : status);
            List<ModuleVerFieldDto> moduleVerFieldDtos = dataPacketClient.ModuleVerFieldList(map);
            return moduleVerFieldDtos;
        } catch (Exception e) {
            LOGGER.error("查询模板字段信息失败", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<BaseTypeDTO> moduleVerFieldDataType(String code) {
        Map<String, String> map = new HashMap<>();
        List<BaseTypeDTO> DataList = new ArrayList<>();
        try {
            List<String> list = new ArrayList<>();
            map.put("code", code);
            //根据code查询具体信息
            List<ModuleVerFieldDto> moduleVerFieldDtos = dataPacketClient.ModuleVerFieldList(map);

            if (moduleVerFieldDtos.size() == 0) {
                return new ArrayList<>();
            }
            moduleVerFieldDtos.stream().forEach(item -> {
                list.add(item.getDataType());
            });
            Set<String> hashSet = new HashSet<>(list);
            List<String> disList = new ArrayList<>(hashSet);
            for (String s : disList) {
                BaseTypeDTO baseTypeDTO = new BaseTypeDTO();
                baseTypeDTO.setCode(s);
                baseTypeDTO.setName(s);
                DataList.add(baseTypeDTO);
            }
            return DataList;
        } catch (Exception e) {
            LOGGER.error("错误信息:" + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ModuleManageDto> moduleVerFieldById(String code) {
        List<ModuleManageDto> moduleManageDtoList = new ArrayList<>();
        try {
            ModuleManageDto moduleManageDto = dataPacketClient.moduleVerFieldById(code);
            moduleManageDtoList.add(moduleManageDto);
            return moduleManageDtoList;
        } catch (Exception e) {
            LOGGER.error("根据模板code查询失败", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ModulePool moduleIsPoolByCode(String code) {
        //根据code查询的当前数据匹配是否向上汇总
        ModulePool modulePool = new ModulePool();
        try {
            QModulePool qModulePool = QModulePool.modulePool;
            Predicate predicate = qModulePool.moduleCode.eq(code);
            modulePool = jpaQueryFactory.selectFrom(qModulePool).where(predicate).fetchOne();
            if (modulePool != null) {
                return modulePool;
            }
            modulePool = new ModulePool();
            modulePool.setIsPool("0");
            modulePool.setModuleCode(code);
            return modulePool;
        } catch (Exception e) {
            LOGGER.error("查询是否向上汇总错误信息:" + e.getMessage());
        }
        return modulePool;
    }

    @Override
    public ModulePool moduleConfig(String code, String isPool) {
        ModulePool modulePool = new ModulePool();
        try {
            //根据code模型汇总信息表
            QModulePool qModulePool = QModulePool.modulePool;
            ModulePool modulePoolByModuleCode = jpaQueryFactory.selectFrom(qModulePool).where(qModulePool.moduleCode.eq(code)).fetchOne();
            ModulePool save = new ModulePool();
            //根据code查询模板具体信息
            ModuleManageDto moduleManageDto = dataPacketClient.moduleVerFieldById(code);
            String ispoolFlag = "";
            if ("0".equals(isPool)) {
                ispoolFlag = "否";
            } else if ("1".equals(isPool)) {
                ispoolFlag = "是";
            }
            if (modulePoolByModuleCode != null) {
                modulePoolByModuleCode.setIsPool(isPool);
                save = modulePoolRepository.save(modulePoolByModuleCode);
                if (moduleManageDto != null) {
                    Message msg = new Message(Message.TYPE_OPT,
                            LogPattenUtils.getProperty("model.planning.config.bmodule"),
                            LogPattenUtils.getProperty("model.planning.manage.fmodule"),
                            LogPattenUtils.getProperty("model.planning.manage.save"),
                            StrUtil.format(LogPattenUtils.getProperty("model.planning.manage.save.message"), moduleManageDto.getName(), ispoolFlag),
                            Message.STATUS_SUCESS);
                    logPostService.postLog(msg);
                }
                return save;
            }
            modulePool.setIsPool(isPool);
            modulePool.setModuleCode(code);
            save = modulePoolRepository.save(modulePool);
            if (StrUtil.isNotBlank(save.getId())) {
                if (moduleManageDto != null) {
                    Message msg = new Message(Message.TYPE_OPT,
                            LogPattenUtils.getProperty("model.planning.config.bmodule"),
                            LogPattenUtils.getProperty("model.planning.manage.fmodule"),
                            LogPattenUtils.getProperty("model.planning.manage.save"),
                            StrUtil.format(LogPattenUtils.getProperty("model.planning.manage.save.message"), moduleManageDto.getName(), ispoolFlag),
                            Message.STATUS_SUCESS);
                    logPostService.postLog(msg);
                }
                return save;
            }
            return new ModulePool();
        } catch (Exception e) {
            LOGGER.error("配置错误信息:" + e.getMessage());
        }
        return new ModulePool();
    }

    @Override
    public List<ModuleManageDto> listInTempleteList(List<String> templateList) {
        //此集合用来过滤是否向上汇总
        List<ModuleManageDto> moduleManages = new ArrayList<>();
        //count记录序号
//        int count = 1;
        try {
            //调用中台接口，分页查询模板管理列表
            //因跨服务掉用接口，实=所以将查询条件转为map传递
            Map<String, String> map = new HashMap<>();
            map.put("code", String.join(",",templateList));
            //获取生效的模板信息（分页）
            List<ModuleManageDto> moduleManageDtos = dataPacketClient.modulePageByIds(map);


            //根据查询的当前页所有模板id匹配是否向上汇总
            QModulePool qModulePool = QModulePool.modulePool;
            List<ModulePool> modulePools = jpaQueryFactory.selectFrom(qModulePool)
                    .from(qModulePool).fetch();
            //遍历查询的所有生效的模板信息，如果ModulePool表中存在此模板信息则屈ModulePool的是否向上汇总信息
            //如果没有则设置为否
            if (modulePools.size() == 0) {
                moduleManageDtos.stream().forEach(item -> {
                    ModuleManageDto moduleManageDto = new ModuleManageDto();
                    moduleManageDto.setCode(item.getCode());
                    moduleManageDto.setName(item.getName());
                    moduleManageDto.setTableName(item.getTableName());
                    moduleManageDto.setIsPool("0");
                    moduleManageDto.setId(item.getId());
                    moduleManageDto.setModelInfo(item.getModelInfo());
                    moduleManages.add(moduleManageDto);

                });
            } else {
                moduleManageDtos.stream().forEach(item -> {
                    ModuleManageDto moduleManageDto = new ModuleManageDto();
                    for (ModulePool p : modulePools) {
                        if (item.getCode().equals(p.getModuleCode())) {
                            moduleManageDto.setIsPool(p.getIsPool());
                            break;
                        } else {
                            moduleManageDto.setIsPool("0");
                        }
                    }
                    moduleManageDto.setCode(item.getCode());
                    moduleManageDto.setName(item.getName());
                    moduleManageDto.setTableName(item.getTableName());
                    moduleManageDto.setId(item.getId());
                    moduleManageDto.setModelInfo(item.getModelInfo());
                    moduleManages.add(moduleManageDto);
                });
            }

            return moduleManages;
        } catch (Exception e) {
            LOGGER.error("查询模板管理列表失败", e);
            return new ArrayList<>();
        }
    }
}
