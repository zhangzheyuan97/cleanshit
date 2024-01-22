package com.meritdata.dam.datapacket.plan.acquistion.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.log.service.ILogPostService;
import com.meritdata.cloud.log.util.Message;
import com.meritdata.cloud.properties.MeritdataCloudProperties;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.utils.LogPattenUtils;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.common.SessionManager;
import com.meritdata.dam.common.client.QualityClient;
import com.meritdata.dam.common.client.service.ClientService;
import com.meritdata.dam.common.enums.*;
import com.meritdata.dam.common.service.nacos.NacosServerTool;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.service.IStandAloneService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.acquistion.vo.QueryNodeDTO;
import com.meritdata.dam.datapacket.plan.application.dao.ModuleGroupPackRepository;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackShowService;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.client.IMeritCloudClient;
import com.meritdata.dam.datapacket.plan.envelope.service.IEnvelopeService;
import com.meritdata.dam.datapacket.plan.manage.entity.client.ModelDataExportParam;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.model.controller.ModuleInfoConfigController;
import com.meritdata.dam.datapacket.plan.model.dao.ModuleCurateRepository;
import com.meritdata.dam.datapacket.plan.model.entity.*;
import com.meritdata.dam.datapacket.plan.model.service.IModuleInfoConfigService;
import com.meritdata.dam.datapacket.plan.model.service.IModulePlanService;
import com.meritdata.dam.datapacket.plan.model.vo.*;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.PageUtil;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowColorEnum;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStructureEnum;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.datamanage.DataQueryDTO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import com.meritdata.dam.entity.datamanage.*;
import com.meritdata.dam.entity.metamanage.*;
import com.netflix.client.ClientException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MaintainServiceImpl implements IMaintainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintainServiceImpl.class);

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IModulePlanService iModulePlanService;

    @Autowired
    IDataPacketClient iDataPacketClient;

    @Autowired
    IFlowBomSheetDataInter iFlowBomSheetDataInter;

    @Autowired
    private IModuleInfoConfigService moduleInfoConfigService;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    MeritdataCloudProperties meritdataCloudProperties;

    @Autowired
    IMeritCloudClient iMeritCloudClient;

    @Autowired
    ILogPostService logPostService;

    @Autowired
    private ClientService clientService;

    @Autowired
    IDataPacketClient dataPacketClient;
    @Autowired
    private IModulePlanService modulePlanService;

    @Autowired
    IDataPackShowService iDataPackShowService;

