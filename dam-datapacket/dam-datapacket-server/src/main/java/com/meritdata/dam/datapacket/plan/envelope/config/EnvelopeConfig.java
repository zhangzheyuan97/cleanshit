package com.meritdata.dam.datapacket.plan.envelope.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * @Author fanpeng
 * @Date 2023/5/18
 * @Describe 包络分析配置字段
 */
@Configuration
public class EnvelopeConfig {

    /**
     * 需要包络分析的表名
     */
    @Value("${conclusion.tableName:}")
    private String tableName;
    /**
     * 结论范围字段
     */
    @Value("${conclusion.range:}")
    private String conclusionRange;
    /**
     * 结论字段
     */
    @Value("${conclusion:}")
    private String conclusion;
    /**
     * 包络范围
     */
    @Value("${envelope.range:}")
    private String envelopeRange;
    /**
     * 包络下边界字段
     */
    @Value("${envelope.range.down:}")
    private String envelopeRangeDown;
    /**
     * 包络上边界字段
     */
    @Value("${envelope.range.up:}")
    private String envelopeRangeUp;
    /**
     * 包络结论字段
     */
    @Value("${envelope:}")
    private String envelope;
    /**
     * 测试值字段
     */
    @Value("${envelope.value:}")
    private String envelopeValue;
    /**
     * 检查测试项目字段
     */
    @Value("${envelope.checkField:}")
    private String checkField;

    /**
     * 匹配字段(含检测项的)
     */
    @Value("${envelope.unique.field:}")
    private String uniqueField;

    /**
     * 页面新增编辑修订隐藏的字段（结论及包络结论）
     */
    @Value("${envelope.page.hide:}")
    private String pageHide;

    public JSONObject getUniqueField() {
        return JSONObject.parseObject(uniqueField);
    }

    public List<String> getUniqueField(String tableName) {
        JSONObject uniqueField = getUniqueField();
        String string = uniqueField.getString(tableName);
        return JSONObject.parseArray(string, String.class);
    }

    public List<String> getPageHide() {
        return JSONObject.parseArray(pageHide, String.class);
    }

    public List<String> getTableName() {
        return JSONObject.parseArray(tableName, String.class);
    }

    public JSONObject getConclusionRange() {
        return JSONObject.parseObject(conclusionRange);
    }

    public JSONObject getConclusion() {
        return JSONObject.parseObject(conclusion);
    }

    public JSONObject getEnvelopeRange() {
        return JSONObject.parseObject(envelopeRange);
    }

    public JSONObject getEnvelopeRangeDown() {
        return JSONObject.parseObject(envelopeRangeDown);
    }

    public JSONObject getEnvelopeRangeUp() {
        return JSONObject.parseObject(envelopeRangeUp);
    }

    public JSONObject getEnvelope() {
        return JSONObject.parseObject(envelope);
    }

    public JSONObject getEnvelopeValue() {
        return JSONObject.parseObject(envelopeValue);
    }

    public JSONObject getCheckField() {
        return JSONObject.parseObject(checkField);
    }
}
