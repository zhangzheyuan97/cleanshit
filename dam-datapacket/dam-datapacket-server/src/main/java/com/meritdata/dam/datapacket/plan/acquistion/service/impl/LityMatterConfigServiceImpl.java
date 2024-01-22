package com.meritdata.dam.datapacket.plan.acquistion.service.impl;

import cn.hutool.core.util.StrUtil;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.dam.common.UUIDUtil;
import com.meritdata.dam.datapacket.plan.acquistion.service.ILityMatterConfigService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.MatterConfigDTO;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.model.vo.ModelDataExportParamDto;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class LityMatterConfigServiceImpl implements ILityMatterConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LityMatterConfigServiceImpl.class);

    @Autowired
    private IDatamationsClient datamationsClient;

    @Autowired
    private ILogPostService logPostService;

    @Override
    public ResultBody getMatterByMatter(String page, String rows, String matter, String physicalNo, String queryCriteria) {
        Map map = new HashMap();
        if (StringUtils.isNotBlank(physicalNo)) {
            map.put("F_PHYSICAL_NO", physicalNo);
        }
        map.put("page", page);
        map.put("rows", rows);
        map.put("queryCriteria", queryCriteria);
        map.put("matter", matter);
        List<Map<String, Object>> list = datamationsClient.getEbomInfo(map, "PHYSICAL_OBJECT_SINGLE_MACHINE");
        List<MatterConfigDTO> matterConfigDTOList = new ArrayList<>();
        AtomicReference<Integer> num = new AtomicReference<>(1);
        list.stream().forEach(item -> {
            MatterConfigDTO matterConfigDTO = new MatterConfigDTO();
            //分类
            if (item.get("F_CLASSIFICATION") != null) {
                matterConfigDTO.setType(item.get("F_CLASSIFICATION").toString());
            }
            //类型二
            matterConfigDTO.setSecondType(MapUtils.getString(item,"F_SECONDTYPE"));
            //类型三
            matterConfigDTO.setThirdType(MapUtils.getString(item,"F_THIRDTYPE"));
            //图号
            matterConfigDTO.setFigure(item.get("F_DRAWING_NO") != null ? item.get("F_DRAWING_NO").toString() : "");

            //批次号
            String batchNo = item.get("F_BATCH_NO") != null ? item.get("F_BATCH_NO").toString() : "";
            matterConfigDTO.setBatchNo(batchNo);

            //实物号
            if (item.get("F_PHYSICAL_NO") != null) {
                matterConfigDTO.setPhysicalNo(item.get("F_PHYSICAL_NO").toString());
                //根据实物号判断是否被引用，最多只有一条引用
                Map mapUse = new HashMap();
                mapUse.put("F_PHYSICAL_NO", item.get("F_PHYSICAL_NO").toString());
                Map supporting_list = datamationsClient.getPcAndSwInfo(mapUse, "SUPPORTING_LIST");
                if (supporting_list.size() > 0) {
                    // 被引用
                    matterConfigDTO.setIsUse("1");
                    //被引用的型号
                    matterConfigDTO.setModel(supporting_list.get("F_MODEL") != null ? supporting_list.get("F_MODEL").toString() : "");
                    //被引用的发次
                    matterConfigDTO.setLity(supporting_list.get("F_ISSUE_NO") != null ? supporting_list.get("F_ISSUE_NO").toString() : "");
                } else {
                    // 未引用
                    matterConfigDTO.setIsUse("0");
                }
            } else {
                matterConfigDTO.setPhysicalNo("");
                //根据批次号判断是否被引用，可能不止一条引用
                Map mapUse = new HashMap();
                mapUse.put("F_BATCH_NO", item.get("F_BATCH_NO").toString());

                List<Map<String, Object>> supporting_list = datamationsClient.getPcAndSwInfoByBatchNo(mapUse, "SUPPORTING_LIST");
                if (supporting_list.size() > 0) {
                    StringBuffer modelSb = new StringBuffer();
                    StringBuffer litySb = new StringBuffer();
                    supporting_list.forEach(support -> {
                        if (modelSb.indexOf(support.get("F_MODEL") != null ? support.get("F_MODEL").toString() : "") < 0) {
                            modelSb.append(support.get("F_MODEL") != null ? support.get("F_MODEL").toString() : "").append(",");
                        }

                        litySb.append(support.get("F_ISSUE_NO") != null ? support.get("F_ISSUE_NO").toString() : "").append(",");
                    });
                    //被引用的型号
                    matterConfigDTO.setModel(modelSb.length() > 0 ? modelSb.deleteCharAt((modelSb.length() - 1)).toString() : "");
                    //被引用的发次
                    matterConfigDTO.setLity(litySb.length() > 0 ? litySb.deleteCharAt((litySb.length() - 1)).toString() : "");
                } else {
                    // 未引用
                    matterConfigDTO.setIsUse("0");
                }
            }
            matterConfigDTO.setId(UUID.randomUUID().toString());
            matterConfigDTOList.add(matterConfigDTO);
        });
        Integer count = datamationsClient.getEbomInfoCount(map, "PHYSICAL_OBJECT_SINGLE_MACHINE");
        matterConfigDTOList.forEach(item -> {
            item.setNum((Integer.parseInt(page) - 1) * 10 + num.getAndSet(num.get() + 1));
        });
        //对发次、实物维护进行排序
        try {
//            sortLityMatter(matterConfigDTOList);
        }catch (Exception e) {
            LOGGER.error("getMatterByMatter sort is error",e);
        }

        return ResultBody.success(new GridView(matterConfigDTOList, count));
    }

    private void sortLityMatter(List<MatterConfigDTO>  matterConfigDTOList){
        Comparator mycmp = ComparableComparator.getInstance();
        mycmp = ComparatorUtils.reversedComparator(mycmp);
        mycmp = ComparatorUtils.nullHighComparator(mycmp);
        ArrayList<Object> sortFields = new ArrayList<Object>();
        sortFields.add(new BeanComparator<>("classIfication",mycmp));
        sortFields.add(new BeanComparator<>("secondType",mycmp));
        sortFields.add(new BeanComparator<>("thirdType",mycmp));
        sortFields.add(new BeanComparator<>("drawingNo",mycmp));
        sortFields.add(new BeanComparator<>("batchNo",mycmp));
        sortFields.add(new BeanComparator<>("physicalNo",mycmp));

        ComparatorChain multSort = new ComparatorChain(sortFields);
        Collections.sort(matterConfigDTOList,multSort);

    }


    @Override
    public ResultBody addMatterConfig(List<MatterConfigDTO> matterList, String issneno) {
        AtomicReference<String> model = new AtomicReference<>("");
        AtomicReference<String> lity = new AtomicReference<>("");
        try {
            Map<String, List<MatterConfigDTO>> collectPhysicalNo = matterList.stream()
                    .filter(item -> item.getPhysicalNo() != null && item.getPhysicalNo() != "").collect(Collectors.groupingBy(e -> (String) e.getPhysicalNo()));
            List<String> collect = matterList.stream().filter(item -> item.getPhysicalNo() != null && item.getPhysicalNo() != "").map(MatterConfigDTO::getPhysicalNo).distinct().collect(Collectors.toList());
            StringBuffer sb = new StringBuffer();
            if (collect.size() > 0) {

                collect.forEach(item -> {
                    List<String> figureList = new ArrayList<>();
                    List<MatterConfigDTO> matterConfigDTOS = collectPhysicalNo.get(item);
                    if (matterConfigDTOS.size() > 1) {
                        StringBuffer stringBuffer = new StringBuffer("【" + item + "】重复:图号");
                        matterConfigDTOS.forEach(matterConfigDTO -> {
                            if (!figureList.contains(matterConfigDTO.getFigure())) {
                                figureList.add(matterConfigDTO.getFigure());
                                stringBuffer.append("【").append(matterConfigDTO.getFigure()).append("】").append(",");
                            }
                        });

                        if (stringBuffer.length() > 0)
                            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                        sb.append(stringBuffer.append("\n"));
                    }
                });

                if (sb.length() > 0) {
                    return ResultBody.failure(sb.deleteCharAt(sb.length() - 1).append(",请检查!").toString());
                }
            }


            //判断相同图号下批次号是否重复
            List<MatterConfigDTO> matterConfigDTOList = new ArrayList<>();

            for (MatterConfigDTO matterConfigDTO : matterList) {
                matterConfigDTO.setUuid(UUID.randomUUID().toString());

            }
            for (MatterConfigDTO matterConfigDTO : matterList) {
                matterConfigDTOList = matterList.stream().filter(matterConfigDTO1 ->
                        !matterConfigDTO.getUuid().equals(matterConfigDTO1.getUuid())
                                && matterConfigDTO.getPhysicalNo() == "" && matterConfigDTO1.getPhysicalNo() == ""
                                && matterConfigDTO.getBatchNo().equals(matterConfigDTO1.getBatchNo())
                                && matterConfigDTO.getFigure().equals(matterConfigDTO1.getFigure())).collect(Collectors.toList())
                        .stream().collect(Collectors.toList());
                if (matterConfigDTOList.size() > 0) {
                    String message = "【" + matterConfigDTO.getBatchNo() + "】" + "重复:" + "图号" + "【" + matterConfigDTO.getFigure() + "】,请检查!";
                    return ResultBody.failure(message);
                }
            }


            //TODO::筛选所有被修改数据的图号信息
            List<String> figureRemove = matterList.stream().filter(item -> item.getIsUpdate() != null && item.getIsUpdate().equals("1")).map(MatterConfigDTO::getFigure).distinct().collect(Collectors.toList());
            //figureRemove为0则说明没有修改
            if (figureRemove.size() == 0) {
                return ResultBody.success();
            }
            //TODO::根据图号删除对应信息，首先根据图号查询对应的所有信息
            List<Map<String, Object>> mapByDrawingNo = datamationsClient.querySupportingListByBatchNo(figureRemove, issneno);
            List<Map<String, Object>> selectGridDataDelete = new ArrayList<>();
            //获取配套清单模型的modelInfo
            Map<String, String> modelInfoMap = new HashMap<>();
            modelInfoMap.put("name", "SUPPORTING_LIST");
            String moduleInfo = datamationsClient.getModuleInfo(modelInfoMap);
            if (mapByDrawingNo.size() > 0) {
                mapByDrawingNo.stream().forEach(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("F_M_SYS_ID", item.get("F_M_SYS_ID"));
                    map.put("S_M_SYS_DATAID", item.get("S_M_SYS_DATAID"));
                    map.put("S_M_SYS_VERSION", "1");
                    map.put("S_M_SYS_VERSIONSTATUS", "1");
                    selectGridDataDelete.add(map);
                });
                if (selectGridDataDelete.size() > 0) {
                    ModelDataExportParamDto modelDataExportParam = new ModelDataExportParamDto();
                    modelDataExportParam.setSelectGridData(selectGridDataDelete);
                    modelDataExportParam.setModelId(moduleInfo);
                    modelDataExportParam.setOperType("dataManageInit");
                    ResultBody dataManageInit = datamationsClient.deletedata("dataManageInit", modelDataExportParam);
                    if (!dataManageInit.isSuccess()) {
                        LOGGER.info("发次实物维护——根据" + figureRemove.toString() + "删除信息失败");
                        return ResultBody.failure("发次实物维护——根据" + figureRemove.toString() + "删除信息失败");
                    }
                    LOGGER.info("发次实物维护——根据" + figureRemove.toString() + "删除信息成功");
                }
            }
            matterList.stream().forEach(item -> {
                if (StringUtils.isNotEmpty(item.getBatchNo()) && item.getIsUpdate().equals("1")) {
                    DataOperateDTO param = new DataOperateDTO();
                    param.setEffect(true);
                    param.setModelId(moduleInfo);
                    Map<String, String> data = new HashMap<>();
                    //型号
                    data.put("F_MODEL", item.getModel());
                    model.set(item.getModel());
                    //发次
                    data.put("F_ISSUE_NO", item.getLity());
                    lity.set(item.getLity());
                    //图号
                    data.put("F_DRAWING_NO", item.getFigure());
                    //批次
                    data.put("F_BATCH_NO", item.getBatchNo());
                    //实物号
                    String physicalNo = item.getPhysicalNo() == null ? "" : item.getPhysicalNo();
                    data.put("F_PHYSICAL_NO", physicalNo);
                    String[] dataArray = {"varchar", "varchar", "varchar", "varchar", "varchar", "varchar"};
                    param.setDataArray(dataArray);

                    data.put("S_M_SYS_SECRETLEVEL", "1");
                    param.setData(data);
                    //数据更新标识
                    data.put("F_Update_ID", UUIDUtil.genUUID().substring(0, 20));
                    datamationsClient.addLity(param, "add");
                }
            });
        } catch (Exception e) {
            return ResultBody.failure("新增实物配套关系失败!");
        }
        Message msg = new Message(Message.TYPE_OPT,
                LogPattenUtils.getProperty("model.data.gather.bmodule"),
                LogPattenUtils.getProperty("model.lity.matter.fmodule"),
                StrUtil.format(LogPattenUtils.getProperty("model.lity.config.save"), model.get(), lity.get()),
                LogPattenUtils.getProperty("model.lity.config.save.message"),
                Message.STATUS_SUCESS);
        logPostService.postLog(msg);
        return ResultBody.success();
    }

}
