package com.meritdata.dam.datapacket.plan.application.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackShowService;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowCreateInter;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowColorEnum;
import com.meritdata.dam.entity.datamanage.FormGroupVO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.font.PhysicalFont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DataPackShowImpl implements IDataPackShowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPackShowImpl.class);

    @Autowired
    IMaintainService iMaintainService;

    @Autowired
    IStandAloneService standAloneService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IFlowBomSheetDataInter iFlowBomSheetDataInter;

    @Autowired
    IDataPacketClient iDataPacketClient;


    @Autowired
    IMaintainService maintainService;


    /**
     * 流程bom和表单的关系
     */
    @Autowired
    IFlowBomSheetInter flowBomSheetInter;

    /**
     * 流程创建表
     */
    @Autowired
    IFlowCreateInter flowCreateInter;

    @Override
    public ResultBody<GridView> queryTemplateList(Map map, String attributes, BatchNoNodeInfo batchNoNodeInfo) {
        JSONObject jsonObject = JSON.parseObject(attributes == null ? "" : attributes);
        //判断是单机还是分系统
        if (jsonObject.containsKey("firstNode") && "单机".equals(jsonObject.get("firstNode"))) {
            String nodeLevel = jsonObject.get("nodeLevel") == null ? "" : jsonObject.get("nodeLevel").toString();
            String tempId = jsonObject.get("tempID") == null ? "" : jsonObject.get("tempID").toString();
            //分系统展示单机字段
            String aloneLevel = jsonObject.get("aloneLevel") == null ? "" : jsonObject.get("aloneLevel").toString();
            map.put("nodeId", tempId);
            if ("4".equals(nodeLevel) || "5".equals(nodeLevel) || "4".equals(aloneLevel) || "5".equals(aloneLevel)) {
                ResultBody<GridView> modelList = standAloneService.getModelList(map);
                List rows = modelList.getData().getRows();
                //如果参数包含bomName则需要获取数据包版本的状态
                if (null != batchNoNodeInfo) {
                    long total = modelList.getData().getTotal();
                    List<JSONObject> jsonObjects = JSONArray.parseArray(JSON.toJSONString(rows), JSONObject.class);
                    //获取数据包状态，并添加到返回值中
                    for (JSONObject json : jsonObjects) {
                        String modelInfo = json.getString("modelInfo");
                        String color = getColor(batchNoNodeInfo, modelInfo);
                        json.put("color", color);
                    }
                    return ResultBody.success(new GridView<>(jsonObjects, total));
                } else {
                    return standAloneService.getModelList(map);
                }
            } else {
                return ResultBody.success(new GridView<>(new ArrayList<>(), 0));
            }
        } else {
            String tempId = jsonObject.get("tempID") == null ? "" : jsonObject.get("tempID").toString();
            map.put("tempId", tempId);
            ResultBody<GridView> resultBody = iMaintainService.centerDataList(map);
            List rows = resultBody.getData().getRows();
            //如果参数包含bomName则需要获取数据包版本的状态
            if (null != batchNoNodeInfo) {
                long total = resultBody.getData().getTotal();
                List<JSONObject> jsonObjects = JSONArray.parseArray(JSON.toJSONString(rows), JSONObject.class);
                //获取数据包状态，并添加到返回值中
                for (JSONObject json : jsonObjects) {
                    String modelInfo = json.getString("modelInfo");
                    String color = getColor(batchNoNodeInfo, modelInfo);
                    json.put("color", color);
                }
                return ResultBody.success(new GridView<>(jsonObjects, total));
            } else {
                return iMaintainService.centerDataList(map);
            }
        }
    }

    @Override
    public ResultBody<GridView> queryDataList(String physicalCode, String modelId, ModelDataQueryParamVO param, String tempId,String batchNoNodeInfo) {
        Map<String, String> lookupByPlan = iMaintainService.getLookupByPlan(modelId, tempId);
        GridView<Map<String, Object>> mapPageResult = iDatamationsClient.dataCollectionListManageByEffecive(physicalCode, modelId, param,batchNoNodeInfo);
        List<Map<String, Object>> rows = mapPageResult.getRows();
//        Long aLong = iDatamationsClient.dataCollectionListManageCount(physicalCode, modelId, param);
        List<FormGroupVO> fields = iDatamationsClient.queryFormFields(modelId, true);
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> stringObjectMap = rows.get(i);
            for (FormGroupVO field : fields) {
                if ("文件信息".equals(field.getGroupName())) {
                    field.getFields().stream().forEach(fie -> {
                        if (stringObjectMap.containsKey(fie.getAliasName()) && stringObjectMap.get(fie.getAliasName()) != null) {
                            String id = stringObjectMap.get(fie.getAliasName()).toString();
                            ResultBody sysInfoByIds = iDatamationsClient.getSysInfoByIds(id.split(","));
                            if (sysInfoByIds.isSuccess()) {
                                stringObjectMap.put(fie.getAliasName() + "_File", stringObjectMap.get(fie.getAliasName()));
                                stringObjectMap.put(fie.getAliasName(), sysInfoByIds.getData());
                            }
                        }
                    });
                }
            }
            checkLookUp(lookupByPlan, rows, i, stringObjectMap);
        }
        return ResultBody.success(new GridView<>(rows, mapPageResult.getTotal()));
    }

    @Override
    public void checkLookUp(Map<String, String> lookupByPlan, List<Map<String, Object>> rows, int i, Map<String, Object> stringObjectMap) {
        if (lookupByPlan != null && lookupByPlan.size() != 0) {
            for (String key : lookupByPlan.keySet()) {
                if (stringObjectMap.get(key) == null) {
                    continue;
                }
                String f_alertDimension = stringObjectMap.get(key).toString();
                String s = lookupByPlan.get(key);
                String[] split = s.split(",");
                if (!Arrays.asList(split).contains(f_alertDimension)) {
                    rows.remove(i);
                }
            }
        }
    }

    /**
     * 红绿灯展示判断逻辑方法
     *
     * @param batchNoNodeInfo
     * @param template
     * @return
     */
    @Override
    public String getColor(BatchNoNodeInfo batchNoNodeInfo, String template) {
        String color = "";
        ModelDataQueryParamVO modelDataQueryParamVO = new ModelDataQueryParamVO();
        JSONObject jsonObject = new JSONObject();
        //根据bom名称和modelid获取表名
        String physicalNo = "";
        //发次本级，实物号为发次号
        if (batchNoNodeInfo.getBatchNo().equals(Constants.OWN_ISSUE)) {
            physicalNo = batchNoNodeInfo.getIssueNo();
        }
        //如果实物号不为null，则实物号为传入的实物号
        if (batchNoNodeInfo.getPhysicalNo() != null) {
            physicalNo = batchNoNodeInfo.getPhysicalNo();
        }
        String drawingNo = batchNoNodeInfo.getDrawingNo() == null ? "" : batchNoNodeInfo.getDrawingNo();
        String mode = batchNoNodeInfo.getModel() == null ? "" : batchNoNodeInfo.getModel();
        String batchNo = (batchNoNodeInfo.getBatchNo() == null || batchNoNodeInfo.getBatchNo().equals(Constants.OWN_ISSUE))? "" : batchNoNodeInfo.getBatchNo();
        //图号或者型号 产品编号
        if (StringUtils.isNotEmpty(drawingNo) || StringUtils.isNotEmpty(mode)) {
            jsonObject.put("F_BatchCode", StringUtils.isNotEmpty(drawingNo) ? drawingNo : mode);
        }
        //发次批次 产品批次
        if (StringUtils.isNotEmpty(batchNo) ){
            jsonObject.put("F_BatchNo", batchNo);
        }
        //实物号
        if (StringUtils.isNotEmpty(physicalNo)){
            jsonObject.put("F_PhysicalCode", physicalNo);
        }
        modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
        List<Map<String, Object>> verlm = iDatamationsClient.packetDataListAll("", template, modelDataQueryParamVO);
        List<String> box = new ArrayList<>();
        int size = verlm.size();
        //如果无数据则返回黄色
        if (size == 0) {
            color = FlowColorEnum.YELLOW.getCode();
            return color;
        }
        //循环获取表中的数据版本M_SYS_VERSIONSTATUS和数据审核状态IsApproval
        for (int i = 0; i < verlm.size(); i++) {
            String m_sys_maxversion = verlm.get(i).get("S_M_SYS_MAXVERSION").toString();
            if ("1".equals(m_sys_maxversion)) {
                String m_sys_versionstatus = verlm.get(i).get("S_M_SYS_VERSIONSTATUS").toString();
                String isApproval = verlm.get(i).get("F_IsApproval") == null ? "" : verlm.get(i).get("F_IsApproval").toString();
                box.add(m_sys_versionstatus + ";" + isApproval);
            }
        }
        //如果查询结果中包含2编辑中时则返回红色；如果是0、4、和2;是代表审核中数据时返回黄色；其余情况则为全部生效，返回绿色
        if (box.contains("2;")) {
            color = FlowColorEnum.RED.getCode();
        } else if (box.contains("0;") || box.contains("4;") || box.contains("2;是")) {
            color = FlowColorEnum.YELLOW.getCode();
        } else {
            color = FlowColorEnum.GREE.getCode();
        }

        return color;
    }
}
