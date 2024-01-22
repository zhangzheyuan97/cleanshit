package com.meritdata.dam.datapacket.plan.model.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.QueryNodeDTO;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.model.dao.MatchFieldRepository;
import com.meritdata.dam.datapacket.plan.model.entity.*;
import com.meritdata.dam.datapacket.plan.model.service.IModuleInfoConfigService;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.impl.PackageSystemImpl;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStructureEnum;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.querydsl.core.types.Predicate;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class ModulePlanServiceImpl implements IModulePlanService {

    private static final Logger logger = LoggerFactory.getLogger(ModulePlanServiceImpl.class);

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private IDatamationsClient datamationsClient;

    @Autowired
    private IStandAloneService standAloneService;

    @Autowired
    private PackageSystemImpl packageSystem;

    @Autowired
    MatchFieldRepository matchFieldRepository;

    @Autowired
    IModuleInfoConfigService iModuleInfoConfigService;

    @Autowired
    IDataPacketClient dataPacketClient;


    String ID = "";
    String nodeTypeflag = "";

    @Override
    public List<TreeDto> tree(String pid, String userId) {
        List<Map<String, Object>> maps = datamationsClient.exhibitionTree(new HashMap<>(), "EBOM");
        QModuleTree qModuleTree = QModuleTree.moduleTree;
        List<ModuleTree> moduleTreeAll = jpaQueryFactory.selectFrom(qModuleTree).fetch();
        //获取权限表信息
        PackageSystemEntity packageSystemEntity = new PackageSystemEntity();
        packageSystemEntity.setSystemId(userId);
        packageSystemEntity.setType("person");
        List<PackageSystemEntity> byEntity = packageSystem.findAuthorityDataByEntity(packageSystemEntity);
        List<String> textList = byEntity.stream().map(PackageSystemEntity::getResourceId).collect(Collectors.toList());
        List<TreeDto> treeDtos = this.treeBuild(textList, pid, maps, moduleTreeAll);
        if (treeDtos.size() > 0) {
            return treeDtos;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<TreeDto> licenseTree(String pid) {
        List<Map<String, Object>> maps = datamationsClient.exhibitionTree(new HashMap<>(), "EBOM");
        QModuleTree qModuleTree = QModuleTree.moduleTree;
        List<ModuleTree> moduleTreeAll = jpaQueryFactory.selectFrom(qModuleTree).fetch();
        List<TreeDto> treeDtos = this.buildLicenseTree(pid, maps, moduleTreeAll);
        if (treeDtos.size() > 0) {
            return treeDtos;
        } else {
            return new ArrayList<>();
        }
    }

    private List<TreeDto> buildLicenseTree(String pid, List<Map<String, Object>> maps, List<ModuleTree> moduleTreeAll) {
        List<ModuleTree> moduleTreeList = moduleTreeAll.stream().filter(item -> item.getPid().equals(pid)).collect(Collectors.toList());
        List<TreeDto> treeDtoList = new ArrayList<>();
        moduleTreeList.stream().forEach(mTree -> {
            TreeDto treeDto = new TreeDto();
            QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
            BeanUtils.copyProperties(mTree, treeDto);
            if (FlowStructureEnum.STANDALONE.getValue().equals(mTree.getText()) || FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue().equals(mTree.getText())) {
                ID = mTree.getId();
                queryNodeDTO.setNodeLevel("1");
                if (FlowStructureEnum.STANDALONE.getValue().equals(mTree.getText())) {
                    nodeTypeflag = "3";
                } else if (FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue().equals(mTree.getText())) {
                    nodeTypeflag = "4";
                }
                queryNodeDTO.setNodeType(nodeTypeflag);
                queryNodeDTO.setFirstNode(mTree.getText());
            }
            if (mTree.getPid().equals(ID) || mTree.getPid().equals(ID)) {
                queryNodeDTO.setSecondNode(mTree.getText());
                queryNodeDTO.setNodeLevel("2");
                queryNodeDTO.setNodeType(nodeTypeflag);
            }
            queryNodeDTO.setUUID(UUID.randomUUID().toString());
            treeDto.setAttributes(queryNodeDTO);
            treeDtoList.add(treeDto);
        });
        for (TreeDto tree : treeDtoList) {
            ModuleTree moduleTree = new ModuleTree();
            moduleTreeAll.stream().forEach(item -> {
                        if (item.getId().equals(tree.getPid())) {
                            moduleTree.setId(item.getId());
                            moduleTree.setPid(item.getPid());
                            moduleTree.setText(item.getText());
                        }
                    }
            );
            AtomicBoolean flag = new AtomicBoolean(false);
            List<TreeDto> dtoList = new ArrayList<>();
            maps.stream().forEach(item -> {
                if (tree.getText().equals(item.get("F_PARENT_BUSINESS_CATEGORY"))) {
                    QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                    if (item.get("F_PARENT_TYPE").equals(FlowStructureEnum.SUBSYSTEM.getValue())) {
                        queryNodeDTO.setNodeType("1");
                    } else if (item.get("F_PARENT_TYPE").equals(FlowStructureEnum.MODULE.getValue())) {
                        queryNodeDTO.setNodeType("2");
                    } else if (item.get("F_PARENT_TYPE").equals(FlowStructureEnum.STANDALONE.getValue())) {
                    }
                    queryNodeDTO.setSecondNode(item.get("F_PARENT_BUSINESS_CATEGORY").toString());
                    if (moduleTree != null && moduleTree.getText().equals(item.get("F_PARENT_TYPE"))) {
                        flag.set(true);
                        TreeDto t = new TreeDto();
                        queryNodeDTO.setFirstNode(item.get("F_PARENT_TYPE").toString());
                        t.setId(item.get("F_M_SYS_ID").toString());
                        t.setPid(tree.getId());
                        t.setText(item.get("F_PARENT_PHYSICAL_ID").toString());
                        queryNodeDTO.setUUID(UUID.randomUUID().toString());
                        t.setAttributes(queryNodeDTO);
                        dtoList.add(t);
                    }
                }
                ArrayList<TreeDto> dtoListTemp = dtoList.stream().collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(
                                Comparator.comparing(p -> p.getText())
                        )), ArrayList::new
                ));
                tree.setChildren(dtoListTemp);
            });
            if (!flag.get()) {
                tree.setChildren(buildLicenseTree(tree.getId(), maps, moduleTreeAll));
            }
        }
        return treeDtoList;
    }


    private List<TreeDto> treeBuild(List<String> textList, String pid, List<Map<String, Object>> maps, List<ModuleTree> moduleTreeAll) {
        List<ModuleTree> moduleTreeList = moduleTreeAll.stream().filter(item -> item.getPid().equals(pid)).collect(Collectors.toList());
        List<TreeDto> treeDtoList = new ArrayList<>();
        if (moduleTreeList != null && moduleTreeList.size() > 0) {
            moduleTreeList.stream().forEach(mTree -> {
                TreeDto treeDto = new TreeDto();
                QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                BeanUtils.copyProperties(mTree, treeDto);
                if (FlowStructureEnum.STANDALONE.getValue().equals(mTree.getText()) || FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue().equals(mTree.getText())) {
                    ID = mTree.getId();
                    queryNodeDTO.setNodeLevel("1");
                    if (FlowStructureEnum.STANDALONE.getValue().equals(mTree.getText())) {
                        nodeTypeflag = "3";
                    } else if (FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue().equals(mTree.getText())) {
                        nodeTypeflag = "4";
                    }
                    queryNodeDTO.setNodeType(nodeTypeflag);
                    queryNodeDTO.setFirstNode(mTree.getText());
                }
                if (mTree.getPid().equals(ID)) {
                    queryNodeDTO.setSecondNode(mTree.getText());
                    queryNodeDTO.setNodeLevel("2");
                    queryNodeDTO.setNodeType(nodeTypeflag);
                }
                queryNodeDTO.setUUID(UUID.randomUUID().toString());
                treeDto.setAttributes(queryNodeDTO);
                treeDtoList.add(treeDto);
            });
        }

        for (TreeDto tree : treeDtoList) {
            ModuleTree moduleTree = new ModuleTree();
            moduleTreeAll.stream().forEach(item -> {
                        if (item.getId().equals(tree.getPid())) {
                            moduleTree.setId(item.getId());
                            moduleTree.setPid(item.getPid());
                            moduleTree.setText(item.getText());
                        }
                    }
            );
            AtomicBoolean flag = new AtomicBoolean(false);
            List<TreeDto> dtoList = new ArrayList<>();
            maps.stream().forEach(item -> {
                if (tree.getText().equals(item.get("F_PARENT_BUSINESS_CATEGORY"))) {
                    QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                    if (item.get("F_PARENT_TYPE") != null) {
                        if (item.get("F_PARENT_TYPE").equals(FlowStructureEnum.SUBSYSTEM.getValue())) {
                            queryNodeDTO.setNodeType("1");
                        } else if (item.get("F_PARENT_TYPE").equals(FlowStructureEnum.MODULE.getValue())) {
                            queryNodeDTO.setNodeType("2");
                        } else if (item.get("F_PARENT_TYPE").equals(FlowStructureEnum.STANDALONE.getValue())) {
                        }
                        queryNodeDTO.setSecondNode(item.get("F_PARENT_BUSINESS_CATEGORY").toString());
                        if (moduleTree != null && moduleTree.getText().equals(item.get("F_PARENT_TYPE"))
                                && item.get("F_PARENT_PHYSICAL_ID") != null && textList.contains(item.get("F_PARENT_PHYSICAL_ID").toString())) {
                            flag.set(true);
                            TreeDto t = new TreeDto();
                            queryNodeDTO.setFirstNode(item.get("F_PARENT_TYPE").toString());
                            t.setId(item.get("F_M_SYS_ID").toString());
                            t.setPid(tree.getId());
                            t.setText(item.get("F_PARENT_PHYSICAL_ID").toString());
                            queryNodeDTO.setUUID(UUID.randomUUID().toString());
                            queryNodeDTO.setThirdlyNode(item.get("F_PARENT_PHYSICAL_ID").toString());
                            t.setAttributes(queryNodeDTO);
                            dtoList.add(t);
                        }
                    }
                }
                ArrayList<TreeDto> dtoListTemp = dtoList.stream().collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(
                                Comparator.comparing(p -> p.getText())
                        )), ArrayList::new
                ));
                tree.setChildren(dtoListTemp);
            });
            if (!flag.get()) {
                tree.setChildren(treeBuild(textList, tree.getId(), maps, moduleTreeAll));
            }
        }
        return treeDtoList;
    }

    @Override
    public List<TreeDto> addTreeNode(String userId, List<TreeDto> treeDtoList) {
        //获取权限表信息
        PackageSystemEntity packageSystemEntity = new PackageSystemEntity();
        packageSystemEntity.setSystemId(userId);
        packageSystemEntity.setType("person");
        List<PackageSystemEntity> byEntity = packageSystem.findAuthorityDataByEntity(packageSystemEntity);
        List<String> textList = byEntity.stream().map(PackageSystemEntity::getResourceId).collect(Collectors.toList());
        List<Map<String, Object>> maps = datamationsClient.exhibitionTree(new HashMap<>(), "PHYSICAL_OBJECT_SINGLE_MACHINE");
        treeDtoList.stream().forEach(item -> {
            if (item.getText().equals(FlowStructureEnum.STANDALONE.getValue())) {
                //遍历二级节点
                item.getChildren().stream().forEach(child -> {
                    List<TreeDto> treeDtoListTH = new ArrayList<>();
                    maps.stream().forEach(param -> {
                        if (maps.size() > 0 && child.getText().equals(param.get("F_CLASSIFICATION"))) {
                            QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                            queryNodeDTO.setFirstNode(item.getText());
                            queryNodeDTO.setSecondNode(param.get("F_CLASSIFICATION").toString());
                            queryNodeDTO.setThirdlyNode(param.get("F_DRAWING_NO").toString());
//                            queryNodeDTO.setNodeType(param.get("F_STAND_ALONE_IDENTIFICATION").toString());
                            queryNodeDTO.setNodeLevel("3");
                            String THflag = "";
                            String firstNodeflag = "";
                            if ("1".equals(param.get("F_STAND_ALONE_IDENTIFICATION") == null ? "1" : param.get("F_STAND_ALONE_IDENTIFICATION").toString())) {
                                //单机
                                THflag = "3";
                                firstNodeflag = FlowStructureEnum.STANDALONE.getValue();
                            } else if ("0".equals(param.get("F_STAND_ALONE_IDENTIFICATION") == null ? "1" : param.get("F_STAND_ALONE_IDENTIFICATION").toString())) {
                                //总装直属件
                                THflag = "4";
                                firstNodeflag = FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue();
                            }
                            if (firstNodeflag.equals(item.getText()) && param.get("F_DRAWING_NO") != null
                                    && textList.contains(param.get("F_DRAWING_NO").toString())) {
                                queryNodeDTO.setNodeType(THflag);
                                TreeDto treeDto = new TreeDto();
                                //单机第三级，前端node需要根据单机、发动机、图号信息展示图号下面的所有所有批次信息
                                treeDto.setId(param.get("F_M_SYS_ID").toString());
                                treeDto.setPid(child.getId());
                                treeDto.setText(param.get("F_DRAWING_NO").toString());
                                treeDto.setAttributes(queryNodeDTO);
                                treeDtoListTH.add(treeDto);
                            }
                        }
                    });
                    ArrayList<TreeDto> treeDtoListTHTemp = treeDtoListTH.stream().collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(
                                    Comparator.comparing(p -> p.getText())
                            )), ArrayList::new
                    ));

                    child.setChildren(treeDtoListTHTemp);
                });
            }
            if (item.getText().equals(FlowStructureEnum.STANDALONE.getValue())) {
                //组装单机树
                standAloneService.formatAloneTree(item.getChildren());
            }
        });
        return treeDtoList;
    }

    @Override
    public List<ModuleCurate> getModuleCurateByNodeId(String nodeId) {
        QModuleCurate moduleCurate = QModuleCurate.moduleCurate;
        return jpaQueryFactory.selectFrom(moduleCurate)
                .where(moduleCurate.nodeId.eq(nodeId))
                .fetch();
    }

    @Override
    public List<ModuleCurate> getModuleCurateByNodeId(String nodeId, String code) {
        QModuleCurate moduleCurate = QModuleCurate.moduleCurate;
        return jpaQueryFactory.selectFrom(moduleCurate)
                .where(moduleCurate.nodeId.eq(nodeId).and(moduleCurate.code.eq(code)))
                .fetch();
    }

    @Override
    public boolean isEndNode(String nodeId) {
        QModuleTree qModuleTree = QModuleTree.moduleTree;
        return jpaQueryFactory.selectFrom(qModuleTree)
                .where(qModuleTree.pid.eq(nodeId))
                .fetch().size() > 0;
    }

    @Override
    public List<MatchFieldEntity> getMatchList(String tableName, String modeltree) {
        QMatchFieldEntity qMatchFieldEntity = QMatchFieldEntity.matchFieldEntity;
        return jpaQueryFactory.selectFrom(qMatchFieldEntity)
                .where(qMatchFieldEntity.tablename.eq(tableName).and(qMatchFieldEntity.modeltree.eq(modeltree)))
                .fetch();
    }

    @Override
    public long getMatchListCount(String Id) {
        try {
            QMatchFieldEntity qMatchFieldEntity = QMatchFieldEntity.matchFieldEntity;
            Predicate predicate = qMatchFieldEntity.id.eq(Id);
            long count = matchFieldRepository.count(predicate);
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean addMatchData(MatchFieldEntity matchFieldEntity) {
        try {
            matchFieldRepository.save(matchFieldEntity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean delMatchData(MatchFieldEntity matchFieldEntity) {
        try {
            matchFieldRepository.delete(matchFieldEntity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getTestItemLookup(String tableName, String nodeId, String cloumnName) {
        List<String> look = new ArrayList<>();
        QModuleColumnConfig qModuleColumnConfig = QModuleColumnConfig.moduleColumnConfig;
        List<ModuleColumnConfig> moduleColumnConfigList = jpaQueryFactory.selectFrom(qModuleColumnConfig)
                .where(qModuleColumnConfig.code.eq(tableName).and(qModuleColumnConfig.nodeId.eq(nodeId)).and(qModuleColumnConfig.columnName.eq(cloumnName)))
                .orderBy(qModuleColumnConfig.sortNumber.asc())
                .fetch();
        for (ModuleColumnConfig moduleColumnConfig : moduleColumnConfigList) {
            //获取数据字典
            List<String> fieldByModelInfo = dataPacketClient.getFieldByModelInfo(moduleColumnConfig.getModelFieldId());
            if (!CollectionUtils.isEmpty(fieldByModelInfo)) {
                //将返回的map字符串转为listMap类型
                List<Map<String, Object>> lookUpString = iModuleInfoConfigService.formatMapStringToJson(fieldByModelInfo);
                String lookupCode = moduleColumnConfig.getLookupCode() == null ? "" : moduleColumnConfig.getLookupCode();
                String[] split = lookupCode.split(",");
                for (String s : split) {
                    for (Map<String, Object> map : lookUpString) {
                        if (s.equals(map.get("code").toString())) {
                            look.add(map.get("name").toString());
                        }
                    }
                }
            }
        }
        return look;
    }

}
