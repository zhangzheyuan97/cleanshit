package com.meritdata.dam.datapacket.plan.acquistion.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.acquistion.vo.QueryNodeDTO;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.model.dao.ModuleCurateRepository;
import com.meritdata.dam.datapacket.plan.model.entity.*;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleConfigDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.impl.PackageSystemImpl;
import com.meritdata.dam.datapacket.plan.utils.PageUtil;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStructureEnum;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author merit
 */
@Service
public class StandAloneServiceImpl implements IStandAloneService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandAloneServiceImpl.class);

    @Autowired
    private IDatamationsClient datamationsClient;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    IDataPacketClient iDataPacketClient;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IModulePlanService iModulePlanService;

    @Autowired
    MaintainServiceImpl maintainService;

    @Autowired
    private IModulePlanService modulePlanService;

    @Autowired
    ModuleCurateRepository moduleCurateRepository;

    @Autowired
    IDataPacketClient dataPacketClient;

    @Autowired
    private PackageSystemImpl packageSystem;

    @Autowired
    private SessionUtils sessionUtils;

    //二级层级
    Set<String> ls = new HashSet<>();

    {
//        ls.add("阀门");
//        ls.add("发动机");
//        ls.add("容器");
//        ls.add("机电");
    }

    //单机与总装直属件实物列表中字段包含的key
    List<String> list = Arrays.asList("F_CLASSIFICATION", "F_DRAWING_NO", "F_BATCH_NO", "F_PHYSICAL_NO");

    List<TreeDto> searchList = new ArrayList<>();

    @Override
    public List<TreeDto> getTree(String userId) {
        try {
            //PHYSICAL_OBJECT_SINGLE_MACHINE单机与总装直属件实物
            List<Map<String, Object>> maps = datamationsClient.exhibitionTree(new HashMap<>(), "PHYSICAL_OBJECT_SINGLE_MACHINE");
            maps = maps.stream().filter(m -> {
                return m.get("F_CLASSIFICATION") != null && m.get("F_DRAWING_NO") != null && m.get("F_BATCH_NO") != null && m.get("F_PHYSICAL_NO") != null;
            }).collect(Collectors.toList());
            maps = maps.stream().filter(m -> {
                return m.get("F_STAND_ALONE_IDENTIFICATION") != null && m.get("F_STAND_ALONE_IDENTIFICATION").equals("1") && (m.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED") == null || m.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED").equals("1"));
            }).collect(Collectors.toList());
            //存储单机型号策划中唯一标识ID
            Map<String, String> idList = new HashMap<>();
            /*for (int i = 0; i < maps.size(); i++) {
                if ("阀门".equals(maps.get(i).get("F_CLASSIFICATION")) && idList.get("阀门" + maps.get(i).get("F_DRAWING_NO").toString()) == null) {
                    idList.put("阀门" + maps.get(i).get("F_DRAWING_NO").toString(), maps.get(i).get("F_M_SYS_ID").toString());
                }
                if ("发动机".equals(maps.get(i).get("F_CLASSIFICATION")) && idList.get("发动机" + maps.get(i).get("F_DRAWING_NO").toString()) == null) {
                    idList.put("发动机" + maps.get(i).get("F_DRAWING_NO").toString(), maps.get(i).get("F_M_SYS_ID").toString());
                }
                if ("容器".equals(maps.get(i).get("F_CLASSIFICATION")) && idList.get("容器" + maps.get(i).get("F_DRAWING_NO").toString()) == null) {
                    idList.put("容器" + maps.get(i).get("F_DRAWING_NO").toString(), maps.get(i).get("F_M_SYS_ID").toString());
                }
                if ("机电".equals(maps.get(i).get("F_CLASSIFICATION")) && idList.get("机电" + maps.get(i).get("F_DRAWING_NO").toString()) == null) {
                    idList.put("机电" + maps.get(i).get("F_DRAWING_NO").toString(), maps.get(i).get("F_M_SYS_ID").toString());
                }
            }*/
            List<TreeDto> result = new ArrayList<>();
            TreeDto treeDto = new TreeDto();
            QModuleTree qModuleTree = QModuleTree.moduleTree;
            List<String> id = jpaQueryFactory.select(qModuleTree.id).from(qModuleTree).where(qModuleTree.text.eq(FlowStructureEnum.STANDALONE.getValue())).fetch();
            if (id.isEmpty() || id.size() == 0) {
                return new ArrayList<>();
            }
            List<ModuleTree> list = jpaQueryFactory.selectFrom(qModuleTree)
                    .where(qModuleTree.pid.eq(id.get(0)))
                    .fetch();
            if (list.size() == 0 || CollectionUtils.isEmpty(list)) {
                return new ArrayList<>();
            }
            for (int i = 0; i < maps.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).getText().equals(maps.get(i).get("F_CLASSIFICATION")) && idList.get(list.get(j).getText() + maps.get(i).get("F_DRAWING_NO").toString()) == null) {
                        idList.put(list.get(j).getText() + maps.get(i).get("F_DRAWING_NO").toString(), maps.get(i).get("F_M_SYS_ID").toString());
                    }
                }
            }
            treeDto.setId(list.get(0).getPid());
            treeDto.setPid("-1");
            treeDto.setText(FlowStructureEnum.STANDALONE.getValue());
            //获取权限表信息
            PackageSystemEntity packageSystemEntity = new PackageSystemEntity();
            packageSystemEntity.setSystemId(userId);
            packageSystemEntity.setType("person");
            List<PackageSystemEntity> byEntity = packageSystem.findAuthorityDataByEntity(packageSystemEntity);
            List<String> textList = byEntity.stream().map(PackageSystemEntity::getResourceId).collect(Collectors.toList());
            List<TreeDto> mapList = buildTree(list, maps, 0, idList, textList);
            //组装单机树
            formatAloneTree(mapList);

            treeDto.setChildren(mapList);
            result.add(treeDto);