//    @Autowired
//    private ModelDataExportParam requestData;

    @Autowired
    private QualityClient qualityclient;

    @Autowired
    ModuleCurateRepository moduleCurateRepository;

    @Autowired
    IModuleInfoConfigService iModuleInfoConfigService;

    static final String regex = "[^!#$%^&'()*+,./;:=?@[/] ]";
    @Autowired
    ModuleGroupPackRepository moduleGroupPackRepository;

    @Autowired
    private IEnvelopeService envelopeService;

    @Autowired
    private IStandAloneService standAloneService;

    @Autowired
    private ModuleInfoConfigController moduleInfoConfigController;

    @Autowired
    private IDatamationsClient datamationsClient;

    // 系统字段别名前缀
    private final static String SYSTEM_FIELD_PREFIX = "S_";

    // 业务字段别名前缀
    private final static String BUSI_FIELD_PREFIX = "F_";


    //nacos配置文件 默认密级 配置
    public static final String NACOS_DATA_ID = "common-dam.properties";
    //nacos分组
    public static final String NACOS_GROUP = "DEFAULT_GROUP";
    //nacos配置项 默认密级配置项
    public static final String NACOS_DEFAULT_SECRET = "datapacket.default.secret";

    //单机
    private final static String STAND_ALONE = "单机";

    //产品编号
    private final static String PRODUCT_NUMBER = "产品编号";

    //产品批次
    private final static String PRODUCT_BATCH = "产品批次";

    //产品代号
    private final static String PRODUCT_CODE = "产品代号";

    //检查测试项目
    private final static String TESTING_ITEMS = "检查测试项目";

    //要求值
    private final static String REQUIRED_VAL = "要求值";

    //实物编码
    private final static String PHYSICAL_CODE = "实物编码";

    /**
     * 实现动态传表名称查询表数据的实体类
     */
    @PersistenceContext
    EntityManager em;

    @Override
    public List<TreeDto> maintainTree(String userId) {
        List<TreeDto> treeDtoList = new ArrayList<>();
        try {
            Map<String, Object> map = new HashMap<>();
            List<Map<String, Object>> mapsFCInfo = iDatamationsClient.getFCInfo(map);
            treeDtoList = iModulePlanService.tree("-1", userId);
            List<TreeDto> treeDtoRemove = new ArrayList<>();
            treeDtoList.forEach(item -> {
                if (FlowStructureEnum.STANDALONE.getValue().equals(item.getText()) || FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue().equals(item.getText())) {
//                treeDtoList.remove(item);
                    treeDtoRemove.add(item);
                }
            });
            //查询配套清单表信息，挂载至数据采集分系统末级节点
            List<Map<String, Object>> maps = iDatamationsClient.querySupportingList(map);
            if (treeDtoList.size() > 0) {
                treeDtoList.removeAll(treeDtoRemove);
            }
            QModuleTree qModuleTree = QModuleTree.moduleTree;
            List<String> ids = new ArrayList<>();
            treeDtoList.stream().forEach(item -> {
                item.getChildren().stream().forEach(itemChildren -> {
                    ids.add(itemChildren.getPid());
                });
            });
            List<ModuleTree> fetch = jpaQueryFactory.selectFrom(qModuleTree).where(qModuleTree.id.in(ids)).fetch();
            HashMap<String, ModuleTree> map1 = new HashMap<>();
            fetch.forEach(item -> {
                map1.put(item.getId(), item);
            });

            //获取单机中图号和类型三的对应关系
            List<Map<String, Object>> singleDataList = datamationsClient.exhibitionTree(new HashMap<>(), "PHYSICAL_OBJECT_SINGLE_MACHINE");
            singleDataList = singleDataList.stream().filter(m ->
                    m.get("F_CLASSIFICATION") != null
                            && m.get("F_THIRDTYPE") != null
                            && m.get("F_DRAWING_NO") != null
                            && m.get("F_BATCH_NO") != null
                            && m.get("F_PHYSICAL_NO") != null
                            && m.get("F_STAND_ALONE_IDENTIFICATION") != null && m.get("F_STAND_ALONE_IDENTIFICATION").equals("1")
                            && (m.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED") == null || m.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED").equals("1"))
            ).collect(Collectors.toList());
            Map<String, String> singleMap = new HashMap<>();
            for (Map<String, Object> stringObjectMap : singleDataList) {
                singleMap.put(stringObjectMap.get("F_DRAWING_NO").toString(), stringObjectMap.get("F_THIRDTYPE").toString());
            }

            //查询发次表的发次信息，挂在型号下，构建实做BOM树结构
            for (TreeDto tree : treeDtoList) {
                List<TreeDto> LBNode = tree.getChildren();
                for (TreeDto lbNode : LBNode) {
                    //判断型号对应的EBOM表格中的的父id与发次表中记录的分类信息一样则挂载
                    ModuleTree moduleTree = map1.get(lbNode.getPid());
//                   ModuleTree moduleTree = jpaQueryFactory.selectFrom(qModuleTree).where(qModuleTree.id.eq(lbNode.getPid())).fetchOne();
                    AtomicBoolean flag = new AtomicBoolean(false);
                    List<TreeDto> XHchildren = lbNode.getChildren();
                    for (TreeDto xhchildren : XHchildren) {
                        List<TreeDto> dtoList = new ArrayList<>();
                        mapsFCInfo.stream().forEach(item -> {
                            if (lbNode.getText().equals(item.get("F_CLASSIFICATION"))) {
                                if (xhchildren.getText().equals(item.get("F_MODEL"))) {
                                    //挂载至分析还是模块
                                    String subSystem = "";
                                    if ("0".equals(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "1" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString())) {
//                                        subSystem = "模块";
                                        subSystem = FlowStructureEnum.MODULE.getValue();
//                                        LOGGER.info("模块:"+subSystem);
                                    } else if ("1".equals(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "1" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString())) {
//                                        subSystem = "分系统";
                                        subSystem = FlowStructureEnum.SUBSYSTEM.getValue();
//                                        LOGGER.info("分系统:"+subSystem);
                                    }
                                    //区分分系统还是模块信息
                                    if (moduleTree != null && moduleTree.getText().equals(subSystem)) {
                                        QueryNodeDTO queryNodeDTO = new QueryNodeDTO();
                                        queryNodeDTO.setNodeType(item.get("F_SUBSYSTEM_IDENTIFICATION") == null ? "1" : item.get("F_SUBSYSTEM_IDENTIFICATION").toString());
                                        queryNodeDTO.setTempID(xhchildren.getId());//第四层加tempid
                                        queryNodeDTO.setNodeLevel("4");
                                        queryNodeDTO.setSecondNode(lbNode.getText());
                                        queryNodeDTO.setThirdlyNode(xhchildren.getText());
                                        queryNodeDTO.setFourthlyNode("4");
                                        queryNodeDTO.setFirstNode(moduleTree.getText());
                                        flag.set(true);
                                        TreeDto t = new TreeDto();
                                        t.setId(UUID.randomUUID().toString());
                                        t.setPid(xhchildren.getId());
                                        t.setText(item.get("F_ISSUE_NO").toString());
                                        t.setAttributes(queryNodeDTO);

                                        //按批次号审批需要的参数封装
                                        BatchNoNodeInfo batchNoNodeInfo = new BatchNoNodeInfo();
                                        batchNoNodeInfo.setFirstNode(subSystem);
                                        batchNoNodeInfo.setType(subSystem);
                                        batchNoNodeInfo.setModel(item.get("F_MODEL").toString());
                                        batchNoNodeInfo.setIssueNo(item.get("F_ISSUE_NO").toString());
                                        t.setBatchNoNodeInfo(batchNoNodeInfo);

                                        //记录本级fc节点信息
                                        List<TreeDto> BJFCList = new ArrayList<>();
                                        TreeDto BJFCListTreeDto = new TreeDto();
                                        QueryNodeDTO queryNodeDTOFCBJ = new QueryNodeDTO();
                                        queryNodeDTOFCBJ.setFirstNode(moduleTree.getText());
                                        queryNodeDTOFCBJ.setNodeLevel("5");
                                        queryNodeDTOFCBJ.setFourthlyNode(item.get("F_ISSUE_NO").toString());
                                        queryNodeDTOFCBJ.setSecondNode(lbNode.getText());
                                        queryNodeDTOFCBJ.setThirdlyNode(xhchildren.getText());
                                        BJFCListTreeDto.setText("本级，" + item.get("F_ISSUE_NO").toString());
                                        queryNodeDTOFCBJ.setTempID(xhchildren.getId());
                                        BJFCListTreeDto.setId(item.get("F_M_SYS_ID").toString());
                                        BJFCListTreeDto.setPid(t.getId());
                                        //设置第五级
                                        queryNodeDTOFCBJ.setFifthNode("5");
                                        BJFCListTreeDto.setAttributes(queryNodeDTOFCBJ);

                                        //按批次号审批需要的参数封装
                                        BatchNoNodeInfo batchNoNodeInfoOwn = new BatchNoNodeInfo();
                                        batchNoNodeInfoOwn.setFirstNode(subSystem);
                                        batchNoNodeInfoOwn.setType(subSystem);
                                        batchNoNodeInfoOwn.setModel(item.get("F_MODEL").toString());
                                        batchNoNodeInfoOwn.setIssueNo(item.get("F_ISSUE_NO").toString());
                                        //发次本级
                                        batchNoNodeInfoOwn.setBatchNo(Constants.OWN_ISSUE);
                                        //发次本级实物号为发次号
                                        batchNoNodeInfoOwn.setPhysicalNo(item.get("F_ISSUE_NO").toString());
                                        BJFCListTreeDto.setBatchNoNodeInfo(batchNoNodeInfoOwn);
                                        BJFCList.add(BJFCListTreeDto);

                                        //单机的树结构拼接
                                        List<String> djTreeLevelList = new ArrayList<>();
                                        Map<String, String> attributes = new HashMap<>();
                                        Map<String, String> batchNoNodeInfoAttr = new HashMap<>();
                                        //单机的叶子节点数据列表
                                        List<TreeDto> djLeafNodeList = new ArrayList<>();
                                        //总装直属件无实物号的树结构拼接
                                        List<String> zzzsjNoPhysTreeLevelList = new ArrayList<>();
                                        //总装直属件无实物号的叶子节点数据列表
                                        List<TreeDto> zzzsjNoPhysLeafNodeList = new ArrayList<>();
                                        //总装直属件有实物号的树结构
                                        List<String> zzzsjHasPhysTreeLevelList = new ArrayList<>();
                                        //总装直属件有实物号的叶子节点数据列表
                                        List<TreeDto> zzzsjHasPhysLeafNodeList = new ArrayList<>();

                                        if (maps.size() > 0) {
                                            maps.stream().forEach(standaloneItem -> {
                                                //挂载至满足的型号和发次信息下
                                                if (xhchildren.getText().equals(standaloneItem.get("F_MODEL"))
                                                        && item.get("F_ISSUE_NO").toString().equals(standaloneItem.get("F_ISSUE_NO"))) {
                                                    TreeDto DJFCListTreeDto = new TreeDto();
                                                    QueryNodeDTO queryNodeDTOFCDJ = new QueryNodeDTO();
                                                    queryNodeDTOFCDJ.setFourthlyNode(item.get("F_ISSUE_NO").toString());
                                                    queryNodeDTOFCDJ.setSecondNode(lbNode.getText());
                                                    queryNodeDTOFCDJ.setThirdlyNode(xhchildren.getText());
                                                    queryNodeDTOFCDJ.setNodeLevel("5");
                                                    queryNodeDTOFCDJ.setFirstNode(moduleTree.getText());
                                                    queryNodeDTOFCDJ.setFifthNode("5");

                                                    queryNodeDTOFCDJ.setTempID(xhchildren.getId());
                                                    DJFCListTreeDto.setId(standaloneItem.get("F_M_SYS_ID").toString());
                                                    DJFCListTreeDto.setPid(t.getId());

                                                    //按批次号审批需要的参数封装
                                                    BatchNoNodeInfo batchNoNodeInfoDJ = new BatchNoNodeInfo();
                                                    batchNoNodeInfoDJ.setFirstNode(moduleTree.getText());
                                                    batchNoNodeInfoDJ.setModel(item.get("F_MODEL").toString());
                                                    batchNoNodeInfoDJ.setIssueNo(item.get("F_ISSUE_NO").toString());
                                                    if (standaloneItem.get("F_DRAWING_NO") != null) {
                                                        batchNoNodeInfoDJ.setDrawingNo(standaloneItem.get("F_DRAWING_NO").toString());
                                                    }
                                                    if (standaloneItem.get("F_BATCH_NO") != null) {
                                                        batchNoNodeInfoDJ.setBatchNo(standaloneItem.get("F_BATCH_NO").toString());
                                                        if (!batchNoNodeInfoAttr.containsKey(standaloneItem.get("F_BATCH_NO").toString())) {
                                                            batchNoNodeInfoAttr.put(standaloneItem.get("F_BATCH_NO").toString(), JSON.toJSONString(batchNoNodeInfoDJ));
                                                        }
                                                    }


                                                    //单机标识
                                                    String aloneIdentification = standaloneItem.get("F_STAND_ALONE_IDENTIFICATION") == null ? "1" : standaloneItem.get("F_STAND_ALONE_IDENTIFICATION").toString();
                                                    //是否管理到实物标识
                                                    String whetherPhysical = standaloneItem.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED") == null ? "0" : standaloneItem.get("F_WHETHER_PHYSICAL_OBJECTS_MANAGED").toString();
                                                    if ("0".equals(whetherPhysical)) {
                                                        if (standaloneItem.get("F_DRAWING_NO") != null && standaloneItem.get("F_BATCH_NO") != null) {
                                                            //未管理到实物，展示批次号
                                                            DJFCListTreeDto.setText(standaloneItem.get("F_BATCH_NO").toString());
                                                            //对于批次号为null的怎么办 (图号/批次号/实物号)
                                                            zzzsjNoPhysTreeLevelList.add(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue() + "," + standaloneItem.get("F_DRAWING_NO") + "," + standaloneItem.get("F_BATCH_NO"));
                                                            //对于批次号为null的怎么办 (实物号)
                                                            zzzsjNoPhysLeafNodeList.add(DJFCListTreeDto);
                                                            //总装直属件
                                                            queryNodeDTOFCDJ.setIsAlone("0");
                                                            queryNodeDTOFCDJ.setFifthNode("5");
                                                            queryNodeDTOFCDJ.setLinkPhysical("0");
                                                            //按批次号审批需要的参数封装
                                                            batchNoNodeInfoDJ.setType(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
                                                            //未管理到实物，实物号为批次号
                                                            batchNoNodeInfoDJ.setPhysicalNo(standaloneItem.get("F_BATCH_NO").toString());

                                                        }
                                                    } else if ("1".equals(whetherPhysical)) {
                                                        if ("1".equals(aloneIdentification)) {
                                                            if (standaloneItem.get("F_DRAWING_NO") != null && standaloneItem.get("F_BATCH_NO") != null && standaloneItem.get("F_PHYSICAL_NO") != null) {
                                                                DJFCListTreeDto.setText(standaloneItem.get("F_PHYSICAL_NO").toString());
                                                                //根据图号获取类型二
                                                                if (singleMap.get(standaloneItem.get("F_DRAWING_NO").toString()) != null) {

                                                                    Map<String, Object> djInfo = getDJInfo(standaloneItem.get("F_PHYSICAL_NO").toString(), standaloneItem.get("F_DRAWING_NO").toString(), standaloneItem.get("F_BATCH_NO").toString());
                                                                    if (null != djInfo) {
                                                                        QueryNodeDTO attr = new QueryNodeDTO();
                                                                        String temId = djInfo.get("F_M_SYS_ID").toString();
                                                                        attr.setTempID(temId);
                                                                        //单机
                                                                        attr.setFirstNode(STAND_ALONE);
                                                                        attr.setSecondNode(djInfo.get("F_CLASSIFICATION") == null ? "" : djInfo.get("F_CLASSIFICATION").toString());
                                                                        attr.setThirdlyNode(djInfo.get("F_DRAWING_NO").toString());
                                                                        attr.setFourthlyNode(djInfo.get("F_BATCH_NO").toString());
//                                                                        //todo：如果有这个参数说明是分系统中的单机 setAuthType(目前没发现有使用)
                                                                        attr.setAuthType("1");
                                                                        attr.setFifthNode("5");
                                                                        String s = JSONObject.toJSONString(attr);
                                                                        if (!attributes.containsKey(standaloneItem.get("F_BATCH_NO").toString())) {
                                                                            attributes.put(standaloneItem.get("F_BATCH_NO").toString(), s);
                                                                        }
                                                                        if (!attributes.containsKey(standaloneItem.get("F_PHYSICAL_NO").toString())) {
                                                                            attributes.put(standaloneItem.get("F_PHYSICAL_NO").toString(), s);
                                                                        }
                                                                        //按批次号审批需要的参数封装
                                                                        batchNoNodeInfoDJ.setType(FlowStructureEnum.STANDALONE.getValue());
                                                                        //管理到实物，实物号
                                                                        batchNoNodeInfoDJ.setPhysicalNo(standaloneItem.get("F_PHYSICAL_NO").toString());
                                                                    }
                                                                    String djTreeLevelStr = singleMap.get(standaloneItem.get("F_DRAWING_NO").toString()) + "," + standaloneItem.get("F_DRAWING_NO") + "," + standaloneItem.get("F_BATCH_NO") + "," + standaloneItem.get("F_PHYSICAL_NO");
                                                                    //对于批次号为null的怎么办 (图号/批次号/实物号)
                                                                    djTreeLevelList.add(djTreeLevelStr);
                                                                    //对于批次号为null的怎么办 (实物号)
                                                                    djLeafNodeList.add(DJFCListTreeDto);
                                                                    //单机
                                                                    queryNodeDTOFCDJ.setIsAlone("1");
                                                                }
                                                            }
                                                        } else if ("0".equals(aloneIdentification)) {
                                                            if (standaloneItem.get("F_DRAWING_NO") != null && standaloneItem.get("F_BATCH_NO") != null && standaloneItem.get("F_PHYSICAL_NO") != null) {
                                                                //管理到实物，展示实物号
                                                                DJFCListTreeDto.setText(standaloneItem.get("F_PHYSICAL_NO").toString());
                                                                //对于批次号为null的怎么办 (图号/批次号/实物号)
                                                                zzzsjHasPhysTreeLevelList.add(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue() + "," + standaloneItem.get("F_DRAWING_NO") + "," + standaloneItem.get("F_BATCH_NO") + "," + standaloneItem.get("F_PHYSICAL_NO"));
                                                                //对于批次号为null的怎么办 (实物号)
                                                                zzzsjHasPhysLeafNodeList.add(DJFCListTreeDto);

                                                                //总装直属件
//                                                                QueryNodeDTO attr = new QueryNodeDTO();
                                                                queryNodeDTOFCDJ.setFifthNode("5");
                                                                queryNodeDTOFCDJ.setIsAlone("0");
                                                                queryNodeDTOFCDJ.setLinkPhysical("1");
                                                                String s = JSONObject.toJSONString(queryNodeDTOFCDJ);
                                                                if (!attributes.containsKey(standaloneItem.get("F_BATCH_NO").toString())) {
                                                                    attributes.put(standaloneItem.get("F_BATCH_NO").toString(), s);
                                                                }
                                                                if (!attributes.containsKey(standaloneItem.get("F_PHYSICAL_NO").toString())) {
                                                                    attributes.put(standaloneItem.get("F_PHYSICAL_NO").toString(), s);
                                                                }
                                                                //按批次号审批需要的参数封装
                                                                batchNoNodeInfoDJ.setType(FlowStructureEnum.DIRECTLYAFFILIATEDPARTS.getValue());
                                                                //管理到实物，实物号
                                                                batchNoNodeInfoDJ.setPhysicalNo(standaloneItem.get("F_PHYSICAL_NO").toString());
                                                            }
                                                        }
                                                    }
                                                    DJFCListTreeDto.setAttributes(queryNodeDTOFCDJ);
                                                    DJFCListTreeDto.setBatchNoNodeInfo(batchNoNodeInfoDJ);
                                                }
                                            });
                                        }
                                        ArrayList<TreeDto> collect = BJFCList.stream().collect(Collectors.collectingAndThen(
                                                Collectors.toCollection(() -> new TreeSet<>(
                                                        Comparator.comparing(p -> p.getText())
                                                )), ArrayList::new
                                        ));

                                        //单机数据list
                                        List<TreeDto> djList = new ArrayList<>();
                                        if (djTreeLevelList != null && djTreeLevelList.size() > 0) {
                                            int levelNum = djTreeLevelList.get(0).split(",").length;
                                            List<TreeDto> childrenTree = getChildrenTree(attributes, djTreeLevelList, levelNum, djLeafNodeList, 0, null, batchNoNodeInfoAttr);
                                            djList.addAll(childrenTree);
                                        }
                                        //总装直属件无实物号数据
                                        List<TreeDto> zzzsjNoPhysChildrenTree = new ArrayList<>();
                                        if (zzzsjNoPhysTreeLevelList != null && zzzsjNoPhysTreeLevelList.size() > 0) {
                                            int levelNum = zzzsjNoPhysTreeLevelList.get(0).split(",").length;
                                            List<TreeDto> childrenTree = getChildrenTree(null, zzzsjNoPhysTreeLevelList, levelNum, zzzsjNoPhysLeafNodeList, 0, null, batchNoNodeInfoAttr);
                                            zzzsjNoPhysChildrenTree.addAll(childrenTree);
                                        }
                                        //总装直属件有实物号数据
                                        List<TreeDto> zzzsjHasPhysChildrenTree = new ArrayList<>();
                                        if (zzzsjHasPhysTreeLevelList != null && zzzsjHasPhysTreeLevelList.size() > 0) {
                                            int levelNum = zzzsjHasPhysTreeLevelList.get(0).split(",").length;
                                            List<TreeDto> childrenTree = getChildrenTree(attributes, zzzsjHasPhysTreeLevelList, levelNum, zzzsjHasPhysLeafNodeList, 0, null, batchNoNodeInfoAttr);
                                            zzzsjHasPhysChildrenTree.addAll(childrenTree);
                                        }
                                        //合并总装直属件数据
                                        mergeZzzs(zzzsjNoPhysChildrenTree, zzzsjHasPhysChildrenTree);
                                        collect.addAll(zzzsjHasPhysChildrenTree); //总装直属件数据
                                        collect.addAll(djList); //单机

                                        t.setChildren(collect);
                                        dtoList.add(t);
                                    }
                                }
                            }
                        });
                        ArrayList<TreeDto> collectList = dtoList.stream().collect(Collectors.collectingAndThen(
                                Collectors.toCollection(() -> new TreeSet<>(
                                        Comparator.comparing(p -> p.getText())
                                )), ArrayList::new
                        ));
                        xhchildren.setChildren(collectList);
                    }
                }
            }
            return treeDtoList;
        } catch (Exception e) {
            LOGGER.error("数据采集左侧树错误信息:", e);
        }
        return treeDtoList;
    }

    @Override
    public List<TreeDto> treeList(List<TreeDto> treeDtoList) {
        //树结构转为list
        List<TreeDto> allSysMenuDto = new ArrayList<>();
        for (TreeDto treeDto : treeDtoList) {
            List<TreeDto> children = treeDto.getChildren();
            allSysMenuDto.add(treeDto);
            if (children != null && children.size() > 0) {
                allSysMenuDto.addAll(treeList(children));
                treeDto.setChildren(null);
            }
        }
        return allSysMenuDto;
    }

    /**
     * 对树进行筛选
     *
     * @param
     * @return
     */
    @Override
    public List<TreeDto> hitPathList(List<TreeDto> treeDtoList, String keywords) {
        List<TreeDto> newNodes = new ArrayList<>();
        List<TreeDto> collect = treeDtoList.stream().filter(item -> item.getText().toLowerCase().contains(keywords.toLowerCase())).collect(Collectors.toList());
        Map<String, TreeDto> map = treeDtoList.stream().collect(Collectors.toMap(TreeDto::getId, Function.identity()));
        for (TreeDto treeDto : collect) {
            if (!newNodes.contains(treeDto)) {
                newNodes.add(treeDto);
                do {
                    treeDto = map.get(treeDto.getPid());
                    if (null != treeDto) {
                        if (newNodes.contains(treeDto)) break;
                        newNodes.add(treeDto);
                    } else {
                        break;
                    }
                } while (!"-1".equals(treeDto.getPid()));
            }
        }
        return newNodes;
    }

    /**
     * 将list转为树
     *
     * @param
     * @return
     */
    @Override
    public List<TreeDto> tree(String pid, List<TreeDto> treeDtoList) {
        List<TreeDto> treeNode = new ArrayList<>();
        Optional.ofNullable(treeDtoList).orElse(new ArrayList<>())
                .stream()
                .filter(root -> root.getPid().equals(pid))
                .forEach(tree -> {
                    List<TreeDto> treeDto = tree(tree.getId(), treeDtoList);
                    tree.setChildren(treeDto);
                    treeNode.add(tree);
                });

        return treeNode;
    }


    @Override
    public ResultBody<GridView> centerDataList(Map<String, String> map) {
        //获取颜色
        JSONObject jsonObject = JSON.parseObject(map.get("attributes") == null ? "" : map.get("attributes").toString());
        List<ModuleManageDto> moduleManageDtoList = new ArrayList<>();
        //页数
        Integer pageNum = Integer.parseInt(map.get("page"));
//        //页数
        Integer sizeNum = Integer.parseInt(map.get("rows"));
        String name = map.get("name");
        String treeName = map.get("treeName");
        try {
            //TODO::远程接口查询数仓建设中生效的模板信息
            //查询出所有模板信息
            List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(map);
            //TODO::查询TM_MODEL_CURATE表是否初始化模板信息
            //根据该节点下的模板信息
            String nodeId = map.get("tempId") == null ? "" : map.get("tempId");
            List<ModuleCurate> moduleCurateByNodeIdList = modulePlanService.getModuleCurateByNodeId(nodeId);
            List<ModuleCurate> moduleCurateArrayList = new ArrayList<>();
            //没有任何配置的情况
            if (moduleCurateByNodeIdList.size() == 0) {
                List<ModuleConfigDto> moduleCurateList = new ArrayList<>();
                moduleManageInfoList.forEach(model -> {
                    //初始化数据
                    ModuleCurate moduleCurate = new ModuleCurate();
                    moduleCurate.setIsPackage("1");
                    moduleCurate.setCode(model.getCode());
                    moduleCurate.setModelInfo(model.getModelInfo());
                    moduleCurate.setNodeId(nodeId);
                    moduleCurateArrayList.add(moduleCurate);
                    //返回给前端的数据
                    ModuleConfigDto moduleConfigDto = new ModuleConfigDto();
                    BeanUtils.copyProperties(model, moduleConfigDto);
                    moduleConfigDto.setIsPackage("1");
                    moduleCurateList.add(moduleConfigDto);
                });
                moduleCurateRepository.saveAll(moduleCurateArrayList);
                List<ModuleConfigDto> list = new PageUtil<>(moduleCurateList, sizeNum, pageNum).getList();
                list.forEach(module -> {
                    //1表示要查询对象表单下是否有数据,分系统和模块
                    if (StringUtils.isNotBlank(map.get("getFormFlag")) && map.get("getFormFlag").equals("1")) {
                        String subSystemColor = this.getModelColor(module.getModelInfo(), treeName, jsonObject);
                        module.setModelColor(subSystemColor);
                    }
                });
                long count = dataPacketClient.moduleCount(map);
                return ResultBody.success(new GridView(list, count));
            }
            //部分配置的情况，需要从801库中查询已经配置的项，若配置为是则展示，不展示配置为否的模板
            List<ModuleConfigDto> moduleCurateList = new ArrayList<>();
            //循环远程结果集
            moduleManageInfoList.forEach(model -> {
                //记录此数据是否在801库中不存在
                boolean flag = true;
                ModuleConfigDto moduleConfigDto = new ModuleConfigDto();
                BeanUtils.copyProperties(model, moduleConfigDto);
                moduleConfigDto.setIsPackage("1");
                for (int i = 0; i < moduleCurateByNodeIdList.size(); i++) {
                    ModuleCurate moduleCurate = moduleCurateByNodeIdList.get(i);
                    //如果当前数据存在且IsPackage为是则展示
                    if (moduleCurate.getCode().equals(moduleConfigDto.getCode())) {
                        flag = false;
//                        moduleConfigDto.setIsPackage(moduleCurate.getIsPackage());
                        //只展示配置为是的
                        if ("1".equals(moduleCurate.getIsPackage())) {
//                            ModuleCurate moduleCurateUpdate=new ModuleCurate();
//                            moduleCurateUpdate.setIsPackage("1");
//                            moduleCurateUpdate.setCode(model.getCode());
//                            moduleCurateUpdate.setModelInfo(model.getModelInfo());
//                            moduleCurateUpdate.setNodeId(nodeId);
//                            moduleCurateArrayList.add(moduleCurateUpdate);
                            moduleCurateList.add(moduleConfigDto);
                        }
                        break;
                    }
                }
                //flag=true则是当前801库里没有，需要添加至801库
                if (flag) {
                    ModuleCurate moduleCurateUpdate = new ModuleCurate();
                    moduleCurateUpdate.setIsPackage("1");
                    moduleCurateUpdate.setCode(model.getCode());
                    moduleCurateUpdate.setModelInfo(model.getModelInfo());
                    moduleCurateUpdate.setNodeId(nodeId);
                    moduleCurateArrayList.add(moduleCurateUpdate);
                    //同理，添加至数据库页面也需要展示词此条新数据
                    moduleCurateList.add(moduleConfigDto);
                }
            });
            if (moduleCurateArrayList.size() > 0) {
                moduleCurateRepository.saveAll(moduleCurateArrayList);
            }
            QModuleCurate qModuleCurate = QModuleCurate.moduleCurate;
//            Predicate predicate = qModuleCurate.isPackage.eq("1");
//            predicate=ExpressionUtils.and(predicate,qModuleCurate.nodeId.eq(nodeId));

            List<ModuleConfigDto> list = new PageUtil<>(moduleCurateList, sizeNum, pageNum).getList();
            list.forEach(module -> {
                //1表示要查询对象表单下是否有数据,分系统和模块
                if (StringUtils.isNotBlank(map.get("getFormFlag")) && map.get("getFormFlag").equals("1")) {
                    String subSystemColor = this.getModelColor(module.getModelInfo(), treeName, jsonObject);
                    module.setModelColor(subSystemColor);
                }
            });
//            long count = jpaQueryFactory.selectFrom(qModuleCurate).where(predicate).fetchCount();
//            long count = dataPacketClient.moduleCount(map);
            return ResultBody.success(new GridView(list, moduleCurateList.size()));
        } catch (Exception e) {
            LOGGER.error("错误信息:" + e.getMessage());
            e.printStackTrace();
            return ResultBody.success(new GridView<>(moduleManageDtoList, 0));
        }
    }

