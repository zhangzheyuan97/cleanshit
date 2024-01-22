package com.meritdata.dam.datapacket.plan.acquistion.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.common.UUIDUtil;
import com.meritdata.dam.datapacket.plan.acquistion.service.ILityMatterService;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.QueryNodeDTO;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.model.vo.ModelDataExportParamDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.impl.PackageSystemImpl;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStructureEnum;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class LityMatterServiceImpl implements ILityMatterService {

    @Autowired
    private IDatamationsClient datamationsClient;

    @Autowired
    private MaintainServiceImpl maintainService;

    @Autowired
    ILogPostService logPostService;

    @Autowired
    private PackageSystemImpl packageSystem;

    @Autowired
    private SessionUtils sessionUtils;


    @Autowired
    private IStandAloneService standAloneService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IDataPacketClient dataPacketClient;

    @Override
    public ResultBody addLity(String attributes, String model, String lity) {
        DataOperateDTO param = new DataOperateDTO();
        Map<String, String> data = new HashMap<>();
        List<Map<String, Object>> fcInfo = datamationsClient.getFCInfo(new HashMap<>());
        fcInfo = fcInfo.stream().filter(m -> {
            return m.get("F_ISSUE_NO") != null;
        }).collect(Collectors.toList());
        AtomicBoolean flag = new AtomicBoolean(false);
        for (Map<String, Object> fcInfos : fcInfo) {
            if (fcInfos.get("F_ISSUE_NO").equals(lity)) {
                flag.set(true);
                break;
            }
        }
        if (flag.get()) {
            return ResultBody.failure("发次不可重复");
        }
        if (StringUtils.isBlank(JSONObject.parseObject(attributes).get("nodeType").toString()) && JSONObject.parseObject(attributes).get("nodeType") == null) {
            return ResultBody.success();
        } else {
            if (JSONObject.parseObject(attributes).get("nodeType").toString().equals("2")) {
                data.put("F_SUBSYSTEM_IDENTIFICATION", "0");
            } else {
                data.put("F_SUBSYSTEM_IDENTIFICATION", JSONObject.parseObject(attributes).get("nodeType").toString());
            }
        }

        param.setEffect(true);
        Map<String, String> modelInfoMap = new HashMap<>();

//        if (JSONObject.parseObject(attributes).get("nodeType").toString().equals("1")||
//                JSONObject.parseObject(attributes).get("nodeType").toString().equals("2")) {
        modelInfoMap.put("name", "TIMES_MATERIAL_OBJECT");
        String moduleInfo = datamationsClient.getModuleInfo(modelInfoMap);
        //分类
        data.put("F_CLASSIFICATION", JSONObject.parseObject(attributes).get("secondNode").toString());
        //发次
        data.put("F_ISSUE_NO", lity);
        //型号
        data.put("F_MODEL", model);

        param.setModelId(moduleInfo);
        data.put("S_M_SYS_SECRETLEVEL", "1");
        param.setData(data);
        String[] dataArray = {"varchar", "varchar", "varchar"};
        param.setDataArray(dataArray);
        ResultBody success = datamationsClient.addLity(param, "add");
        if (success.isSuccess()) {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.data.gather.bmodule"),
                    LogPattenUtils.getProperty("model.lity.matter.fmodule"),
                    StrUtil.format(LogPattenUtils.getProperty("model.lity.matter.add"), lity),
                    LogPattenUtils.getProperty("model.lity.matter.add.message"),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
        }
        return success;
    }

    @Override
    public ResultBody addPhysical(String attributes, String classIfication, String secondType, String thirdType, String drawingNo, String name, String batchNo, String physicalNo, String isManageObject) {
        if (isManageObject.equals("0") && datamationsClient.batchNoExist(batchNo) > 0) {
            //不管理到实物 批次全局唯一
            return ResultBody.failure("批次号不可重复");
        }
        DataOperateDTO param = new DataOperateDTO();
        List<Map<String, Object>> djInfo = datamationsClient.getDJInfo(new HashMap<>());
        djInfo = djInfo.stream().filter(m -> {
            return m.get("F_PHYSICAL_NO") != null;
        }).collect(Collectors.toList());
        AtomicBoolean flag = new AtomicBoolean(false);
        for (Map<String, Object> djInfos : djInfo) {
            if (djInfos.get("F_PHYSICAL_NO").equals(physicalNo)) {
                flag.set(true);
                break;
            }
        }
        if (flag.get()) {
            return ResultBody.failure("实物不可重复");
        }
        param.setEffect(true);
        Map<String, String> modelInfoMap = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        modelInfoMap.put("name", "PHYSICAL_OBJECT_SINGLE_MACHINE");
        String moduleInfo = datamationsClient.getModuleInfo(modelInfoMap);
        //图号
        data.put("F_DRAWING_NO", drawingNo);
        //名称
        data.put("F_NAME", name);
        //批次
        data.put("F_BATCH_NO", batchNo);
        //实物
        data.put("F_PHYSICAL_NO", physicalNo);
        if (JSONObject.parseObject(attributes).get("nodeType").toString().equals("3")) {
            //单机有类别
            data.put("F_CLASSIFICATION", classIfication);
            //单机的类型二
            data.put("F_SECONDTYPE", secondType);
            //单机的类型三
            data.put("F_THIRDTYPE", thirdType);
            //单机标识为1
            data.put("F_STAND_ALONE_IDENTIFICATION", "1");
            //是否管理到实物为默认1,管理到实物
            data.put("F_WHETHER_PHYSICAL_OBJECTS_MANAGED", "1");
            String[] dataArray = {"varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar"};
            param.setDataArray(dataArray);

        } else {
            //单机标识为0
            data.put("F_STAND_ALONE_IDENTIFICATION", "0");
            //是否管理到实物
            data.put("F_WHETHER_PHYSICAL_OBJECTS_MANAGED", isManageObject);
            String[] dataArray = {"varchar", "varchar", "varchar", "varchar", "varchar", "varchar"};
            param.setDataArray(dataArray);
        }
        param.setModelId(moduleInfo);
        data.put("S_M_SYS_SECRETLEVEL", "1");
        param.setData(data);

        ResultBody success = datamationsClient.addLity(param, "add");
        if (success.isSuccess()) {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.data.gather.bmodule"),
                    LogPattenUtils.getProperty("model.lity.matter.fmodule"),
                    StringUtils.isNotEmpty(physicalNo) ? StrUtil.format(LogPattenUtils.getProperty("model.lity.physical.add"), batchNo, physicalNo)
                            : StrUtil.format(LogPattenUtils.getProperty("model.lity.batchNo.add"), batchNo),
                    LogPattenUtils.getProperty(StringUtils.isNotEmpty(physicalNo) ? "model.lity.physical.add.message" : "model.lity.batchNo.add.message"),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
        }
        return success;
    }

    @Override
    public ResultBody deletedata(List<Map<String, Object>> selectGridData, String tableName) {

        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicBoolean FcFlag = new AtomicBoolean(false);
        //根据图号,实物号查询配套清单表是否被引用
        selectGridData.forEach(item -> {
            if ("TIMES_MATERIAL_OBJECT".equals(tableName)) {
                //根据id 获取分系统表的名称
                Map<String, Object> fcInfoByMsysId = datamationsClient.getFCInfoByMsysId(item.get("F_M_SYS_ID") != null ? item.get("F_M_SYS_ID").toString() : "");

                long fcInfo = datamationsClient.getFcInfo(fcInfoByMsysId.get("F_ISSUE_NO") != null ? fcInfoByMsysId.get("F_ISSUE_NO").toString() : "");
                if (fcInfo > 0) {
                    flag.set(true);
                    FcFlag.set(true);
                }
            } else {
                String fMSysId = item.get("F_M_SYS_ID") != null ? item.get("F_M_SYS_ID").toString() : "";
                //用fMSysId查询单机分系统表中的图号，批次，单机实物号
                Map map = new HashMap();
                map.put("F_M_SYS_ID", fMSysId);
                Map supporting_list = datamationsClient.getPcAndSwInfo(map, "PHYSICAL_OBJECT_SINGLE_MACHINE");
                if (supporting_list != null && supporting_list.size() > 0) {
                    Map mapUse = new HashMap();
                    mapUse.put("F_DRAWING_NO", supporting_list.get("F_DRAWING_NO") != null ? supporting_list.get("F_DRAWING_NO").toString() : null);
                    mapUse.put("F_PHYSICAL_NO", supporting_list.get("F_PHYSICAL_NO") != null ? supporting_list.get("F_PHYSICAL_NO").toString() : null);
                    mapUse.put("F_BATCH_NO", supporting_list.get("F_BATCH_NO") != null ? supporting_list.get("F_BATCH_NO").toString() : null);
                    Map pcAndSwInfo = datamationsClient.getPcAndSwInfo(mapUse, "SUPPORTING_LIST");
                    if (pcAndSwInfo != null && pcAndSwInfo.size() > 0) {
                        flag.set(true);
                    }
                }
            }

        });
        if (flag.get()) {
            if (FcFlag.get()) {
                return ResultBody.failure("该发次已有配置，无法删除");
            }
            return ResultBody.failure("该实物行已被引用，无法删除");
        } else {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.data.gather.bmodule"),
                    LogPattenUtils.getProperty("model.lity.matter.fmodule"),
                    StrUtil.format(tableName.equals("TIMES_MATERIAL_OBJECT") ? LogPattenUtils.getProperty("model.lity.matter.dalete") : LogPattenUtils.getProperty("model.lity.physical.dalete")),
                    LogPattenUtils.getProperty(tableName.equals("TIMES_MATERIAL_OBJECT") ? "model.lity.matter.dalete.message" : "model.lity.physical.dalete.message"),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);

        }
        ModelDataExportParamDto modelDataExportParam = new ModelDataExportParamDto();
        modelDataExportParam.setSelectGridData(selectGridData);
        Map<String, String> modelInfoMap = new HashMap<>();
        modelInfoMap.put("name", tableName);
        String moduleInfo = datamationsClient.getModuleInfo(modelInfoMap);
        modelDataExportParam.setModelId(moduleInfo);
        return datamationsClient.deletedata("dataManageInit", modelDataExportParam);
    }

    @Override
    public List<TreeDto> addTreeNode(String userId, List<TreeDto> dataTypeTree) {
        //获取权限表信息
        PackageSystemEntity packageSystemEntity = new PackageSystemEntity();
        packageSystemEntity.setSystemId(userId);
        packageSystemEntity.setType("person");
        List<PackageSystemEntity> byEntity = packageSystem.findAuthorityDataByEntity(packageSystemEntity);
        List<String> textList = byEntity.stream().map(PackageSystemEntity::getResourceId).collect(Collectors.toList());
        List<Map<String, Object>> maps = datamationsClient.exhibitionTree(new HashMap<>(), "PHYSICAL_OBJECT_SINGLE_MACHINE");
        //挂载单机
        //获取单机的数据
        List<TreeDto> DJListTree = dataTypeTree.stream().filter(item -> item.getText().equals(FlowStructureEnum.STANDALONE.getValue())).collect(Collectors.toList());
        DJListTree.stream().forEach(item -> {
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
                            && "1".equals(stringObjectMap.get("F_STAND_ALONE_IDENTIFICATION"))
                            && stringObjectMap.get("F_DRAWING_NO") != null
                            && textList.contains(stringObjectMap.get("F_DRAWING_NO").toString())) {
                        QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                        queryNodeDTO.setFirstNode(item.getText());
                        queryNodeDTO.setSecondNode(stringObjectMap.get("F_CLASSIFICATION").toString());
                        queryNodeDTO.setThirdlyNode(stringObjectMap.get("F_DRAWING_NO").toString());
                        queryNodeDTO.setNodeLevel("3");
                        queryNodeDTO.setNodeType("3");
                        TreeDto treeDto = new TreeDto();
                        //单机第三级，前端node需要根据单机、发动机、图号信息展示图号下面的所有所有批次信息
                        treeDto.setId(UUID.randomUUID().toString());
                        treeDto.setPid(child.getId());
                        treeDto.setText(stringObjectMap.get("F_DRAWING_NO").toString());
                        queryNodeDTO.setUUID(UUID.randomUUID().toString());
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
                                    && "1".equals(stringObjectMap1.get("F_STAND_ALONE_IDENTIFICATION"))
                            ) {
                                QueryNodeDTO queryNodeDTO1 = new QueryNodeDTO();
                                queryNodeDTO1.setFirstNode(item.getText());
                                queryNodeDTO1.setSecondNode(stringObjectMap1.get("F_CLASSIFICATION").toString());
                                queryNodeDTO1.setThirdlyNode(stringObjectMap1.get("F_DRAWING_NO").toString());
                                queryNodeDTO1.setFourthlyNode(stringObjectMap1.get("F_BATCH_NO").toString());
                                queryNodeDTO1.setNodeLevel("4");
                                queryNodeDTO1.setNodeType("3");

                                if (stringObjectMap1.get("F_NAME") != null) {
                                    queryNodeDTO1.setName(stringObjectMap1.get("F_NAME").toString());
                                }
                                if ((treeDto.getText()).equals(stringObjectMap1.get("F_DRAWING_NO"))) {
                                    TreeDto treeDto1 = new TreeDto();
                                    treeDto1.setId(UUID.randomUUID().toString());
                                    treeDto1.setPid(treeDto.getId());
                                    treeDto1.setText(stringObjectMap1.get("F_BATCH_NO").toString());
                                    queryNodeDTO1.setUUID(UUID.randomUUID().toString());
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
                ArrayList<TreeDto> treeDtoListTHTemp = treeDtoListTH.stream().collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(
                                Comparator.comparing(p -> p.getText())
                        )), ArrayList::new
                ));
                child.setChildren(treeDtoListTHTemp);
            });
            //组装单机
            standAloneService.formatAloneTree(item.getChildren());
        });
        //挂载总装直属件
        TreeDto treeDto = new TreeDto();
        String uuid = UUIDUtil.genUUID();
        treeDto.setId(uuid);
        treeDto.setText(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
        treeDto.setPid("-1");
        QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
        queryNodeDTO.setNodeType("4");
        queryNodeDTO.setFirstNode(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
        queryNodeDTO.setNodeLevel("1");
        queryNodeDTO.setUUID(UUID.randomUUID().toString());
        treeDto.setAttributes(queryNodeDTO);
        //挂载总装直属件下级节点
        List<TreeDto> treeDtoList = new ArrayList<>();
        maps.stream().forEach(zsjMap -> {
            if ("0".equals(zsjMap.get("F_STAND_ALONE_IDENTIFICATION") == null ? "1" : zsjMap.get("F_STAND_ALONE_IDENTIFICATION").toString())
                    && zsjMap.get("F_DRAWING_NO") != null
                    && textList.contains(zsjMap.get("F_DRAWING_NO").toString())) {
                TreeDto treeDto1 = new TreeDto();
                treeDto1.setPid(treeDto.getId());
                treeDto1.setId(UUID.randomUUID().toString());
                treeDto1.setText(zsjMap.get("F_DRAWING_NO").toString());
                QueryNodeDTO queryNodeDTO1 = new QueryNodeDTO();
                queryNodeDTO1.setNodeType("4");
                queryNodeDTO1.setFirstNode(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
                queryNodeDTO1.setSecondNode(zsjMap.get("F_DRAWING_NO").toString());
                queryNodeDTO1.setNodeLevel("3");
                queryNodeDTO1.setUUID(UUID.randomUUID().toString());
                treeDto1.setAttributes(queryNodeDTO1);
                List<TreeDto> treeDtoChildList = new ArrayList<>();
                maps.stream().forEach(zsjpc -> {
                    if (zsjpc.get("F_DRAWING_NO") != null && zsjpc.get("F_DRAWING_NO").toString().equals(zsjMap.get("F_DRAWING_NO").toString())
                            && zsjpc.get("F_STAND_ALONE_IDENTIFICATION") != null
                            && !"1".equals(zsjpc.get("F_STAND_ALONE_IDENTIFICATION"))) {
                        TreeDto treeDtoChild = new TreeDto();
                        if (zsjMap.get("F_M_SYS_ID") != null) {
                            treeDtoChild.setPid(treeDto1.getId());
                        }

                        if (zsjpc.get("F_M_SYS_ID") != null) {
                            treeDtoChild.setId(UUID.randomUUID().toString());
                        }

                        if (zsjpc.get("F_BATCH_NO") != null) {
                            treeDtoChild.setText(zsjpc.get("F_BATCH_NO").toString());
                        }
                        QueryNodeDTO queryNodeDTO2 = new QueryNodeDTO();
                        queryNodeDTO2.setNodeType("4");
                        queryNodeDTO2.setFirstNode(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
                        if (zsjMap.get("F_DRAWING_NO") != null) {
                            queryNodeDTO2.setSecondNode(zsjMap.get("F_DRAWING_NO").toString());
                        }
                        if (zsjpc.get("F_BATCH_NO") != null) {
                            queryNodeDTO2.setThirdlyNode(zsjpc.get("F_BATCH_NO").toString());
                        }
                        queryNodeDTO2.setNodeLevel("4");
                        if (zsjpc.get("F_NAME") != null) {
                            queryNodeDTO2.setName(zsjpc.get("F_NAME").toString());
                        }
                        queryNodeDTO2.setUUID(UUID.randomUUID().toString());
                        treeDtoChild.setAttributes(queryNodeDTO2);
                        treeDtoChildList.add(treeDtoChild);
                    }
                });
                ArrayList<TreeDto> collect = treeDtoChildList.stream().collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(
                                Comparator.comparing(p -> p.getText())
                        )), ArrayList::new
                ));
                treeDto1.setChildren(collect);
                treeDtoList.add(treeDto1);
            }
        });
        ArrayList<TreeDto> collect = treeDtoList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(p -> p.getText())
                )), ArrayList::new
        ));
        treeDto.setChildren(collect);
        dataTypeTree.add(treeDto);
        return dataTypeTree;
    }

    @Override
    public List<TreeDto> addlicenseTreeNode(List<TreeDto> dataTypeTree) {

        List<Map<String, Object>> maps = datamationsClient.exhibitionTree(new HashMap<>(), "PHYSICAL_OBJECT_SINGLE_MACHINE");
        //挂载单机
        //获取单机的数据
        List<TreeDto> DJListTree = dataTypeTree.stream().filter(item -> item.getText().equals(FlowStructureEnum.STANDALONE.getValue())).collect(Collectors.toList());
        DJListTree.stream().forEach(item -> {
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
                        TreeDto treeDto = new TreeDto();
                        //单机第三级，前端node需要根据单机、发动机、图号信息展示图号下面的所有所有批次信息
                        treeDto.setId(UUID.randomUUID().toString());
                        treeDto.setPid(child.getId());
                        treeDto.setText(stringObjectMap.get("F_DRAWING_NO").toString());
                        queryNodeDTO.setUUID(UUID.randomUUID().toString());
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
                                if (stringObjectMap1.get("F_NAME") != null) {
                                    queryNodeDTO1.setName(stringObjectMap1.get("F_NAME").toString());
                                }
                                if ((treeDto.getText()).equals(stringObjectMap1.get("F_DRAWING_NO"))) {
                                    TreeDto treeDto1 = new TreeDto();
                                    treeDto1.setId(UUID.randomUUID().toString());
                                    treeDto1.setPid(treeDto.getId());
                                    treeDto1.setText(stringObjectMap1.get("F_BATCH_NO").toString());
                                    queryNodeDTO1.setUUID(UUID.randomUUID().toString());
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
                ArrayList<TreeDto> treeDtoListTHTemp = treeDtoListTH.stream().collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(
                                Comparator.comparing(p -> p.getText())
                        )), ArrayList::new
                ));
                child.setChildren(treeDtoListTHTemp);
            });
        });
        //挂载总装直属件
        TreeDto treeDto = new TreeDto();
        String uuid = UUIDUtil.genUUID();
        treeDto.setId(uuid);
        treeDto.setText(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
        treeDto.setPid("-1");
        QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
        queryNodeDTO.setNodeType("4");
        queryNodeDTO.setFirstNode(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
        queryNodeDTO.setNodeLevel("1");
        queryNodeDTO.setUUID(UUID.randomUUID().toString());
        treeDto.setAttributes(queryNodeDTO);
        //挂载总装直属件下级节点
        List<TreeDto> treeDtoList = new ArrayList<>();
        maps.stream().forEach(zsjMap -> {
            if ("0".equals(zsjMap.get("F_STAND_ALONE_IDENTIFICATION") == null ? "1" : zsjMap.get("F_STAND_ALONE_IDENTIFICATION").toString())) {
                TreeDto treeDto1 = new TreeDto();
                treeDto1.setPid(treeDto.getId());
                treeDto1.setId(UUID.randomUUID().toString());
                treeDto1.setText(zsjMap.get("F_DRAWING_NO").toString());
                QueryNodeDTO queryNodeDTO1 = new QueryNodeDTO();
                queryNodeDTO1.setNodeType("4");
                queryNodeDTO1.setFirstNode(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
                queryNodeDTO1.setSecondNode(zsjMap.get("F_DRAWING_NO").toString());
                queryNodeDTO1.setNodeLevel("3");
                queryNodeDTO1.setUUID(UUID.randomUUID().toString());
                treeDto1.setAttributes(queryNodeDTO1);
                List<TreeDto> treeDtoChildList = new ArrayList<>();
                maps.stream().forEach(zsjpc -> {
                    if (zsjpc.get("F_DRAWING_NO") != null && zsjpc.get("F_DRAWING_NO").toString().equals(zsjMap.get("F_DRAWING_NO").toString())
                            && zsjpc.get("F_STAND_ALONE_IDENTIFICATION") != null
                            && !"1".equals(zsjpc.get("F_STAND_ALONE_IDENTIFICATION"))) {
                        TreeDto treeDtoChild = new TreeDto();
                        if (zsjMap.get("F_M_SYS_ID") != null) {
                            treeDtoChild.setPid(treeDto1.getId());
                        }

                        if (zsjpc.get("F_M_SYS_ID") != null) {
                            treeDtoChild.setId(UUID.randomUUID().toString());
                        }

                        if (zsjpc.get("F_BATCH_NO") != null) {
                            treeDtoChild.setText(zsjpc.get("F_BATCH_NO").toString());
                        }
                        QueryNodeDTO queryNodeDTO2 = new QueryNodeDTO();
                        queryNodeDTO2.setNodeType("4");
                        queryNodeDTO2.setFirstNode(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
                        if (zsjMap.get("F_DRAWING_NO") != null) {
                            queryNodeDTO2.setSecondNode(zsjMap.get("F_DRAWING_NO").toString());
                        }
                        if (zsjpc.get("F_BATCH_NO") != null) {
                            queryNodeDTO2.setThirdlyNode(zsjpc.get("F_BATCH_NO").toString());
                        }
                        queryNodeDTO2.setNodeLevel("4");
                        if (zsjpc.get("F_NAME") != null) {
                            queryNodeDTO2.setName(zsjpc.get("F_NAME").toString());
                        }
                        queryNodeDTO2.setUUID(UUID.randomUUID().toString());
                        treeDtoChild.setAttributes(queryNodeDTO2);
                        treeDtoChildList.add(treeDtoChild);
                    }
                });
                ArrayList<TreeDto> collect = treeDtoChildList.stream().collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(
                                Comparator.comparing(p -> p.getText())
                        )), ArrayList::new
                ));
                treeDto1.setChildren(collect);
                treeDtoList.add(treeDto1);
            }
        });
        ArrayList<TreeDto> collect = treeDtoList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(p -> p.getText())
                )), ArrayList::new
        ));
        treeDto.setChildren(collect);
        dataTypeTree.add(treeDto);
        return dataTypeTree;
    }

    /**
     * 编辑发次
     * @param mSysId
     * @param attributes
     * @param model
     * @param lity
     * @return
     */
    @Override
    public ResultBody editLity(String lityShadow,String mSysId,String attributes, String model, String lity) {
        //校验发次下是否有数据
        ResultBody resultBody = verifyWhetherEdit(lityShadow,attributes,model,lity);
        if(!resultBody.isSuccess()) {
            return resultBody;
        }

        DataOperateDTO param = new DataOperateDTO();
        Map<String, String> data = new HashMap<>();
        List<Map<String, Object>> fcInfo = datamationsClient.getFCInfo(new HashMap<>());
        fcInfo = fcInfo.stream().filter(m -> {
            return m.get("F_ISSUE_NO") != null;
        }).collect(Collectors.toList());
        AtomicBoolean flag = new AtomicBoolean(false);
        for (Map<String, Object> fcInfos : fcInfo) {
            if (fcInfos.get("F_ISSUE_NO").equals(lity)) {
                flag.set(true);
                break;
            }
        }
        if (flag.get()) {
            return ResultBody.failure("发次不可重复");
        }
        if (StringUtils.isBlank(JSONObject.parseObject(attributes).get("nodeType").toString()) && JSONObject.parseObject(attributes).get("nodeType") == null) {
            return ResultBody.success();
        } else {
            if (JSONObject.parseObject(attributes).get("nodeType").toString().equals("2")) {
                data.put("F_SUBSYSTEM_IDENTIFICATION", "0");
            } else {
                data.put("F_SUBSYSTEM_IDENTIFICATION", JSONObject.parseObject(attributes).get("nodeType").toString());
            }
        }

        param.setEffect(true);
        Map<String, String> modelInfoMap = new HashMap<>();

//        if (JSONObject.parseObject(attributes).get("nodeType").toString().equals("1")||
//                JSONObject.parseObject(attributes).get("nodeType").toString().equals("2")) {
        modelInfoMap.put("name", "TIMES_MATERIAL_OBJECT");
        String moduleInfo = datamationsClient.getModuleInfo(modelInfoMap);
        //分类
        data.put("F_CLASSIFICATION", JSONObject.parseObject(attributes).get("secondNode").toString());
        //发次
        data.put("F_ISSUE_NO", lity);
        //型号
        data.put("F_MODEL", model);

        //定义primaryData
        Map<String, String>  primaryData = new HashMap<>();
        primaryData.put("F_M_SYS_ID",mSysId);
        param.setPrimaryData(primaryData);

        param.setModelId(moduleInfo);
        data.put("S_M_SYS_SECRETLEVEL", "1");
        param.setData(data);
        String[] dataArray = {"varchar", "varchar", "varchar"};
        param.setDataArray(dataArray);
        ResultBody success = datamationsClient.editLity(param);
        if (success.isSuccess()) {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.data.gather.bmodule"),
                    LogPattenUtils.getProperty("model.lity.matter.fmodule"),
                    StrUtil.format(LogPattenUtils.getProperty("model.lity.matter.add"), lity),
                    LogPattenUtils.getProperty("model.lity.matter.add.message"),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
        }
        return success;
    }

    /**
     * 校验本级是否有数据
     * @param attributes
     * @param model
     * @param lity
     * @return
     */
    private ResultBody verifyWhetherEdit(String lityShadow,String attributes, String model, String lity) {
        //查询发次下是否有单机或者总装直属件
        ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
        JSONObject filterMap = new JSONObject();
        filterMap.put("F_ISSUE_NO", lity);
        paramVO.setQueryFilter(filterMap.toJSONString());
        List<Map<String, Object>> qdInfo = iDatamationsClient.getQDInfo(paramVO);
        //不为空说明不可编辑
        if(CollectionUtils.isNotEmpty(qdInfo)) {
            return ResultBody.failure("该发次下关联有单机或者总装直属件，不可编辑！");
        } else  {
            //如果没有关联单机或者总装直属件实物，在可编辑为1，需要进一步判断，该发次作为本级时是否关联有数据
            Map moduleMap = new HashMap();
            moduleMap.put("F_MODEL", model);
            moduleMap.put("attributes", attributes);
            moduleMap.put("F_ISSUE_NO",lity);
            moduleMap.put("page","1");
            moduleMap.put("rows","1000000");
            //查看本级别下是否有数据
            List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(moduleMap);
            for (ModuleManageDto moduleManageDto : moduleManageInfoList) {
                //1表示要查询对象表单下是否有数据,分系统和模块
                List<Object[]> verList = (List<Object[]>) maintainService.QuerySql(moduleManageDto.getTableName(), "all", lityShadow);
                //查询表字段
                List<Object[]> cloList = (List<Object[]>) maintainService.QuerySql(moduleManageDto.getTableName(), "column", lityShadow);
                //将List<Object[]>转换成List<Map<String, String>>方便取数据
                List<Map<String, String>> verlm = maintainService.ListForMap(verList, cloList);
                if(CollectionUtils.isNotEmpty(verlm)){
                    return ResultBody.failure("该发次下存在数据，不可编辑！");
                }
            }
        }
        return ResultBody.success();

    }

}