//        List<TreeDto> treeByText = getTreeByText(result, "L");
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    @Override
    public void formatAloneTree(List<TreeDto> mapList) {
        //获取单机表里的生效数据
        List<Map<String, Object>> djInfoList = datamationsClient.getDJInfo(new HashMap<>());
        List<String> treeNodeList = djInfoList.stream().map(item -> MapUtils.getString(item,"F_CLASSIFICATION","")+"-"
                +MapUtils.getString(item,"F_SECONDTYPE","")+"-"
                +MapUtils.getString(item,"F_THIRDTYPE","")+"-"
                +MapUtils.getString(item,"F_DRAWING_NO","")).distinct().collect(Collectors.toList());
        //对单机表里的数据根据类型进行分组
        Map<String, List<Map<String, Object>>> djInfoGroupClassificationMap = djInfoList.stream()
                .filter(item -> StringUtils.isNotBlank(MapUtils.getString(item, "F_CLASSIFICATION"))
                        && StringUtils.isNotBlank(MapUtils.getString(item, "F_SECONDTYPE"))
                        && StringUtils.isNotBlank(MapUtils.getString(item, "F_THIRDTYPE")))
                .collect(Collectors.groupingBy(i -> MapUtils.getString(i, "F_CLASSIFICATION", "")));
        //对当前结构解析重新封装
        for (TreeDto dto : mapList) {
            List<TreeDto> children = dto.getChildren();
            //根据类型获取指定的单机数据
            List<Map<String, Object>> djInfoGroupClassificationList =(List<Map<String, Object>>) MapUtils.getObject(djInfoGroupClassificationMap, dto.getText());
            //单机二级节点
            List<String> secondList = new ArrayList<>();
            if (djInfoGroupClassificationList != null){
                secondList = djInfoGroupClassificationList.stream()
                        .map(item -> MapUtils.getString(item,"F_SECONDTYPE"))
                        .distinct().collect(Collectors.toList());
            }
            //组装二三级
            List<TreeDto> secondDataList = new ArrayList<>();
            for (String s2 : secondList) {
                TreeDto treeDto2 = new TreeDto();
                treeDto2.setId(UUID.randomUUID().toString());
                treeDto2.setPid(dto.getId());
                treeDto2.setText(s2);
                treeDto2.setAttributes(new QueryNodeDTO());
                List<TreeDto> thirdDataList = new ArrayList<>();
                //单机与三级节点
                List<String> thirdList = djInfoGroupClassificationList.stream().filter(s -> s2.equals(MapUtils.getString(s,"F_SECONDTYPE")))
                        .map(item -> MapUtils.getString(item,"F_THIRDTYPE"))
                        .distinct().collect(Collectors.toList());
                for (String s3 : thirdList) {
                    TreeDto treeDto3 = new TreeDto();
                    treeDto3.setId(UUID.randomUUID().toString());
                    treeDto3.setPid(treeDto2.getId());
                    treeDto3.setText(s3);
                    treeDto3.setAttributes(new QueryNodeDTO());
                    List<TreeDto> fourthDataList = new ArrayList<>();
                    //只到批次节点的
                    if (CollectionUtils.isNotEmpty(children)){
                        for (TreeDto child : children) {
                            if (treeNodeList.contains(dto.getText()+"-"+s2+"-"+s3+"-"+child.getText())
                            ){
                                child.setPid(treeDto3.getId());
                                if (child.getChildren() != null){
                                    for (TreeDto treeDto : child.getChildren()) {
                                        treeDto.getAttributes().setSecondType(s2);
                                        treeDto.getAttributes().setThirdType(s3);
                                    }
                                }
                                fourthDataList.add(child);
                            }
                        }
                        treeDto3.setChildren(fourthDataList);
                    }
                    thirdDataList.add(treeDto3);
                }
                treeDto2.setChildren(thirdDataList);
                secondDataList.add(treeDto2);
            }
            dto.setChildren(secondDataList);
        }
    }

    public List<TreeDto> getTreeByText(List<TreeDto> treeDtos, String text) {
        /*if("单机".equals(treeDtos.get(0).getText())){
            searchList = treeDtos;
        }
        List<TreeDto> result = new ArrayList<>();
        treeDtos.stream().forEach(treeDto -> {
            if(treeDto.getChildren() != null || treeDto.getChildren().size() > 0){
                getTreeByText(treeDto.getChildren(),text);
            }else {
                if(!treeDto.getText().contains(text)){
                    filterListTree(treeDto);
                }
            }
        });*/
        /*Iterator<TreeDto> iterator = treeDtos.iterator();
        while (iterator.hasNext()){
            TreeDto next = iterator.next();
            if(!next.getText().contains(text)){
                List<TreeDto> children = next.getChildren();
                if(!CollectionUtils.isEmpty(children)){
                    getTreeByText(children,text);
                }
                if(CollectionUtils.isEmpty(children)){
                    iterator.remove();
                }
            }
        }*/
        rootTree(treeDtos);
        recursionTree(treeDtos);
        if (StringUtils.isNotBlank(text)) {
            del(searchList, text);
            delSonNull(searchList, text);
        }
        return treeDtos;
    }

    private void delSonNull(List<TreeDto> searchList, String text) {
        Iterator<TreeDto> iterator = searchList.iterator();
        while (iterator.hasNext()) {
            TreeDto next = iterator.next();
            if (!next.getText().contains(text)) {
                List<TreeDto> children = next.getChildren();
                if (!CollectionUtils.isEmpty(children) || children.size() > 0) {
                    delSonNull(children, text);
                } else {
                    iterator.remove();
                }
            }
        }
    }

    private void del(List<TreeDto> searchList, String text) {
        Iterator<TreeDto> iterator = searchList.iterator();
        while (iterator.hasNext()) {
            TreeDto next = iterator.next();
            if (!next.getText().contains(text)) {
                List<TreeDto> children = next.getChildren();
                if (!CollectionUtils.isEmpty(children)) {
                    del(children, text);
                }
                if (CollectionUtils.isEmpty(children)) {
                    iterator.remove();
                }
            }
        }
    }

    private void recursionTree(List<TreeDto> treeDtos) {
        treeDtos.forEach(t -> {
            String id = t.getId();
            if (t.getChildren().size() > 0 || !t.getChildren().isEmpty()) {
                List<TreeDto> children = getChildren(treeDtos, id);
                t.setChildren(children);
                recursionTree(children);
            }
        });
    }

    private List<TreeDto> getChildren(List<TreeDto> treeDtos, String id) {
        List<TreeDto> children = new ArrayList<>();
        treeDtos.forEach(t -> {
            if (id.equals(t.getPid())) {
                children.add(t);
            }
        });
        return children;
    }

    private void rootTree(List<TreeDto> treeDtos) {
        treeDtos.forEach(t -> {
            if ("-1".equals(t.getId())) {
                searchList.add(t);
            }
        });
    }

    /*@Override
    public List<TreeDto> getTree() {
        Map<String,Object> map = new HashMap<>();
        try {
            List<TreeDto> treeDtoList = iModulePlanService.tree("-1");
            List<TreeDto> treeList = new ArrayList<>();
            for (TreeDto td: treeDtoList) {
                if("单机".equals(td.getText())){
                    treeList.add(td);
                }
            }
            //查询单机与总装直属件的信息，挂在单机模块下，构建单机树结构
            //挂载单机
            List<Map<String, Object>> maps = iDatamationsClient.exhibitionTree(new HashMap<>(),"PHYSICAL_OBJECT_SINGLE_MACHINE");
            treeList.stream().forEach(item->{
                List<TreeDto> FL = item.getChildren();
                for (TreeDto FLChildren : FL){
                    List<TreeDto> TH = FLChildren.getChildren();
                    for (TreeDto thChildren:TH){
                        List<TreeDto> PC = new ArrayList<>();
                        maps.stream().forEach(mapsInfo->{
                            //pc的信息与thChildren的test信息一致则存储
                            if(mapsInfo.get("").equals(thChildren.getText())){
                                TreeDto treeDtoPC = new TreeDto();
                                treeDtoPC.setId(mapsInfo.get("").toString());
                                treeDtoPC.setPid(thChildren.getId());
                                treeDtoPC.setText(mapsInfo.get("").toString());
                                PC.add(treeDtoPC);
                            }
                        });
                        thChildren.setChildren(PC);
                    }
                }
            });
            return treeList;
        }catch (Exception e){
            LOGGER.error("树构建错误日志："+e.getMessage());
        }
        return new ArrayList<>();
    }*/

    @Override
    public ResultBody<GridView> getModelList(Map map) {
        //获取颜色
        JSONObject jsonObject = JSON.parseObject(map.get("attributes") == null ? "" : map.get("attributes").toString());
        List<ModuleManageDto> moduleManageDtoList = new ArrayList<>();
        //页数
        Integer pageNum = Integer.parseInt(map.get("page").toString());
//        //页数
        Integer sizeNum = Integer.parseInt(map.get("rows").toString());
        String treeName = String.valueOf(map.get("treeName"));
        try {
            //根据map中的nodeId信息
            String nodeId = map.get("nodeId") == null ? "" : map.get("nodeId").toString();
            //TODO::远程接口查询数仓建设中生效的模板信息
            //查询出所有模板信息
            List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(map);
            //TODO::查询TM_MODEL_CURATE表是否初始化模板信息
            //根据该节点下的模板信息
//            String nodeId = map.get("tempId") == null ? "" : map.get("tempId");
            List<ModuleCurate> moduleCurateByNodeIdList = modulePlanService.getModuleCurateByNodeId(nodeId);
            List<ModuleCurate> moduleCurateArrayList = new ArrayList<>();
            //没有任何配置的情况
            if (moduleCurateByNodeIdList.size() == 0) {
                List<ModuleConfigDto> moduleCurateList = new ArrayList<>();
                moduleManageInfoList.forEach(model -> {
                    //初始化数据
                    ModuleCurate moduleCurate = new ModuleCurate();
                    moduleCurate.setIsPackage("1");
                    moduleCurate.setCode(model.getCode());
                    moduleCurate.setModelInfo(model.getModelInfo());
                    moduleCurate.setNodeId(nodeId);
                    moduleCurateArrayList.add(moduleCurate);
                    //返回给前端的数据
                    ModuleConfigDto moduleConfigDto = new ModuleConfigDto();
                    BeanUtils.copyProperties(model, moduleConfigDto);
                    moduleConfigDto.setIsPackage("1");
                    moduleCurateList.add(moduleConfigDto);
                });
                moduleCurateRepository.saveAll(moduleCurateArrayList);
                List<ModuleConfigDto> list = new PageUtil<>(moduleCurateList, sizeNum, pageNum).getList();
                list.forEach(module -> {
                    //1表示要查询对象表单下是否有数据,分系统和模块
                    if(StringUtils.isNotBlank(String.valueOf(map.get("getFormFlag"))) && map.get("getFormFlag").equals("1")) {
                        String subSystemColor = maintainService.getModelColor(module.getModelInfo(), treeName,jsonObject);
                        module.setModelColor(subSystemColor);
                    }
                });
                long count = dataPacketClient.moduleCount(map);
                return ResultBody.success(new GridView<>(list, count));
            }
            //部分配置的情况，需要从801库中查询已经配置的项，若配置为是则展示，不展示配置为否的模板
            List<ModuleConfigDto> moduleCurateList = new ArrayList<>();
            //循环远程结果集
            moduleManageInfoList.forEach(model -> {
                //记录此数据是否在801库中不存在
                boolean flag = true;
                ModuleConfigDto moduleConfigDto = new ModuleConfigDto();
                BeanUtils.copyProperties(model, moduleConfigDto);
                moduleConfigDto.setIsPackage("1");
                for (int i = 0; i < moduleCurateByNodeIdList.size(); i++) {
                    ModuleCurate moduleCurate = moduleCurateByNodeIdList.get(i);
                    //如果当前数据存在且IsPackage为是则展示
                    if (moduleCurate.getCode().equals(moduleConfigDto.getCode())) {
                        flag = false;
//                        moduleConfigDto.setIsPackage(moduleCurate.getIsPackage());
                        //只展示配置为是的
                        if ("1".equals(moduleCurate.getIsPackage())) {
//                            ModuleCurate moduleCurateUpdate=new ModuleCurate();
//                            moduleCurateUpdate.setIsPackage("1");
//                            moduleCurateUpdate.setCode(model.getCode());
//                            moduleCurateUpdate.setModelInfo(model.getModelInfo());
//                            moduleCurateUpdate.setNodeId(nodeId);
//                            moduleCurateArrayList.add(moduleCurateUpdate);
                            moduleCurateList.add(moduleConfigDto);
                        }
                        break;
                    }
                }
                //flag=true则是当前801库里没有，需要添加至801库
                if (flag) {
                    ModuleCurate moduleCurateUpdate = new ModuleCurate();
                    moduleCurateUpdate.setIsPackage("1");
                    moduleCurateUpdate.setCode(model.getCode());
                    moduleCurateUpdate.setModelInfo(model.getModelInfo());
                    moduleCurateUpdate.setNodeId(nodeId);
                    moduleCurateArrayList.add(moduleCurateUpdate);
                    //同理，添加至数据库页面也需要展示词此条新数据
                    moduleCurateList.add(moduleConfigDto);
                }
            });
            if (moduleCurateArrayList.size() > 0) {
                moduleCurateRepository.saveAll(moduleCurateArrayList);
            }
            QModuleCurate qModuleCurate = QModuleCurate.moduleCurate;
//            Predicate predicate = qModuleCurate.isPackage.eq("1");
//            predicate=ExpressionUtils.and(predicate,qModuleCurate.nodeId.eq(nodeId));

            List<ModuleConfigDto> list = new PageUtil<>(moduleCurateList, sizeNum, pageNum).getList();
//            QModuleCurate qModuleCurate = QModuleCurate.moduleCurate;
//            Predicate predicate = qModuleCurate.nodeId.eq(nodeId);
//            predicate = ExpressionUtils.and(predicate, qModuleCurate.isPackage.eq("1"));
//            //查询出所有已经选择了是否成包的数据的code
//            List<String> dataCodeList = jpaQueryFactory.select(qModuleCurate.code)
//                    .from(qModuleCurate)
//                    .where(predicate)
//                    .fetch();
//            //根据code信息去中台库里查询对应的name
//            moduleManageDtoList = iDataPacketClient.centerDataList(dataCodeList, map);
//            Long aLong = iDataPacketClient.centerDataCount(dataCodeList, map);
            list.forEach(module -> {
                //1表示要查询对象表单下是否有数据,分系统和模块
                if(StringUtils.isNotBlank(String.valueOf(map.get("getFormFlag"))) && map.get("getFormFlag").equals("1")) {
                    String subSystemColor = maintainService.getModelColor(module.getModelInfo(), treeName,jsonObject);
                    module.setModelColor(subSystemColor);
                }
            });
            return ResultBody.success(new GridView<>(list, moduleCurateList.size()));
        } catch (Exception e) {
            LOGGER.error("错误信息:" + e.getMessage());
        }
        return ResultBody.success(new GridView<>(moduleManageDtoList, 0));
    }

    @Override
    public List<ModuleColumnConfig> getdynamicList(Map map) {
        String code = map.get("code").toString();
        QModuleColumnConfig qModuleColumnConfig = QModuleColumnConfig.moduleColumnConfig;
        Predicate predicate = qModuleColumnConfig.code.eq(code);
        predicate = ExpressionUtils.and(predicate, qModuleColumnConfig.isSearch.eq("1"));
        predicate = ExpressionUtils.and(predicate, qModuleColumnConfig.needColumn.eq("1"));
        List<ModuleColumnConfig> fetch = jpaQueryFactory.selectFrom(qModuleColumnConfig)
                .where(predicate)
                .orderBy(qModuleColumnConfig.sortNumber.asc())
                .fetch();
        return fetch;
    }

    private List<TreeDto> buildTree(List<ModuleTree> ls, List<Map<String, Object>> collect, int tier, Map<String, String> idList, List<String> textList) {
        try {
            List<TreeDto> treeDtos = new ArrayList<>();
            for (ModuleTree s : ls) {
                TreeDto map = new TreeDto();
                map.setText(s.getText());
                if (tier == 0) {
                    map.setId(s.getId());
                    map.setPid(s.getPid());
                }
                List<Map<String, Object>> collect1 = new ArrayList<>();
                for (Map<String, Object> collects : collect) {
                    if (collects.get(list.get(tier)).equals(s.getText()) && CollectionUtils.contains(textList, collects.get("F_DRAWING_NO").toString())) {
                        collect1.add(collects);
                    }
                }
                if (tier + 1 < list.size()) {
                    Set<String> set = new HashSet<>();
                    List<ModuleTree> collect2 = new ArrayList<>();
                    for (Map<String, Object> collects : collect1) {
                        String s1 = collects.get(list.get(tier + 1)).toString();
                        set.add(s1);
                    }
                    set.stream().forEach(item -> {
                        ModuleTree moduleTree = new ModuleTree();
                        moduleTree.setText(item);
                        moduleTree.setId(UUID.randomUUID().toString());
                        moduleTree.setPid(s.getId());
                        collect2.add(moduleTree);
                    });
                    if (tier < 3 && tier != 0) {
                        for (Map<String, Object> collects : collect1) {
                            if (collects.get(list.get(tier)).toString().equals(s.getText())) {
                                map.setId(s.getId());
                                map.setPid(s.getPid());
                                QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                                queryNodeDTO.setFirstNode(FlowStructureEnum.STANDALONE.getValue());
                                queryNodeDTO.setSecondNode(collects.get("F_CLASSIFICATION").toString());
                                queryNodeDTO.setThirdlyNode(collects.get("F_DRAWING_NO").toString());
                                queryNodeDTO.setNodeType(collects.get("F_STAND_ALONE_IDENTIFICATION").toString());
                                queryNodeDTO.setNodeLevel("3");
                                //按照批次号审批的属性值
                                BatchNoNodeInfo batchNoNodeInfo = new BatchNoNodeInfo();
                                batchNoNodeInfo.setFirstNode(FlowStructureEnum.STANDALONE.getValue());
                                batchNoNodeInfo.setType(FlowStructureEnum.STANDALONE.getValue());
                                batchNoNodeInfo.setIssueNo("");
                                batchNoNodeInfo.setModel("");
                                batchNoNodeInfo.setDrawingNo(collects.get("F_DRAWING_NO").toString());
                                if (tier == 2) {
                                    queryNodeDTO.setFourthlyNode(collects.get("F_BATCH_NO").toString());
                                    batchNoNodeInfo.setBatchNo(collects.get("F_BATCH_NO").toString());
                                    queryNodeDTO.setNodeLevel("4");
                                    //批次号展示
                                    queryNodeDTO.setFifthNode("5");
                                }
                                if (idList.get(collects.get("F_CLASSIFICATION") + collects.get("F_DRAWING_NO").toString()).equals(collects.get("F_M_SYS_ID").toString())) {
                                    queryNodeDTO.setTempID(collects.get("F_M_SYS_ID").toString());
                                } else {
                                    queryNodeDTO.setTempID(idList.get(collects.get("F_CLASSIFICATION") + collects.get("F_DRAWING_NO").toString()));
                                }
                                if (StringUtils.isEmpty(batchNoNodeInfo.getPhysicalNo())){
                                    batchNoNodeInfo.setPhysicalNo("");
                                }
                                map.setBatchNoNodeInfo(batchNoNodeInfo);
                                map.setAttributes(queryNodeDTO);
                            }
                        }
                    }
                    List<TreeDto> mapList = buildTree(collect2, collect1, tier + 1, idList, textList);
                    map.setChildren(mapList);
                } else {
                    for (Map<String, Object> collects : collect1) {
                        if (collects.get(list.get(tier)).toString().equals(s.getText())) {
                            map.setId(collects.get("F_M_SYS_ID").toString());
                            map.setPid(s.getPid());
                            QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                            queryNodeDTO.setFirstNode(FlowStructureEnum.STANDALONE.getValue());
                            queryNodeDTO.setSecondNode(collects.get("F_CLASSIFICATION").toString());
                            queryNodeDTO.setThirdlyNode(collects.get("F_DRAWING_NO").toString());
                            queryNodeDTO.setFourthlyNode(collects.get("F_BATCH_NO").toString());
                            queryNodeDTO.setFifthNode(s.getText());
                            //按照批次号审批的属性值
                            BatchNoNodeInfo batchNoNodeInfo = new BatchNoNodeInfo();
                            batchNoNodeInfo.setFirstNode(FlowStructureEnum.STANDALONE.getValue());
                            batchNoNodeInfo.setType(FlowStructureEnum.STANDALONE.getValue());
                            batchNoNodeInfo.setIssueNo("");
                            batchNoNodeInfo.setModel("");
                            batchNoNodeInfo.setDrawingNo(collects.get("F_DRAWING_NO").toString());
                            batchNoNodeInfo.setBatchNo(collects.get("F_BATCH_NO").toString());
                            batchNoNodeInfo.setPhysicalNo(s.getText());
                            if (idList.get(collects.get("F_CLASSIFICATION") + collects.get("F_DRAWING_NO").toString()).equals(collects.get("F_M_SYS_ID").toString())) {
                                queryNodeDTO.setTempID(collects.get("F_M_SYS_ID").toString());
                            } else {
                                queryNodeDTO.setTempID(idList.get(collects.get("F_CLASSIFICATION") + collects.get("F_DRAWING_NO").toString()));
                            }
                            queryNodeDTO.setNodeType(collects.get("F_STAND_ALONE_IDENTIFICATION").toString());
                            queryNodeDTO.setNodeLevel("5");
                            map.setAttributes(queryNodeDTO);
                            map.setBatchNoNodeInfo(batchNoNodeInfo);
                        }
                    }
                    map.setChildren(new ArrayList<>());
                }
                treeDtos.add(map);
            }
            return treeDtos;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<TreeDto> buildTrees(List<ModuleTree> ls, List<Map<String, Object>> collect, int tier, Map<String, String> idList) {
        List<TreeDto> treeDtos = new ArrayList<>();
        for (ModuleTree s : ls) {
            //第二层级
            TreeDto map = new TreeDto();
            map.setText(s.getText());
            if (tier == 0) {
                map.setId(s.getId());
                map.setPid(s.getPid());
            }
            //第三层级
            List<TreeDto> collect3 = new ArrayList<>();
            //第四层级
            List<TreeDto> collect4 = new ArrayList<>();
            //第五层级
            List<TreeDto> collect5 = new ArrayList<>();
            Map<String, String> mapId = new HashMap<>();
            for (Map<String, Object> collects : collect) {
                if (collects.get(list.get(tier)).equals(s.getText())) {
                    TreeDto moduleTree = new TreeDto();
                    String s1 = collects.get(list.get(tier + 1)).toString();
                    String id = mapId.get(s1);
                    if (StringUtils.isNotBlank(id)) {
                        moduleTree.setId(id);
                    } else {
                        String s3 = UUID.randomUUID().toString();
                        mapId.put(s1, s3);
                        moduleTree.setId(s3);
                    }
                    moduleTree.setText(s1);
                    moduleTree.setPid(s.getId());
                    QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                    queryNodeDTO.setFirstNode(FlowStructureEnum.STANDALONE.getValue());
                    queryNodeDTO.setSecondNode(collects.get("F_CLASSIFICATION").toString());
                    queryNodeDTO.setThirdlyNode(collects.get("F_DRAWING_NO").toString());
                    queryNodeDTO.setNodeType(collects.get("F_STAND_ALONE_IDENTIFICATION").toString());
                    queryNodeDTO.setNodeLevel("3");
                    if (idList.get(collects.get("F_DRAWING_NO").toString()).equals(collects.get("F_M_SYS_ID").toString())) {
                        queryNodeDTO.setTempID(collects.get("F_M_SYS_ID").toString());
                    } else {
                        queryNodeDTO.setTempID(idList.get(collects.get("F_DRAWING_NO").toString()));
                    }
                    moduleTree.setAttributes(queryNodeDTO);
                    if (collects.get(list.get(tier + 1)).equals(moduleTree.getText())) {
                        TreeDto treeDto = new TreeDto();
                        String s2 = collects.get(list.get(tier + 2)).toString();
                        String s4 = mapId.get(s2);
                        if (StringUtils.isNotBlank(s4)) {
                            treeDto.setId(s4);
                        } else {
                            String s3 = UUID.randomUUID().toString();
                            mapId.put(s2, s3);
                            treeDto.setId(s3);
                        }
                        treeDto.setText(s2);
                        treeDto.setPid(moduleTree.getId());
                        QueryNodeDTO queryNodeDTO1 = new QueryNodeDTO();
                        queryNodeDTO1.setFirstNode(FlowStructureEnum.STANDALONE.getValue());
                        queryNodeDTO1.setSecondNode(collects.get("F_CLASSIFICATION").toString());
                        queryNodeDTO1.setThirdlyNode(collects.get("F_DRAWING_NO").toString());
                        queryNodeDTO1.setNodeType(collects.get("F_STAND_ALONE_IDENTIFICATION").toString());
                        queryNodeDTO1.setFourthlyNode(collects.get("F_BATCH_NO").toString());
                        queryNodeDTO1.setNodeLevel("4");
                        if (idList.get(collects.get("F_DRAWING_NO").toString()).equals(collects.get("F_M_SYS_ID").toString())) {
                            queryNodeDTO1.setTempID(collects.get("F_M_SYS_ID").toString());
                        } else {
                            queryNodeDTO1.setTempID(idList.get(collects.get("F_DRAWING_NO").toString()));
                        }
                        treeDto.setAttributes(queryNodeDTO1);
                        if (collects.get(list.get(tier + 2)).equals(treeDto.getText())) {
                            TreeDto treeDto1 = new TreeDto();
                            String s3 = collects.get(list.get(tier + 3)).toString();
                            treeDto1.setText(s3);
                            treeDto1.setId(collects.get("F_M_SYS_ID").toString());
                            treeDto1.setPid(treeDto.getId());
                            QueryNodeDTO queryNodeDTO2 = new QueryNodeDTO();
                            queryNodeDTO2.setFirstNode(FlowStructureEnum.STANDALONE.getValue());
                            queryNodeDTO2.setSecondNode(collects.get("F_CLASSIFICATION").toString());
                            queryNodeDTO2.setThirdlyNode(collects.get("F_DRAWING_NO").toString());
                            queryNodeDTO2.setNodeType(collects.get("F_STAND_ALONE_IDENTIFICATION").toString());
                            queryNodeDTO2.setFourthlyNode(collects.get("F_BATCH_NO").toString());
                            queryNodeDTO2.setFifthNode(s3);
                            queryNodeDTO2.setNodeLevel("5");
                            if (idList.get(collects.get("F_DRAWING_NO").toString()).equals(collects.get("F_M_SYS_ID").toString())) {
                                queryNodeDTO2.setTempID(collects.get("F_M_SYS_ID").toString());
                            } else {
                                queryNodeDTO2.setTempID(idList.get(collects.get("F_DRAWING_NO").toString()));
                            }
                            treeDto1.setAttributes(queryNodeDTO2);
                            collect5.add(treeDto1);
                            treeDto.setChildren(collect5);
                            collect4.add(treeDto);
                            moduleTree.setChildren(collect4);
                            collect3.add(moduleTree);
                        }
                    }
                }
            }
            ArrayList<TreeDto> collectTree3 = collect3.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(p -> p.getText())
                    )), ArrayList::new
            ));
            collectTree3.stream().forEach(item -> {
                List<TreeDto> result4 = new ArrayList<>();
                List<TreeDto> children = item.getChildren();
                children.stream().forEach(child4 -> {
                    if (item.getId().equals(child4.getPid())) {
                        result4.add(child4);
                        List<TreeDto> result5 = new ArrayList<>();
                        List<TreeDto> children1 = child4.getChildren();
                        children1.stream().forEach(child5 -> {
                            if (child4.getId().equals(child5.getPid())) {
                                result5.add(child5);
                            }
                        });
                        child4.setChildren(result5);
                    }
                });
                item.setChildren(result4);
            });
            map.setChildren(collectTree3);
            treeDtos.add(map);
        }
        return treeDtos;
    }
}