//    public String getSubSystemColor(String template, String treeName) {
//        String color = "";
//        //根据bom名称和modelid获取表名
//        String tableName = !"".equals(template) ? iDataPacketClient.getModuleInfo(template).getTableName() : "";
//        //查询表数据
//        List<Object[]> verList = (List<Object[]>) this.QuerySql(tableName, "all", treeName);
//        //查询表字段
//        List<Object[]> cloList = (List<Object[]>) this.QuerySql(tableName, "column", treeName);
//        //将List<Object[]>转换成List<Map<String, String>>方便取数据
//        List<Map<String, String>> verlm = this.ListForMap(verList, cloList);
//        List<String> box = new ArrayList<>();
//        int size = verlm.size();
//        //如果无数据则返回黄色
//        if (size == 0) {
//            color = FlowColorEnum.RED.getCode();
//            return color;
//        } else {
//            color = FlowColorEnum.GREE.getCode();
//            return color;
//        }
//    }

    @Override
    public ResultBody addModelData(DataOperateDTO param, VersionStatusEnum versionStatus, Map<String, String> mapData) {
        try {
            String modelId = param.getModelId();
            ModuleInfoDTO moduleInfo = iDataPacketClient.getModuleInfo(modelId);
            //进行包络分析
            Map<String, String> data = envelopeService.analysis(modelId, moduleInfo.getTableName(), param.getData(), "");
            param.setData(data);
            String flag = mapData.get("operateFlag") == null ? "" : mapData.get("operateFlag");
            ResultBody resultBody = iDatamationsClient.addAloneData(param, flag);
            StringBuffer massage = new StringBuffer();
            if (resultBody.isSuccess() && moduleInfo != null && resultBody.getData() == null) {
                Message msg = new Message(Message.TYPE_OPT,
                        LogPattenUtils.getProperty("model.data.gather.bmodule"),
                        LogPattenUtils.getProperty("model.acquistion.subsystem.fmodule"),
                        LogPattenUtils.getProperty("model.acquistion.subsystem.add"),
                        StrUtil.format(LogPattenUtils.getProperty("model.acquistion.subsystem.add.message"), mapData.get("F_PhysicalCode"), moduleInfo.getName()),
                        Message.STATUS_SUCESS);
                logPostService.postLog(msg);
            }
            return resultBody;
        } catch (Exception e) {
            return new ResultBody();
        }
    }

    @Override
    public Map<String, String> getLookupByPlan(String modelId, String nodeId) {
        Map<String, String> look = new HashMap<>();
        String tableName = iDatamationsClient.TableNameByModelId(modelId);
        QModuleColumnConfig qModuleColumnConfig = QModuleColumnConfig.moduleColumnConfig;
        List<ModuleColumnConfig> moduleColumnConfigList = jpaQueryFactory.selectFrom(qModuleColumnConfig)
                .where(qModuleColumnConfig.code.eq(tableName).and(qModuleColumnConfig.nodeId.eq(nodeId)))
                .orderBy(qModuleColumnConfig.sortNumber.asc())
                .fetch();
        for (ModuleColumnConfig moduleColumnConfig : moduleColumnConfigList) {
            //获取数据字典
            List<String> fieldByModelInfo = dataPacketClient.getFieldByModelInfo(moduleColumnConfig.getModelFieldId());
            if (!CollectionUtils.isEmpty(fieldByModelInfo)) {
                //将返回的map字符串转为listMap类型
                List<Map<String, Object>> lookUpString = iModuleInfoConfigService.formatMapStringToJson(fieldByModelInfo);
                if (!CollectionUtils.isEmpty(lookUpString)) {
                    moduleColumnConfig.setLookup(JSON.toJSONString(lookUpString));
                    String lookupCode = moduleColumnConfig.getLookupCode();
                    String s = "F_" + moduleColumnConfig.getFieldName();
                    look.put(s, lookupCode);
                }
            }
        }
        return look;
    }

    @Override
    public ResultBody<GridView> dataListManage(String physicalCode, String modelId, ModelDataQueryParamVO param, String nodeId) {
        //调用远程接口获取数据采集右侧数据
        try {
            Map<String, String> lookupByPlan = getLookupByPlan(modelId, nodeId);
            String orderName = queryClomName(modelId, nodeId, Constants.ORDER_FIELD);
            if (orderName == null || "".equals(orderName)) {
                return ResultBody.failure("查询失败,该模型未获取到序号字段！！");
            }
            param.setOrder(orderName);
            GridView<Map<String, Object>> mapPageResult = iDatamationsClient.dataCollectionListManage(physicalCode, modelId, param);
            List<Map<String, Object>> rows = mapPageResult.getRows();
//            if (CollectionUtils.isNotEmpty(rows)) {
//                rows.sort((o1, o2) -> {
//                    Integer number1 = Integer.valueOf(ObjectUtil.isEmpty(o1.get("F_ORDER_number")) ? "99999" : o1.get("F_ORDER_number").toString());
//                    Integer number2 = Integer.valueOf(ObjectUtil.isEmpty(o2.get("F_ORDER_number")) ? "99999" : o2.get("F_ORDER_number").toString());
//                    return number1.compareTo(number2);
//                });
//            }
            List<FormGroupVO> fields = getFields(modelId, true);
            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> stringObjectMap = rows.get(i);
                for (FormGroupVO field : fields) {
                    if ("文件信息".equals(field.getGroupName())) {
                        field.getFields().stream().forEach(fie -> {
                            if (stringObjectMap.containsKey(fie.getAliasName()) && stringObjectMap.get(fie.getAliasName()) != null) {
                                String id = stringObjectMap.get(fie.getAliasName()).toString();
                                ResultBody sysInfoByIds = getSysInfoByIds(id.split(","));
                                if (sysInfoByIds.isSuccess()) {
                                    stringObjectMap.put(fie.getAliasName() + "_File", stringObjectMap.get(fie.getAliasName()));
                                    stringObjectMap.put(fie.getAliasName(), sysInfoByIds.getData());
                                }
                            }
                        });
                    }
                }
                iDataPackShowService.checkLookUp(lookupByPlan, rows, i, stringObjectMap);
            }
            return ResultBody.success(new GridView<>(rows, iDatamationsClient.dataCollectionListManageCount(physicalCode, modelId, param)));
        } catch (Exception e) {
            LOGGER.error("查询失败", e);
            return ResultBody.failure("查询失败");
        }
    }

    @Override
    public ResultBody<GridView> dataListManage(String physicalCode, String modelId, ModelDataQueryParamVO param, String nodeId, String attributes) {
        JSONObject attributesJson = JSONObject.parseObject(attributes);
        if ((attributesJson.containsKey("nodeLevel") && attributesJson.containsKey("fourthlyNode"))
                || (attributesJson.containsKey("aloneLevel") && attributesJson.containsKey("fourthlyNode"))) {
            Integer nodeLevel = attributesJson.getInteger("nodeLevel") == null ? 0 : attributesJson.getInteger("nodeLevel");
            String fourthlyNode = attributesJson.getString("fourthlyNode") == null ? "" : attributesJson.getString("fourthlyNode");
            //如果是分系统下的单机
            String aloneLevel = attributesJson.get("aloneLevel") == null ? "" : attributesJson.get("aloneLevel").toString();
            //选择的批次号
            JSONObject jsonObject = new JSONObject();
            String queryFilter = param.getQueryFilter();
            if (StringUtils.isNotEmpty(queryFilter)) {
                jsonObject = JSONObject.parseObject(queryFilter);
            }
            if (nodeLevel == 4 || "4".equals(aloneLevel)) {
                physicalCode = "";
                //根据批次号查询，批次号+实物号为null或者空作为查询条件
                jsonObject.put(Constants.BATCH_NO_FIELD, fourthlyNode);
//                JSONObject jsonNull = new JSONObject();
//                jsonNull.put("$null", "null");
//                jsonObject.put("F_PhysicalCode", jsonNull);
            }
            param.setQueryFilter(jsonObject.toJSONString());
        }
        return dataListManage(physicalCode, modelId, param, nodeId);
    }

    @Override
    public ResultBody<GridView> dataListManageByIds(String physicalCode, String modelId, ModelDataQueryParamVO param, List<String> ids) {
        List<String> list = new PageUtil(ids, param.getRows(), param.getPage()).getList();
        //远程调用查询接口，根据实物号和模板信息
        GridView<Map<String, Object>> mapPageResult = iDatamationsClient.dataCollectionListManageByIds(physicalCode, modelId, list, param);
        List<Map<String, Object>> rows = mapPageResult.getRows();
//        Long aLong = iDatamationsClient.dataCollectionListManageCount(physicalCode, modelId, param);
        return ResultBody.success(new GridView<>(rows, ids.size(), param.getRows(), param.getPage()));
    }

    @Override
    public ResultBody<GridView> dataListManageByIds(String modelId, ModelDataQueryParamVO param, List<String> ids) {
        List<String> list = new PageUtil(ids, param.getRows(), param.getPage()).getList();
        //远程调用查询接口，根据实物号和模板信息
        GridView<Map<String, Object>> mapPageResult = iDatamationsClient.dataCollectionListManageByDataIds(modelId, JSONObject.toJSONString(param), list);
        List<Map<String, Object>> rows = mapPageResult.getRows();
//        Long aLong = iDatamationsClient.dataCollectionListManageCount(physicalCode, modelId, param);
        return ResultBody.success(new GridView<>(rows, ids.size(), param.getRows(), param.getPage()));
    }

    @Override
    public ResultBody<GridView> listManageByBomIds(List<String> physicalCodes, String modelId, ModelDataQueryParamVO param, List<Object> ids) {
        List<String> list = new PageUtil(ids, param.getRows(), param.getPage()).getList();
        //远程调用查询接口，根据实物号和模板信息
        GridView<Map<String, Object>> mapPageResult = iDatamationsClient.dataListManageByIds(physicalCodes, modelId, list, param);
        List<Map<String, Object>> rows = mapPageResult.getRows();
//        Long aLong = iDatamationsClient.dataCollectionListManageCount(physicalCode, modelId, param);
        return ResultBody.success(new GridView<>(rows, ids.size(), param.getRows(), param.getPage()));
    }

    /**
     * 必要的请求头
     *
     * @return
     */
    private HttpHeaders getHttpHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlAllowOrigin("*");
        List<HttpMethod> allowedMethods = new ArrayList<>();
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.DELETE);
        headers.setAccessControlAllowMethods(allowedMethods);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccessControlMaxAge(3600);
        List<String> allowedHeaders = new ArrayList<>();
        allowedHeaders.add("x-requested-with");
        headers.setAccessControlAllowHeaders(allowedHeaders);
        if (request != null) {
            Cookie[] cookies = request.getCookies();
            StringBuilder Cookie = new StringBuilder();
            for (Cookie cookie : cookies) {
                Cookie.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
            }
            headers.set("Cookie", Cookie.toString());
        }
        return headers;
    }


    @Override
    @Transactional
    public ResultBody deleteModelDatas(String dataManageType, ModelDataExportParamDto modelDataExportParam) {
        //调用远程接口进行删除操作
        try {
            //远程接口调用删除接口
            ResultBody resultBody = iDatamationsClient.deletedataByIds(dataManageType, modelDataExportParam);
            //删除完成后获取生效版本数据 （只能删除编辑中的数据，如果有历史数据，则一定有生效数据）
            List<Map<String, Object>> selectGridData = modelDataExportParam.getSelectGridData();
            if (CollectionUtils.isEmpty(selectGridData)) {
                return ResultBody.failure("未选择要删除的数据");
            }
            String modelId = modelDataExportParam.getModelId();
            for (Map<String, Object> selectGridDatum : selectGridData) {
                //获取最高版本为0,并且生效 的数据 ，如果有，只可能有一条
                String dataId = selectGridDatum.get("S_M_SYS_DATAID").toString();
                String physicalCode = selectGridDatum.get("F_PhysicalCode") == null ? "" : selectGridDatum.get("F_PhysicalCode").toString();
                //批次号
                String batchNo = selectGridDatum.get(Constants.BATCH_NO_FIELD) == null ? "" : selectGridDatum.get(Constants.BATCH_NO_FIELD).toString();
                ModelDataQueryParamVO queryParamVO = new ModelDataQueryParamVO();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("S_M_SYS_DATAID", dataId);
                JSONObject order = new JSONObject();
                //根据版本排序
                order.put("S_M_SYS_VERSION", "DESC");
                jsonObject.put("$orderBy", order);
                if (StringUtils.isEmpty(physicalCode) && StringUtils.isNotEmpty(batchNo)) {
                    jsonObject.put(Constants.BATCH_NO_FIELD, batchNo);
                }
                queryParamVO.setQueryFilter(jsonObject.toJSONString());
                //获取
                List<Map<String, Object>> effectDatas = iDatamationsClient.packetDataListAll(physicalCode, modelId, queryParamVO);
                //版本号倒序排序
                effectDatas = effectDatas.stream().sorted(Comparator.comparing(item -> (Integer.parseInt(item.get("S_M_SYS_VERSION").toString())), Comparator.reverseOrder())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(effectDatas)) {
                    //获取最高版本的数据
                    Map<String, Object> map = effectDatas.get(0);
                    //主键字段
                    Map<String, String> primaryData = new HashMap<>();
                    primaryData.put("F_M_SYS_ID", map.get("F_M_SYS_ID").toString());
                    //要修改的字段
                    Map<String, String> modifyData = new HashMap<>();
                    modifyData.put("S_M_SYS_MAXVERSION", "1");
                    //生效版本数据获取完成后，修改最高版本为1
                    DataOperateDTO param = new DataOperateDTO();
                    param.setModelId(modelId);
                    param.setData(modifyData);
                    param.setPrimaryData(primaryData);
                    //生效数据
                    param.setEffect(false);
                    ResultBody body = iDatamationsClient.updateModelData(param);
                    if (!body.isSuccess()) {
                        LOGGER.error("修改历史生效数据版本号失败！");
                        throw new Exception("修改历史生效数据版本号失败！");
                    }
                }
            }

            //根据moduleinfo获取模板信息
            ModuleInfoDTO moduleInfo = iDataPacketClient.getModuleInfo(modelDataExportParam.getModelId());
            Map<String, Object> stringObjectMap = modelDataExportParam.getSelectGridData().get(0);
            if (resultBody.isSuccess() && moduleInfo != null && stringObjectMap != null) {
                Message msg = new Message(Message.TYPE_OPT,
                        LogPattenUtils.getProperty("model.data.gather.bmodule"),
                        LogPattenUtils.getProperty("model.acquistion.subsystem.fmodule"),
                        LogPattenUtils.getProperty("model.acquistion.subsystem.delete"),
                        StrUtil.format(LogPattenUtils.getProperty("model.acquistion.subsystem.delete.message"), modelDataExportParam.getSelectGridData().get(0).get("F_PhysicalCode"), moduleInfo.getName()),
                        Message.STATUS_SUCESS);
                logPostService.postLog(msg);
            }
            return resultBody;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResultBody.failure("删除失败，请联系管理员处理！");
        }
    }

    @Override
    public ResultBody updateModelData(DataOperateDTO param) {

        String modelId = param.getModelId();
        //根据moduleinfo获取模板信息
        ModuleInfoDTO moduleInfo = iDataPacketClient.getModuleInfo(modelId);
        //进行包络分析
        String sysId = param.getPrimaryData().get("F_M_SYS_ID");
        Map<String, String> data = envelopeService.analysis(modelId, moduleInfo.getTableName(), param.getData(), sysId);
        param.setData(data);
        ResultBody resultBody = iDatamationsClient.updateModelData(param);
        if (resultBody.isSuccess() && moduleInfo != null) {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.data.gather.bmodule"),
                    LogPattenUtils.getProperty("model.acquistion.subsystem.fmodule"),
                    LogPattenUtils.getProperty("model.acquistion.subsystem.edit"),
                    StrUtil.format(LogPattenUtils.getProperty("model.acquistion.subsystem.edit.message"), param.getData().get("F_PhysicalCode"), moduleInfo.getName()),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
        }
        return resultBody;
    }

    @Override
    public ResultBody<Map<String, Object>> getModelDataByPrimary(DataQueryDTO queryDTO) {
        return iDatamationsClient.getModelDataByPrimary(queryDTO);
    }

    @Override
    public ResultBody reviseModelData(DataOperateDTO param) {
        String modelId = param.getModelId();
        //根据moduleinfo获取模板信息
        ModuleInfoDTO moduleInfo = iDataPacketClient.getModuleInfo(param.getModelId());
        //进行包络分析
        String sysId = param.getPrimaryData().get("F_M_SYS_ID");
        Map<String, String> data = envelopeService.analysis(modelId, moduleInfo.getTableName(), param.getData(), sysId);
        param.setData(data);
        //远程调用修订接口
        ResultBody resultBody = iDatamationsClient.reviseModelData(param);
        if (resultBody.isSuccess() && moduleInfo != null) {
            Message msg = new Message(Message.TYPE_OPT,
                    LogPattenUtils.getProperty("model.data.gather.bmodule"),
                    LogPattenUtils.getProperty("model.acquistion.subsystem.fmodule"),
                    LogPattenUtils.getProperty("model.acquistion.subsystem.revise"),
                    StrUtil.format(LogPattenUtils.getProperty("model.acquistion.subsystem.revise.message"), param.getData().get("F_PhysicalCode"), moduleInfo.getName()),
                    Message.STATUS_SUCESS);
            logPostService.postLog(msg);
        }
        return resultBody;
    }

    @Override
    public List<Map<String, Object>> queryDataForList(String modelId, ModelDataQueryParamVO param) {
        param.setTranslateDate(true);
        param.setTranslateUser(true);
        param.setEnableRefObj(true);
        param.setTranslateFile(true);
        try {
            List<Map<String, Object>> rows = iDatamationsClient.queryDataForList(modelId, param);
            List<FormGroupVO> fields = getFields(modelId, true);
            rows.stream().forEach(item -> {
                fields.stream().forEach(field -> {
                    if ("文件信息".equals(field.getGroupName())) {
                        field.getFields().stream().forEach(fie -> {
                            if (item.containsKey(fie.getAliasName()) && item.get(fie.getAliasName()) != null) {
                                String id = item.get(fie.getAliasName()).toString();
                                ResultBody sysInfoByIds = getSysInfoByIds(id.split(","));
                                if (sysInfoByIds.isSuccess()) {
//                                item.put(fie.getAliasName() + "_File",item.get(fie.getAliasName()));
                                    item.put(fie.getAliasName(), sysInfoByIds.getData());
                                }
                            }
                        });
                    }
                });
            });
            return rows;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getGradeList() {
        List<Map<String, Object>> lookUpList = new ArrayList<>();
        List<TreeModel<Object>> secretlevel = iMeritCloudClient.listLookupsEnable("secretlevel").get(0).getChildren();
        if (meritdataCloudProperties.getUsePlatformSecret()) {
            String grade = sessionUtils.getEmp().getGrade();
            if (StrUtil.isNotBlank(grade)) {
//                查询数据字典低于当前用户的密级信息
                for (TreeModel treeModel : secretlevel) {
                    Map<String, Object> lookUpInfo = (Map<String, Object>) treeModel.getAttributes();
                    Map<String, Object> lookUp = new HashMap<>();
                    try {
                        int code = Integer.parseInt(lookUpInfo.get("code").toString());
                        int gradeTemp = Integer.parseInt(sessionUtils.getEmp().getGrade());
                        if (gradeTemp >= code) {
                            lookUp.put("code", code);
                            lookUp.put("name", lookUpInfo.get("name"));
                            lookUpList.add(lookUp);
                        }
                    } catch (Exception e) {
                        LOGGER.error("数据字典类型转换错误" + e.getMessage());
                    }
                }
            }
        } else {
            for (TreeModel treeModel : secretlevel) {
                Map<String, Object> lookUpInfo = (Map<String, Object>) treeModel.getAttributes();
                Map<String, Object> lookUp = new HashMap<>();
                try {
                    int code = Integer.parseInt(lookUpInfo.get("code").toString());
                    lookUp.put("code", code);
                    lookUp.put("name", lookUpInfo.get("name"));
                    lookUpList.add(lookUp);
                } catch (Exception e) {
                    LOGGER.error("数据字典类型转换错误" + e.getMessage());
                }
            }
        }
        return lookUpList;
    }

    @Override
    public List<FormGroupVO> getFields(String modelId, boolean createData) {
        return iDatamationsClient.queryFormFields(modelId, createData);
    }

    @Override
    public ResultBody getSysInfoByIds(String[] ids) {
        return iDatamationsClient.getSysInfoByIds(ids);
    }

    @Override
    public ResultBody fileExist(List<String> ids, String selectId, HttpServletResponse httpServletResponse, long fileSize) {
        ResultBody resultBody = iDatamationsClient.fileExist(ids);
        if (resultBody.isSuccess()) {
            ResultBody resultBody1 = diskspaceUseMessage(ids, false);
            Map<String, Object> map = (Map<String, Object>) resultBody1.getData();
            if (map.containsKey("allowDownload") && (Boolean) map.get("allowDownload")) {
                ResultBody resultBody2 = chunksDownloadTolocal(ids, false, selectId);
                if (resultBody2.isSuccess()) {
                    List<String> path = (List<String>) resultBody2.getData();
                    long time = (fileSize > 100 * 1024 * 1024 ? (fileSize > 1024 * 1024 * 1024 ? 20000 : 10000) : 1000);
                    Boolean aBoolean = localFileExit(path.get(0), time);
                    ResultBody resultBody3 = ResultBody.success(aBoolean);
                    if (aBoolean && resultBody3.isSuccess()) {
                        resultBody3.setData(path);
//                        fileDownload(path.get(0),httpServletResponse);
                        return resultBody3;
                    } else {
                        return resultBody3;
                    }
                } else {
                    return resultBody2;
                }
            } else {
                return resultBody1;
            }
        } else {
            return resultBody;
        }
    }

    private Boolean localFileExit(String path, long time) {
        try {
            ResultBody resultBody = loaclFileExist(path);
            Thread.sleep(time);
            if (!(Boolean) resultBody.getData()) {
                return localFileExit(path, time);
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void fileDownload(String path, HttpServletResponse response) {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        String filename = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(path));
            java.io.File file = new java.io.File(path);
            filename = file.getPath().substring(file.getPath().lastIndexOf(java.io.File.separator) + 1);
            filename = encode(filename, "UTF-8");
            String excelname = "attachment;filename=\"" + filename + "\"";
            response.setContentType("application/x-msddownload;charset=UTF-8");
            response.setHeader("Content-Disposition", excelname);
//            response.setCharacterEncoding("utf-8");
            //将远程文件字节写入到输出流
            outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
            inputStream.close();
            //下载完成，删除文件,流未关闭，delete()删除不掉文件
            if (file.exists()) {
                file.delete();
            }
            java.io.File parentFile = file.getParentFile();
            if (parentFile.listFiles() == null || parentFile.listFiles().length == 0) {  //父目录下已无文件，删除父目录文件夹
                if (parentFile.exists()) {
                    parentFile.delete();
                    LOGGER.info("所有文件已下载完成，临时缓存文件夹已删除！");
                }
            }
            //压缩下载时，上面仅会删除压缩后的zip文件，原始文件用下面删除
            java.io.File originfile = new java.io.File(file.getPath().substring(0, file.getPath().lastIndexOf(".")));
            if (originfile.isDirectory()) {
                FileUtils.deleteDirectory(originfile);
            }

        } catch (Exception e) {
            LOGGER.error("文件{}下载失败，原因 :{}", filename, e);
            //String.format(EnumDamDatamanageResultStatus.DOWNLOAD_FILe_FAIL.message());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String encode(String str, String charset) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String group = matcher.group(0);
            matcher.appendReplacement(sb, URLEncoder.encode(group, charset));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public ResultBody diskspaceUseMessage(List<String> ids, Boolean compression) {
        return iDatamationsClient.diskspaceUseMessage(ids, compression);
    }

    @Override
    public ResultBody chunksDownloadTolocal(List<String> ids, Boolean compression, String selectId) {
        return iDatamationsClient.chunksDownloadTolocal(ids, compression, selectId);
    }

    @Override
    public ResultBody loaclFileExist(String path) {
        return iDatamationsClient.loaclFileExist(path);
    }

    @Override
    public void localFileDownload(String path, HttpServletResponse response) {
        fileDownload(path, response);
    }

    @Override
    public ResultBody<GridView<Map<String, Object>>> pageRefModelData(DataQueryDTO queryDTO) {
        return iDatamationsClient.pageRefModelData(queryDTO);
    }

    @Override
    public ResultBody getDicValidDataById(String id, String modelId, String fieldId) {
        return iDatamationsClient.getDicValidDataById(id, modelId, fieldId);
    }

    @Override
    public ResultBody<List<ModelVerFieldDTO>> getBaseTemplateField(String modelId) {
        return iDatamationsClient.getBaseTemplateField(modelId);
    }

    @Override
    public ResultBody effectData(ModelDataExportParam modelDataExportParam) {
        return iDatamationsClient.effectData(modelDataExportParam);
    }

    @Override
    public ResultBody syncData(String physicalCode, String modelId, String attributes) {
        try {
            JSONObject jsonObject = JSON.parseObject(attributes);
            //获取用户的操作节点
            String nodeLevel = jsonObject.get("nodeLevel") == null ? "" : jsonObject.get("nodeLevel").toString();
            //4为批次节点、5为实物节点
            //获取当前批次号
            String fourthlyNode = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
            //根据模型id查询对应模型的表名
            String tableName = !"".equals(modelId) ? iDataPacketClient.getModuleInfo(modelId).getTableName() : "";
            if (tableName != null && !"".equals(tableName)) {
                //与801现场沟通，非版本的数据和业务数据差"_version"后缀
                String verTable = tableName + "_version";
                List<Object[]> verList;
                List<Object[]> cloList;
                try {
                    //非版本数据查询结果
                    if ("4".equals(nodeLevel)) {
                        //如果是批次同步
                        verList = (List<Object[]>) QuerySql(verTable, "aloneBatch", fourthlyNode);
                    } else {
                        verList = (List<Object[]>) QuerySql(verTable, "all", physicalCode);
                    }
                    if (verList.size() == 0) {
                        return ResultBody.success();
                    }
                    //表字段查询结果
                    cloList = (List<Object[]>) QuerySql(verTable, "column", physicalCode);
                } catch (Exception e) {
                    return ResultBody.failure("未找到数据同步关联的模型表，请确认模型表是否存在！");
                }
                //非版本数据结果
                List<Map<String, String>> verlm;
                try {
                    verlm = ListForMap(verList, cloList);
                } catch (Exception e) {
                    return ResultBody.failure("业务版本模型表中的字段与非业务版本模型表中的字段未正确匹配，请检查！");
                }
                StringBuilder arrs = new StringBuilder();
                //新增更新的接口中要求传参不带中台模型自带的M_SYS_开头的字段，这里对获取到的所有字段类型根据是否有M_SYS_进行截取
                for (Object[] objects : cloList) {
                    //cloList.get(i)[0]这个获取查询出的字段名称
                    if (!objects[0].toString().contains("M_SYS_") && !objects[0].toString().contains("m_sys_")) {
                        //cloList.get(i)[1]这个获取的是查询出的字段类型
                        arrs.append(objects[1].toString()).append(",");
                    }
                }
                //将所有字段类型组装成一个数组[varchar,int,varchar...]，用以组装新增接口参数对象中的：param.setDataArray(arr);
                arrs = new StringBuilder(arrs.substring(0, arrs.lastIndexOf(",")));
                String[] arr = arrs.toString().split(",");
                //这里new的对象是新增/更新接口传参的参数
                DataOperateDTO param = new DataOperateDTO();
                //按照一般新增、更新的传参方式，来组装我们的对象参数
                for (Map<String, String> map : verlm) {
                    if (map.containsKey("IsApproval")) {
                        map.remove("IsApproval");
                    }
                    //将查询出来的结果，对应第i条数据，将这条数据的map取出
                    String SecretLevel = map.get("M_SYS_SECRETLEVEL");
                    //这一步是从map中将所有包含"M_SYS_"的字段取出放到list中（这里原本想使用迭代器直接remove，但是会报错：迭代器中无法定位到对应的map位置）
                    List<String> delKeys = map.keySet().stream().filter(item -> item.contains("M_SYS_") || item.contains("m_sys_")).collect(Collectors.toList());
                    //循环list将map中不要的项删除
                    delKeys.forEach(map::remove);
                    Map<String, String> result = new HashMap<>();
                    //将过滤后的map中的字段名称利用循环统一加上"F_"
                    map.keySet().forEach(item -> {
                        result.put("F_" + item, map.get(item));
                    });
                    result.put("S_M_SYS_SECRETLEVEL", SecretLevel);
                    Map<String, String> data = envelopeService.analysis(modelId, tableName, result, "");
                    data.put("operateFlag", "");
                    data.put("moduleId", "");
                    data.put("modelCode", "");
                    if ("4".equals(nodeLevel)) {
                        data.put("F_BatchNo", fourthlyNode);
                    } else {
                        data.put("F_PhysicalCode", physicalCode);
                    }
                    param.setData(data);
                    param.setModelId(modelId);
                    param.setDataArray(arr);
                    param.setEffect(false);
                    iDatamationsClient.addAloneData(param, "");
                }
            } else {
                return ResultBody.failure("该模型并未匹配到相应的表！");
            }
        } catch (Exception e) {
            return ResultBody.failure(e.getMessage());
        } finally {
            em.close();
        }
        return ResultBody.success();
    }

    /**
     * jpa无法满足动态表名查询数据公共方法
     *
     * @param tableName
     * @param sqlType
     * @return
     */
    @Override
    public List<?> QuerySql(String tableName, String sqlType, String s) {
        StringBuffer sql;
        if ("column".equals(sqlType)) {
            //这里需要注意：人大金仓库在查询表的字段信息时，where后的表名'tableName'需要加单引号；from后的"INFORMATION_SCHEMA"需要加双引号
            sql = new StringBuffer("SELECT column_name, udt_name  from \"INFORMATION_SCHEMA\".columns where table_name = '");
            sql.append(tableName);
            sql.append("'");
        } else if ("column_name".equals(sqlType)) {
            //这里需要注意：人大金仓库在查询表的字段信息时，where后的表名'tableName'需要加单引号；from后的"INFORMATION_SCHEMA"需要加双引号
            sql = new StringBuffer("SELECT column_name from \"INFORMATION_SCHEMA\".columns where table_name = '");
            sql.append(tableName);
            sql.append("'");
        } else if ("aloneBatch".equals(sqlType)) {
            //这里需注意：人大金仓库在查询时表名需要加双引号："table_name"
            sql = new StringBuffer("select * from \"");
            sql.append(tableName);
            sql.append("\" where \"BatchNo\" = '");
            sql.append(s);
            sql.append("'");
            // TODO: 2023/7/20
//            sql.append("AND \"PhysicalCode\" IS NULL");
        } else if ("testitemlookup".equals(sqlType)) {
            //这里需注意：人大金仓库在查询时表名需要加双引号："table_name"
            sql = new StringBuffer("select \"LOOK_UP\" from \"");
            sql.append(tableName);
            sql.append("\" where \"CODE\" = '");
            sql.append(s);
            sql.append("' and \"COLUMN_NAME\" = '检查测试项目'");
        } else if ("findTuHaoName".equals(sqlType)) {
            String TuHao = s.substring(0, s.indexOf("<->"));
            String PiCiHao = s.substring(s.indexOf("<->") + 3, s.length());
            //这里需注意：人大金仓库在查询时表名需要加双引号："table_name"
            sql = new StringBuffer("select \"NAME\" from \"");
            sql.append(tableName);
            sql.append("\" where \"DRAWING_NO\" = '");
            sql.append(TuHao);
            sql.append("' and \"BATCH_NO\" = '");
            sql.append(PiCiHao);
            sql.append("'");
        } else if ("PiCiShiWu".equals(sqlType)) {
            //这里需注意：人大金仓库在查询时表名需要加双引号："table_name"
            sql = new StringBuffer("select \"PHYSICAL_NO\" from \"");
            sql.append(tableName);
            sql.append("\" where \"BATCH_NO\" = '");
            sql.append(s);
            sql.append("' order by \"PHYSICAL_NO\"");
        } else {
            //这里需注意：人大金仓库在查询时表名需要加双引号："table_name"
            sql = new StringBuffer("select * from \"");
            sql.append(tableName);
            sql.append("\" where \"PhysicalCode\" = '");
            sql.append(s);
            sql.append("'");
        }
        Query query = em.createNativeQuery(sql.toString());
        return (List<?>) query.getResultList();
    }

    /**
     * Quer查询List<Object>转List<Map<String,Object>>公共方法
     *
     * @param qList
     * @param cloList
     * @return
     */
    @Override
    public List<Map<String, String>> ListForMap(List<Object[]> qList, List<Object[]> cloList) {
        List<Map<String, String>> verlm = new ArrayList<>();
        for (Object[] objects : qList) {//这条循环是查出来几条数据循环几次
            Map<String, String> map = new HashMap<>();
            List<Object> urlist = new ArrayList<>();
            map.clear();//不加这个clear，多条数据情况下最后拼装List<Map>会重复最后一条数据。可优化
            Collections.addAll(urlist, objects);
            for (int j = 0; j < urlist.size(); j++) {//这条循环是将每个字段名称和字段值匹配起来的步骤
                String urlistval = urlist.get(j) == null ? "" : urlist.get(j).toString();
                map.put((String) cloList.get(j)[0], urlistval);
            }
            verlm.add(map);
        }
        return verlm;
    }

    @Override
    public String queryClomName(String modelId, String nodeId, String columnName) {
        String order = "";
        //根据模型id查询对应模型的表名
        String tableName = iDataPacketClient.getModuleInfo(modelId).getTableName();
        String code = iDataPacketClient.getModuleInfo(modelId).getCode();
        List<ModuleColumnConfig> moduleCurateList = moduleInfoConfigService.getModuleCurate(nodeId, code, tableName, modelId);
        for (ModuleColumnConfig moduleColumnConfig : moduleCurateList) {
            if (moduleColumnConfig.getColumnName().equals(columnName)) {
                order = "F_" + moduleColumnConfig.getFieldName();
                break;
            }
        }
        return order;
    }

    /**
     * 批量导入模板下载
     *
     * @param request
     * @param res
     * @param nodeId
     * @param attributes
     * @param treeName
     * @return
     */
    @Override
    public Boolean exportAllModel(HttpServletRequest request, HttpServletResponse res, String nodeId, String attributes, String treeName, String batchNoNodeInfo) {
        try {
            //查字典
            List<TreeModel<Object>> list = clientService.listLookupsEnable("secretlevel");
            //获取Nacos配置文件的密级(默认密级)
            String maxSecretLevel = NacosServerTool.getParamVal(NACOS_DATA_ID, NACOS_GROUP, NACOS_DEFAULT_SECRET);
            String maxSecretLevelName = getTextByCodeLookUp(list, maxSecretLevel);

            //提取前端传来的信息
            JSONObject jsonObject = JSONObject.parseObject(attributes);
            String firstNode = jsonObject.get("firstNode") == null ? "" : jsonObject.get("firstNode").toString();
            String nodelevel = jsonObject.get("nodeLevel") == null ? "" : jsonObject.get("nodeLevel").toString();
            String thirdlyNode = jsonObject.get("thirdlyNode") == null ? "" : jsonObject.get("thirdlyNode").toString();
            String fourthlyNode = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
            String tempId = jsonObject.get("tempID") == null ? "" : jsonObject.get("tempID").toString();
            //前端定义的nodeId有问题，为上级id，与我们需要的不符，在这里进行修改
            nodeId = jsonObject.get("tempID") == null ? "" : jsonObject.get("tempID").toString();
            Map<String, String> map = new HashMap();
//            map.put("page", "1");
//            map.put("rows", "500");
            map.put("nodeId", nodeId);
            map.put("tempId", tempId);
            map.put("getFormFlag", "1");
            map.put("treeName", treeName);
            //查询出所有模板信息
            //List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(map);
            List<ModuleConfigDto> moduleManageInfoList = getModel(map);
            //储存excl的map，用以组装zip
            Map<String, byte[]> byteMap = new HashMap<>();
            assert moduleManageInfoList != null;
            for (ModuleConfigDto moduleManageDto : moduleManageInfoList) {
                HSSFWorkbook wb = new HSSFWorkbook();
                //获取当前modelid
                String modelInfo = moduleManageDto.getModelInfo();
                //获取当前模型表名
                String tableName = moduleManageDto.getTableName();
                //获取当前模型编码
                String code = moduleManageDto.getCode();
                //获取模型的字段信息
                List<ModuleColumnConfig> moduleCurateList = moduleInfoConfigService.getModuleCurate(nodeId, code, tableName, modelInfo);
                //查询当前树节点下的该模型是否配置了匹配关系
                Map<String, List<String>> stringListMap = moduleInfoConfigController.getMatchListByFegin(tableName, thirdlyNode);
                List<String> lookUpForFegin = iModulePlanService.getTestItemLookup(tableName, nodeId, TESTING_ITEMS);
                //用来接收excl表头的list
                List<String> filter = new ArrayList<>();
                for (ModuleColumnConfig moduleColumnConfig : moduleCurateList) {
                    //过滤导出模板，去除掉文件属性的字段
                    if (null != moduleColumnConfig.getFormFieldVO() && null != moduleColumnConfig.getFormFieldVO().getModelFieldConfigDTO()) {
                        ModelFieldConfigDTO modelFieldConfigDTO = moduleColumnConfig.getFormFieldVO().getModelFieldConfigDTO();
                        //如果是文件
                        if (null != modelFieldConfigDTO && modelFieldConfigDTO.getFile() == Constants.IS_FILE) {
                            continue;
                        }
                    }
                    String ColumnName = "F_" + moduleColumnConfig.getFieldName();
                    filter.add(ColumnName);
                }
                // 获取模板可编辑字段 剔除系统字段 大文本类型
                List<ModelVerFieldDTO> modelVerFieldDTOList = getModelEditFields(modelInfo).stream()
                        .filter(item -> !YesOrNoEnum.YES.is(item.getSystemField()) && !SystemFieldEnum.ID.is(item.getSysTableField())
                                && !DataBusiTypeEnum.CLOB.is(item.getFieldDataType().getBusiType()))
                        .collect(Collectors.toList());
                modelVerFieldDTOList.stream().map(temp -> {
                    ModelVerFieldDTO fieldDTO = setModelFieldAlias(temp);
                    return fieldDTO;
                }).collect(Collectors.toList());
                List<ModelVerFieldDTO> editFields = new ArrayList<>();
                //为了保持顺序相同
                filter.forEach(item -> {
                    List<ModelVerFieldDTO> collect = modelVerFieldDTOList.stream().filter(verFieldDTO -> verFieldDTO.getAliasName().equals(item)).collect(Collectors.toList());
                    editFields.addAll(collect);
                });
                //字段升序排序
                editFields.sort(Comparator.comparing(o -> o.getSortNumber()));
                try {
                    // 根据模型id获取单值 赋值字段信息
                    ResultBody<List<ModelFieldAssignConfigDTO>> resultBody1 = iDataPacketClient.getModelFieldAssignByModelId(modelInfo);
                    List<ModelFieldAssignConfigDTO> modelFieldAssignConfigDTOS = resultBody1.getData();
                    // 剔除赋值字段
                    if (modelFieldAssignConfigDTOS.size() > 0) {
                        for (ModelFieldAssignConfigDTO modelFieldAssignConfigDTO : modelFieldAssignConfigDTOS) {
                            for (int i = 0; i < editFields.size(); i++) {
                                if (editFields.get(i).getId().equals(modelFieldAssignConfigDTO.getShowField())) {
                                    editFields.remove(i);
                                    i--; // 索引要减1，不然会报 java.util.ConcurrentModificationException: null
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("批量导入模板下载失败！");
                    e.printStackTrace();
                    continue;
                }
                ResultBody<List<String>> resultBody2 = null;
                try {
                    resultBody2 = qualityclient.getNonnullFieldIdList(modelInfo);
                } catch (RuntimeException e) {
                    // 质量服务未启动不影响管理数据保存
                    if (e.getCause() instanceof ClientException) {
                        LOGGER.warn(e.getMessage());
                    } else {
                        throw e;
                    }
                }
                // 获取模型设置了非空校验信息
                List<String> ruleList = resultBody2 != null ? resultBody2.getData() : new ArrayList<>();
                //如果是分系统的节点，若无实物编码则补充；如果是单机的第五层级，无实物编码则补充
                if (!firstNode.equals(STAND_ALONE)) {
                    if (!filter.contains(PHYSICAL_CODE)) {
                        filter.add(PHYSICAL_CODE);
                    }
                } else {
                    if (nodelevel.equals("5")) {
                        if (!filter.contains(PHYSICAL_CODE)) {
                            filter.add(PHYSICAL_CODE);
                        }
                    }
                }
                filter.add("数据_密级");
                //获取当前模型名称  excl的名称
                String name = moduleManageDto.getName();
                //处理模型名称含有特殊符号
                name = name.replaceAll("[\\s\\\\/:\\*\\?\\\"<>\\|]", "");
                //生成excl
                creatExcelHeader(wb, name, editFields, ruleList, jsonObject, treeName, stringListMap, lookUpForFegin, batchNoNodeInfo);
                //将HSSFWorkbook转成byte[]
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    wb.write(bos);
                    byte[] bytes = bos.toByteArray();
                    byteMap.put(name + "(" + maxSecretLevelName + ")" + ".xls", bytes);
                } finally {
                    bos.close();
                    wb.close();
                }
            }
            //导出的zip名
            String filename = "选择节点有误请确认";
            if (STAND_ALONE.equals(firstNode)) {
                filename = thirdlyNode + "_" + fourthlyNode + "_批量导入模板" + "(" + maxSecretLevelName + ")";
            } else {
                filename = thirdlyNode + "_" + fourthlyNode + "_" + treeName + "_批量导入模板" + "(" + maxSecretLevelName + ")";
            }
            zipFile(res, byteMap, filename);
        } catch (Exception e) {
            LOGGER.error("导出所有模板失败！", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取list的字符长度
     *
     * @param stringList
     * @return
     */
    private static int calculateSumOfLengths(List<String> stringList) {
        int sum = 0;
        for (String str : stringList) {
            sum += str.length();
        }
        return sum;
    }

    /**
     * excl头导出
     *
     * @param wb
     * @param modelName
     * @param editFields
     * @param ruleList
     */
    @Override
    public void creatExcelHeader(HSSFWorkbook wb, String modelName, List<ModelVerFieldDTO> editFields
            , List<String> ruleList, JSONObject jsonObject, String treeName, Map<String, List<String>> stringListMap, List<String> lookUpForFegin, String batchNoNodeInfo) {
        try {
            HSSFSheet sheet = wb.createSheet(modelName);
            HSSFRow row = sheet.createRow(0);
            int index = 0;
            //用来存储数据行
            List<List<String>> result = new ArrayList<>();
            List<String> list;
            //定义日期格式类型或其他需要特殊处理的下标
            List<Integer> ilist = new ArrayList<>();
            // 遍历编辑字段
            for (ModelVerFieldDTO field : editFields) {
                // 剔除文件字段、公式字段
                if (field.getModelFieldConfigDTO() != null) {
                    ModelFieldConfigDTO modelFieldConfig = field.getModelFieldConfigDTO();
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(modelFieldConfig.getExpression())
                            || YesOrNoEnum.YES.is(modelFieldConfig.getFile())) {
                        index++;
                        continue;
                    }
                }
                String fieldName = field.getBusiName();
                //获取备注值，与现场沟通备注值定死与我们形成匹配关系
                String remark = field.getRemark();
                HSSFCell cell = row.createCell(index);
                cell.setCellValue(fieldName);
                HSSFCellStyle titleStype = wb.createCellStyle();
                titleStype.setAlignment(HorizontalAlignment.CENTER);
                String dataType = field.getDataType();
                String firstNode = jsonObject.get("firstNode") == null ? "" : jsonObject.get("firstNode").toString();
                String isAlone = jsonObject.get("isAlone") == null ? "" : jsonObject.get("isAlone").toString();

                //判断是否为varchar类型，单元格格式设置为普通文本类型
                if ("varchar".equalsIgnoreCase(dataType) || "varchar2".equalsIgnoreCase(dataType)) {
                    HSSFCellStyle textStyle = wb.createCellStyle();
                    textStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));
                    sheet.setDefaultColumnStyle(index, textStyle);
                }
                //产品批次
                String PiCiHao = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
                //型号、图号
                String XingHao = jsonObject.get("thirdlyNode") == null ? "" : jsonObject.get("thirdlyNode").toString();
                //产品编号（实物号）
                String ShiWuHao = jsonObject.get("fifthNode") == null ? "" : jsonObject.get("fifthNode").toString();
                //树层级，5代表实物号导入、4代表批次号导入
                String CengJi = jsonObject.get("nodeLevel") == null ? "" : jsonObject.get("nodeLevel").toString();
                //判断是分系统还是单机
                if (STAND_ALONE.equals(firstNode)) {
                    if (stringListMap == null) {
                        if ("产品代号".equals(fieldName)) {
                            String[] tuhao = new String[]{XingHao};
                            // 设置下拉控制范围
                            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                            // 下拉框内容
                            DVConstraint constraint = DVConstraint.createExplicitListConstraint(tuhao);
                            // 绑定下拉框和作用区域
                            HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                            sheet.addValidationData(dataValidation);
                        }
                    }
                    if ("产品名称".equals(fieldName)) {
                        String TuhaoN = "";
                        String param = XingHao + "<->" + PiCiHao;
                        List<?> objects = QuerySql("PHYSICAL_OBJECT_SINGLE_MACHINE", "findTuHaoName", param);
                        if (objects.size() > 0) {
                            TuhaoN = objects.get(0).toString();
                        }
                        if (TuhaoN != null || "null".equals(TuhaoN)) {
                            String[] tuname = new String[]{TuhaoN};
                            // 设置下拉控制范围
                            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                            // 下拉框内容
                            DVConstraint constraint = DVConstraint.createExplicitListConstraint(tuname);
                            // 绑定下拉框和作用区域
                            HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                            sheet.addValidationData(dataValidation);
                        }
                    }
                    if (PRODUCT_BATCH.equals(fieldName)) {
                        String[] pici = new String[]{PiCiHao};
                        // 设置下拉控制范围
                        CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                        // 下拉框内容
                        DVConstraint constraint = DVConstraint.createExplicitListConstraint(pici);
                        // 绑定下拉框和作用区域
                        HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                        sheet.addValidationData(dataValidation);
                    } else if (PRODUCT_NUMBER.equals(fieldName) && !"4".equals(CengJi)) {
                        String[] shiwu = new String[]{ShiWuHao};
                        // 设置下拉控制范围
                        CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                        // 下拉框内容
                        DVConstraint constraint = DVConstraint.createExplicitListConstraint(shiwu);
                        // 绑定下拉框和作用区域
                        HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                        sheet.addValidationData(dataValidation);
                    } else if (PRODUCT_NUMBER.equals(fieldName) && "4".equals(CengJi)) {
                        if (stringListMap == null) {
                            List<String> shiWuForPiCi = (List<String>) QuerySql("PHYSICAL_OBJECT_SINGLE_MACHINE", "PiCiShiWu", PiCiHao);
                            if (shiWuForPiCi != null) {
                                //由于下拉框数据太多超出poi最大长度，使用新建sheet进行处理，具体注释参考下面检查测试项的
                                String[] Pshiwu = shiWuForPiCi.toArray(new String[shiWuForPiCi.size()]);
                                Sheet hidden = wb.createSheet("PRODUCT_NUMBER");
                                Cell cell1 = null;
                                for (int i = 0; i <Pshiwu.length ; i++) {
                                    String name = Pshiwu[i];
                                    Row row1 = hidden.createRow(i);
                                    cell1 = row1.createCell(0);
                                    cell1.setCellValue(name);
                                }
                                Name nameCell = wb.createName();
                                nameCell.setNameName("PRODUCT_NUMBER");
                                nameCell.setRefersToFormula("PRODUCT_NUMBER"+"!$A$1:$A$" + Pshiwu.length);
                                DVConstraint constraint = DVConstraint.createFormulaListConstraint("PRODUCT_NUMBER");
                                CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                                DataValidation dataValidation = new HSSFDataValidation(regions,constraint);
//                                wb.setSheetHidden(1,true);
                                wb.getSheetAt(0).addValidationData(dataValidation);
                            }
                        }
                    }
                } else {
                    //判断是否是总装直属件
                    if ("0".equals(isAlone)) {
                        com.alibaba.fastjson.JSONObject jsonbatchNoNodeInfo = JSON.parseObject(batchNoNodeInfo == null ? "" : batchNoNodeInfo);
                        //判断是否关联实物
                        String isphy = jsonObject.get("linkPhysical") == null ? "" : jsonObject.get("linkPhysical").toString();
                        //总装直属件的批次
                        String ZPiCi = jsonbatchNoNodeInfo.get("batchNo") == null ? "" : jsonbatchNoNodeInfo.get("batchNo").toString();
                        //总装直属件的图号/型号
                        String ZTuHao = jsonbatchNoNodeInfo.get("drawingNo") == null ? "" : jsonbatchNoNodeInfo.get("drawingNo").toString();
                        //如果型号策划中未配置产品代号
                        if (stringListMap == null) {
                            if ("产品代号".equals(fieldName)) {
                                String[] xinghao = new String[]{ZTuHao};
                                // 设置下拉控制范围
                                CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                                // 下拉框内容
                                DVConstraint constraint = DVConstraint.createExplicitListConstraint(xinghao);
                                // 绑定下拉框和作用区域
                                HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                                sheet.addValidationData(dataValidation);
                            }
                        }
                        if ("产品批次".equals(fieldName)) {
                            String[] faci = new String[]{ZPiCi};
                            // 设置下拉控制范围
                            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                            // 下拉框内容
                            DVConstraint constraint = DVConstraint.createExplicitListConstraint(faci);
                            // 绑定下拉框和作用区域
                            HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                            sheet.addValidationData(dataValidation);
                        }
                        if (PRODUCT_NUMBER.equals(fieldName) && "1".equals(isphy)) {
                            //如果是总装直属件，产品编号（实物号）为treeName
                            ShiWuHao = jsonObject.get("treeName") == null ? "" : jsonObject.get("treeName").toString();
                            String[] shiwu = new String[]{ShiWuHao};
                            // 设置下拉控制范围
                            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                            // 下拉框内容
                            DVConstraint constraint = DVConstraint.createExplicitListConstraint(shiwu);
                            // 绑定下拉框和作用区域
                            HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                            sheet.addValidationData(dataValidation);
                        }
                        if (PRODUCT_NUMBER.equals(fieldName) && "0".equals(isphy)) {
                            String[] shiwu = new String[]{ZPiCi};
                            // 设置下拉控制范围
                            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                            // 下拉框内容
                            DVConstraint constraint = DVConstraint.createExplicitListConstraint(shiwu);
                            // 绑定下拉框和作用区域
                            HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                            sheet.addValidationData(dataValidation);
                        }
                    } else {
                        //如果型号策划中未配置产品代号
                        if (stringListMap == null) {
                            if ("产品代号".equals(fieldName)) {
                                String[] xinghao = new String[]{XingHao};
                                // 设置下拉控制范围
                                CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                                // 下拉框内容
                                DVConstraint constraint = DVConstraint.createExplicitListConstraint(xinghao);
                                // 绑定下拉框和作用区域
                                HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                                sheet.addValidationData(dataValidation);
                            }
                        }
                        if ("产品批次".equals(fieldName)) {
                            String[] faci = new String[]{PiCiHao};
                            // 设置下拉控制范围
                            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                            // 下拉框内容
                            DVConstraint constraint = DVConstraint.createExplicitListConstraint(faci);
                            // 绑定下拉框和作用区域
                            HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                            sheet.addValidationData(dataValidation);
                        }
                        if (PRODUCT_NUMBER.equals(fieldName)) {
                            ShiWuHao = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
                            String[] shiwu = new String[]{ShiWuHao};
                            // 设置下拉控制范围
                            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                            // 下拉框内容
                            DVConstraint constraint = DVConstraint.createExplicitListConstraint(shiwu);
                            // 绑定下拉框和作用区域
                            HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                            sheet.addValidationData(dataValidation);
                        }
                    }
                }
                //判断是否为日期类型，增加日期类型8位数批注
                if ("date".equalsIgnoreCase(dataType)) {
                    HSSFPatriarch hp = sheet.createDrawingPatriarch();
                    HSSFComment hc = hp.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 6, 8));
                    hc.setString(new HSSFRichTextString("该字段为日期类型元素，填写数据时，需规范化8位日期格式！例如：20230703"));
                    hc.setAuthor("MeritData");
                    cell.setCellComment(hc);
                    ilist.add(index);
                }
                //判断是否在型号策化中配置了匹配关系
                if (stringListMap != null && stringListMap.size() != 0) {
                    //型号策划配置的数据
                    List<String> procode = stringListMap.get("procode");
                    List<String> testrequire = stringListMap.get("testrequire");
                    List<String> testitem = stringListMap.get("testitem");
                    if (PRODUCT_CODE.equals(remark)) {
                        for (String s : procode) {
                            list = new ArrayList<>();
                            for (int j = 0; j < editFields.size(); j++) {
                                list.add("");
                            }
                            list.set(index, s);
                            result.add(list);
                        }
                        //输入框
                    } else if (REQUIRED_VAL.equals(remark)) {
                        for (int i = 0; i < testrequire.size(); i++) {
                            //如果客户维护的要求值实例数据量小于或等于产品代号（其上层list）时，则正查部分添加；如果大于，则不能set需要新加list
                            if (testrequire.size() <= procode.size()) {
                                //添加第二个示例数据元素，list1，后续再加 list2 3 4....
                                List<String> list1 = result.get(i);
                                list1.set(index, testrequire.get(i));
                                result.set(i, list1);
                            } else {//如果要求值大于产品代号数量时
                                //小于或等于正常set
                                if (i <= procode.size() - 1) {
                                    //添加第二个示例数据元素，list1，后续再加 list2 3 4....
                                    List<String> list1 = result.get(i);
                                    list1.set(index, testrequire.get(i));
                                    result.set(i, list1);
                                } else {//大于则需要add
                                    list = new ArrayList<>();
                                    for (int j = 0; j < editFields.size(); j++) {
                                        list.add("");
                                    }
                                    list.set(index, testrequire.get(i));
                                    result.add(list);
                                }
                            }
                        }
                    } else if (TESTING_ITEMS.equals(remark)) {
                        // 设置下拉控制范围
                        CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                        // 下拉框内容
                        DVConstraint constraint = DVConstraint.createExplicitListConstraint(testitem.toArray(new String[testitem.size()]));
                        // 绑定下拉框和作用区域
                        HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                        sheet.addValidationData(dataValidation);
                        for (int i = 0; i < testitem.size(); i++) {
                            //目前设计的系统，在型号策划中要求了三个都必填，所以返回的3个list数量都一样，不存在长短判断
                            //若后期客户要求不能必填，存在list长短不一的情况就需要结合上两个的判断进行判断逻辑添加
//                        if (testitem.size() <= testrequire.size()) {
                            //添加第三个示例数据元素，list2，后续再加 list2 3 4....
                            List<String> list2 = result.get(i);
                            list2.set(index, testitem.get(i));
                            result.set(i, list2);
//                        }
                        }
                    }
                } else if (lookUpForFegin.size() != 0) {
                    if (stringListMap == null || stringListMap.size() == 0) {
                        if (TESTING_ITEMS.equals(remark)) {
                            String[] lookUp = lookUpForFegin.toArray(new String[lookUpForFegin.size()]);
                            //由于poi的下拉框最大长度只有255超过就会报错，则使用将数据
                            //将下拉框数据放到新的sheet里，然后excl通过新的sheet数据加载下拉框数据
                            Sheet hidden = wb.createSheet("TESTING_ITEMS");
                            //创建单元格对象
                            Cell cell1 = null;
                            //遍历数据，将数据放到sheet单元格中
                            for (int i = 0; i <lookUp.length ; i++) {
                                String name = lookUp[i];
                                Row row1 = hidden.createRow(i);
                                cell1 = row1.createCell(0);
                                cell1.setCellValue(name);
                            }
                            //创建名称，可被其他单元格引用
                            Name nameCell = wb.createName();
                            nameCell.setNameName("TESTING_ITEMS");
                            //设置名称引用的公式
                            nameCell.setRefersToFormula("TESTING_ITEMS"+"!$A$1:$A$" + lookUp.length);
                            DVConstraint constraint = DVConstraint.createFormulaListConstraint("TESTING_ITEMS");
                            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
                            DataValidation dataValidation = new HSSFDataValidation(regions,constraint);
//                            wb.setSheetHidden(2,true);
                            wb.getSheetAt(0).addValidationData(dataValidation);
                        }
                    }
                }
                // 判断是否设置非空字段， 需要将背景色改为淡蓝色
                if (ruleList.contains(field.getModelFieldId())) {
                    titleStype.setFillForegroundColor(new HSSFColor.LIGHT_BLUE().getIndex());
                    titleStype.setFillBackgroundColor(new HSSFColor.LIGHT_BLUE().getIndex());
                    titleStype.setFillPattern(FillPatternType.SPARSE_DOTS);
                }
                cell.setCellStyle(titleStype);
                index++;
                if (field.getModelFieldConfigDTO() != null) {
                    ModelFieldConfigDTO modelFieldConfig = field.getModelFieldConfigDTO();
                    // 如果该字段关联模型或者字典，需要添加批注
                    if (modelFieldConfig.getRefObj() != null && org.apache.commons.lang3.StringUtils.isNotBlank(modelFieldConfig.getRefObj())) {
                        HSSFPatriarch hp = sheet.createDrawingPatriarch();
                        HSSFComment hc = hp.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 6, 8));
                        hc.setString(new HSSFRichTextString("导入将以引用字段为准，如果想要实现按显示字段导入，必须删除引用字段列。"));
                        hc.setAuthor("MeritData");
                        cell.setCellComment(hc);
                    }
                    // 关联了字段需要增加一列主显示字段
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(modelFieldConfig.getRefObj())
                            && modelFieldConfig.getModelFieldShowConfigList().size() > 0) {
                        List<ModelFieldShowConfigDTO> modelFieldShowConfigList = modelFieldConfig.getModelFieldShowConfigList();
                        for (ModelFieldShowConfigDTO modelFieldShowConfig : modelFieldShowConfigList) {
                            // 找到主显示字段
                            if (modelFieldShowConfig.getMainShow().equals(YesOrNoEnum.YES.getValue())) {
                                HSSFCell mainCell = row.createCell(index);
                                mainCell.setCellValue(fieldName + "_" + modelFieldShowConfig.getShowFieldDTO().getBusiName());
                                HSSFCellStyle mainTitleStype = wb.createCellStyle();
                                mainTitleStype.setAlignment(HorizontalAlignment.CENTER);
                                mainCell.setCellStyle(mainTitleStype);
                            }
                        }
                    }
                }
            }
            HSSFCell cell = row.createCell(index);
            cell.setCellValue(SystemFieldEnum.SECRETLEVEL.getName());
            HSSFCellStyle titleStype = wb.createCellStyle();
            titleStype.setAlignment(HorizontalAlignment.CENTER);
            cell.setCellStyle(titleStype);
            // 设置下拉控制范围
            CellRangeAddressList regions = new CellRangeAddressList(1, 99999, index, index);
            // 下拉框内容
            String[] strings = getCurrentUserSecret();
            DVConstraint constraint = DVConstraint.createExplicitListConstraint(strings);
            // 绑定下拉框和作用区域
            HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
            sheet.addValidationData(dataValidation);
            // 遍历集合数据，产生示例数据行
            if (result.size() > 0) {
                for (int i = 0; i < result.size(); i++) {
                    for (Integer I : ilist) {
                        result.get(i).set(I, null);
                    }
                    result.set(i, result.get(i));
                }
                //matchindex为示例数据的行标
                int matchindex = 1;
                for (List<String> m : result) {
                    row = sheet.createRow(matchindex);
                    int cellIndex = 0;
                    for (Object str : m) {
                        //示例数据判空，为空的是上述逻辑筛选出需要特殊处理的数据
                        if (str == null) {
                            cellIndex++;
                            continue;
                        }
                        cell = row.createCell((short) cellIndex);
                        cell.setCellValue(str.toString());
                        cellIndex++;
                    }
                    matchindex++;
                }
            }
        } catch (Exception e) {
            LOGGER.error("批量下载" + modelName + "错误！", e);
            e.printStackTrace();
        }
    }

    /**
     * 获取用户有管理权限的模板中的编辑字段，过滤掉停用的字段
     *
     * @param modelId
     * @return
     * @throws Exception
     */
    @Override
    public List<ModelVerFieldDTO> getModelEditFields(String modelId) {
        try {
            // 获取用户有管理权限的模板中的编辑字段，过滤掉停用的字段
            SelectFieldConditionDTO selectFieldConditionDTO = new SelectFieldConditionDTO();
            selectFieldConditionDTO.setFilterModelTemplate(Boolean.TRUE);
            selectFieldConditionDTO.setFilterStopStatus(Boolean.TRUE);
            selectFieldConditionDTO.setModelId(modelId);
            selectFieldConditionDTO.setTemplateType(ModelPrivilegeEnum.MANAGE.getValue());
            selectFieldConditionDTO.setTemplateFieldType(TemplateFieldTypeEnum.EDIT.getValue());
            ResultBody<List<ModelVerFieldDTO>> resultBody = iDataPacketClient.getFieldByCondition(selectFieldConditionDTO);
            return resultBody.getData();
        } catch (Exception e) {
            LOGGER.error("获取用户有管理权限的模板中的编辑字段，过滤掉停用的字段失败！getModelEditFields");
            return null;
        }
    }

    /**
     * 获取小于等于当前用户的密级集
     *
     * @return
     */
    @Override
    public String[] getCurrentUserSecret() {
        //得到用户当前的密级
        String userSecretlevel = SessionManager.getSecretLevel();
        //根据code值获取密级的数据字典
        List<TreeModel<Object>> list = iDatamationsClient.listLookupsEnable("secretlevel");
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(list)) {
            //获取系统接口的全部信息，通过密级字典的code值获取密级的对应关系
            List<TreeModel<Object>> secretLevel = list.get(0).getChildren();
            List<String> userSecretList = new ArrayList<>();
            for (TreeModel<Object> treeModel : secretLevel) {
                Map<String, Object> map = (Map<String, Object>) treeModel.getAttributes();
                if (Integer.parseInt(userSecretlevel) >= Integer.parseInt(String.valueOf(map.get("code")))) {
                    userSecretList.add(treeModel.getText());
                }
            }
            return userSecretList.toArray(new String[userSecretList.size()]);
        }
        return new String[0];
    }

    @Override
    public void zipFile(HttpServletResponse response, Map<String, byte[]> byteList, String filename) {
        try {
            //定义下载的类型，标明是zip文件
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8") + ".zip");
            response.setContentType("application/octet-stream");
            OutputStream outputStream = response.getOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            Iterator<String> iterator = byteList.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                byte[] value = byteList.get(key);
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(key));
                    zipOutputStream.write(value);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error("写入zip文件失败！");
                }
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("压缩文件失败！");
        }
    }

    @Override
    public String replaceAllBlank(String str) {
        String s = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            /*\n 回车(\u000a)
            \t 水平制表符(\u0009)
            \s 空格(\u0008)
            \r 换行(\u000d)*/
            Matcher m = p.matcher(str);
            s = m.replaceAll("");
        }
        return s;
    }

    /**
     * 设置模型版本属性别名
     *
     * @param modelVerField
     * @return
     */
    @Override
    public ModelVerFieldDTO setModelFieldAlias(ModelVerFieldDTO modelVerField) {
        String fieldName = modelVerField.getFieldName();
        String aliasName = "";
        if (YesOrNoEnum.YES.getValue().equals(modelVerField.getSystemField())) {
            aliasName = SYSTEM_FIELD_PREFIX + fieldName;
        } else {
            aliasName = BUSI_FIELD_PREFIX + fieldName;
        }
        modelVerField.setAliasName(aliasName);
        return modelVerField;
    }

    @Override
    public String getAllFiled(String nodeId, String moduleCode, String tableName, String modelId) {
        List<ModuleColumnConfig> moduleCurateList = moduleInfoConfigService.getModuleCurate(nodeId, moduleCode, tableName, modelId);
        //用来接收excl表头的list
        StringBuilder filter = new StringBuilder();
        for (ModuleColumnConfig moduleColumnConfig : moduleCurateList) {
            //过滤导出模板，去除掉文件属性的字段
            if (null != moduleColumnConfig.getFormFieldVO() && null != moduleColumnConfig.getFormFieldVO().getModelFieldConfigDTO()) {
                ModelFieldConfigDTO modelFieldConfigDTO = moduleColumnConfig.getFormFieldVO().getModelFieldConfigDTO();
                //如果是文件
                if (null != modelFieldConfigDTO && modelFieldConfigDTO.getFile() == Constants.IS_FILE) {
                    continue;
                }
            }
            String ColumnName = "F_" + moduleColumnConfig.getFieldName();
            filter.append(ColumnName).append(",");
        }
        filter = new StringBuilder(filter.substring(0, filter.length() - 1));
        return filter.toString();
    }

    @Override
    public void exportAllData(HttpServletRequest request, HttpServletResponse res, String physicalCode, String attributes, String treeName) {
        try {
            //查字典
            List<TreeModel<Object>> list = clientService.listLookupsEnable("secretlevel");
            List secretList = list.get(0).getChildren();
            Map<String, String> secretMap = new HashMap<>();
            Map<String, String> secretCodeMap = new HashMap<>();
            secretList.stream().forEach(r -> {
                Map attr = (Map) ((TreeModel) r).getAttributes();
                secretMap.put(attr.get("name").toString(), attr.get("code").toString());
                secretCodeMap.put(attr.get("code").toString(), attr.get("name").toString());
            });
            //获取Nacos配置文件的密级(默认密级)
            String maxSecretLevel = NacosServerTool.getParamVal(NACOS_DATA_ID, NACOS_GROUP, NACOS_DEFAULT_SECRET);
            String secretTemp;

            //提取前端传来的信息
            JSONObject jsonObject = JSONObject.parseObject(attributes);
            String firstNode = jsonObject.get("firstNode") == null ? "" : jsonObject.get("firstNode").toString();
            String thirdlyNode = jsonObject.get("thirdlyNode") == null ? "" : jsonObject.get("thirdlyNode").toString();
            String fourthlyNode = jsonObject.get("fourthlyNode") == null ? "" : jsonObject.get("fourthlyNode").toString();
            //前端定义的nodeId有问题，为上级id，与我们需要的不符，在这里进行修改
            String nodeId = jsonObject.get("tempID").toString();
            String tempId = jsonObject.get("tempID") == null ? "" : jsonObject.get("tempID").toString();
            Map<String, String> map = new HashMap();
//            map.put("page", "1");
//            map.put("rows", "500");
            map.put("nodeId", nodeId);
            map.put("tempId", tempId);
            map.put("getFormFlag", "1");
            map.put("treeName", treeName);
            //查询出所有模板信息
            //List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(map);
            //查询出所有模板信息（带过滤）
            List<ModuleConfigDto> moduleManageInfoList = getModel(map);
            //储存excl的map，用以组装zip
            Map<String, byte[]> byteMap = new HashMap<>();
            StringBuilder stringBuilder = new StringBuilder();
            assert moduleManageInfoList != null;
            for (ModuleConfigDto moduleManageDto : moduleManageInfoList) {
                //todo 无法Autowired
                ModelDataExportParam requestData = new ModelDataExportParam();
                //获取当前模型id
                String modelId = moduleManageDto.getModelInfo();
                //获取当前模型表名
                String tableName = moduleManageDto.getTableName();
                //获取当前模型编码
                String code = moduleManageDto.getCode();
                //获取当前模型名称  excl的名称
                String name = moduleManageDto.getName();
                name = name.replaceAll("[\\s\\\\/:\\*\\?\\\"<>\\|]", "");
                Map<String, Object> filedMap = new HashMap<>();
                String allFiled = getAllFiled(nodeId, code, tableName, modelId);
                filedMap.put("queryField", allFiled);
                requestData.setModelId(modelId);
                requestData.setParam(filedMap);
                requestData.setMultiFlag(false);
                requestData.setOperType("dataManage");
                requestData.setMaskFlag(true);
                String AllData = iDatamationsClient.exportData(requestData, modelId, nodeId, attributes, physicalCode);
                JSONObject jsonObject1 = JSON.parseObject(AllData);
                if (jsonObject1 == null) {
                    LOGGER.error("批量导出该模型失败！失败模型：" + name + ",请联系管理员检查当前模型导出是否正常！");
                    stringBuilder.append("批量导出该模型失败！失败模型：").append(name).append(".xls，请联系管理员检查当前模型导出功能是否正常！\r\n");
                    continue;
                }
                //获取字段信息
                JSONArray modelVerFieldDtos = (JSONArray) jsonObject1.get("modelVerFieldDtos");
                //字段升序排序非空修改
                modelVerFieldDtos.sort(Comparator.comparing(obj -> ((JSONObject) obj).getInteger("sortNumber") == null ? 999999 : ((JSONObject) obj).getInteger("sortNumber")));
                //获取数据
                JSONArray datas = (JSONArray) jsonObject1.get("datas");
                List<String> header = new ArrayList<>();
                List<String> AList = new ArrayList<>();
                for (Object modelVerFieldDto : modelVerFieldDtos) {
                    Map<String, String> map1 = (Map<String, String>) modelVerFieldDto;
                    String busiName = map1.get("busiName");
                    String aliasName = map1.get("aliasName");
                    header.add(busiName);
                    AList.add(aliasName);
                }
                List<List<String>> result = new ArrayList<>();
                secretTemp = NacosServerTool.getParamVal(NACOS_DATA_ID, NACOS_GROUP, NACOS_DEFAULT_SECRET);
                for (Object data : datas) {
                    List<String> resultt = new ArrayList<>();
                    if (secretTemp.compareTo(secretMap.get(((Map<String, String>) data).get("S_M_SYS_SECRETLEVEL"))) < 0) {
                        secretTemp = secretMap.get(((Map<String, String>) data).get("S_M_SYS_SECRETLEVEL"));
                    }
                    if (maxSecretLevel.compareTo(secretTemp) < 0) {
                        maxSecretLevel = secretTemp;
                    }
                    for (Object modelVerFieldDto : modelVerFieldDtos) {
                        boolean flag = false;
                        String aliasName = ((Map<String, String>) modelVerFieldDto).get("aliasName");
                        String dataType = ((Map<String, String>) modelVerFieldDto).get("dataType");
                        for (String s : ((Map<String, String>) data).keySet()) {
                            if (s.equals(aliasName)) {
                                flag = true;
                                //批量导出,如果是data类型字段，格式统一去掉连接符‘-’
                                if ("date".equalsIgnoreCase(dataType)) {
                                    resultt.add(StringUtils.isNotEmpty(((Map<String, String>) data).get(s)) ? ((Map<String, String>) data).get(s).replaceAll("-", "") : "");
                                } else {
                                    resultt.add(((Map<String, String>) data).get(s));
                                }
                            }
                        }
                        if (flag) {
                            continue;
                        }
                        resultt.add("");
                    }
                    result.add(resultt);
                }

                String secretLevelName = getTextByCodeLookUp(list, secretTemp);
                String secretStr = "(" + secretLevelName + ")";
                HSSFWorkbook wb = new HSSFWorkbook();
                iFlowBomSheetDataInter.exportExcel(wb, 0, name, header, result);
                //将HSSFWorkbook转成byte[]
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    wb.write(bos);
                    byte[] bytes = bos.toByteArray();
                    byteMap.put(name + secretStr + ".xls", bytes);
                } finally {
                    bos.close();
                    wb.close();
                }
            }
            if (stringBuilder.length() > 0) {
                byte[] errb = stringBuilder.toString().getBytes();
                byteMap.put("最终批量导出失败模型信息.txt", errb);
            }
            //导出的zip名
            //导出的zip名
            String filename = "选择节点有误请确认";
            String maxSecretLevelName = getTextByCodeLookUp(list, maxSecretLevel);
            String secretStr = "(" + maxSecretLevelName + ")";
            if (STAND_ALONE.equals(firstNode)) {
                filename = thirdlyNode + "_" + fourthlyNode + "_批量导出数据" + secretStr;
            } else {
                if (treeName == null || "null".equals(treeName) || "".equals(treeName)) {
                    filename = thirdlyNode + "_" + fourthlyNode + "_批量导出数据" + secretStr;
                } else {
                    filename = thirdlyNode + "_" + fourthlyNode + "_" + treeName + "_批量导出数据" + secretStr;
                }
            }
            zipFile(res, byteMap, filename);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("批量导出数据失败！");
        }
    }

    /**
     * @param treeLevelList       待组装的树结构
     * @param levelNum            待组装的树结构总层级
     * @param treeLeafDataList    树结构叶子节点数据
     * @param currLevel           当前树结构的层级
     * @param currTreeDto         当前节点的数据对象
     * @param attributesMap       节点属性
     * @param batchNoNodeInfoAttr 按批次号审批需要的节点属性
     * @return
     */
    private static List<TreeDto> getChildrenTree(Map<String, String> attributesMap, List<String> treeLevelList, int levelNum, List<TreeDto> treeLeafDataList, int currLevel, TreeDto currTreeDto, Map<String, String> batchNoNodeInfoAttr) {
        List<TreeDto> treeNodeList = new ArrayList<>();
        //若层级小于总层级，则递归
        if (currLevel < levelNum) {
            //获取层级的set集合
            Set<String> set = new HashSet<>();
            for (String l : treeLevelList) {
                if (currLevel == 0) {
                    set.add(l.split(",")[currLevel]);
                } else {
                    if (currTreeDto.getText().equals(l.split(",")[currLevel - 1])) {
                        set.add(l.split(",")[currLevel]);
                    }
                }
            }
            if (currLevel + 1 < levelNum) {
                for (String s : set) {
                    TreeDto treeDto = new TreeDto();
                    treeDto.setId(UUID.randomUUID().toString());
                    if (currTreeDto != null) {
                        treeDto.setPid(currTreeDto.getId());
                    } else {
                        treeDto.setPid(treeLeafDataList.get(0).getPid());
                    }
                    treeDto.setText(s);

                    List<TreeDto> childrenTree = getChildrenTree(attributesMap, treeLevelList, levelNum, treeLeafDataList, currLevel + 1, treeDto, batchNoNodeInfoAttr);
                    treeDto.setChildren(childrenTree);
                    if (null != attributesMap) {
                        if (attributesMap.containsKey(s)) {
                            QueryNodeDTO queryNodeDTO = JSONObject.parseObject(attributesMap.get(s), QueryNodeDTO.class);
                            //总装直属件 关联到实物的
                            if (StringUtils.isNotEmpty(queryNodeDTO.getIsAlone()) && queryNodeDTO.getIsAlone().equals("0")) {
                                treeDto.setAttributes(queryNodeDTO);
                            } else {
//                                queryNodeDTO.setNodeLevel("D-4");
                                queryNodeDTO.setAloneLevel("4");
                                //控制是否显示按钮
                                queryNodeDTO.setIsAlone("1");
                            }
                            treeDto.setAttributes(queryNodeDTO);
                        }
                    }
                    if (null != batchNoNodeInfoAttr) {
                        if (batchNoNodeInfoAttr.containsKey(s)) {
                            BatchNoNodeInfo batchNoNodeInfo = JSONObject.parseObject(batchNoNodeInfoAttr.get(s), BatchNoNodeInfo.class);
                            if (StringUtils.isEmpty(batchNoNodeInfo.getPhysicalNo())) {
                                batchNoNodeInfo.setPhysicalNo("");
                            }
                            treeDto.setBatchNoNodeInfo(batchNoNodeInfo);
                        }
                    }
                    treeNodeList.add(treeDto);
                }
            } else {
                for (String s : set) {
                    for (TreeDto s2 : treeLeafDataList) {
                        if (s.equals(s2.getText())) {
                            s2.setPid(currTreeDto.getId());
                            if (null != attributesMap) {
                                if (attributesMap.containsKey(s)) {
                                    QueryNodeDTO queryNodeDTO = JSONObject.parseObject(attributesMap.get(s), QueryNodeDTO.class);
//                                    queryNodeDTO.setNodeLevel("D-5");
                                    //总装直属件 关联到实物的
                                    if (StringUtils.isNotEmpty(queryNodeDTO.getIsAlone()) && queryNodeDTO.getIsAlone().equals("0")) {
                                        s2.setAttributes(queryNodeDTO);
                                    } else {
                                        queryNodeDTO.setAloneLevel("5");
                                        //控制是否显示按钮
                                        queryNodeDTO.setIsAlone("1");
                                        s2.setAttributes(queryNodeDTO);
                                    }
                                }
                            }
                            treeNodeList.add(s2);
                            break;
                        }
                    }
                }
            }
        }
        return treeNodeList;
    }

    /**
     * 总装直属件有实物号和无实物号的数据合并
     *
     * @param zzzsjNoPhysChildrenTree  总装直属件无实物号
     * @param zzzsjHasPhysChildrenTree 总装直属件有实物号
     * @return
     */
    public void mergeZzzs(List<TreeDto> zzzsjNoPhysChildrenTree, List<TreeDto> zzzsjHasPhysChildrenTree) {
        for (TreeDto zzzsjNoPhys : zzzsjNoPhysChildrenTree) {
            Boolean flag = false;
            for (TreeDto zzzsjHasPhys : zzzsjHasPhysChildrenTree) {
                if (zzzsjNoPhys.getText().equals(zzzsjHasPhys.getText())) {
                    flag = true;
                    List<TreeDto> clist1 = zzzsjNoPhys.getChildren();
                    List<TreeDto> clist2 = zzzsjHasPhys.getChildren();
                    mergeZzzs(clist1, clist2);
                }
            }
            if (!flag) {
                zzzsjHasPhysChildrenTree.add(zzzsjNoPhys);
            }
        }
    }

    public String getModelColor(String template, String treeName, JSONObject attributes) {
        String color = "";
        //根据bom名称和modelid获取表名
        String tableName = !"".equals(template) ? iDataPacketClient.getModuleInfo(template).getTableName() : "";
        //查询单机批次下的数据
        List<Object[]> verList = new ArrayList<>();
        String firstNode = attributes.get("firstNode") == null ? "" : attributes.get("firstNode").toString();
        String aloneLevel = attributes.get("aloneLevel") == null ? "" : attributes.get("aloneLevel").toString();
        if ("4".equals(String.valueOf(attributes.get("nodeLevel"))) && "1".equals(String.valueOf(attributes.get("nodeType"))) ||
                (firstNode.equals(FlowStructureEnum.STANDALONE.getValue()) && "4".equals(aloneLevel))) {
            verList = (List<Object[]>) this.QuerySql(tableName, "aloneBatch", String.valueOf(attributes.get("fourthlyNode")));
        } else {
            //查询表数据
            verList = (List<Object[]>) this.QuerySql(tableName, "all", treeName);
        }
        //查询表字段
        List<Object[]> cloList = (List<Object[]>) this.QuerySql(tableName, "column", treeName);
        //将List<Object[]>转换成List<Map<String, String>>方便取数据
        List<Map<String, String>> verlm = this.ListForMap(verList, cloList);
        int size = verlm.size();
        //如果无数据则返回黄色
        if (size == 0) {
            color = FlowColorEnum.RED.getCode();
            return color;
        } else {
            color = FlowColorEnum.GREE.getCode();
            return color;
        }
    }

    private List<ModuleConfigDto> getModel(Map<String, String> map) {
        //获取颜色
//        JSONObject jsonObject = JSON.parseObject(map.get("attributes") == null ? "" : map.get("attributes").toString());
        String treeName = map.get("treeName");
        try {
            //TODO::远程接口查询数仓建设中生效的模板信息
            //查询出所有模板信息
            List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(map);
            //TODO::查询TM_MODEL_CURATE表是否初始化模板信息
            //根据该节点下的模板信息
            String nodeId = map.get("tempId") == null ? "" : map.get("tempId");
            List<ModuleCurate> moduleCurateByNodeIdList = modulePlanService.getModuleCurateByNodeId(nodeId);
            List<ModuleCurate> moduleCurateArrayList = new ArrayList<>();
            //没有任何配置的情况
            if (moduleCurateByNodeIdList.size() == 0) {
                List<ModuleConfigDto> moduleCurateList = new ArrayList<>();
                moduleManageInfoList.forEach(model -> {
                    //初始化数据
                    ModuleCurate moduleCurate = new ModuleCurate();
                    moduleCurate.setIsPackage("1");
                    moduleCurate.setCode(model.getCode());
                    moduleCurate.setModelInfo(model.getModelInfo());
                    moduleCurate.setNodeId(nodeId);
                    moduleCurateArrayList.add(moduleCurate);
                    //返回给前端的数据
                    ModuleConfigDto moduleConfigDto = new ModuleConfigDto();
                    BeanUtils.copyProperties(model, moduleConfigDto);
                    moduleConfigDto.setIsPackage("1");
                    moduleCurateList.add(moduleConfigDto);
                });
                moduleCurateRepository.saveAll(moduleCurateArrayList);
                List<ModuleConfigDto> list = moduleCurateList;
//                list.forEach(module -> {
//                    //1表示要查询对象表单下是否有数据,分系统和模块
//                    if (StringUtils.isNotBlank(map.get("getFormFlag")) && map.get("getFormFlag").equals("1")) {
////                        String subSystemColor = this.getModelColor(module.getModelInfo(), treeName,jsonObject);
//                        module.setModelColor(subSystemColor);
//                    }
//                });
                return list;
            }
            //部分配置的情况，需要从801库中查询已经配置的项，若配置为是则展示，不展示配置为否的模板
            List<ModuleConfigDto> moduleCurateList = new ArrayList<>();
            //循环远程结果集
            moduleManageInfoList.forEach(model -> {
                //记录此数据是否在801库中不存在
                boolean flag = true;
                ModuleConfigDto moduleConfigDto = new ModuleConfigDto();
                BeanUtils.copyProperties(model, moduleConfigDto);
                moduleConfigDto.setIsPackage("1");
                for (int i = 0; i < moduleCurateByNodeIdList.size(); i++) {
                    ModuleCurate moduleCurate = moduleCurateByNodeIdList.get(i);
                    //如果当前数据存在且IsPackage为是则展示
                    if (moduleCurate.getCode().equals(moduleConfigDto.getCode())) {
                        flag = false;//只展示配置为是的
                        if ("1".equals(moduleCurate.getIsPackage())) {
                            moduleCurateList.add(moduleConfigDto);
                        }
                        break;
                    }
                }
                //flag=true则是当前801库里没有，需要添加至801库
                if (flag) {
                    ModuleCurate moduleCurateUpdate = new ModuleCurate();
                    moduleCurateUpdate.setIsPackage("1");
                    moduleCurateUpdate.setCode(model.getCode());
                    moduleCurateUpdate.setModelInfo(model.getModelInfo());
                    moduleCurateUpdate.setNodeId(nodeId);
                    moduleCurateArrayList.add(moduleCurateUpdate);
                    //同理，添加至数据库页面也需要展示词此条新数据
                    moduleCurateList.add(moduleConfigDto);
                }
            });
            if (moduleCurateArrayList.size() > 0) {
                moduleCurateRepository.saveAll(moduleCurateArrayList);
            }
            List<ModuleConfigDto> list = moduleCurateList;
//            list.forEach(module -> {
//                //1表示要查询对象表单下是否有数据,分系统和模块
//                if (StringUtils.isNotBlank(map.get("getFormFlag")) && map.get("getFormFlag").equals("1")) {
//                    String subSystemColor = this.getModelColor(module.getModelInfo(), treeName,jsonObject);
//                    module.setModelColor(subSystemColor);
//                }
//            });
            return list;
        } catch (Exception e) {
            LOGGER.error("错误信息:" + e.getMessage());
            return null;
        }
    }

    /**
     * 根据实物号、图号和批次号获取 一条单机信息
     *
     * @param physicalNo
     * @param drawingNo
     * @param batchNo
     * @return
     */
    private Map<String, Object> getDJInfo(String physicalNo, String drawingNo, String batchNo) {
        //查询条件封装
        JSONObject filterMap = new JSONObject();
        //如果实物号不为null，则根据实物号，批次号和图号，则一定获取的是一条信息
        filterMap.put("F_PHYSICAL_NO", physicalNo);
        filterMap.put("F_DRAWING_NO", drawingNo);
        filterMap.put("F_BATCH_NO", batchNo);
        filterMap.put("F_WHETHER_PHYSICAL_OBJECTS_MANAGED", "1");
        filterMap.put("F_STAND_ALONE_IDENTIFICATION", "1");
        ModelDataQueryParamVO paramVO = new ModelDataQueryParamVO();
        paramVO.setQueryFilter(filterMap.toJSONString());
        List<Map<String, Object>> djInfoSearch = iDatamationsClient.getDJInfoSearch(paramVO);
        if (CollectionUtils.isEmpty(djInfoSearch) || djInfoSearch.size() != 1) {
            LOGGER.error("未根据实物号、批次号、图号获取到一条单机信息");
            return null;
        }
        return djInfoSearch.get(0);
    }

    public String getTextByCodeLookUp(List<TreeModel<Object>> list, String code) {
        if (com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(list)) {
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
}
