package com.meritdata.dam.datapacket.plan.acquistion.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.properties.MeritdataCloudProperties;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.base.exception.ParamNotBlankException;
import com.meritdata.dam.base.log.annotation.OperateLogger;
import com.meritdata.dam.common.client.service.ClientService;
import com.meritdata.dam.common.enums.VersionStatusEnum;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.ExhibitionDTO;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.envelope.service.IEnvelopeService;
import com.meritdata.dam.datapacket.plan.factory.ExecutorProcessPool;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.model.vo.ModelDataExportParamDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.service.ITreeService;
import com.meritdata.dam.datapacket.plan.service.impl.TreeServiceImpl;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.EnumDamDatamanageResultStatus;
import com.meritdata.dam.datapacket.plan.utils.PageUtil;
import com.meritdata.dam.datapacket.plan.utils.RedisTemplateService;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.datamanage.DataQueryDTO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import com.meritdata.dam.entity.metamanage.ModelVerFieldDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/datapacket/maintain")
@Api(value = "数据采集分系统API接口", tags = {"数据采集分系统API接口"})
public class MaintainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintainController.class);
    private final static String INDEX = "index";
    private final static String PREFIX = "/view";

    @Autowired
    IMaintainService iMaintainService;

    @Autowired
    IDatamationsClient iDatawarehouseClient;

    @Autowired
    private IStandAloneService standAloneService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IDataPacketClient iDataPacketClient;

    @Autowired
    IFlowBomSheetDataInter iFlowBomSheetDataInter;

    @Autowired
    SessionUtils sessionUtils;

    @Autowired
    private ILogPostService logPostService;

    /**
     * 判断密集
     */
    @Autowired
    private MeritdataCloudProperties meritdataCloudProperties;

    @Autowired
    private ITreeService treeService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private RedisTemplateService redisTemplateService;

    @Autowired
    private IEnvelopeService envelopeService;

    @Autowired
    IDataPacketClient dataPacketClient;

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
            mView.setViewName("/dam/datapacket/plan/acquistion/maintain/" + id);
        } else {
            String uri = request.getRequestURI();
            String suffix = uri.substring(uri.indexOf(PREFIX) + PREFIX.length());
            mView.setViewName("/dam/datapacket/plan/acquistion/maintain/view" + suffix);
        }
        mView.addAllObjects(params);
        return mView;
    }

    /**
     * 数据维护模型树查询
     *
     * @return
     */
    @RequestMapping(value = "/maintain-tree", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "实做BOM展示模型树查询", notes = "实做BOM展示模型树查询")
    public ResultBody<List<TreeModel>> maintainTree(@ApiParam(name = "过滤条件", value = "过滤条件", required = false, type = "string") @RequestParam(required = false) String keywords) {
        try {
            String userId = sessionUtils.getEmpId();
            boolean hasKey = redisTemplateService.hasKey(userId, Constants.PageFlagEnum.MAINTAIN_SYSTEM.getCode());
            //如果redis存在该缓存
            if (hasKey) {
                JSONArray treeData = redisTemplateService.getTreeData(userId, Constants.PageFlagEnum.MAINTAIN_SYSTEM.getCode());
                List<TreeDto> treeDtoList = JSONArray.parseArray(treeData.toString(), TreeDto.class);
                //启动获取最新数据线程并更新redis
                Runnable task = () -> {
                    LOGGER.info("开始更新数据采集分系统页面redis树结构数据！");
                    getTreeAndSetRedis(userId);
                    LOGGER.info("更新数据采集分系统页面redis树结构数据完成！");
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
            return ResultBody.failure("实做bom左侧树构建失败！");
        }
    }

    private List<TreeDto> getTreeAndSetRedis(String userId) {
        List<TreeDto> exhibitionTree = iMaintainService.maintainTree(userId);
        try {
            //排序
            treeServiceImpl.sortTreeDtoByKeyWords(exhibitionTree,Constants.TREE_TWO);
        } catch (Exception e) {
            LOGGER.error("maintain-tree sort is error",e);
        }
        //存入redis
        redisTemplateService.setTreeData(userId, Constants.PageFlagEnum.MAINTAIN_SYSTEM.getCode(), JSON.toJSONString(exhibitionTree));
        return exhibitionTree;
    }

    /**
     * 查询模型版本列表
     */
    @ApiOperation(value = "查询模板管理列表", notes = "查询模板管理列表")
    @RequestMapping(value = {"centerPage"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<GridView> list(@ApiParam(name = "页数", value = "页数", required = true, type = "string") @RequestParam String page,
                                     @ApiParam(name = "行数", value = "行数", required = true, type = "string") @RequestParam String rows,
                                     @ApiParam(name = "模板名称", value = "模板名称", required = false, type = "string") @RequestParam(required = false) String name,
                                     @ApiParam(name = "树节点", value = "树节点id", required = false, type = "string") @RequestParam(required = false) String nodeId,
                                     @ApiParam(name = "是否获取表单数据", value = "是否获取表单数据", required = false, type = "string") @RequestParam(required = false) String getFormFlag,
                                     @ApiParam(name = "文本内容", value = "文本内容", required = false, type = "string") @RequestParam(required = false) String text,
                                     @ApiParam(name = "fifthNode标识", value = "fifthNode标识", required = false, type = "string") @RequestParam(required = false) String fifthNode,
                                     @RequestParam(value = "attributes", required = false) String attributes) {
        try {
            ResultBody<GridView> result = null;
            if (StringUtils.isBlank(fifthNode)) {
                return ResultBody.success(new GridView<>());
            }
            final String regex = "\\d+";
            if (StringUtils.isNotBlank(page) && !page.matches(regex)) {
                String message = "分页参数page 传值有误，page:" + page;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            if (StringUtils.isNotBlank(rows) && !rows.matches(regex)) {
                String message = "分页参数rows 传值有误，rows:" + rows;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            JSONObject jsonObject = JSON.parseObject(attributes == null ? "" : attributes);
//            if(ObjectUtil.isEmpty(jsonObject)){
//                return ResultBody.success(new GridView<>());
//            }
            String fifthNodeFlag = jsonObject.get("fifthNode") == null ? "" : jsonObject.get("fifthNode").toString();
            String tempId = jsonObject.get("tempID") == null ? "" : jsonObject.get("tempID").toString();
            //判断是否是分系统中的单机节点
            String FenType = jsonObject.get("authType") == null ? "" : jsonObject.get("authType").toString();
            if (StrUtil.isBlank(fifthNodeFlag)) {
                return ResultBody.success(new GridView<>());
            }
            Map<String, String> map = new HashMap();
            map.put("page", page);
            map.put("rows", rows);
            map.put("nodeId", nodeId);
            map.put("tempId", tempId);
            map.put("code", "");
            map.put("name", name);
            map.put("tableName", "");
            map.put("getFormFlag",getFormFlag);
            map.put("treeName",text);
            map.put("attributes",attributes);
            //如果是分系统的单机，则返回单机
            if ("1".equals(FenType)){
                result = standAloneService.getModelList(map);
            } else {
                result = iMaintainService.centerDataList(map);
            }

            return result;
        } catch (Exception e) {
            return ResultBody.success(new GridView<>());
        }
    }

    /**
     * 根据状态新增维护数据
     *
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "新增模型数据", notes = "新增模型数据")
    @OperateLogger(operation = "数据维护新增模型数据")
    public ResultBody addModelData(@RequestBody DataOperateDTO param,
                                   @RequestParam(name = "flag", required = false) String flag,
                                   @ApiParam(name = "型号id信息", value = "型号id信息", required = false, type = "string") @RequestParam(required = false) String moduleId,
                                   @ApiParam(name = "模板编码", value = "模板编码", required = false, type = "string") @RequestParam(required = false) String modelCode) throws Exception {
        VersionStatusEnum versionStatus = param.isEffect() ? VersionStatusEnum.EFFECT : VersionStatusEnum.EDIT;
        //入库
        Map<String, String> mapData = param.getData();
        //操作标识（新增/复制）
        //操作标识（新增/复制）
        mapData.put("operateFlag", flag);
        mapData.put("moduleId", moduleId);
        mapData.put("modelCode", modelCode);
        try {
            return iMaintainService.addModelData(param, versionStatus, mapData);
        } catch (Exception e) {
            return new ResultBody();
        }
    }

    /**
     * 查询模型字段，构建form表单
     *
     * @param modelId    模型ID
     * @param createData 是否为新建数据表单（新建数据表单没有部分系统信息）
     * @return
     */
    @RequestMapping(value = "/field/form", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "查询模型有权限的模板字段，构建form表单", notes = "查询模型有权限的模板字段，构建form表单")
    public ResultBody queryFormFields(@ApiParam(name = "模型ID", value = "模型ID", required =
            true, type = "String") String modelId, boolean createData) {
        return ResultBody.success(iMaintainService.getFields(modelId, createData));
    }

    /**
     * 根据ID批量查询文件系统属性信息
     *
     * @param ids
     * @return
     */
    @GetMapping(value = "/search-sys")
    @ResponseBody
    @ApiOperation(value = "根据ID批量查询文件系统属性信息", notes = "根据ID批量查询文件系统属性信息")
    public ResultBody searchById(@ApiParam(name = "文件ID,多个用逗号隔开", value = "文件ID,多个用逗号隔开", required =
            true, type = "String") String[] ids) {
        return iMaintainService.getSysInfoByIds(ids);
    }

    /**
     * 按照ID列表判断文件是否存在
     *
     * @param ids
     * @return
     */
    @RequestMapping(value = "/file/exist")
    @ResponseBody
    @ApiOperation(value = "按照ID列表判断文件是否存在", notes = "按照ID列表判断文件是否存在")
    public ResultBody fileExist(@ApiParam(value = "文件IDS") @RequestBody List<String> ids,
                                @ApiParam(value = "选中的分类ID") @RequestParam(value = "selectId", required = true) String selectId,
                                @ApiParam(value = "文件大小") @RequestParam(value = "fileSize", required = true) long fileSize,
                                HttpServletResponse httpServletResponse) {
        ResultBody resultBody = iMaintainService.fileExist(ids, selectId, httpServletResponse, fileSize);
        return resultBody;
    }

    /**
     * 文件下载时获取磁盘空间信息
     * 磁盘空间已占用大小 = 磁盘实际占用 + 本次下载文件大小（若为压缩方式，此处大小翻倍）
     *
     * @param ids 文件id列表
     * @return
     */
    @PostMapping(value = "/download/diskspace-use-message")
    @ResponseBody
    @ApiOperation(value = "文件下载时获取磁盘空间信息", notes = "文件下载时获取磁盘空间信息")
    public ResultBody diskSpaceUseMessage(@ApiParam(value = "文件IDS") @RequestBody List<String> ids,
                                          @ApiParam(value = "是否打包") @RequestParam(value = "compression", required = false) Boolean compression) {
        return iMaintainService.diskspaceUseMessage(ids, compression);
    }

    /**
     * 按照ID列表分片下载文件至服务器
     * 支持打包下载
     *
     * @param ids         文件id列表
     * @param compression 是否打包标识
     * @param selectId    分类文件Id
     * @return
     */
    @PostMapping(value = "/download/chunks-to-local")
    @ResponseBody
    @ApiOperation(value = "按照ID列表分片下载文件", notes = "按照ID列表分片下载文件")
    @OperateLogger(operation = "下载文件")
    public ResultBody chunksDownloadTolocal(@ApiParam(value = "文件IDS") @RequestBody List<String> ids,
                                            @ApiParam(value = "是否打包") @RequestParam(value = "compression", required = false) Boolean compression,
                                            @ApiParam(value = "选中的分类ID") @RequestParam(value = "selectId", required = false) String selectId) {
        return iMaintainService.chunksDownloadTolocal(ids, compression, selectId);
    }

    /**
     * 根据路径判断本地文件是否存在
     *
     * @param path
     * @return
     */
    @RequestMapping(value = "/local-file-exist")
    @ApiOperation(value = "根据路径判断本地文件是否存在", notes = "根据路径判断本地文件是否存在")
    public ResultBody localFileDownload(@ApiParam(value = "文件路径") @RequestParam(value = "path") String path) {
        return iMaintainService.loaclFileExist(path);
    }

    /**
     * 按照路径下载文件
     *
     * @param path
     * @return
     */
    @RequestMapping(value = "/download/local-path")
    @ApiOperation(value = "按照路径下载文件", notes = "按照路径下载文件")
    public void localFileDownload(@ApiParam(value = "文件路径") @RequestParam(value = "path") String path, HttpServletResponse response) {
        //响应文件流
        iMaintainService.localFileDownload(path, response);
    }

    /**
     * 查询关联模型数据
     *
     * @param queryDTO
     * @return list
     */
    @RequestMapping(value = "/data/page-ref", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "查询用户有权限的关联模型生效数据", notes = "查询用户有权限的关联模型生效数据")
    public ResultBody<GridView<Map<String, Object>>> pageRefModelData(@RequestBody DataQueryDTO queryDTO) {
        return iMaintainService.pageRefModelData(queryDTO);
    }

    /**
     * 根据字典ID去获取有行权限的字典数据
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/dic/validData", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "查询数据字典", notes = "查询数据字典")
    public ResultBody getDicValidDataById(@ApiParam(name = "字典ID", value = "字典ID") @RequestParam(name = "id") String id,
                                          @ApiParam(name = "模型ID", value = "模型ID") @RequestParam(name = "modelId") String modelId,
                                          @ApiParam(name = "模型绑定字段ID", value = "模型绑定字段ID") @RequestParam(name = "fieldId") String fieldId) {
        return iMaintainService.getDicValidDataById(id, modelId, fieldId);
    }

    /**
     * 查询基础模板数据
     *
     * @param modelId
     * @return
     */
    @RequestMapping(value = "field/base-tpl", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "根据模型ID查询基础模板字段信息", notes = "根据模型ID查询基础模板字段信息")
    public ResultBody<List<ModelVerFieldDTO>> getBaseTemplateField(@ApiParam(name = "模型ID", value = "模型ID", required =
            true, type = "String") @RequestParam(name = "modelId") String modelId) {
        return iMaintainService.getBaseTemplateField(modelId);
    }

    /**
     *
     */
    @RequestMapping(value = "/dataPage", method = RequestMethod.POST)
    @ResponseBody
    @OperateLogger(operation = "查询数据")
    public cn.hutool.json.JSONObject dataListManage(@ApiParam(name = "physicalCode", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String physicalCode,
                                                    @ApiParam(name = "modelId", value = "型号id信息", required = true, type = "string") @RequestParam(required = false) String modelId,
                                                    @ApiParam(name = "attributes", value = "树节点信息", required = false, type = "string") @RequestParam(required = false) String attributes,
                                                    @ApiParam(name = "nodeId", value = "树节点", required = false, type = "string") @RequestParam String nodeId,
                                                    @RequestBody ModelDataQueryParamVO param) {
        try {
            final String regex = "\\d+";
            if (modelId == null) {
                throw new ParamNotBlankException("模型id");
            }
            String page = param.getPage().toString();
            String rows = param.getRows().toString();
            // TODO: 2023/7/10 暂时java分页处理,后续需要优化整体逻辑
            param.setRows(99999);
            param.setPage(1);
            if (StringUtils.isBlank(page) && !page.matches(regex)) {
                String message = "分页参数page 传值有误，page:" + page;
                return JSONUtil.parseObj(ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message));
            }
            if (StringUtils.isBlank(rows) && !rows.matches(regex)) {
                String message = "分页参数rows 传值有误，rows:" + rows;
                return JSONUtil.parseObj(ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message));
            }
            ResultBody<GridView> gridViewResultBody = iMaintainService.dataListManage(physicalCode, modelId, param, nodeId, attributes);
            List rows1 = gridViewResultBody.getData().getRows();
            //当前第几页
            int pageNo = Integer.parseInt(page);
            //一页十条
            int pageSize = Integer.parseInt(rows);
            //总数
            int total = rows1.size();
            // TODO: 2023/7/10 暂时java分页处理,后续需要优化整体逻辑
            List subList = (List) rows1.stream().skip((pageNo - 1) * pageSize).limit(pageSize).
                    collect(Collectors.toList());
            gridViewResultBody.getData().setRows(subList);
            gridViewResultBody.getData().setTotal(total);
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(JSON.toJSONString(gridViewResultBody, SerializerFeature.WriteMapNullValue), false);
            return jsonObject;
        } catch (Exception e) {
            LOGGER.error("查询表单数据失败", e);
            return new cn.hutool.json.JSONObject();
        }
    }

    /**
     * 查询型号策划配置的字典信息
     *
     * @param nodeId
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/lookupByPlan", method = RequestMethod.POST)
    @ResponseBody
    @OperateLogger(operation = "查询型号策划配置的字典信息")
    public Map<String, String> getLookupByPlan(@ApiParam(name = "nodeId", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String nodeId,
                                               @ApiParam(name = "modelId", value = "型号id信息", required = true, type = "string") @RequestParam(required = false) String modelId) {
        try {
            Map<String, String> lookupByPlan = iMaintainService.getLookupByPlan(modelId, nodeId);
            return lookupByPlan;
        } catch (Exception e) {
            LOGGER.error("查询型号策划配置的字典信息失败", e);
            return null;
        }
    }

    /**
     * 查询序号字段
     *
     * @param nodeId
     * @param modelId
     * @return
     */
    @RequestMapping(value = "/queryClomName", method = RequestMethod.POST)
    @ResponseBody
    @OperateLogger(operation = "查询序号字段")
    public String queryClomName(@ApiParam(name = "nodeId", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String nodeId,
                                @ApiParam(name = "modelId", value = "型号id信息", required = true, type = "string") @RequestParam(required = false) String modelId) {
        try {
            String order = iMaintainService.queryClomName(modelId, nodeId, Constants.ORDER_FIELD);
            return order;
        } catch (Exception e) {
            LOGGER.error("查询序号字段出错", e);
            return null;
        }
    }


    /**
     * 提供远程调用包络分析的controller
     *
     * @param modelId
     * @param tableName
     * @param jsondata
     * @return
     */
    @RequestMapping(value = "/enveLope", method = RequestMethod.POST)
    @ResponseBody
    @OperateLogger(operation = "查询信息是否符合包络分析规则")
    public JSONArray getenveLope(@ApiParam(name = "modelId", value = "型号id信息", required = true, type = "string") @RequestParam(required = false) String modelId,
                                 @ApiParam(name = "tableName", value = "当前模型的表名", required = true, type = "string") @RequestParam(required = false) String tableName,
                                 @ApiParam(name = "jsondata", value = "传递的数据", required = true, type = "String") @RequestParam(required = false) String jsondata) {
        try {
//            Map<String, String> data;
//            JSONArray jsonArray = new JSONArray();
            JSONArray array = JSONArray.parseArray(jsondata);
//            for (int i = 0; i < array.size(); i++) {
//                String s1 = array.getJSONObject(i).toJSONString();
//                Map<String, String> mapdata = JSON.parseObject(s1, new TypeReference<Map<String, String>>() {
//                });
//                data = envelopeService.analysis(modelId, tableName, mapdata, "");
//                jsonArray.add(data);
//            }
//            return jsonArray;
            return envelopeService.analysisArray(modelId, tableName, array);
        } catch (Exception e) {
            LOGGER.error("查询信息是否符合包络分析规则", e);
            return null;
        }
    }


    /**
     * 数据包审核-根据id查询
     */
    @RequestMapping(value = "/dataPageByIds", method = RequestMethod.POST)
    @ResponseBody
    @OperateLogger(operation = "查询数据")
    public ResultBody<GridView> dataListManageByIds(@ApiParam(name = "实物编码", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String physicalCode,
                                                    @ApiParam(name = "型号id信息", value = "型号id信息", required = true, type = "string") @RequestParam(required = false) String modelId,
                                                    @ApiParam(name = "bussinessId", value = "流程实例编号", required = true, type = "string") @RequestParam(required = false) String bussinessId,
                                                    @ApiParam(name = "服务类型", value = "服务类型", required = false, type = "string") @RequestParam(required = false) String oper,
                                                    @RequestBody ModelDataQueryParamVO param) {


        List<String> sysIds = iFlowBomSheetDataInter.findListByBomAndTempleteAndbusinessId(physicalCode, modelId, Long.parseLong(bussinessId));


        final String regex = "\\d+";
        if (modelId == null) {
            throw new ParamNotBlankException("模型id");
        }
        String page = param.getPage().toString();
        String rows = param.getRows().toString();
        int pageInt = Integer.parseInt(page);
        int rowsInt = Integer.parseInt(rows);

        if (StringUtils.isBlank(page) && !page.matches(regex)) {
            String message = "分页参数page 传值有误，page:" + page;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        if (StringUtils.isBlank(rows) && !rows.matches(regex)) {
            String message = "分页参数rows 传值有误，rows:" + rows;
            return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
        }
        param.setPage(Integer.parseInt("1"));
        param.setRows(Integer.parseInt("100000"));
        List<Map<String, Object>> data = iMaintainService.dataListManageByIds(physicalCode, modelId, param, sysIds).getData().getRows();
        int number = 0;
        List<Map<String, Object>> result = new ArrayList<>();

        String grade = StringUtils.isEmpty(sessionUtils.getEmp().getGrade()) ? "0" : sessionUtils.getEmp().getGrade();
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> map = data.get(i);
            String m_sys_secretlev = map.get("S_M_SYS_SECRETLEVEL") == null ? "0" : map.get("S_M_SYS_SECRETLEVEL").toString();
            //密集要求
            //密集是按照数字在排序：人员密集要大于资源密集
            if (meritdataCloudProperties.getUsePlatformSecret() && (Integer.parseInt(grade)) < Integer.parseInt(m_sys_secretlev)) {
                number++;
            } else {
                result.add(map);
            }
        }
        result.sort(Comparator.comparing(o -> Integer.parseInt(o.get("S_M_SYS_VERSIONSTATUS").toString())));
        Collections.reverse(result);

        GridView<Map<String, Object>> mapGridView = new GridView<>(new PageUtil<Map<String, Object>>(result, rowsInt, pageInt).getList(), result.size(), rowsInt, pageInt);
        mapGridView.setRecords(number);
        return ResultBody.success(mapGridView);
    }


    /**
     * 删除模型数据
     *
     * @return
     */
    @RequestMapping(value = "/deletedata", method = {RequestMethod.POST, RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "删除模型数据", notes = "删除模型数据")
    @OperateLogger(operation = "数据维护删除模型数据")
    public ResultBody deleteData(@RequestParam String dataManageType,
                                 @RequestBody ModelDataExportParamDto modelDataExportParam) {
        try {
            //调用删除接口
            ResultBody resultBody = iMaintainService.deleteModelDatas(dataManageType, modelDataExportParam);
            return resultBody;
        } catch (Exception e) {
            LOGGER.error("删除数据失败", e);
            return ResultBody.failure(EnumDamDatamanageResultStatus.DELETE_DATA_ERROR.status(),
                    EnumDamDatamanageResultStatus.DELETE_DATA_ERROR.message(), null, e);
        }
    }

    /**
     * 更新单条数据
     *
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "更新模型数据", notes = "更新模型数据")
    @OperateLogger(operation = "数据维护更新模型数据")
    public ResultBody updateModelData(@RequestBody DataOperateDTO param) throws Exception {
        return iMaintainService.updateModelData(param);
    }

    /**
     * 根据模型唯一字段查询单条数据
     *
     * @return Map
     */
    @RequestMapping(value = "/find-primary", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "根据唯一字段查询模型数据", notes = "根据唯一字段查询模型数据")
    @OperateLogger(operation = "数据维护查询数据")
    public ResultBody<Map<String, Object>> getModleDataByPrimary(@RequestBody DataQueryDTO queryDTO) {
        return ResultBody.success(iMaintainService.getModelDataByPrimary(queryDTO));
    }

    /**
     * 根据状态新增维护数据
     *
     * @return
     */
    @RequestMapping(value = "/revise", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "新增模型数据", notes = "新增模型数据")
    @OperateLogger(operation = "数据维护新增模型数据")
    public ResultBody reviseModelData(@RequestBody DataOperateDTO param) {
        return iMaintainService.reviseModelData(param);
    }

    @RequestMapping("/query/{modelId}")
    @OperateLogger(operation = "查询数据")
    public cn.hutool.json.JSONObject queryDataForList(@PathVariable String modelId, @RequestBody ModelDataQueryParamVO param) {
        try {
            Objects.requireNonNull(modelId, "模型ID不能为空");
            ResultBody success = ResultBody.success(iMaintainService.queryDataForList(modelId, param));
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(JSON.toJSONString(success, SerializerFeature.WriteMapNullValue), false);
            return jsonObject;
        } catch (Exception e) {
            return new cn.hutool.json.JSONObject();
        }
    }

    /**
     * 获取低于当前用户的密级信息
     */
    @RequestMapping("/query/getGradeList")
    @OperateLogger(operation = "查询数据")
    public ResultBody<List<Map<String, Object>>> getGradeList() {
        return ResultBody.success(iMaintainService.getGradeList());
    }


    /**
     * 数据同步接口
     *
     * @param physicalCode 实物编码
     * @param modelId      模型id
     * @throws IOException
     */
    @RequestMapping(value = "/syncData", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "数据同步", notes = "数据同步")
    public ResultBody syncData(@ApiParam(name = "physicalCode", value = "实物编码", required = true, type = "string") @RequestParam(required = false) String physicalCode,
                               @ApiParam(name = "attributes", value = "attributes", required = true, type = "string") @RequestParam(required = false) String attributes,
                               @ApiParam(name = "modelId", value = "型号id信息", required = true, type = "string") @RequestParam(required = false) String modelId) {
        String status = Message.STATUS_FAIL;
        try {
            status = Message.STATUS_SUCESS;
            return iMaintainService.syncData(physicalCode, modelId, attributes);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("数据同步执行失败", e);
            status = Message.STATUS_FAIL;
            return ResultBody.failure("数据同步执行失败,请查看日志信息");
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.search"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.search.message"), physicalCode),
                    status);
            logPostService.postLog(msg);
        }
    }

    /**
     * 批量模板下载
     */
    @RequestMapping(value = "/exportModule", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "批量模板下载", notes = "批量模板下载")
    public void exportModule(HttpServletRequest request, HttpServletResponse res,
                             @ApiParam(name = "nodeId", value = "树节点id", required = true, type = "string") @RequestParam(required = false) String nodeId,
                             @ApiParam(name = "attributes", value = "attributes", required = true, type = "string") @RequestParam(required = false) String attributes,
                             @ApiParam(name = "batchNoNodeInfo", value = "batchNoNodeInfo", required = true, type = "string") @RequestParam(required = false) String batchNoNodeInfo,
                             @ApiParam(name = "text", value = "treeName", required = true, type = "string") @RequestParam(required = false) String text) {
        String status = Message.STATUS_FAIL;
        try {
            status = Message.STATUS_SUCESS;
            iMaintainService.exportAllModel(request, res, nodeId, attributes, text, batchNoNodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("批量模板下载失败", e);
            status = Message.STATUS_FAIL;
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.search"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.search.message"), "批量模板下载"),
                    status);
            logPostService.postLog(msg);
        }
    }


    /**
     * 批量数据导入
     */
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "批量数据导入", notes = "批量数据导入")
    public ResultBody importData(@ApiParam(name = "file", value = "导入excl", required = true, type = "file") @RequestParam("formFile[]") List<MultipartFile> files,
                                 @ApiParam(name = "physicalCode", value = "实物bom", required = true, type = "string") @RequestParam(required = false) String physicalCode,
                                 @ApiParam(name = "attributes", value = "attributes", required = true, type = "string") @RequestParam(required = false) String attributes) {
        String status = Message.STATUS_FAIL;
        ResultBody result = new ResultBody();
        StringBuilder msgg = new StringBuilder();
        //查字典中数据密级，批量导入时去除文件最后的(密级)
        List<TreeModel<Object>> list = clientService.listLookupsEnable("secretlevel");
        List secretList = list.get(0).getChildren();
        List<String> secretNameList = new ArrayList<>();
        secretList.stream().forEach(r->{
            Map attr = (Map) ((TreeModel)r).getAttributes();
            secretNameList.add(attr.get("name").toString());
        });
        try {
            for (MultipartFile file : files) {
                status = Message.STATUS_SUCESS;
                String name = file.getOriginalFilename();
                assert name != null;
                if (name.contains(".xls")) {
                    name = name.substring(0, name.lastIndexOf(".xls"));
                } else if (name.contains(".xlsx")) {
                    name = name.substring(0, name.lastIndexOf(".xlsx"));
                } else {
                    LOGGER.error("批量导入数据失败! 请上传“xls”或“xlsx”类型的文件！");
                    status = Message.STATUS_FAIL;
                }
                int lastIndex = 0;
                String replaceStr = "";
                //找到最后出现的（密级），进行截取
                for (String secretStr : secretNameList){
                   String temp = "(" + secretStr + ")";
                    if(name.lastIndexOf(temp) > lastIndex){
                        lastIndex = name.lastIndexOf(temp);
                        replaceStr = temp;
                    }
                }
                if(lastIndex > 0){
                    name = name.substring(0,lastIndex) + name.substring(lastIndex + replaceStr.length());
                }
                ResultBody<List<String>> modelIdByModelName = iDataPacketClient.getModelIdByModelName(name);
                if (modelIdByModelName.getData().size() <= 0) {
                    msgg.append("导入文件：").append(name).append("；导入信息：").append("此模板导入失败！错误原因：此excl模板名称特殊字符；或excl模板名称结尾存在(1),(2)不唯一！，请修改正确名称！").append("</br>");
                    continue;
                }
                String modelId = modelIdByModelName.getData().get(0);
                //格式化参数
                attributes = iMaintainService.replaceAllBlank(attributes);
                ResultBody resultBody = iDatamationsClient.importData(file, modelId, false, physicalCode, attributes);
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSON(resultBody.getData()).toString());
                String filename = jsonObject.get("filename").toString();
                String msg = jsonObject.get("msg").toString();
                if (msg.contains("数据导入成功")) {
                    continue;
                }
                msgg.append("导入文件：").append(filename).append("；导入信息：").append(msg).append("</br>");
            }
            if (msgg.length() == 0) {
                msgg.append("选择的模型全部导入成功！");
            }
            result.setMessage(msgg.toString());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("批量导入数据失败", e);
            status = Message.STATUS_FAIL;
            return ResultBody.failure("批量导入数据失败失败,请联系管理员！" + msgg);
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.search"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.search.message"), "批量导入数据"),
                    status);
            logPostService.postLog(msg);
        }
    }

    /**
     * 批量数据导出
     */
    @RequestMapping(value = "/exportData", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "批量数据导出", notes = "批量数据导出")
    public void importData(@ApiParam(name = "physicalCode", value = "实物bom", required = true, type = "string") @RequestParam(required = false) String physicalCode,
                           @ApiParam(name = "attributes", value = "attributes", required = true, type = "string") @RequestParam(required = false) String attributes,
                           @ApiParam(name = "text", value = "treeName", required = true, type = "string") @RequestParam(required = false) String text,
                           HttpServletResponse response, HttpServletRequest request) {
        String status = Message.STATUS_FAIL;
        try {
            status = Message.STATUS_SUCESS;
            iMaintainService.exportAllData(request, response, physicalCode, attributes, text);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("批量模板下载失败", e);
            status = Message.STATUS_FAIL;
        } finally {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.manage.packet.bmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.fmodule"),
                    LogPattenUtils.getProperty("model.manage.packet.search"),
                    StrUtil.format(LogPattenUtils.getProperty("model.manage.packet.search.message"), "批量模板下载"),
                    status);
            logPostService.postLog(msg);
        }
    }
}
