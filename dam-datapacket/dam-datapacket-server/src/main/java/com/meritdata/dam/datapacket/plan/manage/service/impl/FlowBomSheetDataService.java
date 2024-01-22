package com.meritdata.dam.datapacket.plan.manage.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.meritdata.cloud.base.entity.Emp;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.utils.SessionUserListUtils;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.common.service.nacos.NacosServerTool;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.acquistion.vo.PackGroupFileVO;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackGroupService;
import com.meritdata.dam.datapacket.plan.application.service.impl.DataPackGroupImpl;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowBomSheetDataDao;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetDataEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.QFlowBomSheetDataEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.QFlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetDataInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetInter;
import com.meritdata.dam.datapacket.plan.model.entity.ModuleColumnConfig;
import com.meritdata.dam.datapacket.plan.model.service.IModuleInfoConfigService;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.utils.CommUtil;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.TempleteUtil;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import com.meritdata.dam.entity.metamanage.ModelFieldConfigDTO;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import feign.Response;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description:
 **/
@Service
public class FlowBomSheetDataService implements IFlowBomSheetDataInter {

    private static final Logger logger = LoggerFactory.getLogger(FlowBomSheetDataService.class);

    @Value("${CLOUD_HOME:cloud_home}")
    private String CLOUD_HOME;

    private static final String FILE_PATH = "/file/version_package/";

    @Autowired
    IFlowBomSheetDataDao iFlowBomSheetDataDao;

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    /**
     * 流程bom和表单的关系
     */
    @Autowired
    IFlowBomSheetInter flowBomSheetInter;

    @Autowired
    com.meritdata.dam.datapacket.plan.client.IDatamationsClient idatamationsClient;

    @Autowired
    IDataPacketClient client;

    @Autowired
    IModuleManageService moduleManageService;

    @Autowired
    private IModuleInfoConfigService moduleInfoConfigService;

    @Autowired
    private TempleteUtil templeteUtil;

    @Autowired
    IDataPackGroupService iDataPackGroupService;

    @Autowired
    private IDatamationsClient iDatamationsClient;

    @Autowired
    private IMaintainService iMaintainService;

    @Autowired
    SessionUtils sessionUtils;


    @Override
    public void save(FlowBomSheetDataEntity entity) {
        entity.setId(CommUtil.getUUID());
        iFlowBomSheetDataDao.saveAndFlush(entity);
    }

    @Override
    public void saveAllAndFlush(List<FlowBomSheetDataEntity> flowBomSheetDataEntityList) {

        List<FlowBomSheetDataEntity> list = new ArrayList<>();
        flowBomSheetDataEntityList.forEach(model -> {
            model.setId(CommUtil.getUUID());
            list.add(model);
        });
        iFlowBomSheetDataDao.saveAllAndFlush(list);
    }

