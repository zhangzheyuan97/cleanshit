package com.meritdata.dam.datapacket.plan.envelope.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.gbasedbt.lang.Decimal;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.envelope.config.EnvelopeConfig;
import com.meritdata.dam.datapacket.plan.envelope.service.IEnvelopeService;
import com.meritdata.dam.datapacket.plan.utils.enumutil.ConclusionAndEnvelopeEnum;
import com.meritdata.dam.datapacket.plan.utils.enumutil.ConclusionEnum;
import com.meritdata.dam.datapacket.plan.utils.enumutil.EnvelopeEnum;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import com.meritdata.dam.entity.metamanage.ModelFieldConfigDTO;
import com.meritdata.dam.entity.metamanage.ModelVerFieldDTO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author fanpeng
 * @Date 2023/5/18
 * @Describe 包络分析接口实现
 */
@Service
public class EnvelopeServiceImpl implements IEnvelopeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvelopeServiceImpl.class);

    @Autowired
    private EnvelopeConfig envelopeConfig;

    @Autowired
    private IDatamationsClient datamationsClient;

    @Autowired
    private IMaintainService maintainService;

    //范围使用,隔开
    private static final String REG_RANGE = ",";
    //多个值使用、隔开
    private static final String REG_VALUE = "、";
    //字段名前缀
    private static final String PREFIX_FIELD = "F_";
    //type为2 则判断是否包络
    private static final String ENVELOPE_TYPE = "2";
    //type为1 则判断是否合格
    private static final String CONCLUSION_TYPE = "1";

    //四舍五入保留的小数位数
    private static final int DECIMAL = 3;
    //四舍五入保留的小数位数
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");
    //下限最小值
    private static final double MIN_NUMBER = Double.parseDouble("-999999");


    @Override
    public Map<String, String> analysis(String modelId, String tableName, Map<String, String> data, String sysId) {

//        //sysId不为空。则必定为修订或者编辑，需要删除
//        if (!StringUtils.isEmpty(sysId)) {
//            //结论字段
//            JSONObject conclusion = envelopeConfig.getConclusion();
//            if (conclusion.containsKey(tableName)) {
//                //编辑修订删除结论字段
//                String delKey = PREFIX_FIELD + conclusion.getString(tableName);
//                data.remove(delKey);
//            }
//        }
        //获取需要包络分析的表名列表（配置文件中）
        List<String> tableNames = envelopeConfig.getTableName();
        // TODO: 2023/6/28 -------------不修改历史及相同检测项的数据配置
        //修改相同检测项的数据
//        Map<String, String> modifyData = new HashMap<>();
        //如果该表需要包络分析
        if (tableNames.contains(tableName)) {
            //封装检查测试项字段
//            JSONObject checkWhere = formatCheckField(tableName, data);
            formatNumber(tableName, data);
            //结论字段
            JSONObject conclusion = envelopeConfig.getConclusion();
            //包络结论字段
            JSONObject envelope = envelopeConfig.getEnvelope();
            // 包络分析
            Boolean conclusionResult = analysisConclusion(tableName, data, data);
            if (null != conclusionResult) {
                //不管有没有合格字段，如果本次合格，都要去判断历史的，如果历史有一条不合格则本条也不合格
//                if (conclusionResult && null != checkWhere) {
//                    //获取最高版本的数据,并判断是否合格或者是否包络
//                    Boolean conclusionResultHistory = analysisHistory(modelId, tableName, checkWhere, CONCLUSION_TYPE, data, sysId);
//                    conclusionResult = null == conclusionResultHistory ? true : conclusionResultHistory;
//                }
                //如果有是否合格字段
                if (conclusion.containsKey(tableName)) {
                    String conclusionString = conclusion.getString(tableName);
                    //结论字段名
                    String conclusionKey = PREFIX_FIELD + conclusionString;
                    //结论字段值
                    String conclusionValue = conclusionResult ? ConclusionEnum.PASS.getValue() : ConclusionEnum.NO_PASS.getValue();
                    //如果 含合格字段，且合格字段为null 则给合格字段赋值，否则不赋值
                    if (data.containsKey(conclusionKey) && StringUtils.isEmpty(data.get(conclusionKey))) {
                        data.put(conclusionKey, conclusionValue);
                        // 修改所有检测项，所有检测项除了生效的结论都是一致的 2023-0608 不修改历史的结论字段
//                    modifyData.put(conclusionKey, conclusionValue);
                    }
                }
            }else{
                //如果有合格字段，则合格字段置空
                if (conclusion.containsKey(tableName)) {
                    String conclusionString = conclusion.getString(tableName);
                    //结论字段名
                    String conclusionKey = PREFIX_FIELD + conclusionString;
                    data.put(conclusionKey, "");
                }
            }
            // 结论分析
            Boolean envelopeResult = analysisEnvelope(tableName, data, data);
            //不管有没有合格字段，如果本次合格，都要去判断历史的，如果历史有一条不合格则本条也不合格
//            if (null != envelopeResult && envelopeResult && null != checkWhere) {
//                //获取最高版本的数据,并判断是否合格或者是否包络
//                Boolean envelopeResultHistory = analysisHistory(modelId, tableName, checkWhere, ENVELOPE_TYPE, data, sysId);
//                envelopeResult = null == envelopeResultHistory ? true : envelopeResultHistory;
//            }
            //是否有合格字段(或者有合格范围) 并且有包络字段
            //结论范围
            JSONObject conclusionRange = envelopeConfig.getConclusionRange();
            if ((conclusion.containsKey(tableName) || conclusionRange.containsKey(tableName)) && envelope.containsKey(tableName)) {
                String envelopeString = envelope.getString(tableName);
                //结论+包络字段名
                String conclusionAddEnvelopeKey = PREFIX_FIELD + envelopeString;
                //合格结果和包络结果都计算出来
                if (null != envelopeResult && null != conclusionResult) {
                    //结论+包络字段值
                    String conclusionAddEnvelopeValue = conclusionResult ?
                            //合格 判断是否包络
                            (envelopeResult ? ConclusionAndEnvelopeEnum.PASS_SUCCESS.getValue() : ConclusionAndEnvelopeEnum.PASS_FAIL.getValue()) :
                            //不合格 判断是否包络
                            (envelopeResult ? ConclusionAndEnvelopeEnum.NOPASS_SUCCESS.getValue() : ConclusionAndEnvelopeEnum.NOPASS_FAIL.getValue());
                    data.put(conclusionAddEnvelopeKey, conclusionAddEnvelopeValue);
                    // 修改所有检测项，所有检测项除了生效的结论都是一致的
//                    modifyData.put(conclusionAddEnvelopeKey, conclusionAddEnvelopeValue);
                }else{
                    data.put(conclusionAddEnvelopeKey, "");
                }
            }
            //无合格字段，也无合格范围字段，仅有包络字段
            if (!conclusion.containsKey(tableName) && !conclusionRange.containsKey(tableName) && envelope.containsKey(tableName)) {
                String envelopeString = envelope.getString(tableName);
                //包络字段名
                String envelopeKey = PREFIX_FIELD + envelopeString;
                if (null != envelopeResult) {
                    //包络字段值
                    String envelopeValue = envelopeResult ? EnvelopeEnum.SUCCESS.getValue() : EnvelopeEnum.FAIL.getValue();
                    data.put(envelopeKey, envelopeValue);
                    // 修改所有检测项，所有检测项除了生效的结论都是一致的
//                    modifyData.put(envelopeKey, envelopeValue);
                }else{
                    data.put(envelopeKey, "");
                }
            }
            //含检测项。则需要修改相同检测项的数据
//            if (null != checkWhere) {
//                // 修改历史
//                updateHistory(modelId, tableName, data, sysId, checkWhere, modifyData, conclusionResult, envelopeResult);
//            }
        }
        return data;
    }

    /**
     * 修改历史数据（范围-包络分析结论）
     *
     * @param modelId          模型id
     * @param tableName        表名
     * @param data             数据
     * @param sysId            数据id
     * @param checkWhere       查询条件
     * @param modifyData       修改字段
     * @param conclusionResult 结论计算结果
     * @param envelopeResult   包络计算结果
     */
    private void updateHistory(String modelId, String tableName, Map<String, String> data,
                               String sysId, JSONObject checkWhere, Map<String, String> modifyData,
                               Boolean conclusionResult, Boolean envelopeResult) {
        //结论范围  更新结论范围
//        JSONObject conclusionRange = envelopeConfig.getConclusionRange();
        //结论为null则无法计算,不更新历史数据的范围
//        if (conclusionRange.containsKey(tableName) && conclusionResult != null) {
//            String conclusionRangeKey = PREFIX_FIELD + conclusionRange.getString(tableName);
//            String conclusionRangeValue = data.get(conclusionRangeKey);
//            modifyData.put(conclusionRangeKey, conclusionRangeValue);
//        }

        //包络范围字段 更新包络范围
        JSONObject envelopeRange = envelopeConfig.getEnvelopeRange();
        //包络为null则无法计算,不更新历史数据的范围
        if (envelopeRange.containsKey(tableName) && envelopeResult != null) {
            String envelopeRangeKey = PREFIX_FIELD + envelopeRange.getString(tableName);
            String envelopeRangeValue = data.get(envelopeRangeKey);
            modifyData.put(envelopeRangeKey, envelopeRangeValue);
        }
        //获取包络范围上限 更新包络范围上限
        JSONObject envelopeRangeDown = envelopeConfig.getEnvelopeRangeDown();
        //包络为null则无法计算,不更新历史数据的范围
        if (envelopeRangeDown.containsKey(tableName) && envelopeResult != null) {
            String envelopeRangeDownKey = PREFIX_FIELD + envelopeRangeDown.getString(tableName);
            String envelopeRangeDownValue = data.get(envelopeRangeDownKey);
            modifyData.put(envelopeRangeDownKey, envelopeRangeDownValue);
        }
        //获取包络范围下限 更新包络范围下限
        JSONObject envelopeRangeUp = envelopeConfig.getEnvelopeRangeUp();
        //包络为null则无法计算,不更新历史数据的范围
        if (envelopeRangeUp.containsKey(tableName) && envelopeResult != null) {
            String envelopeRangeUpKey = PREFIX_FIELD + envelopeRangeUp.getString(tableName);
            String envelopeRangeUpValue = data.get(envelopeRangeUpKey);
            modifyData.put(envelopeRangeUpKey, envelopeRangeUpValue);
        }

        //只修改最高版本
        checkWhere.put("S_M_SYS_MAXVERSION", "1");
        //只修改编辑中的数据
        checkWhere.put("S_M_SYS_VERSIONSTATUS", "2");
//        checkWhere.put("S_M_SYS_VERSIONSTATUS", "2");
        //JSONObject抓为map
        Map<String, String> primaryData = JSONObject.parseObject(checkWhere.toJSONString(), new TypeReference<Map<String, String>>() {
        });
        //要修改的数据为 modifyData
        updateData(modelId, primaryData, modifyData, sysId);
    }

    /**
     * 计算历史包络
     *
     * @param modelId    模型id
     * @param tableName  表名称
     * @param checkWhere 查询条件
     * @param type       类型
     * @param newData    新数据
     * @param sysId      修改才传入，否则传""
     * @return
     */
    private Boolean analysisHistory(String modelId, String tableName, JSONObject checkWhere, String type, Map<String, String> newData, String sysId) {
//        List<String> matchField = getMatchFieldByModelId(modelId);
        JSONObject jsonObject = JSONObject.parseObject(checkWhere.toJSONString());
        Boolean aBoolean = null;
        ModelDataQueryParamVO modelDataQueryParamVO = new ModelDataQueryParamVO();
        //只判断最大版本，不管是否生效
        if (!StringUtils.isEmpty(sysId)) {
            //排除一下自身数据
            JSONObject idJson = new JSONObject();
            idJson.put("$ne", sysId);
            jsonObject.put(PREFIX_FIELD + "M_SYS_ID", idJson);
        }
//        //排除一下匹配字段
//        if (CollectionUtils.isNotEmpty(matchField)){
//            for (String match : matchField) {
//                //相同则作为查询条件了，直接跳过
//                if (checkWhere.containsKey(match)){
//                    continue;
//                }
//                String key = PREFIX_FIELD + match;
//                JSONObject matchJson = new JSONObject();
//                matchJson.put("$ne", newData.get(key));
//                jsonObject.put(key, matchJson);
//            }
//        }
        jsonObject.put("S_M_SYS_MAXVERSION", "1");
        // TODO: 2023/6/27 增加逻辑，只查询不生效的数据
        // 生效数据不参与计算
        JSONObject versionJson = new JSONObject();
        versionJson.put("$ne", 1);
        jsonObject.put("S_M_SYS_VERSIONSTATUS", versionJson);
        modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
        List<Map<String, Object>> dataListAll = datamationsClient.packetDataListAll("", modelId, modelDataQueryParamVO);
        for (Map<String, Object> map : dataListAll) {
            Map<String, String> item = new HashMap(map);
            if (type.equals(CONCLUSION_TYPE)) {
                //计算结论
                aBoolean = analysisConclusion(tableName, newData, item);
            } else {
                //计算包络
                aBoolean = analysisEnvelope(tableName, newData, item);
            }
            //有任意不合格或者不包络，则退出循环返回结果
            if (aBoolean != null && !aBoolean) {
                return false;
            }
        }
        return aBoolean;
    }

