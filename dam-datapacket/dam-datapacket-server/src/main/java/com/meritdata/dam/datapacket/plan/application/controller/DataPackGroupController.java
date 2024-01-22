package com.meritdata.dam.datapacket.plan.application.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.meritdata.cloud.base.entity.Emp;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.common.SessionManager;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.application.dao.ModuleGroupPackRepository;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackGroupService;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.factory.ExecutorProcessPool;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/datapacket/group")
@Api(value = "数据包组包Api接口", tags = {"数据包组包Api接口"})
public class DataPackGroupController {
    private static final Logger logger = LoggerFactory.getLogger(DataPackGroupController.class);

    private static ExecutorService executorService = Executors.newFixedThreadPool(Constants.THREAD_MAX_NUM);

    @Autowired
    IMaintainService exhibitionService;

    @Autowired
    IStandAloneService standAloneService;

    @Autowired
    IDataPackGroupService dataPackGroupService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    ModuleGroupPackRepository moduleGroupPackRepository;

    @Autowired
    IMaintainService maintainService;

    @Autowired
    private ILogPostService logPostService;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private RedisTemplateService redisTemplateService;

    @Autowired
    private ITreeService treeService;

    //treeServiceImpl其中写了排序的公共方法
    @Autowired
    TreeServiceImpl treeServiceImpl;

    private final static String INDEX = "index";
    private final static String PREFIX = "/view";


