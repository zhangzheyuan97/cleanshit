package com.meritdata.dam.datapacket.plan.application.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.entity.Emp;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.base.exception.ParamNotBlankException;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackShowService;
import com.meritdata.dam.datapacket.plan.factory.ExecutorProcessPool;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.service.ITreeService;
import com.meritdata.dam.datapacket.plan.service.impl.TreeServiceImpl;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.RedisTemplateService;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/datapacket/show")
@Api(value = "数据包展示Api接口", tags = {"数据包展示Api接口"})
public class DataPackViewController {
    private static final Logger logger = LoggerFactory.getLogger(DataPackViewController.class);

    @Autowired
    IMaintainService maintainService;

    @Autowired
    IStandAloneService standAloneService;

    @Autowired
    IDataPackShowService dataPackShowService;

    private final static String INDEX = "index";
    private final static String PREFIX = "/view";

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
//    @RequestMapping("/view/{id}/**")
//    public ModelAndView view(@PathVariable String id, HttpServletRequest request, @RequestParam(required = false) Map<String, String> params) {
//        ModelAndView mView = new ModelAndView();
//        if (INDEX.equals(id)) {
//            mView.setViewName("/dam/datapacket/plan/model/curate/" + id);
//        } else {
//            String uri = request.getRequestURI();
//            String suffix = uri.substring(uri.indexOf(PREFIX) + PREFIX.length());
//            mView.setViewName("/dam/datapacket/plan/model/curate/view/" + suffix);
//        }
//        mView.addAllObjects(params);
//        return mView;
//    }


    /**
     * 数据包展示左侧树
     *
     * @param keywords
     * @return
     */
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "数据包展示模型树查询", notes = "数据包展示模型树查询")
    public ResultBody maintainTree(@ApiParam(name = "过滤条件", value = "keywords", required = false, type = "string") @RequestParam(required = false) String keywords) {
        try {
            String userId = sessionUtils.getEmpId();
            boolean hasKey = redisTemplateService.hasKey(userId, Constants.PageFlagEnum.PACKAGE_SHOW.getCode());
            //如果redis存在该缓存
            if (hasKey) {
                //获取左侧树信息
                JSONArray treeData = redisTemplateService.getTreeData(userId, Constants.PageFlagEnum.PACKAGE_SHOW.getCode());
                List<TreeDto> treeDtoList = JSONArray.parseArray(treeData.toString(), TreeDto.class);
                //启动获取最新数据线程并更新redis
                Runnable task = () -> {
                    logger.info("开始更新数据包展示页面redis树结构数据！");
                    getTreeAndSetRedis(userId);
                    logger.info("更新数据包展示页面redis树结构数据完成！");
                };
                //执行线程
                ExecutorProcessPool.getInstance().execute(task);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords,treeDtoList));
            }else{
                List<TreeDto> applicationTree = getTreeAndSetRedis(userId);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords,applicationTree));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取左侧树信息失败", e);
            return ResultBody.failure("获取左侧树信息失败");
        }
    }

    private  List<TreeDto> getTreeAndSetRedis(String userId){
        List<TreeDto> exhibitionTree = maintainService.maintainTree(userId);
        List<TreeDto> dataTypeTree = standAloneService.getTree(userId);
        List<TreeDto> applicationTree = Stream.concat(exhibitionTree.stream(), dataTypeTree.stream()).collect(Collectors.toList());
        try {
            //排序
            treeServiceImpl.sortTreeDtoByKeyWords(applicationTree,Constants.TREE_THREE);
        }catch (Exception e) {
            logger.error("数据包展示左侧树 sort is error",e);
        }

        //存入redis
        redisTemplateService.setTreeData(userId, Constants.PageFlagEnum.PACKAGE_SHOW.getCode(), JSON.toJSONString(applicationTree));
        return applicationTree;
    };

    /**
     * 查询模型版本列表
     *
     * @param page
     * @param rows
     * @param name
     * @param nodeId
     * @param attributes
     * @return
     */
    @ApiOperation(value = "查询模板管理列表", notes = "查询模板管理列表")
    @RequestMapping(value = "/templatePage", method = RequestMethod.POST)
    @ResponseBody
    public ResultBody<GridView> list(@ApiParam(name = "page", value = "页数", required = true, type = "string") @RequestParam String page,
                                     @ApiParam(name = "rows", value = "行数", required = true, type = "string") @RequestParam String rows,
                                     @ApiParam(name = "name", value = "模板名称", required = false, type = "string") @RequestParam(required = false) String name,
                                     @ApiParam(name = "nodeId", value = "树节点Id", required = false, type = "string") @RequestParam(required = false) String nodeId,
                                     @ApiParam(name = "bomName", value = "树节点名称（非必填-获取状态需传入）", required = false, type = "string", defaultValue = "") @RequestParam(value = "bomName", required = false, defaultValue = "") String bomName,
                                     @ApiParam(name = "attributes", value = "树节点属性", required = false, type = "string") @RequestParam(value = "attributes", required = false) String attributes,
                                     @ApiParam(name = "batchNoNodeInfo", value = "批次号审批的树节点属性", required = false, type = "string") @RequestParam(value = "batchNoNodeInfo", required = false) String batchNoNodeInfo) {
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
        //后边调用公共方法，有使用getFormFlag判断模板是否有数据，数据展示没有此功能，不穿getFormFlag参数导致报错，给默认值避免报错问题
        map.put("getFormFlag","0");
        if (!StringUtils.isEmpty(bomName)) {
            map.put("bomName", bomName);
        }
        BatchNoNodeInfo batchNoInfo = null;
        if (StringUtils.isNotEmpty(batchNoNodeInfo)){
            batchNoInfo = JSONObject.parseObject(batchNoNodeInfo, BatchNoNodeInfo.class);
        }
        return dataPackShowService.queryTemplateList(map, attributes,batchNoInfo);
    }

    /**
     * 查询右侧列表
     *
     * @param physicalCode
     * @param modelId
     * @param param
     * @return
     */
    @RequestMapping(value = "/viewDataPage", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "数据包展示右侧列表查询", notes = "数据包展示右侧列表查询")
    public ResultBody<GridView> dataListManage(@ApiParam(name = "physicalCode", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String physicalCode,
                                               @ApiParam(name = "modelId", value = "型号id信息", required = true, type = "string") @RequestParam(required = false) String modelId,
                                               @ApiParam(name = "tempID", value = "型号id信息", required = true, type = "string") @RequestParam(required = false) String tempID,
                                               @RequestParam(value = "batchNoNodeInfo", required = false) String batchNoNodeInfo,
                                               @RequestBody ModelDataQueryParamVO param) {
        try {
            final String regex = "\\d+";
            if (modelId == null) {
                throw new ParamNotBlankException("模型id");
            }
            String page = param.getPage().toString();
            String rows = param.getRows().toString();
            if (StringUtils.isBlank(page) && !page.matches(regex)) {
                String message = "分页参数page 传值有误，page:" + page;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            if (StringUtils.isBlank(rows) && !rows.matches(regex)) {
                String message = "分页参数rows 传值有误，rows:" + rows;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            //按照批次号审批后修改
            return dataPackShowService.queryDataList("", modelId, param,tempID,batchNoNodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询数据失败", e);
            return ResultBody.failure("查询数据失败！");
        }
    }
}