    @Override
    public List<String> findListByBomAndTempleteAndbusinessId(String bom, String templete, Long businessId) {

        QFlowBomSheetDataEntity qFlowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
        Predicate predicate = qFlowBomSheetDataEntity.bussinessId.eq(businessId);
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.bomName.eq(bom));
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.template.eq(templete));
        List<FlowBomSheetDataEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetDataEntity)
                .where(predicate)
                .fetchResults().getResults();
        List<String> collect = entityList.stream().map(FlowBomSheetDataEntity::getDataId).collect(Collectors.toList());
        return collect;
    }


    @Override
    public List<String> findListBybatchNoAndIssueNoAndDrawingNoAndModel(String templete, Long businessId, String batchNo, String issueNo, String drawingNo, String model) {
        QFlowBomSheetDataEntity qFlowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
        Predicate predicate = qFlowBomSheetDataEntity.bussinessId.eq(businessId);
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.template.eq(templete));
        if (StringUtils.isNotEmpty(batchNo)) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.batchNo.eq(batchNo));
        }
        if (StringUtils.isNotEmpty(issueNo)) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.issueNo.eq(issueNo));
        }
        if (StringUtils.isNotEmpty(drawingNo)) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.drawingNo.eq(drawingNo));
        }
        if (StringUtils.isNotEmpty(model)) {
            predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.model.eq(model));
        }
        List<FlowBomSheetDataEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetDataEntity)
                .where(predicate)
                .fetchResults().getResults();
        List<String> collect = entityList.stream().map(FlowBomSheetDataEntity::getDataId).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<FlowBomSheetDataEntity> findListByBusinessId(Long businessId) {

        QFlowBomSheetDataEntity qFlowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
        Predicate predicate = qFlowBomSheetDataEntity.bussinessId.eq(businessId);
        List<FlowBomSheetDataEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetDataEntity)
                .where(predicate)
                .fetchResults().getResults();
        return entityList;
    }


    /**
     * 初始化审核数据的id到表内
     *
     * @param bomNameList
     * @param templateList
     * @param bussinessId
     */
    @Override
    public List<FlowBomSheetDataEntity> initDate(List<String> bomNameList, List<String> templateList, long bussinessId) {
        List<FlowBomSheetDataEntity> flowBomSheetDataEntityList = new ArrayList<>();

        bomNameList.forEach(bom -> {
            templateList.forEach(templateId -> {

                Map<String, String> map = new HashMap<>();
                map.put("bom", bom);
                map.put("template", templateId);

                //根据模板code获取模板下的所有数据
                List<Map<String, Object>> DJdataAllList = idatamationsClient.querySupportingListByBomAndTemplete(map);

                /**
                 * 根据bom和template查询数据，查询不到则删除对应的 bom和template关系
                 */
                if (DJdataAllList.size() == 0) {
                    flowBomSheetInter.deleteBomSheet(templateId, bom, bussinessId);
                }

                //最高版本数据
                List<Map<String, Object>> maxVersion = DJdataAllList.stream().filter(mode ->
                        mode.get("S_M_SYS_MAXVERSION").toString().equals("1"))
                        .collect(Collectors.toList());
                //最高版本编辑中数据
                List<Map<String, Object>> edit = maxVersion.stream().filter(mode ->
                        mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("2") && (null == mode.get("F_IsApproval")
                                || StringUtils.isEmpty(mode.get("F_IsApproval").toString()))
                ).collect(Collectors.toList());

                //查询生效的数据
                List<Map<String, Object>> effect = maxVersion.stream().filter(mode ->
                        mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("1")
                ).collect(Collectors.toList());

                List<String> ids = new ArrayList<>();
                edit.forEach(model -> {
                    ids.add(model.get("F_M_SYS_ID").toString());
                });

                List<String> idsEffect = new ArrayList<>();
                effect.forEach(model -> {
                    idsEffect.add(model.get("F_M_SYS_ID").toString());
                });

                ids.forEach(sysId -> {
                    FlowBomSheetDataEntity build = FlowBomSheetDataEntity.builder()
                            .id(CommUtil.getUUID())
                            .bussinessId(bussinessId)
                            .template(templateId)
                            .dataId(sysId)
                            .type(0)
                            .bomName(bom).build();
                    flowBomSheetDataEntityList.add(build);
                });

                idsEffect.forEach(sysId -> {
                    FlowBomSheetDataEntity build = FlowBomSheetDataEntity.builder()
                            .id(CommUtil.getUUID())
                            .bussinessId(bussinessId)
                            .template(templateId)
                            .dataId(sysId)
                            .type(1)
                            .bomName(bom).build();
                    flowBomSheetDataEntityList.add(build);
                });
            });
        });
        saveAllAndFlush(flowBomSheetDataEntityList);
        return flowBomSheetDataEntityList;


    }

    /**
     * 获取审批的数据
     *
     * @param businessId
     * @param modelId
     * @param batchNoNodeInfo
     * @return
     */
    @Override
    public List<Map<String, Object>> getApproveDataList(String businessId, String modelId, BatchNoNodeInfo batchNoNodeInfo) {
        //获取数据id
        QFlowBomSheetDataEntity flowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
        Predicate predicate = flowBomSheetDataEntity.bussinessId.eq(Long.parseLong(businessId))
                .and(flowBomSheetDataEntity.template.eq(modelId));
        if (StringUtils.isNotEmpty(batchNoNodeInfo.getBatchNo())) {
            predicate = ExpressionUtils.and(predicate, flowBomSheetDataEntity.batchNo.eq(batchNoNodeInfo.getBatchNo()));
        }
        if (StringUtils.isNotEmpty(batchNoNodeInfo.getModel())) {
            predicate = ExpressionUtils.and(predicate, flowBomSheetDataEntity.model.eq(batchNoNodeInfo.getModel()));
        }
        if (StringUtils.isNotEmpty(batchNoNodeInfo.getDrawingNo())) {
            predicate = ExpressionUtils.and(predicate, flowBomSheetDataEntity.drawingNo.eq(batchNoNodeInfo.getDrawingNo()));
        }
        if (StringUtils.isNotEmpty(batchNoNodeInfo.getIssueNo())) {
            predicate = ExpressionUtils.and(predicate, flowBomSheetDataEntity.issueNo.eq(batchNoNodeInfo.getIssueNo()));
        }
        List<String> sysIds = jpaQueryFactory.select(flowBomSheetDataEntity.dataId)
                .from(flowBomSheetDataEntity)
                .where(predicate)
                .fetch();
        if (CollectionUtils.isEmpty(sysIds)){
            return new ArrayList<>();
        }
        //实物号
        String physicalNo = batchNoNodeInfo.getPhysicalNo();
        //分系统，发次本级的业务数据
        ModelDataQueryParamVO modelDataQueryParamVO = new ModelDataQueryParamVO();
        JSONObject jsonObject = new JSONObject();
        //再根据实物号查询数据(如果选择了实物号节点)
        if (StringUtils.isNotEmpty(physicalNo)) {
            jsonObject.put("F_PhysicalCode", physicalNo);
        }
        //根据数据id查询
        JSONObject sysIdJson = new JSONObject();
        sysIdJson.put("$in", sysIds.toArray());
        //根据数据id
        jsonObject.put("F_M_SYS_ID", sysIdJson);
        modelDataQueryParamVO.setQueryFilter(jsonObject.toJSONString());
        return iDatamationsClient.packetDataListAll("", modelId, modelDataQueryParamVO);
    }

    @Override
    public Boolean dataVersionDownload(HttpServletResponse response, HashMap attributes, BatchNoNodeInfo batchNoNodeInfo) {
        try {
            //定义excl的对象
            HSSFWorkbook workbook = new HSSFWorkbook();
            //该模型的文件属性（用以存储如果包含附件的数据中的附件信息）
            List<PackGroupFileVO> files = new ArrayList<>();
            //字段中文名称
            List<String> columnl = new ArrayList<>();
            //存储文件类型表头的集合
            List<String> filekey = new ArrayList<>();
            //这个list用来储存有文件数据的集合
            List<String> havefilekey = new ArrayList<>();
            //这个list用来记录date类型字段的集合
            List<String> haveDateKey = new ArrayList<>();
            //定义excl数据list
            List<List<String>> result = new ArrayList<>();
            //这个map用来接收有附件的数据
            List<Map<String, Object>> mapList = new ArrayList<>();
            //获取当前登录人的密级
            Emp emp = sessionUtils.getEmp();
            String grade = emp.getGrade();
            if (StringUtils.isEmpty(grade)) {
                //如果没有获取到当前登录人的密级，则退回
                return false;
            }
            //从attributes中获取一些必要的信息
            String nodeId = attributes.get("nodeId").toString();
            String moduleName = attributes.get("moduleName").toString();
            String moduleCode = attributes.get("moduleCode").toString();
            String modelId = attributes.get("moduleId").toString();
            String businessId = attributes.get("businessId").toString();
            String bomname = attributes.get("text").toString();
            HashMap attrmap = (HashMap) attributes.get("attributes");
            String fourthlyNode = attrmap.get("fourthlyNode").toString();
            String tempID = attrmap.get("tempID").toString();
            //拼接excle 名字
            String eName = fourthlyNode + "_" + bomname + "_" + moduleName + "_" + businessId;
            //查询出excl表头信息
            List<ModuleColumnConfig> dynamicHeadlist = moduleInfoConfigService.getModuleCurate(nodeId, moduleCode, null, modelId);
            for (ModuleColumnConfig moduleColumnConfig : dynamicHeadlist) {
                //判断该表头字段是否是文件
                if (null != moduleColumnConfig.getFormFieldVO() && null != moduleColumnConfig.getFormFieldVO().getModelFieldConfigDTO()) {
                    ModelFieldConfigDTO modelFieldConfigDTO = moduleColumnConfig.getFormFieldVO().getModelFieldConfigDTO();
                    //如果是文件
                    if (null != modelFieldConfigDTO && modelFieldConfigDTO.getFile() == Constants.IS_FILE) {
                        String filefieldName = "F_" + moduleColumnConfig.getFieldName();
                        filekey.add(filefieldName);
                    }
                }
                String fieldName = "F_" + moduleColumnConfig.getFieldName();
                if ("date".equalsIgnoreCase(moduleColumnConfig.getDataType())) {
                    haveDateKey.add(fieldName);
                }
                String columnName = moduleColumnConfig.getColumnName();
                columnl.add(columnName);
            }
            //在excl表头最后添加一列，用来放密级
            columnl.add("密级");
            //获取密级映射关系
            List<Map<String, Object>> gradeList = iMaintainService.getGradeList();
            Map<String, String> lookupByPlan = iMaintainService.getLookupByPlan(modelId, tempID);
            //获取最大密级 (下载文件数据应包含密级)
            String maxSecretLevel = "";
            //查询当前businessId下这个模型的流程template、bomName、dataId
//            List<FlowBomSheetDataEntity> listByBusinessId = findEntityByBomAndTempleteAndbusinessId(bomname, modelId, Long.valueOf(businessId));
            List<Map<String, Object>> allDataByModelInfoAndBomIds = getApproveDataList(businessId, modelId, batchNoNodeInfo);
            for (Map<String, Object> map : allDataByModelInfoAndBomIds) {
                boolean flag = false;
                List<String> datalist = new ArrayList<>();
                //判断该发次下的模型有没有配置字典，若配置 则需要过滤
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
                if (flag){
                    continue;
                }
                //获取当前数据的涉密程度
                String m_sys_secretlevel = map.get("S_M_SYS_SECRETLEVEL").toString();

                //如果当前数据的涉密程度高于当前登录人时，则不下载该条数据
                if (Integer.parseInt(grade) < Integer.parseInt(m_sys_secretlevel)) {
                    continue;
                }
                String s_m_sys_versionstatus = map.get("S_M_SYS_VERSIONSTATUS").toString();
                if (!"1".equals(s_m_sys_versionstatus) && !"4".equals(s_m_sys_versionstatus)) {//1.生效2.编辑3.审批中4.历史0.停用
                    continue;
                }

                if (StringUtils.isNotEmpty(m_sys_secretlevel)) {
                    //要在包含此数据的情况下判断最大密级
                    if (StringUtils.isEmpty(maxSecretLevel) || Integer.parseInt(m_sys_secretlevel) > Integer.parseInt(maxSecretLevel)) {
                        //给个初始值
                        maxSecretLevel = m_sys_secretlevel;
                    }
                }

                //按照中台数据格式进行数据处理
                List<String> delKeys = map.keySet().stream().filter(item -> item.contains("M_SYS_") || item.contains("m_sys_")).collect(Collectors.toList());
                delKeys.forEach(map::remove);
                for (ModuleColumnConfig moduleColumnConfig : dynamicHeadlist) {
                    String key = "F_" + moduleColumnConfig.getFieldName();
                    String value;
                    if (map.get(key) != null) {
                        value = map.get(key).toString();
                        //判断该字段是否是字典，如果是字典，则将字典编码转成字典名称
                        value = iDataPackGroupService.setLookUpValue(moduleColumnConfig, value);
                        //如果循环到这里的key，是有附件的，则将这个key对应的数据放到mapList中（这个map用来接收有附件的数据）
                        if (filekey.contains(key)) {
                            Map<String, Object> mapfil = new HashMap<>();
                            mapfil.put(key, map.get(key));
                            mapList.add(mapfil);
                            havefilekey.add(key);
                            //带附件的将附件名展示到excel
                            List<PackGroupFileVO> packGroupFileVOS = iDataPackGroupService.setPackGroupFile(map, key);
                            List<String> fileNames = packGroupFileVOS.stream().map(item -> item.getFileName() + "." + item.getFileType()).collect(Collectors.toList());
                            value = StringUtils.join(fileNames, ",");
//                                value = "文件";
                        }
                        //如果是date格式，去掉-，格式修改为yyyyMMdd格式导出
                        if (haveDateKey.contains(key)) {
                            value = value.replaceAll("-", "");
                        }
                    } else {
                        value = "";
                    }
                    datalist.add(value);
                }
                //数据密级
                for (Map<String, Object> stringObjectMap : gradeList) {
                    if (MapUtils.getString(stringObjectMap, "code").equals(m_sys_secretlevel)) {
                        datalist.add(datalist.size(), MapUtils.getString(stringObjectMap, "name"));
                    }
                }
                result.add(datalist);
            }
            //给默认密级
            if (StringUtils.isEmpty(maxSecretLevel)) {
                //获取配置文件的密级
                maxSecretLevel = NacosServerTool.getParamVal(Constants.NACOS_DATA_ID, Constants.NACOS_GROUP, Constants.NACOS_DEFAULT_SECRET);
            }
            String maxSecretLevelName = getSecretLevelName(gradeList, maxSecretLevel);
            eName = eName + "(" + maxSecretLevelName + ")";
            //下载结构化数据的excl
            exportExcel(workbook, 0, bomname, columnl, result);
            //如果这条数据包含附件,则下载附件
            if (mapList.size() > 0) {
                ZipOutputStream zipOutputStream = null;
                for (int i = 0; i < mapList.size(); i++) {
                    List<PackGroupFileVO> physicalFiles = iDataPackGroupService.setPackGroupFile(mapList.get(i), havefilekey.get(i));
                    List<String> collect = physicalFiles.stream().map(item -> item.getFileName() + "." + item.getFileType()).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(collect)) {
                        files.addAll(physicalFiles);
                    }
                }
                String filename = URLEncoder.encode(eName, "UTF-8");
                response.setHeader("Content-disposition", "attachment;filename=" + filename + ".zip");
                //定义下载的类型，标明是excel文件
                response.setContentType("application/octet-stream");
                //如果成功获取到这条附件的信息时，则下载该附件
                if (files.size() > 0) {
                    //存放文件
                    for (PackGroupFileVO file : files) {
                        //文件夹写入
                        if (null == zipOutputStream) {
                            zipOutputStream = new ZipOutputStream(response.getOutputStream());
                        }
                        //文件密级
                        String secretLevelName = getFileSecretByCode(file.getSecretLevel());
                        String fileName = file.getFileName() + "(" + secretLevelName + ")" + "." + file.getFileType();
                        zipOutputStream.putNextEntry(new ZipEntry(fileName));
                        //获取中台服务器的文件
                        Response response1 = iDatamationsClient.getFileByPath(file.getFilePath());
                        Response.Body body = response1.body();
                        InputStream fis = body.asInputStream();
                        if (null == fis) {
                            logger.error("附件【" + file.getFileName() + "】下载失败");
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
                //将excel写入byte数组
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                workbook.write(byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                zipOutputStream.putNextEntry(new ZipEntry(URLDecoder.decode(filename) + ".xls"));
                zipOutputStream.write(bytes);
                zipOutputStream.closeEntry();
                //关闭所有流
                zipOutputStream.flush();
                zipOutputStream.close();
                byteArrayOutputStream.close();
            } else {
                OutputStream output;
                output = response.getOutputStream();
                //清空缓存
                response.reset();
                //定义浏览器响应表头，顺带定义下载名,(中文名需要转义)
                String filename = URLEncoder.encode(eName, "UTF-8");
                response.setHeader("Content-disposition", "attachment;filename=" + filename + ".xls");
                //定义下载的类型，标明是excel文件
                response.setContentType("application/vnd.ms-excel");
                workbook.write(output);
                output.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据密级code获取密级name
     *
     * @param gradeList      密级集合
     * @param maxSecretLevel 密级code
     * @return
     */
    private String getSecretLevelName(List<Map<String, Object>> gradeList, String maxSecretLevel) {
        for (Map<String, Object> item : gradeList) {
            String code = item.get("code").toString();
            if (code.equals(maxSecretLevel)) {
                return item.get("name").toString();
            }
        }
        return "";
    }

    /**
     * 多sheet的excl下载公共方法
     *
     * @param workbook
     * @param sheetNum
     * @param sheetTitle
     * @param headers
     * @param result
     * @throws Exception
     */
    @Override
    public void exportExcel(HSSFWorkbook workbook, int sheetNum, String sheetTitle, List<String> headers, List<List<String>> result) {
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(sheetNum, sheetTitle);
        // 设置表格默认列宽度为2000个字节
        //sheet.setDefaultColumnWidth((short) 2000);//老版本office打开宽度会很大
        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            HSSFCell cell = row.createCell((short) i);
            HSSFRichTextString text = new HSSFRichTextString(headers.get(i));
            cell.setCellValue(text.toString());

            //批量导出文件--全部列设置成普通文本格式
            HSSFCellStyle textStyle = workbook.createCellStyle();
            textStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));
            sheet.setDefaultColumnStyle(i, textStyle);
        }
        // 遍历集合数据，产生数据行
        if (result != null) {
            int index = 1;
            for (List<String> m : result) {
                row = sheet.createRow(index);
                int cellIndex = 0;
                for (Object str : m) {
                    HSSFCell cell = row.createCell((short) cellIndex);
                    cell.setCellValue(str.toString());
                    cellIndex++;
                }
                index++;
            }
        }
    }

    /**
     * 根据bom、templete、businessId查询，返回list实体（满足下载excl）
     *
     * @param bom
     * @param templete
     * @param businessId
     * @return
     */
    @Override
    public List<FlowBomSheetDataEntity> findEntityByBomAndTempleteAndbusinessId(String bom, String templete, Long businessId) {
        QFlowBomSheetDataEntity qFlowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
        QFlowCreateEntity qFlowCreateEntity = QFlowCreateEntity.flowCreateEntity;
        Predicate predicate = qFlowBomSheetDataEntity.bussinessId.eq(businessId);
        Predicate leftOn = qFlowBomSheetDataEntity.bussinessId.stringValue().eq(qFlowCreateEntity.bussinessId);
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.bomName.eq(bom));
        predicate = ExpressionUtils.and(predicate, qFlowBomSheetDataEntity.template.eq(templete));
        predicate = ExpressionUtils.and(predicate, qFlowCreateEntity.flowState.eq("pass"));
        return jpaQueryFactory.selectFrom(qFlowBomSheetDataEntity).leftJoin(qFlowCreateEntity)
                .on(leftOn)
                .where(predicate)
                .fetchResults().getResults();
    }

    @Override
    public List<FlowBomSheetDataEntity> findListByBusinessIdAndType(long businessId, int type) {
        QFlowBomSheetDataEntity qFlowBomSheetDataEntity = QFlowBomSheetDataEntity.flowBomSheetDataEntity;
        Predicate predicate = qFlowBomSheetDataEntity.bussinessId.eq(businessId).and(qFlowBomSheetDataEntity.type.eq(type));
        List<FlowBomSheetDataEntity> entityList = jpaQueryFactory.selectFrom(qFlowBomSheetDataEntity)
                .where(predicate)
                .fetchResults().getResults();
        return entityList;
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
}