    /**
     * 数据包组包左侧树
     *
     * @param keywords
     * @return
     */
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "数据包组包模型树查询", notes = "数据包组包模型树查询")
    public ResultBody maintainTree(@ApiParam(name = "keywords", value = "查询条件", required = false, type = "string") @RequestParam(required = false) String keywords) {
        try {
            //当前登陆人
            String userId = sessionUtils.getEmpId();
            boolean hasKey = redisTemplateService.hasKey(userId, Constants.PageFlagEnum.PACKAGE_GROUP.getCode());
            //如果redis存在该缓存
            if (hasKey) {
                JSONArray treeData = redisTemplateService.getTreeData(userId, Constants.PageFlagEnum.PACKAGE_GROUP.getCode());
                List<TreeDto> treeDtoList = JSONArray.parseArray(treeData.toString(), TreeDto.class);
                //启动获取最新数据线程并更新redis
                Runnable task = () -> {
                    logger.info("开始更新数据包组包页面redis树结构数据！");
                    getTreeAndSetRedis(userId);
                    logger.info("更新数据包组包页面redis树结构数据完成！");
                };
                //执行线程
                ExecutorProcessPool.getInstance().execute(task);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtoList));
            } else {
                //redis数据不存在，则需要查询，并更新至redis
                List<TreeDto> groupPackTree = getTreeAndSetRedis(userId);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, groupPackTree));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取组包树信息失败", e);
            return ResultBody.failure("获取组包树信息失败");
        }
    }

    private List<TreeDto> getTreeAndSetRedis(String userId) {
        List<TreeDto> subsystemTree = dataPackGroupService.groupPackTree(userId);
        List<TreeDto> standAloneTree = standAloneService.getTree(userId);
        List<TreeDto> groupPackTree = Stream.concat(subsystemTree.stream(), standAloneTree.stream()).collect(Collectors.toList());
        //排序
        try {
            treeServiceImpl.sortTreeDtoByKeyWords(groupPackTree,Constants.TREE_THREE);
        }catch (Exception e) {
            logger.error("据包组包左侧树 sort is error",e);
        }
        //存入redis
        redisTemplateService.setTreeData(userId, Constants.PageFlagEnum.PACKAGE_GROUP.getCode(), JSON.toJSONString(groupPackTree));
        return groupPackTree;
    }

    /**
     * 组包
     *
     * @param
     * @param attributes
     * @return
     */
    @RequestMapping(value = "/groupPack", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "数据包组包", notes = "数据包组包")
    public ResultBody exhibitionList(@ApiParam(name = "id", value = "树节点Id", required = true, type = "string") @RequestParam String id,
                                     @ApiParam(name = "pid", value = "父节点Id", required = true, type = "string") @RequestParam String pid,
                                     @ApiParam(name = "text", value = "树节点名称", required = true, type = "string") @RequestParam String text,
                                     @ApiParam(name = "attributes", value = "树节点属性", required = true, type = "string") @RequestParam String attributes) {
        String status = Message.STATUS_FAIL;

        try {
            status = Message.STATUS_SUCESS;
            String currentUserName = sessionUtils.getCurrentUserName();
            Emp emp = sessionUtils.getEmp();
            emp.setUserName(currentUserName);
            //活动线程数量小于10
            if (((ThreadPoolExecutor) executorService).getActiveCount() < Constants.THREAD_MAX_NUM) {
                executorService.execute(() -> {
                    try {
                        dataPackGroupService.groupPack(id, pid, text, attributes, emp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                return ResultBody.success("正在组包中，请稍后查看结果！");
            } else {
                return ResultBody.failure("组包请求过多，请稍后再进行组包！");
            }
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.packet"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.packet.message"), text),
                    status);
            logPostService.postLog(msg);
        }

    }

    /**
     * 清除缓存
     */
    @RequestMapping(value = "/clearCache", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "清除缓存", notes = "清除缓存")
    public ResultBody clearCache(@ApiParam(name = "text", value = "树节点名称", required = true, type = "string") @RequestParam String text) {
        String status = Message.STATUS_FAIL;
        try {
            status = Message.STATUS_SUCESS;
            return dataPackGroupService.clearCache(text);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("清除缓存失败", e);
            status = Message.STATUS_FAIL;
            return ResultBody.failure("清除缓存失败，请查看日志信息");
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.clearCache"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.clearCache.message"), text),
                    status);
            logPostService.postLog(msg);
        }
    }

    /**
     * 单行下载
     *
     * @param text
     * @param res
     * @return
     */
    @RequestMapping(value = "/singleDownload", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "组包单行下载", notes = "组包单行下载")
    public ResultBody singleDownload(@ApiParam(name = "text", value = "树节点名称", required = true, type = "string") @RequestParam String text,
                                     @ApiParam(name = "attributes", value = "树节点属性", required = true, type = "string") @RequestParam String attributes,
                                     @ApiParam(name = "id", value = "树节点id", required = true, type = "string") @RequestParam String id,
                                     @ApiParam(name = "groupPackDate", value = "组包时间", required = true, type = "string") @RequestParam String groupPackDate,
                                     HttpServletResponse res) {
        String status = Message.STATUS_FAIL;
        try {
            status = Message.STATUS_SUCESS;
            return dataPackGroupService.singleDownload(text, id, attributes, res,groupPackDate);
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("下载失败", e);
            status = Message.STATUS_FAIL;
            return ResultBody.failure("下载失败");
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.download"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.download.message"), text),
                    status);
            logPostService.postLog(msg);
        }
    }

    /**
     * 组包表格数据
     *
     * @param page
     * @param rows       //     * @param nodeId
     * @param nodeName
     * @param physicalNo
     * @param packager
     * @param startTime
     * @param endTime
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/groupPackList", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "数据包组包表格数据", notes = "数据包组包表格数据")
    public ResultBody groupPackList(@ApiParam(name = "page", value = "页数", required = true, type = "string") @RequestParam String page,
                                    @ApiParam(name = "rows", value = "行数", required = true, type = "string") @RequestParam String rows,
//                                              @ApiParam(name = "nodeId", value = "树节点Id", required = true, type = "string") @RequestParam String nodeId,
                                    @ApiParam(name = "nodeName", value = "树节点名称", required = true, type = "string") @RequestParam String nodeName,
                                    @ApiParam(name = "physicalNo", value = "实物号", required = false, type = "string") @RequestParam(required = false) String physicalNo,
                                    @ApiParam(name = "packager", value = "组包人", required = false, type = "string") @RequestParam(required = false) String packager,
                                    @ApiParam(name = "startTime", value = "开始时间", required = false, type = "string") @RequestParam(required = false) String startTime,
                                    @ApiParam(name = "endTime", value = "结束时间", required = false, type = "string") @RequestParam(required = false) String endTime) throws ParseException {

        final String regex = "\\d+";
        String status = Message.STATUS_FAIL;
        try {
            if (StringUtils.isNotBlank(page) && !page.matches(regex)) {
                String message = "分页参数page 传值有误，page:" + page;
                status = Message.STATUS_FAIL;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            if (StringUtils.isNotBlank(rows) && !rows.matches(regex)) {
                String message = "分页参数rows 传值有误，rows:" + rows;
                status = Message.STATUS_FAIL;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("page", page);
            map.put("rows", rows);
            status = Message.STATUS_SUCESS;
            return dataPackGroupService.dataListGroupPack(nodeName, physicalNo, packager, startTime, endTime, map);
        } catch (ParseException e) {
            status = Message.STATUS_FAIL;
            e.printStackTrace();
            logger.error("查询失败", e);
            return ResultBody.failure("查询失败");
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.search"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.search.message"), nodeName),
                    status);
            logPostService.postLog(msg);
        }
    }

    /**
     * 数据包组包数据校验
     *
     * @param
     * @param attributes
     * @return
     */
    @RequestMapping(value = "/validData", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "数据包组包数据校验", notes = "数据包组包数据校验")
    public ResultBody validData(@ApiParam(name = "id", value = "树节点Id", required = true, type = "string") @RequestParam String id,
                                @ApiParam(name = "pid", value = "父节点Id", required = true, type = "string") @RequestParam String pid,
                                @ApiParam(name = "text", value = "树节点名称", required = true, type = "string") @RequestParam String text,
                                @ApiParam(name = "attributes", value = "树节点属性", required = true, type = "string") @RequestParam String attributes) {
        try {
            Emp emp = sessionUtils.getEmp();
            List<String> strings = dataPackGroupService.validSecretData(id, pid, text, attributes, emp);
            return ResultBody.success(StringUtils.join(strings, "<br/>"));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("组包数据权限校验失败", e);
            return ResultBody.failure("数据权限校验失败,请查看日志信息");
        }
    }

}