//    /**
//     * 根据模型id获取匹配字段信息
//     * @param modelId
//     * @return
//     */
//    private List<String> getMatchFieldByModelId(String modelId) {
//        List<ModelVerFieldDTO> modelEditFields = maintainService.getModelEditFields(modelId);
//        return modelEditFields.stream().filter(item -> {
//            //不是系统字段
//            if (item.getModelFieldConfigDTO() != null && item.getSystemField() != 1) {
//                ModelFieldConfigDTO modelFieldConfigDTO = item.getModelFieldConfigDTO();
//                //是否匹配字段
//                Integer match = modelFieldConfigDTO.getMatch();
//                //1则为匹配字段
//                return match == 1;
//            }
//            return false;
//        }).map(ModelVerFieldDTO::getFieldName).collect(Collectors.toList());
//    }

    @Override
    public Boolean analysisConclusion(String tableName, Map<String, String> newData, Map<String, String> oldData) {

        //结论字段
        JSONObject conclusion = envelopeConfig.getConclusion();
        if (conclusion.containsKey(tableName)) {
            String conclusionKey = PREFIX_FIELD + conclusion.getString(tableName);
            //数据本来有结论字段
            if (oldData.containsKey(conclusionKey)) {
                String oldConclusionValue = oldData.get(conclusionKey);
                //本来的结论
                if (!StringUtils.isEmpty(oldConclusionValue)) {
                    return oldConclusionValue.equals(ConclusionEnum.PASS.getValue());
                }
            }
        }

        //结论范围
        JSONObject conclusionRange = envelopeConfig.getConclusionRange();
        //获取要比较的字段列表（值列表）
        JSONObject envelopeValue = envelopeConfig.getEnvelopeValue();
        //如果该模型含有结论范围字段。则需要结论分析
        if (conclusionRange.containsKey(tableName)) {
            //获取结论范围字段
            String conclusionRangeStr = PREFIX_FIELD + conclusionRange.getString(tableName);
            //实际填入的值
            String conclusionRangeVal = newData.get(conclusionRangeStr);
            if (StringUtils.isEmpty(conclusionRangeVal)) {
                LOGGER.info("模型" + tableName + "未填入范围值,不参与计算是否合格");
                return null;
            }
            //","在第一个
            if (conclusionRangeVal.indexOf(REG_RANGE) == 0) {
                LOGGER.info("模型" + tableName + "合格范围值填写有误[,前面无数值],不参与计算是否包络");
                return null;
            }
            String[] conclusionRangeArray = conclusionRangeVal.split(REG_RANGE);
            if (conclusionRangeArray.length > 2) {
                LOGGER.info("模型" + tableName + "合格范围值填写有误[填写了两个以上的数值],不参与计算是否包络");
                return null;
            }
            //仅填入了一个值,则为最小值
            if (conclusionRangeArray.length == 1) {
                double min;
                try {
                    min = Double.parseDouble(conclusionRangeArray[0]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    LOGGER.info("模型" + tableName + "结论范围值填写错误:【" + conclusionRangeArray[0] + "】无法转为数字", e);
                    return null;
                }
                return rangeCalc(tableName, oldData, envelopeValue, min, null);
            }
            //填入了一个范围 最小值，最大值
            if (conclusionRangeArray.length == 2) {
                double min;
                try {
                    min = Double.parseDouble(conclusionRangeArray[0]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    LOGGER.info("模型" + tableName + "结论范围值填写错误:【" + conclusionRangeArray[0] + "】无法转为数字", e);
                    return null;
                }
                double max;
                try {
                    max = Double.parseDouble(conclusionRangeArray[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    LOGGER.info("模型" + tableName + "结论范围值填写错误:【" + conclusionRangeArray[1] + "】无法转为数字", e);
                    return null;
                }
                if (min > max) {
                    LOGGER.info("模型" + tableName + "结论范围值填写错误:【,前面的下限值大于了,后面的上限值】");
                    return null;
                }
                return rangeCalc(tableName, oldData, envelopeValue, min, max);
            }
        }
        return null;
    }

    @Override
    public Boolean analysisEnvelope(String tableName, Map<String, String> newData, Map<String, String> oldData) {
        //包络范围字段
        JSONObject envelopeRange = envelopeConfig.getEnvelopeRange();
        //获取包络范围上限
        JSONObject envelopeRangeDown = envelopeConfig.getEnvelopeRangeDown();
        //获取包络范围下限
        JSONObject envelopeRangeUp = envelopeConfig.getEnvelopeRangeUp();
        //获取要比较的字段列表（值列表）
        JSONObject envelopeValue = envelopeConfig.getEnvelopeValue();

        //是否含有包络范围字段-根据包络范围判断是否包络
        if (envelopeRange.containsKey(tableName)) {
            //获取包络范围
            String envelopeRangeField = envelopeRange.getString(tableName);
            String envelopeRangeValue = newData.get(PREFIX_FIELD + envelopeRangeField);
            if (StringUtils.isEmpty(envelopeRangeValue)) {
                LOGGER.info("模型" + tableName + "未填入包络范围,不参与计算是否包络");
                return null;
            }
            //","在第一个
            if (envelopeRangeValue.indexOf(REG_RANGE) == 0) {
                LOGGER.info("模型" + tableName + "包络范围填写有误[,前面无数值],不参与计算是否包络");
                return null;
            }
            String[] envelopeRangeValueArray = envelopeRangeValue.split(REG_RANGE);
            if (envelopeRangeValueArray.length > 2) {
                LOGGER.info("模型" + tableName + "包络范围填写有误[填写了两个以上的数值],不参与计算是否包络");
                return null;
            }
            if (envelopeRangeValueArray.length == 1) {
                double min;
                try {
                    min = Double.parseDouble(envelopeRangeValueArray[0]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    LOGGER.info("模型" + tableName + "包络范围值填写错误:【" + envelopeRangeValueArray[0] + "】无法转为数字", e);
                    return null;
                }
                return rangeCalc(tableName, oldData, envelopeValue, min, null);
            }
            if (envelopeRangeValueArray.length == 2) {
                double min;
                try {
                    min = Double.parseDouble(envelopeRangeValueArray[0]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    LOGGER.info("模型" + tableName + "包络范围值填写错误:【" + envelopeRangeValueArray[0] + "】无法转为数字", e);
                    return null;
                }
                double max;
                try {
                    max = Double.parseDouble(envelopeRangeValueArray[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    LOGGER.info("模型" + tableName + "包络范围值填写错误:【" + envelopeRangeValueArray[1] + "】无法转为数字", e);
                    return null;
                }
                if (min > max) {
                    LOGGER.info("模型" + tableName + "包络范围值填写错误:【,前面的下限值大于了,后面的上限值】");
                    return null;
                }
                return rangeCalc(tableName, oldData, envelopeValue, min, max);
            }
        }
        //是否含有包络上限及包络下限字段，如果含有则根据包络上下限判断是否包络
        if (envelopeRangeDown.containsKey(tableName) && envelopeRangeUp.containsKey(tableName)) {
            //获取包络下限
            String envelopeRangeDownField = envelopeRangeDown.getString(tableName);
            String down = newData.get(PREFIX_FIELD + envelopeRangeDownField);

            //获取包络上限
            String envelopeRangeUpField = envelopeRangeUp.getString(tableName);
            String up = newData.get(PREFIX_FIELD + envelopeRangeUpField);

            if (StringUtils.isEmpty(down) && StringUtils.isEmpty(up)) {
                LOGGER.info("模型" + tableName + "包络上下限填写有误[上下限均为空],不参与计算是否包络");
                return null;
            }
            Double min = null;
            if (!StringUtils.isEmpty(down)) {
                try {
                    min = Double.parseDouble(down);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    LOGGER.info("模型" + tableName + "包络下限值填写错误:【" + down + "】无法转为数字", e);
                    return null;
                }
            }
            Double max = null;
            if (!StringUtils.isEmpty(up)) {
                try {
                    max = Double.parseDouble(up);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    LOGGER.info("模型" + tableName + "包络上限值填写错误:【" + up + "】无法转为数字", e);
                    return null;
                }
            }
            //校验下限值是否大于上限值
            if (null != min && null != max) {
                if (min > max) {
                    LOGGER.info("模型" + tableName + "包络上下限填写错误:【下限值" + min + "不能大于上限值" + max + "】");
                    return null;
                }
            }
            return rangeCalc(tableName, oldData, envelopeValue, min, max);
        }
        return null;
    }

    @Override
    public void updateData(String modelId, Map<String, String> primaryData, Map<String, String> modifyData, String sysId) {
        //获取所有要修改的数据
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(primaryData));
        if (!StringUtils.isEmpty(sysId)) {
            //排除一下自身数据
            JSONObject idJson = new JSONObject();
            idJson.put("$ne", sysId);
            jsonObject.put(PREFIX_FIELD + "M_SYS_ID", idJson);
        }
        ModelDataQueryParamVO modelDataQueryParamVO = new ModelDataQueryParamVO();
        modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
        List<Map<String, Object>> dataListAll = datamationsClient.packetDataListAll("", modelId, modelDataQueryParamVO);

        dataListAll.forEach(item -> {
            primaryData.put(PREFIX_FIELD + "M_SYS_ID", item.get(PREFIX_FIELD + "M_SYS_ID").toString());
            //修改参数
            DataOperateDTO param = new DataOperateDTO();
            param.setModelId(modelId);
            param.setData(modifyData);
            param.setPrimaryData(primaryData);
            //是否生效
            param.setEffect(false);
            ResultBody resultBody = datamationsClient.updateModelData(param);
            if (!resultBody.isSuccess()) {
                LOGGER.error("模型" + modelId + "修改其他检测项数据失败！");
            }
        });
    }

    //计算范围是否合格或者包络
    private Boolean rangeCalc(String tableName, Map<String, String> data, JSONObject envelopeValue, Double min, Double max) {
        if (envelopeValue.containsKey(tableName)) {
            //获取测量值的字段
            String string = envelopeValue.getString(tableName);
            List<String> valueFields = JSONObject.parseArray(string, String.class);
            for (String valueField : valueFields) {
                //获取测量值
                String value = data.get(PREFIX_FIELD + valueField);
                // 、隔开的为多值测试，只要有一个不合格（包络）则全部不合格（包络）
                if (StringUtils.isEmpty(value)) {
                    //未填入测量值，不参与计算
                    LOGGER.info("模型" + tableName + "未填入测量值,不参与计算");
                    return null;
                }
                if (value.contains(REG_VALUE)) {
                    //计算、隔开的的多值
                    String[] valueStrArray = value.split(REG_VALUE);
                    //有计算的值不符合规定则不进行计算
                    boolean flag = false;
                    for (String valueStr : valueStrArray) {
                        double valueDouble;
                        try {
                            valueDouble = Double.parseDouble(valueStr);
                            flag = true;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            LOGGER.error("模型" + tableName + "实测值【" + value + "】填入有误，【" + valueStr + "】无法转为数字，不参与计算！");
                            continue;
                        }
                        if (calc(min, max, valueDouble)) return false;
                    }
                    //如果值都不和规则，则返回null，否则为合格或者包络
                    if (!flag){
                        return null;
                    }
                } else {
                    //计算单值
                    double valueDouble;
                    try {
                        valueDouble = Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        LOGGER.error("模型" + tableName + "实测值【" + value + "】填入有误，【" + value + "】无法转为数字，不参与计算！");
                        return null;
                    }
                    if (calc(min, max, valueDouble)) return false;
                }
            }
            return true;
        }
        return null;
    }

    /**
     * 计算结果
     *
     * @param min         上限
     * @param max         下限
     * @param valueDouble 要对比的数据
     * @return
     */
    private boolean calc(Double min, Double max, Double valueDouble) {
        if (null != min) {
            //下限值不为-999999 才计算下限，否则不计算只计算下限
            if (min != MIN_NUMBER || null == max) {
                if (valueDouble < min) {
                    //不合格或者不包络
                    return true;
                }
            }
        }
        if (null != max) {
            //不合格或者不包络
            return valueDouble > max;
        }
        return false;
    }

    @Override
    public List<String> getHideField() {
        List<String> pageHide = envelopeConfig.getPageHide();
        return pageHide.stream().map(item -> PREFIX_FIELD + item).collect(Collectors.toList());
    }

    private void formatNumber(String tableName, Map<String, String> data) {
        //结论范围
        JSONObject conclusionRange = envelopeConfig.getConclusionRange();
        if (conclusionRange.containsKey(tableName)) {
            String conclusionRangeField = PREFIX_FIELD + conclusionRange.getString(tableName);
            String conclusionRangeValue = data.get(conclusionRangeField) == null ? "" : data.get(conclusionRangeField);
            data.put(conclusionRangeField, roundHalfUp(conclusionRangeValue, REG_RANGE));
        }
        //包络范围
        JSONObject envelopeRange = envelopeConfig.getEnvelopeRange();
        if (envelopeRange.containsKey(tableName)) {
            String envelopeRangeField = PREFIX_FIELD + envelopeRange.getString(tableName);
            String envelopeRangeValue = data.get(envelopeRangeField) == null ? "" : data.get(envelopeRangeField);
            data.put(envelopeRangeField, roundHalfUp(envelopeRangeValue, REG_RANGE));
        }
        //上包络
        JSONObject envelopeRangeUp = envelopeConfig.getEnvelopeRangeUp();
        if (envelopeRangeUp.containsKey(tableName)) {
            String envelopeRangeUpField = PREFIX_FIELD + envelopeRangeUp.getString(tableName);
            String envelopeRangeUpValue = data.get(envelopeRangeUpField) == null ? "" : data.get(envelopeRangeUpField);
            data.put(envelopeRangeUpField, roundHalfUp(envelopeRangeUpValue, ""));
        }
        //下包络
        JSONObject envelopeRangeDown = envelopeConfig.getEnvelopeRangeDown();
        if (envelopeRangeDown.containsKey(tableName)) {
            String envelopeRangeDownField = PREFIX_FIELD + envelopeRangeDown.getString(tableName);
            String envelopeRangeDownValue = data.get(envelopeRangeDownField) == null ? "" : data.get(envelopeRangeDownField);
            data.put(envelopeRangeDownField, roundHalfUp(envelopeRangeDownValue, ""));
        }
        //实测值
        JSONObject envelopeValue = envelopeConfig.getEnvelopeValue();
        if (envelopeValue.containsKey(tableName)) {
            List<String> valueFields = JSONObject.parseArray(envelopeValue.getString(tableName), String.class);
            for (String fieldName : valueFields) {
                String envelopeValueField = PREFIX_FIELD + fieldName;
                String envelopeValueValue = data.get(envelopeValueField) == null ? "" : data.get(envelopeValueField);
                data.put(envelopeValueField, roundHalfUp(envelopeValueValue, REG_VALUE));
            }

        }
    }

    /**
     * 四舍五入计算方法
     *
     * @param data 数据
     * @param reg  分隔符
     * @return
     */
    @Override
    public String roundHalfUp(String data, String reg) {
        if (StringUtils.isEmpty(data)){
            return data;
        }
        if (!data.contains(".")){
            return data;
        }
        if (StringUtils.isNotEmpty(reg)) {
            String[] conclusionRangeValueArray = data.split(reg);
            List<String> range = new ArrayList<>();
            for (String value : conclusionRangeValueArray) {
                try {
                    BigDecimal bigDecimal = new BigDecimal(value);
                    BigDecimal valueStr = bigDecimal.setScale(DECIMAL, BigDecimal.ROUND_HALF_UP);
                    range.add(decimalFormat.format(valueStr));
                } catch (Exception e) {
                    range.add(value);
                    e.printStackTrace();
                    LOGGER.error("四舍五入时转换数字失败", e);
                }
            }
            if (CollectionUtils.isNotEmpty(range)) {
                return StringUtils.join(range, reg);
            }
        } else {
            try {
                BigDecimal bigDecimal = new BigDecimal(data);
                BigDecimal valueStr = bigDecimal.setScale(DECIMAL, BigDecimal.ROUND_HALF_UP);
                return decimalFormat.format(valueStr);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("四舍五入时转换数字失败", e);
                return data;
            }
        }
        return data;
    }

    @Override
    public JSONArray analysisArray(String modelId, String tableName, JSONArray dataArray) {
        //获取需要包络分析的表名列表（配置文件中）
        List<String> tableNames = envelopeConfig.getTableName();
        //todo 不修改历史及相同检测项的数据配置
        JSONArray result = new JSONArray();
        // TODO: 2023/6/28 -------------不修改历史及相同检测项的数据配置
        //修改相同检测项的数据
//        Map<String, String> modifyData = new HashMap<>();
        //如果该表需要包络分析
        if (tableNames.contains(tableName)) {
            //按照最后一个范围字段判断和修改
            for (int i = dataArray.size() - 1; i >= 0; i--) {
                //按照第一个范围字段修改
//            for (int i = 0; i < dataArray.size(); i++) {
                String s1 = dataArray.getJSONObject(i).toJSONString();
                Map<String, String> data = JSON.parseObject(s1, new TypeReference<Map<String, String>>() {
                });
                formatNumber(tableName, data);
//                JSONObject checkWhere = formatCheckField(tableName, data);
                //结论字段
                JSONObject conclusion = envelopeConfig.getConclusion();
                //包络结论字段
                JSONObject envelope = envelopeConfig.getEnvelope();
                // 包络分析
                Boolean conclusionResult = analysisConclusion(tableName, data, data);
                if (null != conclusionResult) {
                    //不管有没有合格字段，如果本次合格，都要去判断历史的，如果历史有一条不合格则本条也不合格
//                    if (conclusionResult && null != checkWhere) {
//                        //获取最高版本的数据,并判断是否合格或者是否包络
//                        Boolean conclusionResultHistory = analysisHistoryArray(tableName, dataArray, CONCLUSION_TYPE, data,checkWhere);
//                        conclusionResult = null == conclusionResultHistory ? true : conclusionResultHistory;
//                        //如果合格则还需要计算数据库中结论
//                        if (conclusionResult) {
//                            conclusionResultHistory = analysisHistory(modelId, tableName, checkWhere, CONCLUSION_TYPE, data, "");
//                            conclusionResult = null == conclusionResultHistory ? true : conclusionResultHistory;
//                        }
//
//                    }
                    //如果有是否合格字段
                    if (conclusion.containsKey(tableName)) {
                        String conclusionString = conclusion.getString(tableName);
                        //结论字段名
                        String conclusionKey = PREFIX_FIELD + conclusionString;
                        //结论字段值
                        String conclusionValue = conclusionResult ? ConclusionEnum.PASS.getValue() : ConclusionEnum.NO_PASS.getValue();
                        //如果 含合格字段，且合格字段为null 则给合格字段赋值，否则不赋值
                        if (conclusion.containsKey(tableName) && StringUtils.isEmpty(data.get(conclusionKey))) {
                            data.put(conclusionKey, conclusionValue);
                            // 修改所有检测项，所有检测项除了生效的结论都是一致的 2023-0608 不修改历史的结论字段
//                    modifyData.put(conclusionKey, conclusionValue);
                        }
                    }
                }else{
                    if (conclusion.containsKey(tableName)) {
                        String conclusionString = conclusion.getString(tableName);
                        //结论字段名
                        String conclusionKey = PREFIX_FIELD + conclusionString;
                        data.put(conclusionKey, "");
                    }
                }
                // 结论分析
                Boolean envelopeResult = analysisEnvelope(tableName, data, data);
                //不管有没有合格字段，如果本次合格，都要去判断历史的，如果历史有一条不合格则本条也不合格
//                if (null != envelopeResult && envelopeResult && null != checkWhere) {
//                    //获取最高版本的数据,并判断是否合格或者是否包络
//                    Boolean envelopeResultHistory = analysisHistoryArray(tableName, dataArray, ENVELOPE_TYPE, data,checkWhere);
//                    envelopeResult = null == envelopeResultHistory ? true : envelopeResultHistory;
//                    //如果包络，则还需要计算历史
//                    if (envelopeResult) {
//                        envelopeResultHistory = analysisHistory(modelId, tableName, checkWhere, ENVELOPE_TYPE, data, "");
//                        envelopeResult = null == envelopeResultHistory ? true : envelopeResultHistory;
//                    }
//                }
                //是否有合格字段(或者有合格范围) 并且有包络字段
                //结论范围
                JSONObject conclusionRange = envelopeConfig.getConclusionRange();
                if ((conclusion.containsKey(tableName) || conclusionRange.containsKey(tableName)) && envelope.containsKey(tableName)) {
                    String envelopeString = envelope.getString(tableName);
                    //结论+包络字段名
                    String conclusionAddEnvelopeKey = PREFIX_FIELD + envelopeString;
                    //合格结果和包络结果都计算出来
                    if (null != envelopeResult && null != conclusionResult) {
                        //结论+包络字段值
                        String conclusionAddEnvelopeValue = conclusionResult ?
                                //合格 判断是否包络
                                (envelopeResult ? ConclusionAndEnvelopeEnum.PASS_SUCCESS.getValue() : ConclusionAndEnvelopeEnum.PASS_FAIL.getValue()) :
                                //不合格 判断是否包络
                                (envelopeResult ? ConclusionAndEnvelopeEnum.NOPASS_SUCCESS.getValue() : ConclusionAndEnvelopeEnum.NOPASS_FAIL.getValue());
                        data.put(conclusionAddEnvelopeKey, conclusionAddEnvelopeValue);
                        // 修改所有检测项，所有检测项除了生效的结论都是一致的
//                        modifyData.put(conclusionAddEnvelopeKey, conclusionAddEnvelopeValue);
                    }else{
                        data.put(conclusionAddEnvelopeKey, "");
                    }
                }
                //无合格字段，也无合格范围字段，仅有包络字段
                if (!conclusion.containsKey(tableName) && !conclusionRange.containsKey(tableName) && envelope.containsKey(tableName)) {
                    String envelopeString = envelope.getString(tableName);
                    //包络字段名
                    String envelopeKey = PREFIX_FIELD + envelopeString;
                    if (null != envelopeResult) {
                        //包络字段值
                        String envelopeValue = envelopeResult ? EnvelopeEnum.SUCCESS.getValue() : EnvelopeEnum.FAIL.getValue();
                        data.put(envelopeKey, envelopeValue);
                        // 修改所有检测项，所有检测项除了生效的结论都是一致的
//                        modifyData.put(envelopeKey, envelopeValue);
                    }else{
                        data.put(envelopeKey, "");
                    }
                }
                //含检测项。则需要修改相同检测项的数据
//                if (null != checkWhere) {
//                    // 修改其他数据
//                    updateHistoryArray(tableName, data, checkWhere, modifyData, conclusionResult, envelopeResult, dataArray, i);
//                    // 修改完成后还需要修改数据库中数据
//                    updateHistory(modelId, tableName, data, "", checkWhere, modifyData, conclusionResult, envelopeResult);
//                }

                result.add(data);
            }
        }else{
            return dataArray;
        }
        return result;
    }

    private JSONObject formatCheckField(String tableName, Map<String, String> data) {
        // TODO: 2023/6/25 保留三位小数
        formatNumber(tableName, data);
        //获取检查项目字段
        JSONObject checkField = envelopeConfig.getCheckField();
        JSONObject checkWhere = null;
        //含有检项字段，则需要修改历史包络属性
        if (checkField.containsKey(tableName)) {
            //update 语句后面的where条件
            //检测箱字段值
            String primaryKey = PREFIX_FIELD + checkField.getString(tableName);
            String primaryValue = data.get(primaryKey);
            checkWhere = new JSONObject();
            checkWhere.put(primaryKey, primaryValue);
            //其他条件，用以联合匹配字段查询准确数据
            JSONObject uniqueFieldJson = envelopeConfig.getUniqueField();
            //如果配置文件含匹配字段
            if (uniqueFieldJson.containsKey(tableName)) {
                List<String> uniqueField = envelopeConfig.getUniqueField(tableName);
                for (String item : uniqueField) {
                    //将匹配字段加入查询条件
                    String key = PREFIX_FIELD + item;
                    checkWhere.put(key, data.get(key));
                }
            }
        }
        return checkWhere;
    }

    /**
     * 计算相同检测项的其他包络分析 (根据数组数据)
     *
     * @param tableName 表名
     * @param type      类型 （结论或者包络）
     * @param newData   最新字段名
     * @return
     */
    private Boolean analysisHistoryArray(String tableName, JSONArray dataArray, String type, Map<String, String> newData, JSONObject checkWhere) {
        checkWhere.remove("F_M_SYS_ID");
        checkWhere.remove("S_M_SYS_VERSIONSTATUS");
        checkWhere.remove("S_M_SYS_MAXVERSION");
        Boolean aBoolean = null;
        List<Map> dataListAll = dataArray.toJavaList(Map.class);
        for (Map map : dataListAll) {
            Map<String, String> item = new HashMap(map);
            boolean flag = true;
            for (String key : checkWhere.keySet()) {
                //有一个不同则flag为false,为不同的检测配置项
                if (!item.get(key).equals(checkWhere.getString(key))){
                    flag = false;
                    break;
                }
            }
            if (flag){
                if (type.equals(CONCLUSION_TYPE)) {
                    //计算结论
                    aBoolean = analysisConclusion(tableName, newData, item);
                } else {
                    //计算包络
                    aBoolean = analysisEnvelope(tableName, newData, item);
                }
                //有任意不合格或者不包络，则退出循环返回结果
                if (aBoolean != null && !aBoolean) {
                    return false;
                }
            }
        }
        return aBoolean;
    }


    /**
     * @param tableName
     * @param data
     * @param checkWhere
     * @param modifyData
     * @param conclusionResult
     * @param envelopeResult
     * @param jsonArray
     * @param i
     */
    private void updateHistoryArray(String tableName, Map<String, String> data,
                                    JSONObject checkWhere, Map<String, String> modifyData,
                                    Boolean conclusionResult, Boolean envelopeResult, JSONArray jsonArray, int i) {
        //结论范围  更新结论范围
//        JSONObject conclusionRange = envelopeConfig.getConclusionRange();
        //结论为null则无法计算,不更新历史数据的范围
//        if (conclusionRange.containsKey(tableName) && conclusionResult != null) {
//            String conclusionRangeKey = PREFIX_FIELD + conclusionRange.getString(tableName);
//            String conclusionRangeValue = data.get(conclusionRangeKey);
//            modifyData.put(conclusionRangeKey, conclusionRangeValue);
//        }
        //把本条数据替换掉
        jsonArray.remove(i);
        jsonArray.add(i,JSONObject.parseObject(JSON.toJSONString(data)));
        //包络范围字段 更新包络范围
        JSONObject envelopeRange = envelopeConfig.getEnvelopeRange();
        //包络为null则无法计算,不更新历史数据的范围
        if (envelopeRange.containsKey(tableName) && envelopeResult != null) {
            String envelopeRangeKey = PREFIX_FIELD + envelopeRange.getString(tableName);
            String envelopeRangeValue = data.get(envelopeRangeKey);
            modifyData.put(envelopeRangeKey, envelopeRangeValue);
        }
        //获取包络范围上限 更新包络范围上限
        JSONObject envelopeRangeDown = envelopeConfig.getEnvelopeRangeDown();
        //包络为null则无法计算,不更新历史数据的范围
        if (envelopeRangeDown.containsKey(tableName) && envelopeResult != null) {
            String envelopeRangeDownKey = PREFIX_FIELD + envelopeRangeDown.getString(tableName);
            String envelopeRangeDownValue = data.get(envelopeRangeDownKey);
            modifyData.put(envelopeRangeDownKey, envelopeRangeDownValue);
        }
        //获取包络范围下限 更新包络范围下限
        JSONObject envelopeRangeUp = envelopeConfig.getEnvelopeRangeUp();
        //包络为null则无法计算,不更新历史数据的范围
        if (envelopeRangeUp.containsKey(tableName) && envelopeResult != null) {
            String envelopeRangeUpKey = PREFIX_FIELD + envelopeRangeUp.getString(tableName);
            String envelopeRangeUpValue = data.get(envelopeRangeUpKey);
            modifyData.put(envelopeRangeUpKey, envelopeRangeUpValue);
        }

        Map<String, String> primaryData = JSONObject.parseObject(checkWhere.toJSONString(), new TypeReference<Map<String, String>>() {
        });
        //要修改的数据为 modifyData
        updateDataArray(jsonArray, primaryData, modifyData);
    }

    private void updateDataArray(JSONArray jsonArray, Map<String, String> primaryData, Map<String, String> modifyData) {
        //修改导入数据,去掉数据库查询的条件
        primaryData.remove("F_M_SYS_ID");
        primaryData.remove("S_M_SYS_VERSIONSTATUS");
        primaryData.remove("S_M_SYS_MAXVERSION");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            boolean flag = true;
            //判断查询条件包含的字段是否都相同
            for (String key : primaryData.keySet()) {
                //有一个不同则flag为false,为不同的检测配置项
                if (!jsonObject.getString(key).equals(primaryData.get(key))) {
                    flag = false;
                    break;
                }
            }
            //如果是相同检测项目，则需要修改
            if (flag) {
                //修改所有修改项
                for (String key : modifyData.keySet()) {
                    jsonObject.put(key, modifyData.get(key));
                }
            }
        }
    }
}
