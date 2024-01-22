package com.meritdata.dam.datapacket.plan.model.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.mvc.RequestJsonParam;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.dam.base.log.annotation.OperateLogger;
import com.meritdata.dam.common.enums.DataBusiTypeEnum;
import com.meritdata.dam.common.enums.SystemFieldEnum;
import com.meritdata.dam.common.enums.YesOrNoEnum;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.model.entity.MatchFieldEntity;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleCurate;
import com.meritdata.dam.datapacket.plan.model.service.IModuleInfoConfigService;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.metamanage.ModelVerFieldDTO;
import com.meritdata.dam.entity.metamanage.ModelVersionHiveDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ModelInfoConfigController
 *
 * @author jianglei
 */
@RestController
@RequestMapping("/api/datapacket/config")
public class ModuleInfoConfigController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleInfoConfigController.class);

    /**
     * 首页
     */
    private final static String INDEX_PATTH = "index";

    /**
     * 模块基础路径
     */
    private final static String VIEW_PATH_PRE = "/dam/datapacket/plan/model/config/infoconfig/";

    private final static String VIEW_MODULE_PATH_PRE = "/dam/datapacket/plan/model/config/moduleConfig/";
    /**
     * 视图地址前缀
     */

    private final static String VIEW = "view/";

    @Autowired
    private IModuleInfoConfigService moduleInfoConfigService;

