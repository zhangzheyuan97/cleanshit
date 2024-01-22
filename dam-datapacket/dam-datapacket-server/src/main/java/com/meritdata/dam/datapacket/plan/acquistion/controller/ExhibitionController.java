package com.meritdata.dam.datapacket.plan.acquistion.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.acquistion.service.IExhibitionService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.ExhibitionDTO;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.factory.ExecutorProcessPool;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.service.ITreeService;
import com.meritdata.dam.datapacket.plan.service.impl.TreeServiceImpl;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.impl.PackageSystemImpl;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.RedisTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/datapacket/show")
@Api(value = "实做BOMapi接口", tags = {"实做BOMapi接口"})
public class ExhibitionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExhibitionController.class);
    private final static String INDEX = "index";
    private final static String PREFIX = "/view";

    @Autowired
    IDatamationsClient iDatawarehouseClient;

    @Autowired
    IExhibitionService iExhibitionService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    SessionUtils sessionUtils;

    @Autowired
    private PackageSystemImpl packageSystem;

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
            mView.setViewName("/dam/datapacket/plan/acquistion/show/" + id);
        } else {
            String uri = request.getRequestURI();
            String suffix = uri.substring(uri.indexOf(PREFIX) + PREFIX.length());
            mView.setViewName("/dam/datapacket/plan/acquistion/show/view" + suffix);
        }
        mView.addAllObjects(params);
        return mView;
    }

    /**
     * 数据维护模型树查询
     *
     * @return
     */
    @RequestMapping(value = "/exhibition-tree", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "实做BOM展示模型树查询", notes = "实做BOM展示模型树查询")
    public ResultBody<List<TreeModel>> exhibitionTree(@ApiParam(name = "过滤条件", value = "过滤条件", required = false, type = "string") @RequestParam(required = false) String keywords) {
        try {
            String userId = sessionUtils.getEmpId();
            //判断是否存在key
            boolean hasKey = redisTemplateService.hasKey(userId, Constants.PageFlagEnum.DATA_MAINTAIN.getCode());
            //如果redis存在该缓存
            if (hasKey) {
                JSONArray treeData = redisTemplateService.getTreeData(userId, Constants.PageFlagEnum.DATA_MAINTAIN.getCode());
                List<TreeDto> treeDtoList = JSONArray.parseArray(treeData.toString(), TreeDto.class);
                //启动获取最新数据线程并更新redis
                Runnable task = () -> {
                    LOGGER.info("开始更新数据维护页面redis树结构数据！");
                    getTreeAndSetRedis(userId);
                    LOGGER.info("更新数据维护页面redis树结构数据完成！");
                };
                //执行线程
                ExecutorProcessPool.getInstance().execute(task);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, treeDtoList));
            } else {
                //redis数据不存在，则需要查询，并更新至redis
                List<TreeDto> exhibitionTree = getTreeAndSetRedis(userId);
                return ResultBody.success(treeService.getTreeListByKeyWords(keywords, exhibitionTree));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("获取左侧树信息失败", e);
            return ResultBody.failure("获取左侧树信息失败");
        }
    }


    private List<TreeDto> getTreeAndSetRedis(String userId) {
        List<TreeDto> treeDtos = iExhibitionService.exhibitionTree(userId);
        //排序
        try{
            treeServiceImpl.sortTreeDtoByKeyWords(treeDtos, Constants.TREE_THREE);
        }catch (Exception e) {
            LOGGER.error("exhibition-tree sort is error",e);
        }
        //存入redis
        redisTemplateService.setTreeData(userId, Constants.PageFlagEnum.DATA_MAINTAIN.getCode(), JSON.toJSONString(treeDtos));
        return treeDtos;
    }

    /**
     * 实做BOM展示左侧树与右侧表关系
     *
     * @return
     */
    @RequestMapping(value = "/exhibitionList", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "实做BOM展示右侧列表", notes = "实做BOM展示右侧列表")
    public ResultBody<GridView> exhibitionList(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                               @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                               @RequestParam(value = "id", required = false) String id,
                                               @RequestParam(value = "classIfication", required = false) String classIfication,
                                               @RequestParam(value = "drawingNo", required = false) String drawingNo,
                                               @RequestParam(value = "name", required = false) String name,
                                               @RequestParam(value = "batchNo", required = false) String batchNo,
                                               // TODO: 2023/6/20 左侧树批次号必填条件查询 后续有时间优化整体逻辑
                                               @RequestParam(value = "andBatchNo", required = false,defaultValue = "") String andBatchNo,
                                               @RequestParam(value = "physicalNo", required = false) String physicalNo,
                                               @RequestParam(value = "attributes", required = false) String attributes,
                                               @RequestParam(value = "text", required = false) String text,
                                               @RequestParam(value = "standAloneOrSubSys", required = false) String standAloneOrSubSys) {
        try {
            final String regex = "\\d+";
            //此id为分系统(发次)表的id，开发思路为：根据id查询分系统发次表的信息并且关联单机实物表的信息
            Map<String, Object> map = new HashMap();
            /**
             * 由于数据展示页面，分系统下分页参数传了，但返回全量数据，单机下传递分页参数，
             * 返回数据确实又分页了。导致后面的逻辑无法处理。因此查全量数据，解决禅道bug74237
             */
            if(StringUtils.isNotBlank(standAloneOrSubSys)) {    //为性能考虑，只有在需要排序的时候查全量数据
                map.put("page", "1");
                map.put("rows", "10000000");
            }else {
                map.put("page", page);
                map.put("rows", rows);
            }
            map.put("id", id);
            map.put("classIfication", classIfication);
            map.put("drawingNo", drawingNo);
            map.put("name", name);
            map.put("batchNo", batchNo);
            // TODO: 2023/6/20 左侧树批次号必填条件查询 后续有时间优化整体逻辑
            if (StringUtils.isNotEmpty(andBatchNo)){
                map.put("andBatchNo", andBatchNo);
            }
            map.put("physicalNo", physicalNo);
            map.put("attributes", attributes);
            map.put("text", text);
            JSONObject jsonObject = JSON.parseObject(map.get("attributes") == null ? "" : map.get("attributes").toString());
            String nodeType = jsonObject.get("nodeType") == null ? "" : jsonObject.get("nodeType").toString();
            if (StrUtil.isBlank(nodeType)) {
                return ResultBody.success(new GridView<>());
            }
            if (StringUtils.isBlank(page) && !page.matches(regex)) {
                String message = "分页参数page 传值有误，page:" + page;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            if (StringUtils.isBlank(rows) && !rows.matches(regex)) {
                String message = "分页参数rows 传值有误，rows:" + rows;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            //获取权限表信息
            PackageSystemEntity packageSystemEntity = new PackageSystemEntity();
            packageSystemEntity.setSystemId(sessionUtils.getEmpId());
            packageSystemEntity.setType("person");
            List<PackageSystemEntity> byEntity = packageSystem.findAuthorityDataByEntity(packageSystemEntity);
            List<String> textList = byEntity.stream().map(PackageSystemEntity::getResourceId).collect(Collectors.toList());
            map.put("textList", textList.toString().replace(" ", ""));
            List<ExhibitionDTO> exhibitionDTOS = iExhibitionService.dataList(map);
            //当前第几页
            int pageNo = Integer.parseInt(page);
            //一页五条
            int pageSize = Integer.parseInt(rows);
            // TODO: 2023/6/8 暂时假分页处理,后续需要优化整体逻辑
            //排序
            try {
                if("standAlone".equals(standAloneOrSubSys)) {  //单机按照实物排序
                    exhibitionDTOS.sort(customComparator);
                }else if("subSys".equals(standAloneOrSubSys)) {    //分系统按照分类，类型二，类型三，图号，批次，实物排序，空值排在最后面
                    Comparator mycmp = ComparableComparator.getInstance();
                    mycmp = ComparatorUtils.reversedComparator(mycmp);
                    mycmp = ComparatorUtils.nullHighComparator(mycmp);
                    ArrayList<Object> sortFields = new ArrayList<Object>();
                    sortFields.add(new BeanComparator<>("classIfication",mycmp));
                    sortFields.add(new BeanComparator<>("secondType",mycmp));
                    sortFields.add(new BeanComparator<>("thirdType",mycmp));
                    sortFields.add(new BeanComparator<>("drawingNo",mycmp));
                    sortFields.add(new BeanComparator<>("batchNo",mycmp));
                    sortFields.add(new BeanComparator<>("physicalNo",mycmp));

                    ComparatorChain multSort = new ComparatorChain(sortFields);
                    Collections.sort(exhibitionDTOS,multSort);
                }

            } catch (Exception e) {
                LOGGER.error("exhibitionList sort is error", e.getMessage());
            }
            //发次实物维护，实做BOM展示，选择实做BOM都会调用此接口，分系统和单机之间也会偶现分页或者不分页的情况，没有总结出来，暂时这么改。
            if(exhibitionDTOS.size() > pageSize) {
                exhibitionDTOS = exhibitionDTOS.stream().skip((pageNo - 1) * pageSize).limit(pageSize).
                    collect(Collectors.toList());
            }
            return ResultBody.success(new GridView<>(exhibitionDTOS, iDatamationsClient.dataCount(map)));
        } catch (Exception e) {
            LOGGER.info("列表查询错误信息：" + e.getMessage());
            return ResultBody.success(new GridView<>());
        }

    }

    /**
     * 实做BOM展示，单机批次进入该排序，使用实物号对返回数据的排序
     */
    private Comparator<ExhibitionDTO> customComparator = new Comparator<ExhibitionDTO>() {
        @Override
        public int compare(ExhibitionDTO o1, ExhibitionDTO o2) {
            String s1 = o1.getPhysicalNo();
            String s2 = o2.getPhysicalNo();

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
                return collator.compare(s1, s2);
            }
        }
    };

    /**
     * 数据包审核-实做BOM展示左侧树与右侧表关系
     *
     * @return
     */
    @RequestMapping(value = "/exhibitionListUsedManage", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "实做BOM展示右侧列表", notes = "实做BOM展示右侧列表")
    public ResultBody<GridView> exhibitionListUsedManage(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                                         @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                                         @RequestParam(value = "id", required = false) String id,
                                                         @RequestParam(value = "classIfication", required = false) String classIfication,
                                                         @RequestParam(value = "drawingNo", required = false) String drawingNo,
                                                         @RequestParam(value = "name", required = false) String name,
                                                         @RequestParam(value = "batchNo", required = false) String batchNo,
                                                         @RequestParam(value = "physicalNo", required = false) String physicalNo,
                                                         @RequestParam(value = "attributes", required = false) String attributes,
                                                         @RequestParam(value = "text", required = false) String text,
                                                         @RequestParam(value = "fcId", required = false) String fcId,
                                                         @RequestParam(value = "fcPhysicalNo", required = false) String fcPhysicalNo) {
        //定义初始分页值，先查出全量，再进行分页。否则有bug
        String pageInit = "1",rowsInit = "1000000";
        //TODO:分页问题 后期有时间做优化
        //当前第几页
        int pageNo = Integer.parseInt(page);
        //一页五条
        int pageSize = Integer.parseInt(rows);
        JSONObject jsonObject = JSON.parseObject(attributes);
//        //如果是分系统和模块，则fourthlyNode为发次，单机fourthlyNode为批次
        batchNo = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
        //如果是分系统和模块，thirdlyNode为型号，单机thirdlyNode为图号
        drawingNo = jsonObject.get("thirdlyNode") == null ? "" : jsonObject.get("thirdlyNode").toString();
        //如果是分系统和模块，secondNode为分系统类型，单机secondNode为单机类型
        classIfication = jsonObject.get("secondNode") == null ? "" : jsonObject.get("secondNode").toString();
        if (StringUtils.isEmpty(fcId) && StringUtils.isEmpty(fcPhysicalNo)) {
//            ResultBody<GridView> result = exhibitionList(pageInit, rowsInit, id, classIfication, drawingNo, name, batchNo, physicalNo, attributes, text);
            //TODO 解决模糊查询 批次号和实物号一个输入框 后期有时间做优化
            ResultBody<GridView> result = exhibitionList(pageInit, rowsInit, id, classIfication, drawingNo, name,"", batchNo, "", attributes, text,null);
            List<ExhibitionDTO> exhibitionDTOListAll = result.getData().getRows();
            List<ExhibitionDTO> exhibitionDTOList = new ArrayList<>();

            //总装直属件-没有实物号展示批次号
            exhibitionDTOListAll.forEach(model -> {
                if (StringUtils.isEmpty(model.getPhysicalNo())) {
                    model.setPhysicalNo(model.getBatchNo());
                }
                exhibitionDTOList.add(model);
            });
            //TODO 解决模糊查询 批次号和实物号一个输入框 后期有时间做优化
            List<ExhibitionDTO> resultArray= exhibitionDTOList;
            if (StringUtils.isNotEmpty(physicalNo)){
                resultArray = exhibitionDTOList.stream().filter(item -> item.getPhysicalNo().contains(physicalNo)).collect(Collectors.toList());
            }
            //总数
            int total = resultArray.size();
            List<ExhibitionDTO> subList = resultArray.stream().skip((pageNo - 1) * pageSize).limit(pageSize).
                    collect(Collectors.toList());
            return ResultBody.success(new GridView<>(subList,
                    total));
        } else {

            ResultBody<GridView> result = exhibitionList(pageInit, rowsInit, id, classIfication, drawingNo, name, "",batchNo, "", attributes, text,null);
            List<ExhibitionDTO> exhibitionDTOListAll = result.getData().getRows();
            List<ExhibitionDTO> exhibitionDTOList = new ArrayList<>();

            //总装直属件-没有实物号展示批次号
            exhibitionDTOListAll.forEach(model -> {
                if (StringUtils.isEmpty(model.getPhysicalNo())) {
                    model.setPhysicalNo(model.getBatchNo());
                }
                exhibitionDTOList.add(model);
            });

            List<ExhibitionDTO> exhibitionAll = new ArrayList<>();
            ExhibitionDTO dto = new ExhibitionDTO();
            dto.setId(fcId);
            dto.setPhysicalNo(fcPhysicalNo);
            exhibitionAll.add(dto);

            if (null != exhibitionDTOList && exhibitionDTOList.size() > 0) {
                exhibitionAll.addAll(exhibitionDTOList);
            }

            //TODO 解决模糊查询 批次号和实物号一个输入框 后期有时间做优化
            if (StringUtils.isNotEmpty(physicalNo)){
                exhibitionAll = exhibitionAll.stream().filter(item -> item.getPhysicalNo().contains(physicalNo)).collect(Collectors.toList());
            }
            //排序
            try{
                exhibitionAll.sort(customComparator);
            } catch (Exception e) {
                LOGGER.error("exhibitionListUsedManage sort is error",e);
            }
            //TODO:分页问题
            //总数
            int total = exhibitionAll.size();
            //总页数
//            int pageSum = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
            //分页
            // TODO: 2023/6/8 暂时假分页处理,后续需要优化整体逻辑 
            List<ExhibitionDTO> subList = exhibitionAll.stream().skip((pageNo - 1) * pageSize).limit(pageSize).
                    collect(Collectors.toList());

            return ResultBody.success(new GridView<>(subList,
                    total));
        }
    }
}
