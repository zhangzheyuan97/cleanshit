package com.meritdata.dam.datapacket.plan.acquistion.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gbasedbt.json.JSON;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.acquistion.service.ILityMatterService;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.LityInfoVo;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.factory.ExecutorProcessPool;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.service.ITreeService;
import com.meritdata.dam.datapacket.plan.service.impl.TreeServiceImpl;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.PageResult;
import com.meritdata.dam.datapacket.plan.utils.RedisTemplateService;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/datapacket/gather")
@Api(value = "发次实物api接口", tags = {"发次实物api接口"})
public class LityMatterController {

    private static final Logger logger = LoggerFactory.getLogger(LityMatterController.class);


    private final static String INDEX = "index";
    private final static String PREFIX = "/view";

    @Autowired
    private IDatamationsClient datamationsClient;

    @Autowired
    private IModulePlanService modulePlanService;

    @Autowired
    private ILityMatterService lityMatterService;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private ITreeService treeService;

    @Autowired
    private RedisTemplateService redisTemplateService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IDataPacketClient dataPacketClient;

    @Autowired
    IMaintainService maintainService;

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
            mView.setViewName("/dam/datapacket/plan/acquistion/dataAcquistion/" + id);
        } else {
            String uri = request.getRequestURI();
            String suffix = uri.substring(uri.indexOf(PREFIX) + PREFIX.length());
            mView.setViewName("/dam/datapacket/plan/acquistion/dataAcquistion/view" + suffix);
        }
        mView.addAllObjects(params);
        return mView;
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
        String userId = sessionUtils.getEmpId();
        boolean hasKey = redisTemplateService.hasKey(userId, Constants.PageFlagEnum.LITY_MATTER.getCode());
        //如果redis存在该缓存
        if (hasKey) {
            JSONArray treeData = redisTemplateService.getTreeData(userId, Constants.PageFlagEnum.LITY_MATTER.getCode());
            List<TreeDto> treeDtoList = JSONArray.parseArray(treeData.toString(), TreeDto.class);
            //启动获取最新数据线程并更新redis
            Runnable task = () -> {
                logger.info("开始更新发次实物页面redis树结构数据！");
                getTreeAndSetRedis(userId);
                logger.info("更新发次实物页面redis树结构数据完成！");
            };
            //执行线程
            ExecutorProcessPool.getInstance().execute(task);
            return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtoList));
        } else {
            //redis数据不存在，则需要查询，并更新至redis
            List<TreeDto> treeDtoList = getTreeAndSetRedis(userId);
            return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtoList));
        }
    }

    private List<TreeDto> getTreeAndSetRedis(String userId){
        List<TreeDto> dataTypeTree = modulePlanService.tree("-1", userId);
        //挂载单机
        List<TreeDto> tree = lityMatterService.addTreeNode(userId, dataTypeTree);
        //排序
        treeServiceImpl.sortTreeDtoByKeyWords(tree,Constants.TREE_FOUR);
        //存入redis
        redisTemplateService.setTreeData(userId, Constants.PageFlagEnum.LITY_MATTER.getCode(), com.alibaba.fastjson.JSON.toJSONString(tree));
        return tree;
    }

    /**
     * 查询模型树列表,包含系统默认节点
     *
     * @return
     */
    @RequestMapping(value = "/licenseTree", method = {RequestMethod.POST})
    @ApiOperation(value = "授权树接口", notes = "授权树接口")
    @ResponseBody
    public ResultBody<List<TreeModel<Object>>> licenseTree(@ApiParam(name = "关键字", value = "关键字", required = false, type = "string") @RequestParam(required = false) String keywords) {
        List<TreeDto> dataTypeTree = modulePlanService.licenseTree("-1");
        //挂载单机
        List<TreeDto> tree = lityMatterService.addlicenseTreeNode(dataTypeTree);

        try {
            //排序
            treeServiceImpl.sortTreeDtoByKeyWords(tree,Constants.TREE_FOUR);
        }catch (Exception e) {
            logger.error("licenseTree sort is error",e);
        }


        return ResultBody.success(treeService.getTreeListByKeyWords(keywords, tree));
    }

    /**
     * 查询发次列表
     */
    @ApiOperation(value = "查询发次列表", notes = "查询发次列表")
    @RequestMapping(value = {"page"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<GridView> list(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                     @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                     @ApiParam(name = "发次", value = "发次", required = false, type = "string") @RequestParam(required = false) String lity,
                                     @ApiParam(name = "是否获取编辑标识", value = "是否获取编辑标识", required = false, type = "string") @RequestParam(required = false) String getEditFlag,
                                     @ApiParam(name = "型号id", value = "型号id", required = true, type = "string") @RequestParam(required = false) String modelName,
                                     @ApiParam(name = "类型", value = "类型", required = false, type = "string") @RequestParam(required = false) String attributes) {
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
            Map map = new HashMap();
            map.put("page", page);
            map.put("rows", rows);
            map.put("F_ISSUE_NO", lity);
            map.put("F_MODEL", modelName);
            map.put("attributes", attributes);
            //查询发次信息
            PageResult<Map<String, Object>> list = datamationsClient.getFCInfoList(map);
            List<LityInfoVo> lityInfoVoList = new ArrayList<>();

            list.getRows().stream().forEach(item -> {
                LityInfoVo lityInfoVo = new LityInfoVo();
                lityInfoVo.setId(item.get("F_M_SYS_ID").toString());
                lityInfoVo.setLity(item.get("F_ISSUE_NO").toString());
                lityInfoVo.setModelName(item.get("F_MODEL").toString());

                lityInfoVo.setCreateTime(item.get("S_M_SYS_CREATETIME").toString());

                if("1".equals(getEditFlag)) {
                    //查询发次下是否有单机或者总装直属件
                    ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
                    JSONObject filterMap = new JSONObject();
                    filterMap.put("F_ISSUE_NO", item.get("F_ISSUE_NO").toString());
                    paramVO.setQueryFilter(filterMap.toJSONString());
                    List<Map<String, Object>> qdInfo = iDatamationsClient.getQDInfo(paramVO);
                    //1表示可以编辑
                    if(CollectionUtils.isEmpty(qdInfo)) {
                        lityInfoVo.setEditAbleOrNot("1");
                    }else {
                        lityInfoVo.setEditAbleOrNot("0");
                    }
                    //如果没有关联单机或者总装直属件实物，在可编辑为1，需要进一步判断，该发次作为本级时是否关联有数据
                    if("1".equals(lityInfoVo.getEditAbleOrNot())) {
                        Map moduleMap = new HashMap();
                        moduleMap.putAll(map);
                        moduleMap.put("F_ISSUE_NO",item.get("F_ISSUE_NO").toString());
                        moduleMap.put("page","1");
                        moduleMap.put("rows","1000000");
                        //查看本下是否有数据
                        List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(moduleMap);
                        moduleManageInfoList.forEach(model -> {
                            //1表示要查询对象表单下是否有数据,分系统和模块
                            List<Object[]> verList = (List<Object[]>) maintainService.QuerySql(model.getTableName(), "all", item.get("F_ISSUE_NO").toString());
                            //查询表字段
                            List<Object[]> cloList = (List<Object[]>) maintainService.QuerySql(model.getTableName(), "column", item.get("F_ISSUE_NO").toString());
                            //将List<Object[]>转换成List<Map<String, String>>方便取数据
                            List<Map<String, String>> verlm = maintainService.ListForMap(verList, cloList);
                            if(CollectionUtils.isNotEmpty(verlm)){
                                lityInfoVo.setEditAbleOrNot("0");
                            }
                        });
                    }
                }

                lityInfoVoList.add(lityInfoVo);
            });
            long count = datamationsClient.getFCInfoListCount(map);
//            List<LityInfoVo> lityInfoVos = lityInfoVoList.stream().sorted(Comparator.comparing(LityInfoVo::getCreateTime).reversed()).collect(Collectors.toList());
            AtomicReference<Integer> num = new AtomicReference<>(1);
            //排序
            try{
                lityInfoVoList.sort(customComparator);
            }catch (Exception e) {
                logger.error("查询发次列表 sort is error",e);
            }
            lityInfoVoList.forEach(item -> {
                item.setNum((Integer.parseInt(page) - 1) * 10 + num.getAndSet(num.get() + 1));
            });
            return ResultBody.success(new GridView(lityInfoVoList, count));
        } catch (Exception e) {
            logger.error("feign调用失败！", e.getMessage());
            return ResultBody.success(new GridView(new ArrayList(), 0));
        }
    }

    /**
     * 对返回数据的排序
     */
    private Comparator<LityInfoVo> customComparator = new Comparator<LityInfoVo>() {
        @Override
        public int compare(LityInfoVo o1, LityInfoVo o2) {
            String s1 = o1.getLity();
            String s2 = o2.getLity();

            //判断s1和s2是否为纯数字或者数字开头
            boolean isNumeric1 = s1.matches("\\d+");
            boolean isNumeric2 = s2.matches("\\d+");
            if (isNumeric1 && !isNumeric2) {
                return -1; // 数字排在前面
            } else if (!isNumeric1 && isNumeric2) {
                return 1; // 字母排在数字后面
            } else if (isNumeric1 && isNumeric2) {
                //如果都是数字，则按照从小到大排序
                BigInteger num1 = new BigInteger(s1);
                BigInteger num2 = new BigInteger(s2);
                return num1.compareTo(num2);
            } else {
                //其他情况按照字符串比较
                Collator collator = Collator.getInstance(Locale.CHINA);
                return collator.compare(s1,s2);
            }
        }
    };


    /**
     * 新增发次
     */
    @ApiOperation(value = "新增发次", notes = "新增发次")
    @RequestMapping(value = {"addLity"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody addLity(@ApiParam(name = "分类", value = "分类", required = true, type = "string") @RequestParam String attributes,
                              @ApiParam(name = "型号", value = "型号", required = true, type = "string") @RequestParam String model,
                              @ApiParam(name = "发次", value = "发次", required = true, type = "string") @RequestParam String lity
    ) {
        return lityMatterService.addLity(attributes, model, lity);
    }

    /**
     * 编辑发次
     */
    @ApiOperation(value = "编辑发次", notes = "编辑发次")
    @RequestMapping(value = {"editLity"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody editLity(@ApiParam(name = "发次ID", value = "发次ID", required = true, type = "string") @RequestParam String mSysId,
                               @ApiParam(name = "分类", value = "分类", required = true, type = "string") @RequestParam String attributes,
                              @ApiParam(name = "型号", value = "型号", required = true, type = "string") @RequestParam String model,
                              @ApiParam(name = "原发次", value = "原发次", required = true, type = "string") @RequestParam String lityShadow,
                              @ApiParam(name = "发次", value = "发次", required = true, type = "string") @RequestParam String lity
    ) {
        return lityMatterService.editLity(lityShadow,mSysId,attributes, model, lity);
    }

    /**
     * 新增发次
     */
    @ApiOperation(value = "新增单机或总装直属件实物", notes = "新增单机或总装直属件实物")
    @RequestMapping(value = {"addPhysical"}, method = {RequestMethod.POST})
    @ResponseBody
    public  ResultBody addMatter(@ApiParam(name = "分类", value = "分类", required = false, type = "string") @RequestParam String classIfication,
                                 @ApiParam(name = "类型二", value = "类型二", required = false, type = "string") @RequestParam(required = false) String secondType,
                                 @ApiParam(name = "类型三", value = "类型三", required = false, type = "string") @RequestParam(required = false) String thirdType,
                                @ApiParam(name = "图号", value = "图号", required = true, type = "string") @RequestParam String drawingNo,
                                @ApiParam(name = "名称", value = "名称", required = true, type = "string") @RequestParam String name,
                                @ApiParam(name = "批次号", value = "批次号", required = true, type = "string") @RequestParam String batchNo,
                                @ApiParam(name = "实物号", value = "实物号", required = true, type = "string") @RequestParam String physicalNo,
                                @ApiParam(name = "类别", value = "类别", required = true, type = "string") @RequestParam String attributes,
                                @ApiParam(name = "是否管理到实物", value = "是否管理到实物", required = false, type = "string") @RequestParam String isManageObject
    ) {
        return   lityMatterService.addPhysical(attributes, classIfication, secondType, thirdType, drawingNo, name, batchNo, physicalNo, isManageObject);
    }


    /**
     * 删除模型数据
     */
    @ApiOperation(value = "删除模型数据", notes = "删除模型数据")
    @RequestMapping(value = {"deleteModelData"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody deleteModelData(@RequestParam String selectGridData, @RequestParam String tableName) {
        List<Map<String, Object>> parse = (List<Map<String, Object>>) JSON.parse(selectGridData);
        ResultBody resultBody = lityMatterService.deletedata(parse, tableName);
        return resultBody;
    }

    /**
     * 获取该型号下的所有图号
     */
    @ApiOperation(value = "获取该型号下的所有图号", notes = "获取该型号下的所有图号")
    @RequestMapping(value = {"getFigure"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody getFigure(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                @ApiParam(name = "节点text", value = "节点text", required = true, type = "string") @RequestParam String text,
                                @ApiParam(name = "发次", value = "发次", required = true, type = "string") @RequestParam String lity) {

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
            Map map = new HashMap();
            map.put("page", page);
            map.put("rows", rows);
            map.put("F_PARENT_PHYSICAL_ID", text);
            AtomicInteger numFlag = new AtomicInteger();
            List<Map<String, Object>> list = datamationsClient.exhibitionTree(map, "EBOM");
            List<LityInfoVo> lityInfoVoList = new ArrayList<>();
            List<String> batchList = new ArrayList<>();
            AtomicReference<Integer> num = new AtomicReference<>(1);
            list.stream().forEach(item -> {
                //根据型号和图号查询是否有实物号
                Map maps = new HashMap();
                maps.put("F_MODEL", text);
                maps.put("F_DRAWING_NO", item.get("F_SUB_NO").toString());
                maps.put("F_ISSUE_NO", lity);
                List<Map<String, Object>> supportingListInfo = datamationsClient.getPhysicalNo(maps);
                if (supportingListInfo != null) {
                    supportingListInfo.stream().forEach(supportingInfo -> {
                        LityInfoVo lityInfoVo = new LityInfoVo();
                        lityInfoVo.setFigure(item.get("F_SUB_NO").toString());
                        lityInfoVo.setNum(num.getAndSet(num.get() + 1));
                        String physicalNo = "";
                        physicalNo = supportingInfo.get("F_PHYSICAL_NO") != null ? supportingInfo.get("F_PHYSICAL_NO").toString() : "";
                        lityInfoVo.setId(supportingInfo.get("F_Update_ID").toString());
                        lityInfoVo.setFMSysId(supportingInfo.get("F_M_SYS_ID").toString());


                        //根据图号查询单机表的批次号，和实物号
                        Map mapInfo = new HashMap();
                        mapInfo.put("F_DRAWING_NO", item.get("F_SUB_NO").toString());
                        if (org.apache.commons.lang3.StringUtils.isNotBlank(physicalNo)) {
                            mapInfo.put("F_PHYSICAL_NO", physicalNo);
                        }
                        Map physical_object_single_machine = datamationsClient.getPcAndSwInfo(mapInfo, "SUPPORTING_LIST");

                        if (physicalNo != null) {
                            lityInfoVo.setPhysicalNo(physicalNo);
                            if (physicalNo != "") {
                                //批次号
                                lityInfoVo.setBatchNo(physical_object_single_machine.get("F_BATCH_NO").toString());
                            } else {
                                Map mapLity = new HashMap();
                                mapLity.put("F_DRAWING_NO", item.get("F_SUB_NO").toString());
                                mapLity.put("F_ISSUE_NO", lity);
                                List<Map<String, Object>> supporting_list = datamationsClient.getPcAndSwInfoByDrawing(mapLity, "SUPPORTING_LIST");
                                lityInfoVo.setBatchNo(supporting_list.get(numFlag.get()).get("F_BATCH_NO").toString());
                                numFlag.getAndIncrement();
                            }
                        } else {
                            lityInfoVo.setPhysicalNo("");
                        }
                        Map drawingMap = new HashMap();
                        drawingMap.put("F_DRAWING_NO", item.get("F_SUB_NO").toString());
                        String expandNum = item.get("F_SUB_QUANTITY") == null ? "1" : item.get("F_SUB_QUANTITY").toString();
                        lityInfoVo.setExpandNum(expandNum);
                        lityInfoVo.setIsUpdate("0");
                        lityInfoVoList.add(lityInfoVo);
                    });

                } else {
                    LityInfoVo lityInfoVo = new LityInfoVo();
                    lityInfoVo.setFigure(item.get("F_SUB_NO").toString());
                    lityInfoVo.setNum(num.getAndSet(num.get() + 1));
                    //根据图号和发次查询单机表的批次号，和实物号
                    Map mapInfo = new HashMap();
                    mapInfo.put("F_DRAWING_NO", item.get("F_SUB_NO").toString());
                    mapInfo.put("F_ISSUE_NO", lity);
//                    Map physical_object_single_machine = datamationsClient.getPcAndSwInfo(mapInfo, "PHYSICAL_OBJECT_SINGLE_MACHINE");
                    List<Map<String, Object>> physical_object_single_machine = datamationsClient.getPcAndSwInfoByPhysicalNo(mapInfo, "SUPPORTING_LIST");
                    if (physical_object_single_machine.size() > 0) {
                        //批次号
                        physical_object_single_machine.forEach(items -> {
                            if (items.get("F_BATCH_NO") != null && !batchList.contains(items.get("F_BATCH_NO").toString())) {
                                lityInfoVo.setBatchNo(items.get("F_BATCH_NO").toString());
                                batchList.add(items.get("F_BATCH_NO").toString());
                            }
                        });
                    }
                    lityInfoVo.setId(UUID.randomUUID().toString());
                    Map drawingMap = new HashMap();
                    drawingMap.put("F_DRAWING_NO", item.get("F_SUB_NO").toString());
                    String expandNum = item.get("F_SUB_QUANTITY") == null ? "1" : item.get("F_SUB_QUANTITY").toString();
                    lityInfoVo.setExpandNum(expandNum);
                    lityInfoVo.setIsUpdate("0");
                    lityInfoVoList.add(lityInfoVo);
                }
            });
            long count = datamationsClient.exhibitionTree(map, "EBOM").size();
            return ResultBody.success(new GridView(lityInfoVoList, count));
        } catch (Exception e) {
            logger.error("feign调用失败！", e.getMessage());
            return ResultBody.success(new GridView(new ArrayList(), 0));
        }
    }


}