//    /**
//     * 检查测试项目配置的数据字典值
//     */
//    @Value("${testing.item.lookup:}")
//    private String testitemlookup;

    @Autowired
    ILogPostService logPostService;

    @Autowired
    IModulePlanService iModulePlanService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IMaintainService iMaintainService;

    //产品代号
    private final static String PRODUCT_CODE = "产品代号";

    //检查测试项目
    private final static String TESTING_ITEMS = "检查测试项目";

    //要求值
    private final static String REQUIRED_VAL = "要求值";

    /**
     * 页面跳转
     */
    @RequestMapping({"/view/{id}"})
    public ModelAndView getPage(@PathVariable String id, HttpServletRequest request) {
        ModelAndView mView = new ModelAndView();
        if (INDEX_PATTH.equals(id)) {
            mView.setViewName(VIEW_PATH_PRE + id);
        } else {
            mView.setViewName(VIEW_PATH_PRE + VIEW + id);
        }
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = (String) paramNames.nextElement();
            mView.addObject(name, request.getParameter(name));
        }
        return mView;
    }

    /**
     * 查询模型的具体配置
     *
     * @return
     */
    @RequestMapping(value = "/column/list", method = {RequestMethod.POST})
    @ApiOperation(value = "查询模板所有字段", notes = "查询模板所有字段")
    @ResponseBody
    public ResultBody getColumnList(@ApiParam(name = "模板编码", value = "模板编码", required = false, type = "string") @RequestParam String moduleCode,
                                    @ApiParam(name = "树节点", value = "树节点", required = false, type = "string") @RequestParam String nodeId,
                                    @ApiParam(name = "表名", value = "表名", required = false, type = "string") @RequestParam String tableName,
                                    @ApiParam(name = "模型id", value = "模型id", required = false, type = "string") @RequestParam String modelId) {
        try {
            List<ModuleColumnConfig> moduleCurateList = moduleInfoConfigService.getModuleCurate(nodeId, moduleCode, tableName, modelId);
            return ResultBody.success(moduleCurateList);
        } catch (Exception e) {
            return ResultBody.failure("查询模板所有字段失败!");
        }
    }

    /**
     * 配置保存接口
     *
     * @return
     */
    @RequestMapping(value = "/column/config/save", method = {RequestMethod.POST})
    @ApiOperation(value = "保存字段配置", notes = "保存字段配置")
    @ResponseBody
    public ResultBody saveColumnConfig(
            @ApiParam(name = "树节点", value = "树节点", required = false, type = "string") @RequestParam String treeNode,
            @ApiParam(name = "模板编码", value = "模板编码", required = false, type = "string") @RequestParam String code,
            @ApiParam(name = "成包是否包含", value = "成包是否包含", required = false, type = "string") @RequestParam String isPackage,
            @ApiParam(name = "模板信息", value = "模板信息", required = false, type = "string") @RequestParam String modelInfo,
            @ApiParam(name = "字段配置集合", value = "字段配置集合", required = false, type = "string") @RequestJsonParam List<ModuleColumnConfig> columnConfigList) {
        try {
            if (columnConfigList.size() > 0) {
                columnConfigList.forEach(columnConfig -> {
                    columnConfig.setNodeId(treeNode);
                });
            }
            columnConfigList.stream().forEach(item -> {
                if (item.getFormFieldVO() != null) {
                    item.setFormFieldVO(null);
                }
            });
            moduleInfoConfigService.saveColumnConfig(columnConfigList);
            ModuleCurate moduleCurate = new ModuleCurate();
            moduleCurate.setIsPackage(isPackage);
            moduleCurate.setCode(code);
            moduleCurate.setNodeId(treeNode);
            moduleCurate.setModelInfo(modelInfo);
            ModuleCurate saveModuleCurate = moduleInfoConfigService.saveModuleCurate(moduleCurate);
            if (saveModuleCurate != null) {
                Message msg = new Message(Message.TYPE_OPT,
                        LogPattenUtils.getProperty("model.planning.config.bmodule"),
                        LogPattenUtils.getProperty("model.planning.config.fmodule"),
                        LogPattenUtils.getProperty("model.planning.config.save"),
                        LogPattenUtils.getProperty("model.planning.config.save.message"),
                        Message.STATUS_SUCESS);
                logPostService.postLog(msg);
            }
            return ResultBody.success();
        } catch (Exception e) {
            return ResultBody.failure("保存字段配置失败!");
        }
    }

    /**
     * 查询匹配字段关系数据（返回前端用）
     *
     * @return
     */
    @RequestMapping(value = "/listMatch", method = {RequestMethod.POST})
    @ApiOperation(value = "查询匹配字段关系数据", notes = "查询匹配字段关系数据")
    @ResponseBody
    public ResultBody getMatchList(@ApiParam(name = "tableName", value = "tableName", required = false, type = "string") @RequestParam String tableName,
                                   @ApiParam(name = "modelId", value = "modelId", required = false, type = "string") @RequestParam String modelId,
                                   @ApiParam(name = "modeltree", value = "modeltree", required = false, type = "string") @RequestParam String modeltree) {
        try {
            Map<String, Object> reMap = new LinkedHashMap<>();
            Map<String, String> nameMap = new HashMap<>();
            List<String> proList = new ArrayList<>();
            List<String> iteList = new ArrayList<>();
            List<String> reqList = new ArrayList<>();
            //查询当前模型的字段信息
            List<ModelVerFieldDTO> modelVerFieldDTOList = iMaintainService.getModelEditFields(modelId).stream()
                    .filter(item -> !YesOrNoEnum.YES.is(item.getSystemField()) && !SystemFieldEnum.ID.is(item.getSysTableField())
                            && !DataBusiTypeEnum.CLOB.is(item.getFieldDataType().getBusiType()))
                    .collect(Collectors.toList());
            modelVerFieldDTOList.stream().map(temp -> {
                ModelVerFieldDTO fieldDTO = iMaintainService.setModelFieldAlias(temp);
                return fieldDTO;
            }).collect(Collectors.toList());
            //根据固定备注值判断，取正确的字段名称
            for (ModelVerFieldDTO modelVerFieldDTO : modelVerFieldDTOList) {
                if (PRODUCT_CODE.equals(modelVerFieldDTO.getRemark())) {
                    nameMap.put("procode", modelVerFieldDTO.getBusiName());
                } else if (TESTING_ITEMS.equals(modelVerFieldDTO.getRemark())) {
                    nameMap.put("testitem", modelVerFieldDTO.getBusiName());
                } else if (REQUIRED_VAL.equals(modelVerFieldDTO.getRemark())) {
                    nameMap.put("testrequire", modelVerFieldDTO.getBusiName());
                }
            }
            List<MatchFieldEntity> matchFieldEntities = iModulePlanService.getMatchList(tableName, modeltree);
            //查出来的结果永远只有唯一一条，如果多于一条，则说明有垃圾数据
            if (matchFieldEntities.size() > 1) {
                return ResultBody.failure("查询匹配字段关系数据失败!,对应数据库中，相同的型号和表数据有重复，请联系管理员。");
            } else if (matchFieldEntities.size() == 0) {
                reMap.put("ID", "");
                reMap.put(nameMap.get("procode") == null ? PRODUCT_CODE : nameMap.get("procode"), JSON.toJSON(proList));
                reMap.put(nameMap.get("testitem") == null ? TESTING_ITEMS : nameMap.get("testitem"), JSON.toJSON(iteList));
                reMap.put(nameMap.get("testrequire") == null ? REQUIRED_VAL : nameMap.get("testrequire"), JSON.toJSON(reqList));
                String sreMap = JSON.toJSONString(reMap);
                return ResultBody.success(sreMap);
            }
            MatchFieldEntity matchFieldEntity = matchFieldEntities.get(0);
            //ID信息
            String Id = matchFieldEntity.getId() == null ? "" : matchFieldEntity.getId();
            //产品代号
            String procode = matchFieldEntity.getProcode() == null ? "" : matchFieldEntity.getProcode();
            String[] splitpro = procode.split("<->");
            Collections.addAll(proList, splitpro);
            //检查测试项目
            String testitem = matchFieldEntity.getTestitem() == null ? "" : matchFieldEntity.getTestitem();
            String[] splitite = testitem.split("<->");
            Collections.addAll(iteList, splitite);
            //要求值
            String testrequire = matchFieldEntity.getTestrequire() == null ? "" : matchFieldEntity.getTestrequire();
            String[] splitreq = testrequire.split("<->");
            Collections.addAll(reqList, splitreq);
            reMap.put("ID", Id);
            reMap.put(nameMap.get("procode") == null ? PRODUCT_CODE : nameMap.get("procode"), JSON.toJSON(proList));
            reMap.put(nameMap.get("testitem") == null ? TESTING_ITEMS : nameMap.get("testitem"), JSON.toJSON(iteList));
            reMap.put(nameMap.get("testrequire") == null ? REQUIRED_VAL : nameMap.get("testrequire"), JSON.toJSON(reqList));
            String sreMap = JSON.toJSONString(reMap);
            return ResultBody.success(sreMap);
        } catch (Exception e) {
            return ResultBody.failure("查询匹配字段关系数据失败");
        }
    }

    /**
     * 查询匹配字段关系数据（datawarhouse调用fegin使用）
     *
     * @return
     */
    @RequestMapping(value = "/listMatchFegin", method = {RequestMethod.POST})
    @ApiOperation(value = "查询匹配字段关系数据", notes = "查询匹配字段关系数据")
    @ResponseBody
    public Map<String, List<String>> getMatchListByFegin(@ApiParam(name = "tableName", value = "tableName", required = false, type = "string") @RequestParam String tableName,
                                                         @ApiParam(name = "modeltree", value = "modeltree", required = false, type = "string") @RequestParam String modeltree) {
        try {
            Map<String, List<String>> reMap = new HashMap<>();
            List<String> proList = new ArrayList<>();
            List<String> iteList = new ArrayList<>();
            List<String> reqList = new ArrayList<>();
            List<MatchFieldEntity> matchFieldEntities = iModulePlanService.getMatchList(tableName, modeltree);
            //查出来的结果永远只有唯一一条，如果多于一条，则说明有垃圾数据
            if (matchFieldEntities.size() > 1) {
                return null;
            }
            MatchFieldEntity matchFieldEntity = matchFieldEntities.get(0);
            //产品代号
            String procode = matchFieldEntity.getProcode() == null ? "" : matchFieldEntity.getProcode();
            String[] splitpro = procode.split("<->");
            Collections.addAll(proList, splitpro);
            //检查测试项目
            String testitem = matchFieldEntity.getTestitem() == null ? "" : matchFieldEntity.getTestitem();
            String[] splitite = testitem.split("<->");
            Collections.addAll(iteList, splitite);
            //要求值
            String testrequire = matchFieldEntity.getTestrequire() == null ? "" : matchFieldEntity.getTestrequire();
            String[] splitreq = testrequire.split("<->");
            Collections.addAll(reqList, splitreq);
            reMap.put("procode", proList);
            reMap.put("testitem", iteList);
            reMap.put("testrequire", reqList);
            return reMap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据图号和批次号查询图号名称
     *
     * @return
     */
    @RequestMapping(value = "/FindTuHaoName", method = {RequestMethod.POST})
    @ApiOperation(value = "查询匹配字段关系数据", notes = "查询匹配字段关系数据")
    @ResponseBody
    public String FindTuHaoName(@ApiParam(name = "TuHao", value = "TuHao", required = false, type = "string") @RequestParam String TuHao,
                                @ApiParam(name = "PiCiHao", value = "PiCiHao", required = false, type = "string") @RequestParam String PiCiHao) {
        try {
            String TuhaoN = "";
            String param = TuHao + "<->" + PiCiHao;
            List<?> objects = iMaintainService.QuerySql("PHYSICAL_OBJECT_SINGLE_MACHINE", "findTuHaoName", param);
            if (objects.size() > 0) {
                TuhaoN = objects.get(0).toString();
            }
            return TuhaoN;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * 新增/修改匹配字段关系数据
     *
     * @return
     */
    @RequestMapping(value = "/addMatch", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "新增匹配字段关系数据", notes = "新增匹配字段关系数据")
    @OperateLogger(operation = "数据维护新增模型数据")
    public ResultBody addMatchData(@RequestBody MatchFieldEntity matchFieldEntity) {
        try {
//            现场要求全必填。该校验无用了。（个人觉得全必填有问题，后期可能会重新启用该逻辑，勿删！）
            if ("".equals(matchFieldEntity.getProcode()) && "".equals(matchFieldEntity.getTestrequire()) && "".equals(matchFieldEntity.getTestrequire())) {
                if (!"".equals(matchFieldEntity.getId()) || matchFieldEntity.getId() != null) {
                    long size = iModulePlanService.getMatchListCount(matchFieldEntity.getId());
                    if (size > 0) {
                        iModulePlanService.delMatchData(matchFieldEntity);
                    } else {
                        return ResultBody.success();
                    }
                }
            }
//            //增加为空校验逻辑
//            String procode = matchFieldEntity.getProcode() == null ? "" : matchFieldEntity.getProcode() ;
//            String[] splitpro = procode.split("<->");
//            String testitem = matchFieldEntity.getTestitem() == null ? "" : matchFieldEntity.getTestitem();
//            String[] splitite = testitem.split("<->");
//            String testrequire = matchFieldEntity.getTestrequire() == null ? "" : matchFieldEntity.getTestrequire();
//            String[] splitreq = testrequire.split("<->");
//            if ("".equals(procode) || "".equals(testitem) || "".equals(testrequire)) {
//                return ResultBody.failure("必填值为空！请检查填报数据");
//            } else if (splitpro.length != splitite.length || splitpro.length != splitreq.length) {
//                return ResultBody.failure("必填值为空！请检查填报数据");
//            }
            boolean flag = iModulePlanService.addMatchData(matchFieldEntity);
            if (flag) {
                return ResultBody.success();
            } else {
                return ResultBody.failure("新增匹配字段关系数据有误！");
            }
        } catch (Exception e) {
            return ResultBody.failure("新增匹配字段关系数据有误！");
        }
    }

    @RequestMapping(value = "/getLookUp", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "获取检查测试项目字典值", notes = "获取检查测试项目字典值")
    @OperateLogger(operation = "获取检查测试项目字典值")
    public ResultBody getLookUp(@ApiParam(name = "tableName", value = "tableName", required = false, type = "string") @RequestParam String tableName,
                                @ApiParam(name = "nodeId", value = "nodeId", required = false, type = "string") @RequestParam String nodeId) {
        try {
            List<Map<String, String>> mapList = new ArrayList<>();
            List<String> testitemlookup = iModulePlanService.getTestItemLookup(tableName, nodeId, TESTING_ITEMS);
            for (String name : testitemlookup) {
                Map<String, String> stringMap = new HashMap<>();
                stringMap.put("label", name);
                stringMap.put("value", name);
                mapList.add(stringMap);
            }
            if (mapList.size() == 0) {
                Map<String, String> stringMap = new HashMap<>();
                stringMap.put("label", "该模型无检查测试项目");
                stringMap.put("value", "该模型无检查测试项目");
                mapList.add(stringMap);
            }
            return ResultBody.success(mapList);
        } catch (Exception e) {
            LOGGER.error("获取检查测试项目字典值失败，失败原因" + e.getMessage());
            return ResultBody.failure("获取检查测试项目字典值失败!");
        }
    }

    @RequestMapping(value = "/getLookUpForFegin", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "获取检查测试项目字典值", notes = "获取检查测试项目字典值")
    @OperateLogger(operation = "获取检查测试项目字典值")
    public List<String> getLookUpForFegin(@ApiParam(name = "tableName", value = "tableName", required = false, type = "string") @RequestParam String tableName,
                                @ApiParam(name = "nodeId", value = "nodeId", required = false, type = "string") @RequestParam String nodeId) {
        try {
            List<String> testitemlookup = iModulePlanService.getTestItemLookup(tableName, nodeId, TESTING_ITEMS);
            return testitemlookup;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/getShiWuForPiCi", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "根据批次获取下面的实物", notes = "根据批次获取下面的实物")
    @OperateLogger(operation = "根据批次获取下面的实物")
    public List<String> getShiWuForPiCi(@ApiParam(name = "PiCi", value = "PiCi", required = false, type = "string") @RequestParam String PiCi) {
        try {
            List<String> shiwu = (List<String>) iMaintainService.QuerySql("PHYSICAL_OBJECT_SINGLE_MACHINE", "PiCiShiWu", PiCi);
            return shiwu;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
