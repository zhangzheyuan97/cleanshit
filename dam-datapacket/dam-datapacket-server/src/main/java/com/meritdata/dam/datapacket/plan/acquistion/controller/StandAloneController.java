package com.meritdata.dam.datapacket.plan.acquistion.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.factory.ExecutorProcessPool;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/datapacket/alone")
@Api(value = "数据包采集单机api接口", tags = {"数据包采集单机api接口"})
public class StandAloneController {
    private static final Logger logger = LoggerFactory.getLogger(StandAloneController.class);


    private final static String INDEX = "index";
    private final static String PREFIX = "/view";

    @Autowired
    private IStandAloneService standAloneService;

    @Autowired
    private IDataPacketClient dataPacketClient;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private ITreeService treeService;

    @Autowired
    private RedisTemplateService redisTemplateService;

    //treeServiceImpl其中写了排序的公共方法
    @Autowired
    TreeServiceImpl treeServiceImpl;

    /**
     * 页面跳转
     */
    @RequestMapping("/view/{id}/**")
    public ModelAndView view(@PathVariable String id, HttpServletRequest request, @RequestParam(required = false) Map<String, String> params) {
        ModelAndView mView = new ModelAndView();
        if (INDEX.equals(id)) {
            mView.setViewName("/dam/datapacket/plan/acquistion/dataAcquistion/standAlone/" + id);
        } else {
            String uri = request.getRequestURI();
            String suffix = uri.substring(uri.indexOf(PREFIX) + PREFIX.length());
            mView.setViewName("/dam/datapacket/plan/acquistion/dataAcquistion/standAlone/view" + suffix);
        }
        mView.addAllObjects(params);
        return mView;
    }

    /**
     * 查询数据包采集单机树
     *
     * @return
     */
    @RequestMapping(value = "/nodeTree", method = {RequestMethod.POST})
    @ApiOperation(value = "数据包采集单机树", notes = "数据包采集单机树")
    @ResponseBody
    public ResultBody<List<TreeModel<Object>>> tree(@ApiParam(name = "过滤条件", value = "过滤条件", required = false, type = "string") @RequestParam(required = false) String keywords) {
        try {
            String userId = sessionUtils.getEmpId();
            boolean hasKey = redisTemplateService.hasKey(userId, Constants.PageFlagEnum.MAINTAIN_SINGLE.getCode());
            //如果redis存在该缓存
            if (hasKey) {
                JSONArray treeData = redisTemplateService.getTreeData(userId, Constants.PageFlagEnum.MAINTAIN_SINGLE.getCode());
                List<TreeDto> treeDtoList = JSONArray.parseArray(treeData.toString(), TreeDto.class);
                //启动获取最新数据线程并更新redis
                Runnable task = () -> {
                    logger.info("开始更新数据采集单机页面redis树结构数据！");
                    getTreeAndSetRedis(userId);
                    logger.info("更新数据采集单机页面redis树结构数据完成！");
                };
                //执行线程
                ExecutorProcessPool.getInstance().execute(task);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtoList));
            } else {
                //redis数据不存在，则需要查询，并更新至redis
                List<TreeDto> treeDtoList = getTreeAndSetRedis(userId);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtoList));
            }
        } catch (Exception e) {
            logger.error("获取数据采集单机页面树结构失败",e);
            e.printStackTrace();
            return ResultBody.failure("获取数据采集单机页面树结构失败");
        }
    }

    private List<TreeDto> getTreeAndSetRedis(String userId){
        List<TreeDto> dataTypeTree = standAloneService.getTree(userId);
        //排序
        try {
            treeServiceImpl.sortTreeDtoByKeyWords(dataTypeTree,Constants.TREE_ONE);
        }catch (Exception e) {
            logger.error("nodeTree is error",e);
        }

        //存入redis
        redisTemplateService.setTreeData(userId, Constants.PageFlagEnum.MAINTAIN_SINGLE.getCode(), JSON.toJSONString(dataTypeTree));
        return dataTypeTree;
    }

    /**
     * 查询模板列表
     */
    @ApiOperation(value = "查询模板列表", notes = "查询模板列表")
    @RequestMapping(value = {"page"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<GridView> list(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                     @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                     @ApiParam(name = "treeName", value = "树节点名称（非必填-获取状态需传入）", required = false, type = "string", defaultValue = "0") @RequestParam(value = "treeName", required = false, defaultValue = "") String treeName,
                                     @ApiParam(name = "是否获取表单数据",value = "是否获取表单数据",required = false,type = "string")  @RequestParam(required = false) String getFormFlag,
                                     @ApiParam(name = "模板名称", value = "模板名称", required = false, type = "string") @RequestParam(required = false) String name,
                                     @ApiParam(name = "树节点", value = "树节点id", required = true, type = "string") @RequestParam(required = false) String nodeId,
                                     @RequestParam(value = "attributes", required = false) String attributes,
                                     @ApiParam(name = "树层级", value = "数层级", required = true, type = "string") @RequestParam(required = false) String nodeLevel) {
        final String regex = "\\d+";
        if (StringUtils.isNotBlank(page) && !page.matches(regex)) {
            String message = "分页参数page 传值有误，page:" + page;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        if (StringUtils.isNotBlank(rows) && !rows.matches(regex)) {
            String message = "分页参数rows 传值有误，rows:" + rows;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        Map map = new HashMap();
        map.put("page", page);
        map.put("rows", rows);
        map.put("name", name);
        map.put("nodeId", nodeId);
        map.put("getFormFlag",getFormFlag);
        map.put("attributes", attributes);
        map.put("treeName",treeName);
        if ("4".equals(nodeLevel) || "5".equals(nodeLevel)) {
            ResultBody<GridView> result = standAloneService.getModelList(map);
            return result;
        } else {
            return ResultBody.success(new GridView<>(new ArrayList<>(), 0));
        }
    }

    /**
     * 查询动态模板列表
     */
    @ApiOperation(value = "根据模板查询条件列表", notes = "根据模板查询条件列表")
    @RequestMapping(value = {"dynamicpage"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<GridView> dynamicList(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                            @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                            @ApiParam(name = "模板名称", value = "模板名称", required = true, type = "string") @RequestParam(required = false) String code,
                                            @ApiParam(name = "树节点", value = "树节点id", required = true, type = "string") @RequestParam(required = false) String nodeId) {
        final String regex = "\\d+";
        if (StringUtils.isNotBlank(page) && !page.matches(regex)) {
            String message = "分页参数page 传值有误，page:" + page;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        if (StringUtils.isNotBlank(rows) && !rows.matches(regex)) {
            String message = "分页参数rows 传值有误，rows:" + rows;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        Map map = new HashMap();
        map.put("page", page);
        map.put("rows", rows);
        map.put("code", code);
//        ResultBody<GridView> result = standAloneService.getdynamicList(map);
        return null;
    }

    /**
     * 查询条件以及展示列
     */
    @ApiOperation(value = "查询条件以及展示列", notes = "查询条件以及展示列")
    @RequestMapping(value = {"dynamicpagecolumn"}, method = {RequestMethod.POST})
    @ResponseBody
    public List<ModuleColumnConfig> dynamicListColumn(
            @ApiParam(name = "模板名称", value = "模板名称", required = true, type = "string") @RequestParam(required = false) String code) {
        Map map = new HashMap();
        map.put("code", code);
        List<ModuleColumnConfig> result = standAloneService.getdynamicList(map);
        return result;
    }

}
