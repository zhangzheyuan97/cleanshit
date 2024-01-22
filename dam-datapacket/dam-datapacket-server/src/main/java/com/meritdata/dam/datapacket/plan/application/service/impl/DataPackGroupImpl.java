package com.meritdata.dam.datapacket.plan.application.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.meritdata.cloud.base.entity.Emp;
import com.meritdata.cloud.base.entity.User;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.common.SessionManager;
import com.meritdata.dam.common.service.nacos.NacosServerTool;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.acquistion.vo.ExhibitionDTO;
import com.meritdata.dam.datapacket.plan.acquistion.vo.PackGroupFileVO;
import com.meritdata.dam.datapacket.plan.acquistion.vo.QueryNodeDTO;
import com.meritdata.dam.datapacket.plan.application.dao.ModuleGroupPackRepository;
import com.meritdata.dam.datapacket.plan.application.entity.ModuleGroupPack;
import com.meritdata.dam.datapacket.plan.application.entity.QModuleGroupPack;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackGroupService;
import com.meritdata.dam.datapacket.plan.application.vo.GroupPackDto;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.manage.entity.QFlowBomSheetEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.QFlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.entity.ModulePool;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleTree;
import com.meritdata.dam.datapacket.plan.model.entity.QModuleTree;
import com.meritdata.dam.datapacket.plan.model.service.IModuleInfoConfigService;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleConfigDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.DateUtils;
import com.meritdata.dam.datapacket.plan.utils.TempleteUtil;
import com.meritdata.dam.entity.datamanage.FileDTO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import com.meritdata.dam.entity.metamanage.ModelFieldConfigDTO;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import feign.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DataPackGroupImpl implements IDataPackGroupService {
    private static final Logger logger = LoggerFactory.getLogger(DataPackGroupImpl.class);

    @Value("${CLOUD_HOME:cloud_home}")
    private String CLOUD_HOME;

    private static final String FILE_PATH = "/file/group_package/";


    //保留缓存文件数量
    private static final int SAVE_FILE_NUM = 10;

    @Autowired
    private IDatamationsClient iDatamationsClient;

    @Autowired
    private IModulePlanService iModulePlanService;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private IMaintainService iMaintainService;

    @Autowired
    private ModuleGroupPackRepository moduleGroupPackRepository;

    @Autowired
    private IModuleInfoConfigService moduleInfoConfigService;

    @Autowired
    private IModuleManageService moduleManageService;

    @Autowired
    private IFlowBomSheetDataInter flowBomSheetDataInter;


    /**
     * 查询分系统/模板下树节点数据，只到批次结点
     *
     * @param userId 当前登陆人empId
     * @return
     */
    @Override
    public List<TreeDto> groupPackTree(String userId) {
        Map<String, Object> map = new HashMap<>();
        try {
            List<Map<String, Object>> mapsFCInfo = iDatamationsClient.getFCInfo(map);
            List<TreeDto> treeDtoList = iModulePlanService.tree("-1", userId).stream().filter(item -> !Constants.SINGLE_NAME.equals(item.getText())).collect(Collectors.toList());
            QModuleTree qModuleTree = QModuleTree.moduleTree;
            List<String> ids = new ArrayList<>();
            treeDtoList.forEach(item -> {
                item.getChildren().forEach(itemChildren -> {
                    ids.add(itemChildren.getPid());
                });
            });
            List<ModuleTree> fetch = jpaQueryFactory.selectFrom(qModuleTree).where(qModuleTree.id.in(ids)).fetch();
            HashMap<String, ModuleTree> map2 = new HashMap<>();
            fetch.forEach(item -> {
                map2.put(item.getId(), item);
            });
            //查询发次表的发次信息，挂在型号下，构建实做BOM树结构
            for (TreeDto tree : treeDtoList) {
                List<TreeDto> LBchildren = tree.getChildren();
                for (TreeDto lbchildren : LBchildren) {
                    //判断型号对应的EBOM表格中的的父id与发次表中记录的分类信息一样则挂载
                    ModuleTree moduleTree = map2.get(lbchildren.getPid());
                    List<TreeDto> XHchildren = lbchildren.getChildren();
                    for (TreeDto xhchildren : XHchildren) {
                        List<TreeDto> dtoList = new ArrayList<>();
                        mapsFCInfo.forEach(item -> {
                            if (lbchildren.getText().equals(item.get("F_CLASSIFICATION"))) {
                                if (xhchildren.getText().equals(item.get("F_MODEL"))) {
                                    //挂载至分析还是模块
                                    String subSystem = "";
                                    if ("0".equals(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "1" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString())) {
                                        subSystem = Constants.MODEL_NAME;
                                    } else if ("1".equals(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "1" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString())) {
                                        subSystem = Constants.SYSTEM_NAME;
                                    }
                                    //区分分系统还是模块信息
                                    if (moduleTree != null && moduleTree.getText().equals(subSystem)) {
                                        QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                                        queryNodeDTO.setNodeType(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString());
                                        queryNodeDTO.setThirdlyNode(xhchildren.getText());
                                        //关联模板的id
                                        queryNodeDTO.setTempID(xhchildren.getId());
                                        //nodeLevel为4 第四级
                                        queryNodeDTO.setNodeLevel("4");
                                        queryNodeDTO.setFourthlyNode(item.get("F_ISSUE_NO").toString());
                                        //单机类别
                                        queryNodeDTO.setSecondNode(lbchildren.getText());
                                        TreeDto t = new TreeDto();
                                        t.setId(item.get("F_M_SYS_ID").toString());
                                        t.setPid(xhchildren.getId());
                                        t.setText(item.get("F_ISSUE_NO").toString());
                                        t.setAttributes(queryNodeDTO);
                                        dtoList.add(t);
                                    }
                                }
                            }
                        });
                        ArrayList<TreeDto> treeDtoListTHTemp = dtoList.stream().collect(Collectors.collectingAndThen(
                                Collectors.toCollection(() -> new TreeSet<>(
                                        Comparator.comparing(ModuleTree::getText)
                                )), ArrayList::new
                        ));
                        xhchildren.setChildren(treeDtoListTHTemp);
                    }
                }
            }
            return treeDtoList;
        } catch (Exception e) {
            logger.error("树构建错误日志：", e);
        }
        return new ArrayList<>();

    }


    @Override
    public ResultBody groupPack(String id, String pid, String text, String attributes, Emp current) throws IOException {
        logger.info("组包日志：----> 开始组包");
        long start = System.currentTimeMillis();
        logger.info("开始组包时间：" + start);
        JSONObject jsonObject = JSON.parseObject(StringUtils.isEmpty(attributes) ? "" : attributes);
        //如果是分系统和模块，则fourthlyNode为发次，单机fourthlyNode为批次
        String batchNo = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
        //如果是分系统和模块，thirdlyNode为型号，单机thirdlyNode为图号
        String drawingNo = jsonObject.get("thirdlyNode") == null ? "" : jsonObject.get("thirdlyNode").toString();
        //如果是分系统和模块，secondNode为分系统类型，单机secondNode为单机类型
        String classIfication = jsonObject.get("secondNode") == null ? "" : jsonObject.get("secondNode").toString();
        //关联模板id
        String tempId = (!jsonObject.containsKey("tempID") || StringUtils.isEmpty(jsonObject.getString("tempID"))) ? "" : jsonObject.getString("tempID");
        //点击的节点层级
        String nodeLevel = (!jsonObject.containsKey("nodeLevel") || StringUtils.isEmpty(jsonObject.getString("nodeLevel"))) ? "" : jsonObject.getString("nodeLevel");
        //点击的单机
        if (StringUtils.isNotBlank(jsonObject.getString("firstNode")) && jsonObject.getString("firstNode").equals(Constants.SINGLE_NAME)) {
            //单机组包
            return packGroupSingle(id, pid, text, batchNo, drawingNo, classIfication, tempId, nodeLevel, current);
        } else {//分系统或者模块
            //分系统组包
            return packGroupSystem(id, pid, text, batchNo, drawingNo, classIfication, tempId, nodeLevel, current);
        }
    }

    /**
     * 单机组包
     *
     * @param id             节点id
     * @param pid            父节点id
     * @param text           节点名称
     * @param batchNo        批次号
     * @param drawingNo      图号
     * @param classIfication 类型
     * @param tempId         关联到模板的id（图号id）
     * @param nodeLevel      节点层级
     * @param emp            人员密级
     * @return
     */
    private ResultBody packGroupSingle(String id, String pid, String text, String batchNo, String drawingNo,
                                       String classIfication, String tempId, String nodeLevel, Emp emp) {
        logger.info("组包日志[单机]：----> 获取单机数据");
        List<ExhibitionDTO> exhibitionDTOS = new ArrayList<>();
        //如果点击的是批次号节点
        if (nodeLevel.equals(Constants.DJ_PC_LEVEL)) {
            //获取单机节点pc下所有实物
            List<Map<String, Object>> single = getSingle(batchNo, drawingNo, classIfication);
            if (CollectionUtils.isEmpty(single)) {
                logger.error("组包日志[单机]：----> 批次号[" + batchNo + "]下无实物！");
                return ResultBody.failure("批次号[" + batchNo + "]下无实物！");
            }
            exhibitionDTOS = single.stream().map(item -> {
                ExhibitionDTO exhibitionDTO = new ExhibitionDTO();
                exhibitionDTO.setBatchNo(item.get("F_BATCH_NO").toString());
                exhibitionDTO.setClassIfication(item.get("F_CLASSIFICATION").toString());
                exhibitionDTO.setDrawingNo(item.get("F_DRAWING_NO").toString());
                exhibitionDTO.setPhysicalNo(item.get("F_PHYSICAL_NO").toString());
                return exhibitionDTO;
            }).collect(Collectors.toList());
        } else if (nodeLevel.equals(Constants.DJ_SW_LEVEL)) {//如果点击的是实物节点
            ExhibitionDTO exhibitionDTO = new ExhibitionDTO();
            exhibitionDTO.setBatchNo(batchNo);
            exhibitionDTO.setClassIfication(classIfication);
            exhibitionDTO.setDrawingNo(drawingNo);
            exhibitionDTO.setPhysicalNo(text);
            exhibitionDTOS.add(exhibitionDTO);
        } else {
            logger.error("请选择正确的单机节点！");
            return ResultBody.failure("请选择正确的单机节点！");
        }
        logger.info("组包日志[单机]");
        //获取展示的模板列表
        List templateList = getTemplateList(tempId);
        if (!CollectionUtils.isEmpty(templateList)) {
            HSSFWorkbook wb = new HSSFWorkbook();
            List<GroupPackDto> groupPackDtos = new ArrayList<>();
            try {
                for (Object obj : templateList) {
                    GroupPackDto groupPackDto = new GroupPackDto();
                    //判断是分系统还是单机模板对象类型
                    ModuleConfigDto configDto = (ModuleConfigDto) obj;
                    groupPackDto.setModelInfo(configDto.getModelInfo());
                    groupPackDto.setCode(configDto.getCode());
//                    groupPackDto.setTemplateName(configDto.getName().replaceAll(Constants.REG, ""));
                    groupPackDto.setTemplateName(configDto.getName());
                    //获取动态表头
                    List<ModuleColumnConfig> dynamicHeadlist = moduleInfoConfigService.getModuleCurate(id, groupPackDto.getCode(), groupPackDto.getTableName(), groupPackDto.getModelInfo());
                    //获取排序字段
                    String order = "";
                    List<String> orderList = dynamicHeadlist.stream()
                            .filter(item -> (item.getFormFieldVO() == null ? item.getColumnName() : item.getFormFieldVO().getBusiName()).equals(Constants.ORDER_FIELD))
                            .map(item -> item.getFormFieldVO() == null ? "F_" + item.getFieldName() : item.getFormFieldVO().getAliasName())
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(orderList)) {
                        order = orderList.get(0);
                    }
                    //获取分组字段
                    String group = "";
                    List<String> groupList = dynamicHeadlist.stream()
                            .filter(item -> (item.getFormFieldVO() == null ? item.getColumnName() : item.getFormFieldVO().getBusiName()).equals(Constants.GROUP_FIELD))
                            .map(item -> item.getFormFieldVO() == null ? "F_" + item.getFieldName() : item.getFormFieldVO().getAliasName())
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(groupList)) {
                        group = groupList.get(0);
                    }
                    List<Map<String, Object>> tableList = new ArrayList<>();
                    List<String> message = new ArrayList<>();
                    String modelInfo = configDto.getModelInfo();
                    //获取实物号列表
                    List<String> physicalList = exhibitionDTOS.stream().map(ExhibitionDTO::getPhysicalNo).collect(Collectors.toList());
                    JSONObject searchJson = getSearchJson(physicalList, emp, order);
                    Map<String, List<Map<String, Object>>> dataListByPhysicals = getDataListByPhysicals(modelInfo, searchJson);
                    //查询当前发次下的型号策划是否配置了字典
                    Map<String, String> lookupByPlan = iMaintainService.getLookupByPlan(modelInfo, tempId);
                    for (ExhibitionDTO exhibitionDTO : exhibitionDTOS) {
                        //获取表格中分类、图号、型号、实物号数据
                        String classifyCell = exhibitionDTO.getClassIfication();
                        String drawingNoCell = exhibitionDTO.getDrawingNo();
                        String batchNoCell = exhibitionDTO.getBatchNo();
                        String physicalNoCell = exhibitionDTO.getPhysicalNo();
                        //成包包含的才去获取组包数据
                        if (configDto.getIsPackage().equals(Constants.IS_PACKAGE)) {
                            tableList.addAll(formatTableList(classifyCell, drawingNoCell, batchNoCell, physicalNoCell, modelInfo, dataListByPhysicals.get(physicalNoCell), searchJson, group, lookupByPlan));
                            long num = iDatamationsClient.dataCountByParam(physicalNoCell, configDto.getModelInfo(), getModelDataQueryParamVO(emp));
                            formatMessage(physicalNoCell, num, message);
                        }
                    }
                    groupPackDto.setTableList(tableList);
                    //如果存在无权限数据
                    if (!CollectionUtils.isEmpty(message)) {
                        groupPackDto.setMessage(Constants.NO_PERMISSION_PROMPT_FIX + StringUtils.join(message, ""));
                    }
                    //导出结构化数据
                    exportExcel(dynamicHeadlist, id, wb, groupPackDto);
                    groupPackDtos.add(groupPackDto);
                }
                //记录写入数据库
                saveExcel(pid, text, id, wb, groupPackDtos, drawingNo, emp);
                logger.info("组包日志[单机]：----> 组包成功");
                return ResultBody.success("组包成功");
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("获取组包数据并写入文件失败，", e);
                return ResultBody.failure(e.getMessage());
            }
        } else {
            return ResultBody.success("无可组包数据");
        }
    }

    /**
     * 分系统组包
     *
     * @param id             树节点id
     * @param pid            父节点id
     * @param text           节点名称
     * @param batchNo        发次
     * @param drawingNo      型号
     * @param classIfication 类型
     * @param tempId         模板关联id（型号id）
     * @param nodeLevel      当前节点层级
     * @return
     */
    private ResultBody packGroupSystem(String id, String pid, String text, String batchNo, String drawingNo,
                                       String classIfication, String tempId, String nodeLevel, Emp emp) {
        logger.info("组包日志[分系统]：----> 获取分系统数据");
        List<ExhibitionDTO> exhibitionDTOS = new ArrayList<>();
        if (nodeLevel.equals(Constants.FXT_FC_LEVEL)) {
            //根据型号和发次获取配套清单表中实物信息
            List<Map<String, Object>> qdInfo = getQDInfo(drawingNo, batchNo);
            //如果查询出数据为空，则仅返回本级实物信息
            if (!CollectionUtils.isEmpty(qdInfo)) {
                try {
                    //获取单机及总装直属件实物信息
                    exhibitionDTOS = getPhysical(qdInfo, classIfication, batchNo, drawingNo);
                } catch (Exception e) {
                    logger.error("组包日志[分系统]：----> 获取单机及总装直属件实物信息失败！", e);
                    return ResultBody.failure(e.getMessage());
                }
            }
            //添加本级虚拟实体
            ExhibitionDTO exhibitionDTO = new ExhibitionDTO();
            exhibitionDTO.setBatchNo(batchNo);
            exhibitionDTO.setClassIfication(classIfication);
            exhibitionDTO.setDrawingNo(drawingNo);
            exhibitionDTO.setPhysicalNo(text);
            //TODO 大写的请注意！！！！！本次为了区分是否向上汇总，特将是否管理到实物作为是否向上汇总条件如果为1则为本级，否则为向上汇总需要统计的
            exhibitionDTO.setIsManageObject(Constants.THIS_LEVEL);
            exhibitionDTOS.add(exhibitionDTO);
        } else {
            logger.error("组包日志[分系统]：----> 请选择正确的发次节点！");
            return ResultBody.failure("请选择正确的发次节点！");
        }
        logger.info("组包日志[分系统]：");
        //获取展示的模板列表
        List templateList = getTemplateList(tempId);
        if (!CollectionUtils.isEmpty(templateList)) {
            HSSFWorkbook wb = new HSSFWorkbook();
            List<GroupPackDto> groupPackDtos = new ArrayList<>();
            try {
                //去掉特殊字符
                for (Object obj : templateList) {
                    GroupPackDto groupPackDto = new GroupPackDto();
                    //判断是分系统还是单机模板对象类型
                    ModuleConfigDto configDto = (ModuleConfigDto) obj;
                    groupPackDto.setModelInfo(configDto.getModelInfo());
                    groupPackDto.setCode(configDto.getCode());
//                    groupPackDto.setTemplateName(configDto.getName().replaceAll(Constants.REG, ""));
                    groupPackDto.setTemplateName(configDto.getName());
                    //获取动态表头
                    List<ModuleColumnConfig> dynamicHeadlist = moduleInfoConfigService.getModuleCurate(id, groupPackDto.getCode(), groupPackDto.getTableName(), groupPackDto.getModelInfo());
                    //获取排序字段
                    String order = "";
                    List<String> orderList = dynamicHeadlist.stream()
                            .filter(item -> (item.getFormFieldVO() == null ? item.getColumnName() : item.getFormFieldVO().getBusiName()).equals(Constants.ORDER_FIELD))
                            .map(item -> item.getFormFieldVO() == null ? "F_" + item.getFieldName() : item.getFormFieldVO().getAliasName())
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(orderList)) {
                        order = orderList.get(0);
                    }
                    //获取分组字段
                    String group = "";
                    List<String> groupList = dynamicHeadlist.stream()
                            .filter(item -> (item.getFormFieldVO() == null ? item.getColumnName() : item.getFormFieldVO().getBusiName()).equals(Constants.GROUP_FIELD))
                            .map(item -> item.getFormFieldVO() == null ? "F_" + item.getFieldName() : item.getFormFieldVO().getAliasName())
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(groupList)) {
                        group = groupList.get(0);
                    }
                    List<Map<String, Object>> tableList = new ArrayList<>();
                    List<String> message = new ArrayList<>();
                    //需要判断是否向上汇总 是否向上汇总，0为否，1为是
                    ModulePool modulePool = moduleManageService.moduleIsPoolByCode(configDto.getCode());
                    String modelInfo = configDto.getModelInfo();
                    //获取实物号列表
                    List<String> physicalList = exhibitionDTOS.stream().map(ExhibitionDTO::getPhysicalNo).collect(Collectors.toList());
                    JSONObject searchJson = getSearchJson(physicalList, emp, order);
                    Map<String, List<Map<String, Object>>> dataListByPhysicals = getDataListByPhysicals(modelInfo, searchJson);
                    //查询当前发次下的型号策划是否配置了字典
                    Map<String, String> lookupByPlan = iMaintainService.getLookupByPlan(modelInfo, tempId);
                    for (ExhibitionDTO exhibitionDTO : exhibitionDTOS) {
                        //获取表格中分类、图号、型号、实物号数据
                        String classifyCell = exhibitionDTO.getClassIfication();
                        String drawingNoCell = exhibitionDTO.getDrawingNo();
                        String batchNoCell = exhibitionDTO.getBatchNo();
                        String physicalNoCell = exhibitionDTO.getPhysicalNo();
                        //如果不汇总 仅需要拿本级的数据
                        if (modulePool.getIsPool().equals(Constants.NO_POOL)) {
                            if (exhibitionDTO.getIsManageObject().equals(Constants.THIS_LEVEL)) {
                                //成包包含的才去获取组包数据
                                if (configDto.getIsPackage().equals(Constants.IS_PACKAGE)) {
                                    tableList.addAll(formatTableList(classifyCell, drawingNoCell, batchNoCell, physicalNoCell, modelInfo, dataListByPhysicals.get(physicalNoCell), searchJson, group, lookupByPlan));
                                    long num = iDatamationsClient.dataCountByParam(physicalNoCell, configDto.getModelInfo(), getModelDataQueryParamVO(emp));
                                    formatMessage(physicalNoCell, num, message);
                                }
                            }
                        } else {
                            //成包包含的才去获取组包数据
                            if (configDto.getIsPackage().equals(Constants.IS_PACKAGE)) {
                                tableList.addAll(formatTableList(classifyCell, drawingNoCell, batchNoCell, physicalNoCell, modelInfo, dataListByPhysicals.get(physicalNoCell), searchJson, group, lookupByPlan));
                                long num = iDatamationsClient.dataCountByParam(physicalNoCell, configDto.getModelInfo(), getModelDataQueryParamVO(emp));
                                formatMessage(physicalNoCell, num, message);
                            }
                        }
                    }
                    groupPackDto.setTableList(tableList);
                    //如果存在无权限数据
                    if (!CollectionUtils.isEmpty(message)) {
                        groupPackDto.setMessage(Constants.NO_PERMISSION_PROMPT_FIX + StringUtils.join(message, ""));
                    }
                    //导出结构化数据
                    exportExcel(dynamicHeadlist, id, wb, groupPackDto);
                    groupPackDtos.add(groupPackDto);
                }
                //记录写入数据库
                saveExcel(pid, text, id, wb, groupPackDtos, drawingNo, emp);
                logger.info("组包日志[分系统]：----> 组包成功");
                return ResultBody.success("组包成功");
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("获取分系统组包数据并写入文件失败", e);
                return ResultBody.failure(e.getMessage());
            }
        } else {
            logger.info("无可组包数据");
            return ResultBody.success("无可组包数据");
        }
    }

    /**
     * 获取模板列表
     *
     * @param tempId
     * @return
     */
    private List getTemplateList(String tempId) {
        Map<String, String> param = new HashMap<>();
        param.put("rows", Constants.ROWS + "");
        param.put("page", Constants.PAGE + "");
        param.put("name", "");
        param.put("tempId", tempId);
        //获取模板列表
        return iMaintainService.centerDataList(param).getData().getRows();
    }

    /**
     * 导出结构化文件
     *
     * @param id           分系统为型号id 单机为图号
     * @param wb
     * @param groupPackDto
     * @return
     */
    private void exportExcel(List<ModuleColumnConfig> dynamicHeadlist, String id, HSSFWorkbook wb, GroupPackDto groupPackDto) {
        logger.info("组包日志：----> 导出结构化文件;模型名称【" + groupPackDto.getTemplateName() + "】");
        //如果没有最大密级设置默认值
        //该模型的文件属性
        List<PackGroupFileVO> files = new ArrayList<>();

        //固定表头
        List<String> headList = new ArrayList<>(Arrays.asList(Constants.TABLE_HEADS_FIX));
        if (!CollectionUtils.isEmpty(dynamicHeadlist)) {
            //将表头追加至动态表头后
            headList.addAll(dynamicHeadlist.stream().map(ModuleColumnConfig::getColumnName).collect(Collectors.toList()));
        }
        //添加数据密级
        headList.add(Constants.SECRET_LEVEL_NAME);
        //创建sheet页
        HSSFSheet sheet = wb.createSheet(groupPackDto.getTemplateName());
        //包含无权限信息的提示
        int rowNum = 0;
        String message = groupPackDto.getMessage();
        if (!StringUtils.isEmpty(message)) {
            int headLength = headList.size();
            //创建提示信息
            createSheetPrompt(wb, headLength, sheet, message);
            rowNum++;
        }
        //添加表头
        HSSFRow headCell = sheet.createRow(rowNum);
        for (int j = 0; j < headList.size(); j++) {
            headCell.createCell(j).setCellValue(headList.get(j));

            //循环创建表头单元格时，同时设置整列的单元格格式为文本
            HSSFCellStyle textStyle = wb.createCellStyle();
            textStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));
            sheet.setDefaultColumnStyle(j, textStyle);
        }
        //添加数据
        List<Map<String, Object>> tableList = groupPackDto.getTableList();
        logger.info("组包日志：----> 保存结构化数据");
        if (!CollectionUtils.isEmpty(tableList)) {
            tableList.forEach(mapList -> {
                HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
                String classifyCell = (null == mapList.get("classifyCell")) ? "" : mapList.get("classifyCell").toString();
                String drawingNoCell = (null == mapList.get("drawingNoCell")) ? "" : mapList.get("drawingNoCell").toString();
                String batchNoCell = (null == mapList.get("batchNoCell")) ? "" : mapList.get("batchNoCell").toString();
                String physicalNoCell = (null == mapList.get("physicalNoCell")) ? "" : mapList.get("physicalNoCell").toString();
                row.createCell(0).setCellValue(classifyCell);
                row.createCell(1).setCellValue(drawingNoCell);
                row.createCell(2).setCellValue(batchNoCell);
                row.createCell(3).setCellValue(physicalNoCell);
//                row.createCell(4).setCellValue(sheet.getLastRowNum() - finalRowNum);
                for (int j = 0; j < dynamicHeadlist.size(); j++) {
                    ModuleColumnConfig moduleColumnConfig = dynamicHeadlist.get(j);
                    HSSFCell cell = row.createCell(j + 4);
                    sheet.autoSizeColumn(j + 4);
                    String key = "F_" + moduleColumnConfig.getFieldName();
                    if (mapList.containsKey(key)) {
                        String cellVal = (mapList.get(key) == null) ? "" : mapList.get(key).toString();
                        //判断数据字典项
                        cellVal = setLookUpValue(moduleColumnConfig, cellVal);
                        //判断是否是文件,并设置包含文件路径的文件集合
                        cellVal = setFiles(files, mapList, moduleColumnConfig, cellVal);
                        //date类型数据进行日期统一，使用yyyyMMdd格式
                        if ("date".equalsIgnoreCase(moduleColumnConfig.getDataType())) {
                            cellVal = ObjectUtil.isNotEmpty(mapList.get(key)) ? mapList.get(key).toString().replaceAll("-", "") : "";
                        }

                        cell.setCellValue(cellVal);
                    }
                }
                //循环完成后增加密级
                HSSFCell cell = row.createCell(dynamicHeadlist.size() + 4);
                String defaultSecretLevel = NacosServerTool.getParamVal(Constants.NACOS_DATA_ID, Constants.NACOS_GROUP, Constants.NACOS_DEFAULT_SECRET);
                String secretLevel = (null == mapList.get("S_M_SYS_SECRETLEVEL")) ? defaultSecretLevel + "" : mapList.get("S_M_SYS_SECRETLEVEL").toString();
                cell.setCellValue(getTextByCodeLookUp(secretLevel));
                String maxSecretLevel = StringUtils.isEmpty(groupPackDto.getMaxSecretLevel()) ? "0" : groupPackDto.getMaxSecretLevel();
                //取最大密级
                if (Integer.parseInt(secretLevel) > Integer.parseInt(maxSecretLevel)) {
                    groupPackDto.setMaxSecretLevel(secretLevel);
                }
            });
        }
        groupPackDto.setFiles(files);
    }

    /**
     * 判断是否是文件,并设置包含文件路径的文件集合
     *
     * @param files              已有文件集合
     * @param mapList            数据集合
     * @param moduleColumnConfig 字段属性
     * @param cellVal
     * @return
     */
    @Override
    public String setFiles(List<PackGroupFileVO> files, Map<String, Object> mapList, ModuleColumnConfig moduleColumnConfig, String cellVal) {
        if (null != moduleColumnConfig.getFormFieldVO() && null != moduleColumnConfig.getFormFieldVO().getModelFieldConfigDTO()) {
            //判断是否是文件
            ModelFieldConfigDTO modelFieldConfigDTO = moduleColumnConfig.getFormFieldVO().getModelFieldConfigDTO();
            //如果是文件
            if (null != modelFieldConfigDTO && modelFieldConfigDTO.getFile() == Constants.IS_FILE) {
                //字段名前缀加上F_则为数据对应的key
                String key = "F_" + moduleColumnConfig.getFieldName();
                List<PackGroupFileVO> physicalFiles = setPackGroupFile(mapList, key);
                List<String> collect = physicalFiles.stream().map(item -> item.getFileName() + "." + item.getFileType()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect)) {
                    if (collect.size() == 1) {
                        //将文件名存入表中
                        cellVal = collect.get(0);
                    } else {
                        //将文件名以，隔开存入表中
                        cellVal = StringUtils.join(collect, ",");
                    }
                    files.addAll(physicalFiles);
                }
            }
        }
        return cellVal;
    }

    @Override
    public String setLookUpValue(ModuleColumnConfig moduleColumnConfig, String cellVal) {
        //此字段不为空则判断为数据字典
        if (StringUtils.isNotEmpty(moduleColumnConfig.getLookup())) {
            JSONArray jsonArray = JSONArray.parseArray(moduleColumnConfig.getLookup());
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("code").equals(cellVal)) {
                    cellVal = jsonObject.getString("name");
                    break;
                }
            }
        }
        return cellVal;
    }

    private void createSheetPrompt(HSSFWorkbook wb, int headLength, HSSFSheet sheet, String message) {
        //创建提示信息
        HSSFRow prompt = sheet.createRow(0);
        //单元格样式
        HSSFCellStyle cellStyle = wb.createCellStyle();
        HSSFCell cell = prompt.createCell(0);
        HSSFFont font = wb.createFont();
        font.setBold(true); //加粗
        font.setColor(HSSFColor.RED.index); //红色
        cellStyle.setFont(font);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(message);
        //合并单元格
        CellRangeAddress cra = new CellRangeAddress(0, 0, 0, headLength - 1);
        sheet.addMergedRegion(cra);
        //给合并的单元格设置边框
        RegionUtil.setBorderBottom(1, cra, sheet);
        RegionUtil.setBorderLeft(1, cra, sheet);
        RegionUtil.setBorderRight(1, cra, sheet);
        RegionUtil.setBorderTop(1, cra, sheet);

    }

    /**
     * 获取数据中的文件属性，返回该模板的文件
     *
     * @param mapList
     * @param key
     */
    @Override
    public List<PackGroupFileVO> setPackGroupFile(Map<String, Object> mapList, String key) {
        List<PackGroupFileVO> files = new ArrayList<>();
        String[] ids = mapList.get(key).toString().split(",");
        ResultBody sysInfoByIds = iDatamationsClient.getSysInfoByIds(ids);
        if (sysInfoByIds.isSuccess()) {
            //则可以返回文件对象
            List<FileDTO> fileDTOS = JSON.parseArray(JSON.toJSONString(sysInfoByIds.getData()), FileDTO.class);
            if (!CollectionUtils.isEmpty(fileDTOS)) {
                fileDTOS.forEach(item -> {
                    PackGroupFileVO fileVO = new PackGroupFileVO();
                    String fileTypeId = item.getFileTypeId();
                    fileVO.setFileId(item.getId());
                    fileVO.setFileTypeId(fileTypeId);
                    fileVO.setFileName(item.getName());
                    fileVO.setFileType(item.getExtension());
                    //文件密级
                    fileVO.setSecretLevel(item.getSecretLevel());
                    List<String> fileIds = new ArrayList<>(Collections.singletonList(item.getId()));
                    ResultBody resultBody = iMaintainService.fileExist(fileIds, fileTypeId, null, 0);
                    if (resultBody.isSuccess() && null != resultBody.getData()) {
                        List<String> filePaths = JSONArray.parseArray(JSON.toJSONString(resultBody.getData()), String.class);
                        if (!CollectionUtils.isEmpty(filePaths)) {
                            fileVO.setFilePath(filePaths.get(0));
                            files.add(fileVO);
                        }
                    }
                });
            }
        }
        return files;
    }


    /**
     * 保存组包数据及保存文件
     *
     * @param pid
     * @param text
     * @param id
     * @param wb
     * @param groupPackDtos 包含非结构化文件的组包数据
     * @throws IOException
     */
    private void saveExcel(String pid, String text, String id, HSSFWorkbook wb,
                           List<GroupPackDto> groupPackDtos, String drawingNo, Emp emp) throws Exception {
        logger.info("组包日志：----> 保存组包数据及保存文件");
        //excel密级
        String maxSecretLevel = "";
        for (GroupPackDto groupPackDto : groupPackDtos) {
            if (StringUtils.isEmpty(groupPackDto.getMaxSecretLevel())) {
                continue;
            }
            if (StringUtils.isEmpty(maxSecretLevel)) {
                maxSecretLevel = groupPackDto.getMaxSecretLevel();
            } else {
                //取最大密级
                if (Integer.parseInt(groupPackDto.getMaxSecretLevel()) > Integer.parseInt(maxSecretLevel)) {
                    maxSecretLevel = groupPackDto.getMaxSecretLevel();
                }
            }
        }
        //默认密级
        if (StringUtils.isEmpty(maxSecretLevel)) {
            //获取nacos配置文件
            maxSecretLevel = NacosServerTool.getParamVal(Constants.NACOS_DATA_ID, Constants.NACOS_GROUP, Constants.NACOS_DEFAULT_SECRET);
        }
        String maxSecretLevelName = getTextByCodeLookUp(maxSecretLevel);
        //文件名称构成
        String name = drawingNo + "_" + text + "_" + DateUtils.getCurrentDateTimeStr();
        //记录写入数据库
        ModuleGroupPack moduleGroupPack = new ModuleGroupPack();
        moduleGroupPack.setPid(pid);
        moduleGroupPack.setPackager(emp.getName());
        moduleGroupPack.setPhysicalNo(text);
        moduleGroupPack.setGroupPackDate(new Date());
        moduleGroupPack.setNodeId(id);
        moduleGroupPack.setCreateUser(emp.getUserName());
        ModuleGroupPack saveGroupPackModule = moduleGroupPackRepository.save(moduleGroupPack);
        //存放组包文件
        isPathExist(CLOUD_HOME + FILE_PATH);
        String fileUrl = CLOUD_HOME + FILE_PATH + saveGroupPackModule.getId();
        //增加密级
        fileUrl = fileUrl + "(" + maxSecretLevelName + ")";
        boolean packFile = false;
        //判断是否获取到文件
        ZipOutputStream zipOutputStream = null;
        for (GroupPackDto groupPackDto : groupPackDtos) {
            List<PackGroupFileVO> files = groupPackDto.getFiles();
            if (!CollectionUtils.isEmpty(groupPackDto.getFiles())) {
                packFile = true;
                //文件夹写入
                if (null == zipOutputStream) {
                    zipOutputStream = new ZipOutputStream(new FileOutputStream(fileUrl + ".zip"));
                }
//                String maxFileSecret = NacosServerTool.getParamVal(Constants.NACOS_DATA_ID, Constants.NACOS_GROUP, Constants.NACOS_DEFAULT_SECRET);
                //附件密级都为非密，直接给死
                String maxFileSecret = "1";
                for (PackGroupFileVO file : files) {
                    if (maxFileSecret.compareTo(file.getSecretLevel()) < 0) {
                        maxFileSecret = file.getSecretLevel();
                    }
                }
                maxFileSecret = getFileSecretByCode(maxFileSecret);
                for (PackGroupFileVO file : files) {
                    //文件密级
                    String secretLevelName = getFileSecretByCode(file.getSecretLevel());
                    String fileName = file.getFileName() + "(" + secretLevelName + ")." + file.getFileType();
                    logger.info("组包日志：----> 保存组包文件，模型【" + groupPackDto.getTemplateName() + "】文件名【" + fileName + "】");
                    //创建压缩包里面的文件  groupPackDto.getTemplateName()为文件夹名字
                    zipOutputStream.putNextEntry(new ZipEntry(groupPackDto.getTemplateName() + "(" + maxFileSecret + ")" + File.separator + fileName));
                    //将文件写入zip
                    //获取中台服务器的文件
                    Response response = iDatamationsClient.getFileByPath(file.getFilePath());
                    Response.Body body = response.body();
                    InputStream fis = body.asInputStream();
                    if (null == fis) {
                        logger.error("附件【" + file.getFileName() + "】下载失败");
                        throw new Exception("附件【" + file.getFileName() + "】下载失败");
                    }
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = fis.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, len);
                    }
                    zipOutputStream.closeEntry();
                    fis.close();
                    //删除缓存
                    iDatamationsClient.delTempFile(file.getFilePath());
                }
            }
        }
        //如果需要组包
        if (packFile) {
            //将excel写入byte数组
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            wb.write(byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            //增加密级
            name = name + "(" + maxSecretLevelName + ")";
            zipOutputStream.putNextEntry(new ZipEntry(name + ".xls"));
            zipOutputStream.write(bytes);
            zipOutputStream.closeEntry();
            //关闭所有流
            zipOutputStream.flush();
            zipOutputStream.close();
            byteArrayOutputStream.close();
            wb.close();
        } else {
            FileOutputStream fileOutputStream = new FileOutputStream(fileUrl + ".xls");
            wb.write(fileOutputStream);
            wb.close();
            fileOutputStream.close();
        }
        long end = System.currentTimeMillis();
        logger.info("结束组包时间：" + end);
        logger.info(text + "====》组包完成！！");
    }


    private boolean delTempFile(String filePath) {
        File result = new File(filePath);
        if (result.exists() && result.isFile()) {
            File parentFile = result.getParentFile();
            if (parentFile.isDirectory()) {
                File[] files = parentFile.listFiles();
                assert files != null;
                for (File file : files) {
                    if (!file.delete()) {
                        return false;
                    }
                }
                return parentFile.delete();
            } else {
                return parentFile.delete();
            }
        }
        return false;
    }

    /**
     * 获取列表数据 追加
     *
     * @param classifyCell   分类
     * @param drawingNoCell  型号/图号
     * @param batchNoCell    发次/批次
     * @param physicalNoCell 实物
     * @param modelInfo      模板id
     * @param group          分组字段
     * @param lookupByPlan   型号策划配置的数据字典
     */
    private List<JSONObject> formatTableList(String classifyCell, String drawingNoCell, String batchNoCell,
                                             String physicalNoCell, String modelInfo, List<Map<String, Object>> rows,
                                             JSONObject jsonObject, String group, Map<String, String> lookupByPlan) {
        List<JSONObject> result = new ArrayList<>();
        ModelDataQueryParamVO modelDataQueryParamVO = new ModelDataQueryParamVO();
        // TODO: 2023/7/20 jsonobject 要去掉实物号字段
        //如果该实物号下无数据，则获取批次号下的数据
        if (CollectionUtils.isEmpty(rows)) {
            //根据批次号查询，批次号+实物号为null或者空作为查询条件
            jsonObject.put(Constants.BATCH_NO_FIELD, batchNoCell);
            JSONObject jsonNull = new JSONObject();
            jsonNull.put("$null", "null");
            jsonObject.put("F_PhysicalCode", jsonNull);
            modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
            rows = iDatamationsClient.packetDataListAll("", modelInfo, modelDataQueryParamVO);
        }
        //以产品代号分组
//        String group = iMaintainService.queryClomName(modelInfo, tempid, Constants.GROUP_FIELD);
        if (StringUtils.isNotEmpty(group)) {
            Map<Object, List<Map<String, Object>>> collect = rows.stream().collect(Collectors.groupingBy(item -> null != item.get(group) ? item.get(group) : "无产品代号数据"));
            rows.clear();
            for (Object o : collect.keySet()) {
                if (null != o) {
                    rows.addAll(collect.get(o));
                }
            }
            if (CollectionUtils.isEmpty(rows)) {
                return result;
            }
        }
        for (Map<String, Object> map : rows) {
            boolean flag = false;
            //如果配置了字典值，再进行字典值过滤
            if (null != lookupByPlan && lookupByPlan.size() != 0) {
                for (String key : lookupByPlan.keySet()) {
                    if (map.get(key) == null) {
                        continue;
                    }
                    String f_alertDimension = map.get(key).toString();
                    String s = lookupByPlan.get(key);
                    String[] split = s.split(",");
                    if (!Arrays.asList(split).contains(f_alertDimension)) {
                        flag = true;
                    }
                }
            }
            if (flag) {
                continue;
            }
            JSONObject json = JSONObject.parseObject(JSON.toJSONString(map));
            json.put("classifyCell", classifyCell);
            json.put("drawingNoCell", drawingNoCell);
            json.put("batchNoCell", batchNoCell);
            json.put("physicalNoCell", physicalNoCell);
            result.add(json);
        }
        return result;
    }

    private static void isPathExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 单行下载
     *
     * @param text
     * @param id
     * @param attributes
     * @param response
     */
    @Override
    public ResultBody singleDownload(String text, String id, String attributes, HttpServletResponse response, String groupPackDate) {
        String folderPath = CLOUD_HOME + FILE_PATH;
//        File file = new File(CLOUD_HOME + FILE_PATH + id + ".xlsx");
        long LgroupPackDate = Long.valueOf(groupPackDate);
        String format = new SimpleDateFormat("yyyy-MM-ddHH-mm-ss").format(LgroupPackDate);
        String packetData = format.replaceAll("-", "").substring(0, 12);
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (null != files && files.length > 0) {
                for (File file : files) {
                    //文件全名
                    String fullName = file.getName();
                    //文件名不带后缀
                    String fileName;
                    if (fullName.contains("(")) {
                        fileName = fullName.substring(0, fullName.indexOf("("));
                    } else {
                        fileName = fullName.substring(0, fullName.lastIndexOf("."));
                    }
                    //文件后缀
                    String fileSuffix = fullName.substring(fullName.lastIndexOf("."));
                    //文件密级
                    String maxSecretLevel = "";
                    if (fullName.contains("(")) {
                        maxSecretLevel = fullName.substring(fullName.indexOf("("), fullName.lastIndexOf("."));
                    }
                    //文件名为数据id
                    if (fileName.equals(id)) {
                        JSONObject jsonObject = JSON.parseObject(attributes == null ? "" : attributes);
                        String batchNo = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
                        String drawingNo = jsonObject.get("thirdlyNode") == null ? "" : jsonObject.get("thirdlyNode").toString();
                        String name;
                        String firstNode = jsonObject.getString("firstNode");
                        //单机图号+批次号
                        if (StringUtils.isNotBlank(firstNode) && firstNode.equals(Constants.SINGLE_NAME)) {
                            name = drawingNo + "_" + batchNo + "_" + packetData;
                        } else {
                            name = drawingNo + "_" + text + "_" + packetData;
                        }
                        FileInputStream fis = null;
                        BufferedInputStream bis = null;
                        try {
                            byte[] size = new byte[1024];
                            String filename = URLEncoder.encode(name + maxSecretLevel, "UTF-8");
                            response.addHeader("Content-Disposition", "attachment;filename=" + filename + fileSuffix);
                            fis = new FileInputStream(file);
                            bis = new BufferedInputStream(fis);
                            OutputStream os = response.getOutputStream();
                            int i = bis.read(size);
                            while (i != -1) {
                                os.write(size, 0, i);
                                i = bis.read(size);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            logger.error("下载组包文件失败", e);
                        } finally {
                            if (bis != null) {
                                try {
                                    bis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return ResultBody.success();
                    }
                }
            }
        }
        return ResultBody.success("无此组包数据");
    }

    /**
     * 查询组包表格数据
     *
     * @param nodeName
     * @param packager
     * @param startTime
     * @param endTime
     * @param map
     * @return
     * @throws ParseException
     */
    @Override
    public ResultBody<GridView> dataListGroupPack(String nodeName, String physicalNo, String packager, String startTime, String endTime, Map<String, Object> map) throws ParseException {
        QModuleGroupPack qModuleGroupPack = QModuleGroupPack.moduleGroupPack;
        //nodename为实物号，根据实物号去查询 及当前登陆人username查询自己的组包数据
        User currentUser = SessionManager.getCurrentUser();
        Predicate predicate = qModuleGroupPack.physicalNo.eq(nodeName).and(qModuleGroupPack.createUser.eq(currentUser.getUsername()));
        if (StrUtil.isNotBlank(physicalNo)) {
            predicate = ExpressionUtils.and(predicate, qModuleGroupPack.physicalNo.like("%" + physicalNo + "%"));
        }
        if (StrUtil.isNotBlank(packager)) {
            predicate = ExpressionUtils.and(predicate, qModuleGroupPack.packager.like("%" + packager + "%"));
        }
        if (StrUtil.isNotBlank(startTime)) {
            predicate = ExpressionUtils.and(predicate, qModuleGroupPack.groupPackDate.after(new Date(Long.parseLong(startTime) * 1000)));
        }
        if (StrUtil.isNotBlank(endTime)) {
            predicate = ExpressionUtils.and(predicate, qModuleGroupPack.groupPackDate.before(new Date(Long.parseLong(endTime) * 1000 + 24 * 59 * 60 * 1000)));
        }
        //页码
        Integer pageNum = Integer.parseInt(map.get("page").toString());
        //每页显示个数
        Integer sizeNum = Integer.parseInt(map.get("rows").toString());
        Pageable pageable = PageRequest.of(pageNum - 1, sizeNum);
        List<ModuleGroupPack> groupPackList = jpaQueryFactory
                .selectFrom(qModuleGroupPack)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qModuleGroupPack.groupPackDate.desc())
                .fetch();

        long count = jpaQueryFactory.selectFrom(qModuleGroupPack).where(predicate).fetchCount();
        return ResultBody.success(new GridView<>(groupPackList, count));
    }

    /**
     * 根据批次获取单机和总装直属件表中所有内容
     *
     * @param batchNo
     * @return
     */
    private List<Map<String, Object>> getSingle(String batchNo, String drawingNO, String classIfication) {
        ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
        JSONObject filterMap = new JSONObject();
//        JSONObject param = new JSONObject();
//        param.put("$eq",batchNo);
        filterMap.put("F_BATCH_NO", batchNo);
        filterMap.put("F_DRAWING_NO", drawingNO);
        filterMap.put("F_CLASSIFICATION", classIfication);
        //单机必定关联到实物 并且是单机
        filterMap.put("F_WHETHER_PHYSICAL_OBJECTS_MANAGED", "1");
        filterMap.put("F_STAND_ALONE_IDENTIFICATION", "1");
        paramVO.setQueryFilter(filterMap.toJSONString());
        return iDatamationsClient.getDJInfoSearch(paramVO);
    }

    /**
     * 根据型号和发次获取配套清单表中所有信息
     *
     * @param model
     * @param issue
     * @return
     */
    private List<Map<String, Object>> getQDInfo(String model, String issue) {
        ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
        JSONObject filterMap = new JSONObject();
//        JSONObject param = new JSONObject();
//        param.put("$eq",batchNo);
        filterMap.put("F_ISSUE_NO", issue);
        filterMap.put("F_MODEL", model);
        paramVO.setQueryFilter(filterMap.toJSONString());
        return iDatamationsClient.getQDInfo(paramVO);
    }


    /**
     * 根据配套清单表获取相应的单机表总装直属件信息
     *
     * @param qdInfo
     * @return
     */
    private List<ExhibitionDTO> getPhysical(List<Map<String, Object>> qdInfo, String classIfication, String issueNo, String model) throws Exception {
        List<ExhibitionDTO> result = new ArrayList<>();
        for (Map<String, Object> item : qdInfo) {
            //实物号 单机总装直属件
            String physicalNo = (null == item.get("F_PHYSICAL_NO")) ? "" : item.get("F_PHYSICAL_NO").toString();
            //批次号 单机总装直属件
            String batchNo = (null == item.get("F_BATCH_NO")) ? "" : item.get("F_BATCH_NO").toString();
            //图号 单机总装直属件
            String drawingNo = (null == item.get("F_DRAWING_NO")) ? "" : item.get("F_DRAWING_NO").toString();
//            //发次号 分系统模块
//            String issueNo = (null == item.get("F_ISSUE_NO")) ? "" : item.get("F_ISSUE_NO").toString();
//            //型号 分系统模块
//            String model = (null == item.get("F_MODEL")) ? "" : item.get("F_MODEL").toString();

            //查询条件封装
            JSONObject filterMap = new JSONObject();
            //如果实物号为null 则一定是总装直属件
            if (StringUtils.isEmpty(physicalNo)) {
                //实物号未null, 则一定未管理到实物，并且一定不是单机
                filterMap.put("F_WHETHER_PHYSICAL_OBJECTS_MANAGED", "0");
                filterMap.put("F_STAND_ALONE_IDENTIFICATION", "0");
                //总装直属件未管理到实物的。根据图号和批次号获取的一定是一条信息
                filterMap.put("F_DRAWING_NO", drawingNo);
                filterMap.put("F_BATCH_NO", batchNo);
            } else {
                //如果实物号不为null，则根据实物号，批次号和图号，则一定获取的是一条信息
                filterMap.put("F_PHYSICAL_NO", physicalNo);
                filterMap.put("F_DRAWING_NO", drawingNo);
                filterMap.put("F_BATCH_NO", batchNo);
            }
            ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
            paramVO.setQueryFilter(filterMap.toJSONString());
            //根据以上条件，获取的必定是一条信息，如果未获取到信息或者获取到多条信息，则必定异常！！！
            List<Map<String, Object>> physicalList = iDatamationsClient.getDJInfoSearch(paramVO);
            if (physicalList.size() != 1) {
                logger.error("获取向上汇总数据失败！未获取到单机或者总装直属件相应信息！查询条件：" + JSON.toJSONString(filterMap));
                throw new Exception("获取向上汇总数据失败！未获取到单机或者总装直属件相应信息！");
            }
            Map<String, Object> physical = physicalList.get(0);
            ExhibitionDTO exhibitionDTO = new ExhibitionDTO();
            //分系统为发次号
            exhibitionDTO.setBatchNo(issueNo);
            exhibitionDTO.setClassIfication(classIfication);
            //分系统为型号
            exhibitionDTO.setDrawingNo(model);
            //总装直属件的实物号不存在则批次号为实物号
            exhibitionDTO.setPhysicalNo((null == physical.get("F_PHYSICAL_NO")) ? physical.get("F_BATCH_NO").toString() : physical.get("F_PHYSICAL_NO").toString());
            //TODO 大写的请注意！！！！！本次为了区分是否向上汇总，特将是否管理到实物作为是否向上汇总条件如果为1则为本级，否则为向上汇总需要统计的
            exhibitionDTO.setIsManageObject(Constants.OTHER_LEVEL);
            result.add(exhibitionDTO);
        }
        return result;
    }

//    @Override
//    public List<Map<String, Object>> getTableDataListByBomAndModelId(String bom, String modelId) {
//        List<Map<String, Object>> result = new ArrayList<>();
//        List<Long> businessIds = getBusinessId(bom, modelId);
//        if (CollectionUtils.isEmpty(businessIds)) {
//            return result;
//        }
//        //根据businessId获取dataId集合
//        QFlowBomSheetDataEntity qFlowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
//        Predicate businessIdPredicate = qFlowBomSheetDataEntity.bussinessId.in(businessIds);
//        List<String> dataIds = jpaQueryFactory.select(qFlowBomSheetDataEntity.dataId).from(qFlowBomSheetDataEntity).where(businessIdPredicate).fetch();
//        if (CollectionUtils.isEmpty(dataIds)) {
//            return result;
//        }
//        for (String dataId : dataIds) {
//            List<Map<String, Object>> allDataByModelInfoAndBomIds = templeteUtil.getALLDataByModelInfoAndBomIds(modelId, bom, Collections.singletonList(dataId));
//            result.addAll(allDataByModelInfoAndBomIds);
//        }
//        return result;
//    }

    @Override
    public List<Long> getBusinessId(BatchNoNodeInfo batchNoNodeInfo, String modelId) {
        //根据实物号和modelId获取审批通过的businessId集合
        QFlowBomSheetEntity qFlowBomSheetEntity = QFlowBomSheetEntity.flowBomSheetEntity;
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        Predicate leftOn = qFlowBomSheetEntity.bussinessId.stringValue().eq(qFlowCreateEntity.bussinessId);
        Predicate predicate = qFlowBomSheetEntity.template.eq(modelId)
                .and(qFlowCreateEntity.flowState.eq(Constants.PROCESS_STATUS_PASS));
        if (StringUtils.isNotEmpty(batchNoNodeInfo.getBatchNo())) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.batchNo.eq(batchNoNodeInfo.getBatchNo()));
        }
        if (StringUtils.isNotEmpty(batchNoNodeInfo.getModel())) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.model.eq(batchNoNodeInfo.getModel()));
        }
        if (StringUtils.isNotEmpty(batchNoNodeInfo.getDrawingNo())) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.drawingNo.eq(batchNoNodeInfo.getDrawingNo()));
        }
        if (StringUtils.isNotEmpty(batchNoNodeInfo.getIssueNo())) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetEntity.issueNo.eq(batchNoNodeInfo.getIssueNo()));
        }
        List<Long> result = jpaQueryFactory.select(qFlowBomSheetEntity.bussinessId)
                .from(qFlowBomSheetEntity)
                .leftJoin(qFlowCreateEntity)
                .on(leftOn)
                .where(predicate)
                .fetch();
        //过滤掉没有数据的记录
        return result.stream().filter(item -> CollectionUtils.isNotEmpty(flowBomSheetDataInter.getApproveDataList(item.toString(), modelId, batchNoNodeInfo))).collect(Collectors.toList());
    }

    @Override
    public List<String> validSecretData(String id, String pid, String text, String attributes, Emp emp) throws Exception {
        List<String> result = new ArrayList<>();
        //获取当前登陆人的查询条件
        ModelDataQueryParamVO param = getModelDataQueryParamVO(emp);

        JSONObject jsonObject = JSON.parseObject(StringUtils.isEmpty(attributes) ? "" : attributes);
        //如果是分系统和模块，则fourthlyNode为发次，单机fourthlyNode为批次
        String batchNo = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
        //如果是分系统和模块，thirdlyNode为型号，单机thirdlyNode为图号
        String drawingNo = jsonObject.get("thirdlyNode") == null ? "" : jsonObject.get("thirdlyNode").toString();
        //如果是分系统和模块，secondNode为分系统类型，单机secondNode为单机类型
        String classIfication = jsonObject.get("secondNode") == null ? "" : jsonObject.get("secondNode").toString();
        //关联模板id
        String tempId = (!jsonObject.containsKey("tempID") || StringUtils.isEmpty(jsonObject.getString("tempID"))) ? "" : jsonObject.getString("tempID");
        //点击的节点层级
        String nodeLevel = (!jsonObject.containsKey("nodeLevel") || StringUtils.isEmpty(jsonObject.getString("nodeLevel"))) ? "" : jsonObject.getString("nodeLevel");
        List templateList = getTemplateList(tempId);
        if (CollectionUtils.isEmpty(templateList)) {
            return result;
        }
        for (Object obj : templateList) {
            ModuleConfigDto configDto = (ModuleConfigDto) obj;
            //成包包含的才去获取组包数据
            if (configDto.getIsPackage().equals(Constants.IS_PACKAGE)) {
//                String templateName = configDto.getName().replaceAll(Constants.REG, "");
                String templateName = configDto.getName();
                String modelId = configDto.getModelInfo();
                ModulePool modulePool = moduleManageService.moduleIsPoolByCode(configDto.getCode());
                if (StringUtils.isNotBlank(jsonObject.getString("firstNode")) && jsonObject.getString("firstNode").equals(Constants.SINGLE_NAME)) {
                    if (nodeLevel.equals(Constants.DJ_PC_LEVEL)) {
                        //获取单机节点pc下所有实物
                        List<Map<String, Object>> single = getSingle(batchNo, drawingNo, classIfication);
                        for (Map<String, Object> item : single) {
                            String physical_no = item.get("F_PHYSICAL_NO").toString();
                            long count = iDatamationsClient.dataCountByParam(physical_no, modelId, param);
                            formatMessage(templateName, physical_no, count, result);
                        }
                    } else if (nodeLevel.equals(Constants.DJ_SW_LEVEL)) {//如果点击的是实物节点
                        long count = iDatamationsClient.dataCountByParam(text, modelId, param);
                        formatMessage(templateName, text, count, result);
                    }
                } else {
                    long count = iDatamationsClient.dataCountByParam(text, modelId, param);
                    formatMessage(templateName, text, count, result);
                    //向上汇总
                    if (modulePool.getIsPool().equals(Constants.IS_POOL)) {
                        //根据型号和发次获取配套清单表中实物信息
                        List<Map<String, Object>> qdInfo = getQDInfo(drawingNo, batchNo);
                        try {
                            //获取单机及总装直属件实物信息
                            List<ExhibitionDTO> physical = getPhysical(qdInfo, classIfication, batchNo, drawingNo);
                            for (ExhibitionDTO exhibitionDTO : physical) {
                                String physicalCode = exhibitionDTO.getPhysicalNo();
                                long num = iDatamationsClient.dataCountByParam(physicalCode, modelId, param);
                                formatMessage(templateName, physicalCode, num, result);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("获取向上汇总数据校验信息失败", e);
                        }
                    }
                }
            }
        }
        return result;
    }

    private ModelDataQueryParamVO getModelDataQueryParamVO(Emp emp) throws Exception {
        String grade = emp.getGrade();
        if (StringUtils.isEmpty(grade)) {
            throw new Exception("未获取到人员密级！");
        }
        //封装密级大于当前登陆人的查询条件
        ModelDataQueryParamVO param = new ModelDataQueryParamVO();

        JSONObject jsonObj = new JSONObject();
        JSONObject jsonObjectGrade = new JSONObject();
        jsonObjectGrade.put("$gt", Integer.parseInt(grade));
        jsonObj.put("S_M_SYS_SECRETLEVEL", jsonObjectGrade);
        param.setQueryFilter(jsonObj.toJSONString());
        return param;
    }

    private void formatMessage(String templateName, String physicalNo, long count, List<String> result) {
        if (count > 0) {
            result.add("实做BOM[" + physicalNo + "]模板[" + templateName + "]" + count + "条;");
        }
    }

    private void formatMessage(String physicalNo, long count, List<String> result) {
        if (count > 0) {
            result.add("实做BOM[" + physicalNo + "]" + count + "条;");
        }
    }

    /**
     * 清除缓存
     *
     * @return
     */
    @Override
    public ResultBody clearCache(String text) {
        //要删除的文件id集合
        List<String> delIds;
        //当月第一天
        Date monthFirstDay;
        try {
            monthFirstDay = DateUtils.getMonthFirstDay(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("获取当月第一天，下月第一天日期失败", e);
            return ResultBody.failure(e.getMessage());
        }
        //首先获取当前月数据
        //根据树节点获取组包数据
        QModuleGroupPack qModuleGroupPack = QModuleGroupPack.moduleGroupPack;
        //根据当前节点查询
        Predicate predicate = qModuleGroupPack.physicalNo.eq(text)
                .and(qModuleGroupPack.groupPackDate.after(monthFirstDay));
        long count = jpaQueryFactory
                .selectFrom(qModuleGroupPack)
                .where(predicate).orderBy(qModuleGroupPack.groupPackDate.desc()).fetchCount();
        // 如果大于10条，则删除上个月之前数据
        if (count >= SAVE_FILE_NUM) {
            //获取要删除的id集合
            delIds = getDelId(text, monthFirstDay);
        } else {
            List<String> delId = getDelId(text, null);
            //保留10条
            delIds = delId.stream().skip(SAVE_FILE_NUM).collect(Collectors.toList());
        }
        //删除文件及数据
        if (!CollectionUtils.isEmpty(delIds)) {
            try {
                delFileAndData(delIds);
                return ResultBody.success("清除缓存成功");
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("删除组包文件及数据失败", e);
                return ResultBody.failure(e.getMessage());
            }
        }
        return ResultBody.success("无需要清楚缓存的数据");
    }

    /**
     * 删除文件及数据
     *
     * @param delIds
     * @throws Exception
     */
    private void delFileAndData(List<String> delIds) throws Exception {
        File folder = new File(CLOUD_HOME + FILE_PATH);
        File[] files = folder.listFiles();
        if (null != files && files.length > 0) {
            for (File file : files) {
                String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
                if (delIds.contains(fileName)) {
                    if (!file.delete()) {
                        throw new Exception("删除文件[" + file.getName() + "]失败！");
                    }
                }
            }
            //批量删除
            moduleGroupPackRepository.deleteAllByIdInBatch(delIds);
        }
    }


    /**
     * 根据树节点和开始时间获取要删除的文件id
     *
     * @param text
     * @param startTime
     * @return
     */
    private List<String> getDelId(String text, Date startTime) {
        //根据树节点获取组包数据
        QModuleGroupPack qModuleGroupPack = QModuleGroupPack.moduleGroupPack;
        //根据当前节点查询,增加当前登陆人逻辑
        String username = SessionManager.getCurrentUser().getUsername();
        Predicate predicate = qModuleGroupPack.physicalNo.eq(text).and(qModuleGroupPack.createUser.eq(username));
        if (null != startTime) {
            predicate = ExpressionUtils.and(predicate, qModuleGroupPack.groupPackDate.before(startTime));
        }
        return jpaQueryFactory
                .select(qModuleGroupPack.id)
                .from(qModuleGroupPack)
                .where(predicate).orderBy(qModuleGroupPack.groupPackDate.desc()).fetch();
    }

    private String getTextByCodeLookUp(String code) {
        //根据code值获取密级的数据字典
        List<TreeModel<Object>> list = iDatamationsClient.listLookupsEnable("secretlevel");
        if (CollectionUtils.isNotEmpty(list)) {
            //获取系统接口的全部信息，通过密级字典的code值获取密级的对应关系
            List<TreeModel<Object>> secretLevel = list.get(0).getChildren();
            for (TreeModel<Object> objectTreeModel : secretLevel) {
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(objectTreeModel.getAttributes()));
                if (jsonObject.getString("code").equals(code)) {
                    return jsonObject.getString("name");
                }
            }
        }
        return "";
    }

    private String getFileSecretByCode(String code) {
        //根据code值获取密级的数据字典
        List<TreeModel<Object>> list = iDatamationsClient.listLookupsEnable("secretfilelevel");
        if (CollectionUtils.isNotEmpty(list)) {
            //获取系统接口的全部信息，通过密级字典的code值获取密级的对应关系
            List<TreeModel<Object>> secretLevel = list.get(0).getChildren();
            for (TreeModel<Object> objectTreeModel : secretLevel) {
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(objectTreeModel.getAttributes()));
                if (jsonObject.getString("code").equals(code)) {
                    return jsonObject.getString("name");
                }
            }
        }
        return "";
    }


    /**
     * @param modelInfo 模板id
     * @return
     * @throws Exception
     */
    private Map<String, List<Map<String, Object>>> getDataListByPhysicals(String modelInfo, JSONObject jsonObject) {
        if (jsonObject == null) {
            return new HashMap<>();
        }
        ModelDataQueryParamVO modelDataQueryParamVO = new ModelDataQueryParamVO();
        modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
        List<Map<String, Object>> rows = iDatamationsClient.packetDataListAll("", modelInfo, modelDataQueryParamVO);
        //根据实物号分组
        return rows.stream().collect(Collectors.groupingBy(item -> item.get("F_PhysicalCode").toString()));
    }

    /**
     * 获取查询条件
     *
     * @param physical 实物号字段集合
     * @param emp      当前登陆人信息
     * @param order    排序字段
     * @return
     */
    private JSONObject getSearchJson(List<String> physical, Emp emp, String order) throws Exception {
        JSONObject jsonObject = new JSONObject();
        //只要生效版本
//        jsonObject.put("S_M_SYS_MAXVERSION", 1);
        String grade = emp.getGrade();
        if (StringUtils.isEmpty(grade)) {
            logger.error("未获取到人员密级！");
            throw new Exception("未获取到人员密级！");
        }
        JSONObject jsonObjectGrade = new JSONObject();
        //获取数据密级小于等于人员密级的
        jsonObjectGrade.put("$lt", Integer.parseInt(grade) + 1);
        jsonObject.put("S_M_SYS_SECRETLEVEL", jsonObjectGrade);
        jsonObject.put("S_M_SYS_VERSIONSTATUS", 1);
//        String order = iMaintainService.queryClomName(modelInfo, tempid, Constants.ORDER_FIELD);
        if (StringUtils.isNotEmpty(order)) {
            JSONObject orderJson = new JSONObject();
            orderJson.put(order, "ASC");
            jsonObject.put("$orderBy", orderJson);
        }
        if (CollectionUtils.isEmpty(physical)) {
            return null;
        }
        //性能优化根据实物号批量查询
        JSONObject physicalJson = new JSONObject();
        physicalJson.put("$in", physical.toArray());
        jsonObject.put("F_PhysicalCode", physicalJson);
        return jsonObject;
    }
}
