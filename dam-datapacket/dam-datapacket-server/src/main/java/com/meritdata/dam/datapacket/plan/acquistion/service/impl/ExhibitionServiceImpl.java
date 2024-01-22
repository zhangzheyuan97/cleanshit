package com.meritdata.dam.datapacket.plan.acquistion.service.impl;

import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.ExhibitionDTO;
import com.meritdata.dam.datapacket.plan.acquistion.vo.QueryNodeDTO;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.impl.PackageSystemImpl;
import com.meritdata.dam.datapacket.plan.utils.PageResult;
import com.meritdata.dam.datapacket.plan.acquistion.service.IExhibitionService;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleTree;
import com.meritdata.dam.datapacket.plan.model.entity.QModuleTree;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStructureEnum;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class ExhibitionServiceImpl implements IExhibitionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExhibitionServiceImpl.class);

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IModulePlanService iModulePlanService;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    MaintainServiceImpl maintainService;

    @Autowired
    private PackageSystemImpl packageSystem;

    @Autowired
    private IStandAloneService standAloneService;

    @Override
    public List<TreeDto> exhibitionTree(String userId) {
        Map<String, Object> map = new HashMap<>();
        try {
            long start = System.currentTimeMillis();
            List<Map<String, Object>> mapsFCInfo = iDatamationsClient.getFCInfo(map);
            List<TreeDto> treeDtoList = iModulePlanService.tree("-1", userId);
            QModuleTree qModuleTree = QModuleTree.moduleTree;
            List<String> ids = new ArrayList<>();
            treeDtoList.stream().forEach(item -> {
                item.getChildren().stream().forEach(itemChildren -> {
                    ids.add(itemChildren.getPid());
                });
            });
            List<ModuleTree> fetch = jpaQueryFactory.selectFrom(qModuleTree).where(qModuleTree.id.in(ids)).fetch();
            HashMap<String, ModuleTree> map2 = new HashMap<>();
            fetch.forEach(item -> {
                map2.put(item.getId(), item);
            });
            //查询发次表的发次信息，挂在型号下，构建实做BOM树结构
            for (TreeDto tree : treeDtoList) {
                List<TreeDto> LBchildren = tree.getChildren();
                for (TreeDto lbchildren : LBchildren) {
                    //判断型号对应的EBOM表格中的的父id与发次表中记录的分类信息一样则挂载
                    ModuleTree moduleTree = map2.get(lbchildren.getPid());
//                    ModuleTree moduleTree = jpaQueryFactory.selectFrom(qModuleTree).where(qModuleTree.id.eq(lbchildren.getPid())).fetchOne();
                    AtomicBoolean flag = new AtomicBoolean(false);
                    //用发次表中的分类信息和型号信息作为挂载条件，满足则过载至对应的树节点
                    List<TreeDto> XHchildren = lbchildren.getChildren();
                    for (TreeDto xhchildren : XHchildren) {
                        List<TreeDto> dtoList = new ArrayList<>();
                        mapsFCInfo.stream().forEach(item -> {
                            if (lbchildren.getText().equals(item.get("F_CLASSIFICATION"))) {
                                if (xhchildren.getText().equals(item.get("F_MODEL"))) {
                                    //挂载至分析还是模块
                                    String subSystem = "";
                                    if ("0".equals(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "1" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString())) {
//                                        subSystem = "模块";
                                        subSystem = FlowStructureEnum.MODULE.getValue();
//                                        LOGGER.info("模块:"+subSystem);
                                    } else if ("1".equals(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "1" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString())) {
//                                        subSystem = "分系统";
                                        subSystem = FlowStructureEnum.SUBSYSTEM.getValue();
//                                        LOGGER.info("分系统:"+subSystem);
                                    }
                                    //区分分系统还是模块信息
                                    if (moduleTree != null && moduleTree.getText().equals(subSystem)) {
                                        QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                                        queryNodeDTO.setNodeLevel("4");
                                        queryNodeDTO.setNodeType(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString());
                                        flag.set(true);
                                        TreeDto t = new TreeDto();
                                        t.setId(item.get("F_M_SYS_ID").toString());
                                        t.setPid(xhchildren.getId());
                                        t.setText(item.get("F_ISSUE_NO").toString());
                                        t.setAttributes(queryNodeDTO);
                                        dtoList.add(t);
                                    }
                                }
                            }
                        });
                        ArrayList<TreeDto> treeDtoListTHTemp = dtoList.stream().collect(Collectors.collectingAndThen(
                                Collectors.toCollection(() -> new TreeSet<>(
                                        Comparator.comparing(p -> p.getText())
                                )), ArrayList::new
                        ));
                        xhchildren.setChildren(treeDtoListTHTemp);
                    }
                }
            }
            //查询单机与总装直属件的信息，挂在单机模块下，构建单机树结构
            //挂载单机
            List<Map<String, Object>> maps = iDatamationsClient.exhibitionTree(new HashMap<>(), "PHYSICAL_OBJECT_SINGLE_MACHINE");
            //获取权限表信息
            PackageSystemEntity packageSystemEntity = new PackageSystemEntity();
            packageSystemEntity.setSystemId(userId);
            packageSystemEntity.setType("person");
            List<PackageSystemEntity> byEntity = packageSystem.findAuthorityDataByEntity(packageSystemEntity);
            List<String> textList = byEntity.stream().map(PackageSystemEntity::getResourceId).collect(Collectors.toList());
            LOGGER.info("当前用户权限信息:" + textList.toString());
            //获取单机的数据
            List<TreeDto> DJListTree = treeDtoList.stream().filter(item -> item.getText().equals(FlowStructureEnum.STANDALONE.getValue())).collect(Collectors.toList());
            DJListTree.stream().forEach(item -> {
//                if (item.getText().equals("单机")) {
                item.getChildren().stream().forEach(child -> {
                    List<TreeDto> treeDtoListTH = new ArrayList<>();
                    for (int i = 0; i < maps.size(); i++) {
                        Map<String, Object> stringObjectMap = maps.get(i);
                        if (maps.size() > 0
                                && stringObjectMap.get("F_CLASSIFICATION") != null
                                && child.getText().equals(stringObjectMap.get("F_CLASSIFICATION"))
                                && stringObjectMap.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED") != null
                                && stringObjectMap.get("F_STAND_ALONE_IDENTIFICATION") != null
                                && "1".equals(stringObjectMap.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED"))
                                && "1".equals(stringObjectMap.get("F_STAND_ALONE_IDENTIFICATION"))) {
                            QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                            queryNodeDTO.setFirstNode(item.getText());
                            queryNodeDTO.setSecondNode(stringObjectMap.get("F_CLASSIFICATION").toString());
                            queryNodeDTO.setThirdlyNode(stringObjectMap.get("F_DRAWING_NO").toString());
                            queryNodeDTO.setNodeLevel("3");
                            queryNodeDTO.setNodeType("3");
                            if (stringObjectMap.get("F_DRAWING_NO") != null
                                    && textList.contains(stringObjectMap.get("F_DRAWING_NO").toString())) {
                                TreeDto treeDto = new TreeDto();
                                //单机第三级，前端node需要根据单机、发动机、图号信息展示图号下面的所有所有批次信息
                                treeDto.setId(stringObjectMap.get("F_M_SYS_ID").toString());
                                treeDto.setPid(child.getId());
                                treeDto.setText(stringObjectMap.get("F_DRAWING_NO").toString());
                                treeDto.setAttributes(queryNodeDTO);
                                List<TreeDto> treeDtoListPC = new ArrayList<>();
                                //首先根据获取maps中所有当前图号信息，然后遍历这些图号信息挂载批次
                                List<Map<String, Object>> collect1 = maps.stream().filter(filterItem -> filterItem.get("F_DRAWING_NO").equals(stringObjectMap.get("F_DRAWING_NO").toString())).collect(Collectors.toList());
                                for (int j = 0; j < collect1.size(); j++) {
                                    Map<String, Object> stringObjectMap1 = collect1.get(j);
                                    if (child.getText().equals(stringObjectMap1.get("F_CLASSIFICATION"))
                                            && stringObjectMap1.get("F_CLASSIFICATION") != null
                                            && child.getText().equals(stringObjectMap1.get("F_CLASSIFICATION"))
                                            && stringObjectMap1.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED") != null
                                            && stringObjectMap1.get("F_STAND_ALONE_IDENTIFICATION") != null
                                            && "1".equals(stringObjectMap1.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED"))
                                            && "1".equals(stringObjectMap1.get("F_STAND_ALONE_IDENTIFICATION"))) {
                                        QueryNodeDTO queryNodeDTO1 = new QueryNodeDTO();
                                        queryNodeDTO1.setFirstNode(item.getText());
                                        queryNodeDTO1.setSecondNode(stringObjectMap1.get("F_CLASSIFICATION").toString());
                                        queryNodeDTO1.setThirdlyNode(stringObjectMap1.get("F_DRAWING_NO").toString());
                                        queryNodeDTO1.setFourthlyNode(stringObjectMap1.get("F_BATCH_NO").toString());
                                        queryNodeDTO1.setNodeLevel("4");
                                        queryNodeDTO1.setNodeType("3");
                                        if ((treeDto.getText()).equals(stringObjectMap1.get("F_DRAWING_NO"))) {
                                            TreeDto treeDto1 = new TreeDto();
                                            treeDto1.setId(UUID.randomUUID().toString());
                                            treeDto1.setPid(stringObjectMap.get("F_M_SYS_ID").toString());
                                            treeDto1.setText(stringObjectMap1.get("F_BATCH_NO").toString());
                                            treeDto1.setAttributes(queryNodeDTO1);
                                            treeDtoListPC.add(treeDto1);
                                        }
                                    }
                                }
                                ArrayList<TreeDto> collect = treeDtoListPC.stream().collect(Collectors.collectingAndThen(
                                        Collectors.toCollection(() -> new TreeSet<>(
                                                Comparator.comparing(p -> p.getText())
                                        )), ArrayList::new
                                ));
                                treeDto.setChildren(collect);
                                treeDtoListTH.add(treeDto);
                            }
                        }
                    }
                    ArrayList<TreeDto> treeDtoListTHTemp = treeDtoListTH.stream().collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(
                                    Comparator.comparing(p -> p.getText())
                            )), ArrayList::new
                    ));
                    child.setChildren(treeDtoListTHTemp);
                });
                //组装单机
                standAloneService.formatAloneTree(item.getChildren());
//                }
            });
            return treeDtoList;
        } catch (Exception e) {
            LOGGER.error("树构建错误日志：" + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<ExhibitionDTO> dataList(Map<String, Object> map) {
        List<ExhibitionDTO> exhibitionDTOList = new ArrayList<>();
        try {
            PageResult<Map<String, Object>> mapGridView = iDatamationsClient.dataList(map);
            if (mapGridView.getRows().size() > 0) {
                List<Map<String, Object>> rows = mapGridView.getRows();
                for (Map<String, Object> exhibition : rows) {
                    ExhibitionDTO exhibitionDTO = new ExhibitionDTO();
                    //类别
                    exhibitionDTO.setClassIfication(exhibition.get("F_CLASSIFICATION") == null ? "" : exhibition.get("F_CLASSIFICATION").toString());
                    //类型二
                    exhibitionDTO.setSecondType(MapUtils.getString(exhibition,"F_SECONDTYPE",""));
                    //类型三
                    exhibitionDTO.setThirdType(MapUtils.getString(exhibition,"F_THIRDTYPE",""));
                    //图号
                    exhibitionDTO.setDrawingNo(exhibition.get("F_DRAWING_NO") == null ? "" : exhibition.get("F_DRAWING_NO").toString());
                    //名称
                    exhibitionDTO.setName(exhibition.get("F_NAME") == null ? "" : exhibition.get("F_NAME").toString());
                    //批次号
                    exhibitionDTO.setBatchNo(exhibition.get("F_BATCH_NO") == null ? "" : exhibition.get("F_BATCH_NO").toString());
                    //实物号
                    exhibitionDTO.setPhysicalNo(exhibition.get("F_PHYSICAL_NO") == null ? "" : exhibition.get("F_PHYSICAL_NO").toString());
                    exhibitionDTO.setId(exhibition.get("F_M_SYS_ID") == null ? "" : exhibition.get("F_M_SYS_ID").toString());
                    //是否管理到实物
                    String isManageObject = exhibition.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED") == null ? "" : exhibition.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED").toString();
                    exhibitionDTO.setIsManageObject(isManageObject);
                    exhibitionDTOList.add(exhibitionDTO);
                }
            }
            return exhibitionDTOList;
        } catch (Exception e) {
            LOGGER.error("查询错误" + e.getMessage());
        }
        return exhibitionDTOList;
    }
}
