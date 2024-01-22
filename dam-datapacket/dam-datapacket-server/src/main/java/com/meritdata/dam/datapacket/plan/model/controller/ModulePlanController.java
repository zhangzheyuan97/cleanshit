package com.meritdata.dam.datapacket.plan.model.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.factory.ExecutorProcessPool;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleCurate;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleConfigDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.service.ITreeService;
import com.meritdata.dam.datapacket.plan.service.impl.TreeServiceImpl;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.RedisTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/datapacket/planning")
@Api(value = "型号策划api接口", tags = {"型号策划api接口"})
public class ModulePlanController {

    private static final Logger logger = LoggerFactory.getLogger(ModulePlanController.class);

    @Autowired
    private IDataPacketClient dataPacketClient;

    @Autowired
    private IModulePlanService modulePlanService;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private ITreeService treeService;

    @Autowired
    private RedisTemplateService redisTemplateService;

    //treeServiceImpl其中写了排序的公共方法
    @Autowired
    TreeServiceImpl treeServiceImpl;

    private final static String INDEX = "index";
    private final static String PREFIX = "/view";

    /**
     * 页面跳转
     */
    @RequestMapping("/view/{id}/**")
    public ModelAndView view(@PathVariable String id, HttpServletRequest request, @RequestParam(required = false) Map<String, String> params) {
        ModelAndView mView = new ModelAndView();
        if (INDEX.equals(id)) {
            mView.setViewName("/dam/datapacket/plan/model/curate/" + id);
        } else {
            String uri = request.getRequestURI();
            String suffix = uri.substring(uri.indexOf(PREFIX) + PREFIX.length());
            mView.setViewName("/dam/datapacket/plan/model/curate/view/" + suffix);
        }
        mView.addAllObjects(params);
        return mView;
    }

    /**
     * 查询模型版本列表
     */
    @ApiOperation(value = "查询模板管理列表", notes = "查询模板管理列表")
    @RequestMapping(value = {"page"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<GridView> list(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                     @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                     @ApiParam(name = "模板名称", value = "模板名称", required = false, type = "string") @RequestParam(required = false) String name,
                                     @ApiParam(name = "模板编码", value = "模板编码", required = false, type = "string") @RequestParam(required = false) String code,
                                     @ApiParam(name = "表名", value = "表名", required = false, type = "string") @RequestParam(required = false) String tableName,
                                     @ApiParam(name = "树节点", value = "树节点id", required = false, type = "string") @RequestParam(required = false) String nodeId) {
        final String regex = "\\d+";
        if (StringUtils.isNotBlank(page) && !page.matches(regex)) {
            String message = "分页参数page 传值有误，page:" + page;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        if (StringUtils.isNotBlank(rows) && !rows.matches(regex)) {
            String message = "分页参数rows 传值有误，rows:" + rows;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        try {
            //是否是末级节点
            if (modulePlanService.isEndNode(nodeId)) {
                return ResultBody.success(new GridView(new ArrayList<>(), 0));
            }
            Map map = new HashMap();
            map.put("page", page);
            map.put("rows", rows);
            map.put("name", name);
            map.put("code", code);
            map.put("tableName", tableName);

            //查询出所有模板信息
            List<ModuleManageDto> moduleManageInfoList = dataPacketClient.modulePage(map);
            //根据该节点下的模板信息
            List<ModuleCurate> moduleCurateByNodeIdList = modulePlanService.getModuleCurateByNodeId(nodeId);
            if (moduleCurateByNodeIdList.size() == 0) {
                List<ModuleConfigDto> moduleCurateList = new ArrayList<>();
                moduleManageInfoList.forEach(model -> {
                    ModuleConfigDto moduleConfigDto = new ModuleConfigDto();
                    BeanUtils.copyProperties(model, moduleConfigDto);
                    moduleConfigDto.setIsPackage("1");
                    moduleCurateList.add(moduleConfigDto);

                });
                long count = dataPacketClient.moduleCount(map);
                return ResultBody.success(new GridView(moduleCurateList, count));
            }
            List<ModuleConfigDto> moduleCurateList = new ArrayList<>();
            //循环远程结果集
            moduleManageInfoList.forEach(model -> {
                ModuleConfigDto moduleConfigDto = new ModuleConfigDto();
                BeanUtils.copyProperties(model, moduleConfigDto);
                moduleConfigDto.setIsPackage("1");
                moduleCurateByNodeIdList.stream().forEach(moduleCurate -> {
                    if (moduleCurate.getCode().equals(moduleConfigDto.getCode())) {
                        moduleConfigDto.setIsPackage(moduleCurate.getIsPackage());
                    }
                });
                moduleCurateList.add(moduleConfigDto);
            });
            long count = dataPacketClient.moduleCount(map);
            return ResultBody.success(new GridView(moduleCurateList, count));
        } catch (Exception e) {
            logger.error("查询模板管理列表失败！", e.getMessage());
            return ResultBody.failure("查询模板管理列表失败!");
        }
    }

    /**
     * 查询模型树列表,包含系统默认节点
     *
     * @return
     */
    @RequestMapping(value = "/nodeTree", method = {RequestMethod.POST})
    @ApiOperation(value = "查询模型树列表", notes = "查询模型树列表")
    @ResponseBody
    public ResultBody<List<TreeModel<Object>>> tree(@ApiParam(name = "关键字", value = "关键字", required = false, type = "string") @RequestParam(required = false) String keywords) {
        try {
            String userId = sessionUtils.getEmpId();
            boolean hasKey = redisTemplateService.hasKey(userId, Constants.PageFlagEnum.MODULE_PLAN.getCode());
            //如果redis存在该缓存
            if (hasKey) {
                JSONArray treeData = redisTemplateService.getTreeData(userId, Constants.PageFlagEnum.MODULE_PLAN.getCode());
                List<TreeDto> treeDtoList = JSONArray.parseArray(treeData.toString(), TreeDto.class);
                //启动获取最新数据线程并更新redis
                Runnable task = () -> {
                    logger.info("开始更新型号策划页面redis树结构数据！");
                    getTreeAndSetRedis(userId);
                    logger.info("更新型号策划页面redis树结构数据完成！");
                };
                //执行线程
                ExecutorProcessPool.getInstance().execute(task);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtoList));
            }else{
                //redis数据不存在，则需要查询，并更新至redis
                List<TreeDto> treeDtos = getTreeAndSetRedis(userId);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtos));
            }
        } catch (Exception e) {
            logger.error("查询模型树列表失败！", e.getMessage());
            return ResultBody.failure("查询模型树列表失败!");
        }
    }

    private  List<TreeDto> getTreeAndSetRedis(String userId){
        List<TreeDto> dataTypeTree = modulePlanService.tree("-1", userId);
        //挂载单机
        List<TreeDto> tree = modulePlanService.addTreeNode(userId, dataTypeTree);
        try {
            treeServiceImpl.sortTreeDtoByKeyWords(tree,Constants.TREE_THREE);
        }catch (Exception e) {
            logger.error("查询模型树列表,包含系统默认节点 sort is error",e);
        }
        //排序

        //存入redis
        redisTemplateService.setTreeData(userId, Constants.PageFlagEnum.MODULE_PLAN.getCode(), JSON.toJSONString(tree));
        return tree;
    }
}
