package com.meritdata.dam.datapacket.plan.client;


import cn.hutool.json.JSONObject;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.entity.quality.DqRuleConditionDTO;
import com.meritdata.dam.entity.quality.DqRuleDTO;
import com.meritdata.dam.entity.quality.DqTaskMonitorProcessDTO;
import com.meritdata.dam.entity.standard.process.Node;
import com.meritdata.dam.entity.standard.process.Operator;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 功能描述：数据质量服务接口
 */
@Service(value = "qualityclient")
@FeignClient(value = "dam-datagovern", path = "/api/quality")
public interface QualityClient {

    /**
     * 根据模型ID查询非空规则list
     *
     * @param modelId 所属模型ID
     * @return 模型属性
     */
    @RequestMapping(value = "/rule/manage/get-nonnull-field-id-list", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<String>> getNonnullFieldIdList(@RequestParam(required = false) String modelId);


    /**
     * 根据模型ID查询除非空规则list
     *
     * @param modelId 所属模型ID
     * @return 模型属性
     */
    @RequestMapping(value = "/rule/manage/get-no-nonnull-field-id-list", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<String>> getNoNonnullFieldIdList(@RequestParam(required = false) String modelId);

    /**
     * 获取入库质量校验规则列表信息
     *
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/rule/manage/get-insert-open-rule", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<DqRuleDTO>> getInsertCheckRuleListByModelId(@RequestParam String modelId);

    /**
     * 获取最新的评估结果
     *
     * @return
     */
    @RequestMapping(value = "/assessresult/only-list", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<DqTaskMonitorProcessDTO>> newAssessResult();

    /**
     * 获取组合规则 规则列表
     *
     * @param ruleId
     * @return
     */
    @RequestMapping(value = "/rule/combine/ref_rule_info_list", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<DqRuleDTO>> getRefRuleInfoList(@RequestParam String ruleId);

    /**
     * 获取组合规则 规则
     *
     * @param ruleId
     * @return
     */
    @RequestMapping(value = "/rule/combine/rule_content", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<String> getRuleContent(@RequestParam String ruleId);

    /**
     * 获取组合规则
     *
     * @param ruleId
     * @return
     */
    @RequestMapping(value = "/rule/condition/get-condition-rule", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<DqRuleConditionDTO> getRefRule(@RequestParam String ruleId);

    /**
     * 根据组合表达式获取解析后节点
     *
     * @param combineExp
     * @param nodeList
     * @return
     */
    @RequestMapping(value = "/assesstask/analysis-combineExp", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<Node> analysisCombineExp(@ApiParam(value = "组合规则表达式") @RequestParam String combineExp,
                                        @ApiParam(value = "业务流程所有节点") @RequestBody List<Node> nodeList);


    /**
     * 通过规则ID获取规则
     *
     * @param ruleId
     * @return
     */
    @RequestMapping(value = "/rule/manage/get-rule-info", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<DqRuleDTO> getRuleInfo(@RequestParam String ruleId);

    /**
     * 根据规则信息组装单个算子对象 (columns,tag 需要应用端自己接到Operator对象自己赋值)
     *
     * @param isFromCondition 是否条件规则
     * @param isFromCombine   是否组合规则
     * @param ruleDTO         规则实体类
     * @return
     */
    @RequestMapping(value = "/assesstask/install-operator", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<Operator> installOperator(@RequestParam Boolean isFromCondition,
                                         @RequestParam Boolean isFromCombine,
                                         @RequestBody DqRuleDTO ruleDTO);

    /**
     * 根据核准或者一直规则获取对比模型信息ID
     *
     * @param ruleDTO
     * @return
     */
    @RequestMapping(value = "/assesstask/get-correlation-model", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<String> getCorrelationModelId(@RequestBody DqRuleDTO ruleDTO);

    /**
     * 根据核准或者一直规则获取对比模型字段信息
     *
     * @param ruleDTO
     * @return
     */
    @RequestMapping(value = "/assesstask/get-correlation-fields", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<String>> getCorrelationFieldIds(@RequestBody DqRuleDTO ruleDTO);

    /**
     * 根据核准或者一直规则获取对比模型字段信息
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/rule/manage/get-rule-details", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<JSONObject> getRuleDetails(@RequestParam("ruleId") String ruleId);

    /**
     * 获取评估最后一次结果
     *
     * @return 结果集合
     */
    @RequestMapping(value = "/assessresult/model-last-result", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<DqTaskMonitorProcessDTO>> getModelLastResult();

    /**
     * 根据模型ID获取某规则字段ID
     *
     * @return 结果集合
     */
    @RequestMapping(value = "/rule/manage/model-rule-list", method = RequestMethod.POST)
    @ResponseBody
    ResultBody<List<String>> getFieldIdListByRule(@RequestParam(required = false) String modelId,
                                                  @RequestParam(required = false) String ruleType);

    /**
     * 删除规则
     *
     * @param modelIds 模型ids
     * @return 结果
     */
    @RequestMapping(value = "/rule/manage/deleteByModelId", method = RequestMethod.POST)
    @ResponseBody
    ResultBody deleteRuleByModelId(@ApiParam(value = "模型ids") @RequestParam(required = false) String[] modelIds);
}
