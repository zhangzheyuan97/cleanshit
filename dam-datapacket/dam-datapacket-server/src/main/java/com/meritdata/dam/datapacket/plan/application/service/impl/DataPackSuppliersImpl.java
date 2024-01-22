package com.meritdata.dam.datapacket.plan.application.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.application.dao.ModuleAllLevelSuppliersRepository;
import com.meritdata.dam.datapacket.plan.application.entity.ModuleAllLevelSuppliers;
import com.meritdata.dam.datapacket.plan.application.entity.QModuleAllLevelSuppliers;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackSuppliersService;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Service
public class DataPackSuppliersImpl implements IDataPackSuppliersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataPackSuppliersImpl.class);

    @Autowired
    ModuleAllLevelSuppliersRepository moduleAllLevelSuppliersRepository;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Override
    public ResultBody<GridView> querySuppliersList(Map map, String productHierarchy, String productCode, String productName, String classification, String drawingCode,String importance) {

        QModuleAllLevelSuppliers moduleAllLevelSuppliers = QModuleAllLevelSuppliers.moduleAllLevelSuppliers;
        Predicate predicate = null;
        if (StrUtil.isNotBlank(productHierarchy)) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.productHierarchy.like("%" + productHierarchy + "%"));
        }
        if (StrUtil.isNotBlank(productCode)) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.productCode.like("%" + productCode + "%"));
        }
        if (StrUtil.isNotBlank(productName)) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.productName.like("%" + productName + "%"));
        }
        if (StrUtil.isNotBlank(classification)) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.classification.like("%" + classification + "%"));
        }
        if (StrUtil.isNotBlank(drawingCode)) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.drawingCode.like("%" + drawingCode + "%"));
        }
        if (StrUtil.isNotBlank(importance)) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.importance.like("%" + importance + "%"));
        }
        //页码
        Integer pageNum = Integer.parseInt(map.get("page").toString());
        //每页显示个数
        Integer sizeNum = Integer.parseInt(map.get("rows").toString());
        Pageable pageable = PageRequest.of(pageNum - 1, sizeNum);
        List<ModuleAllLevelSuppliers> list = jpaQueryFactory.selectFrom(moduleAllLevelSuppliers)
                .where(predicate)
                .orderBy(moduleAllLevelSuppliers.createtime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long count = jpaQueryFactory.select(moduleAllLevelSuppliers.productCode).from(moduleAllLevelSuppliers).where(predicate).fetchCount();
        return ResultBody.success(new GridView<>(list, count));
    }

    @Override
    public void exportData(HttpServletResponse response, JSONObject param) {
        QModuleAllLevelSuppliers moduleAllLevelSuppliers = QModuleAllLevelSuppliers.moduleAllLevelSuppliers;
        Predicate predicate = null;
        //产品层级
        if (StrUtil.isNotBlank(param.get("productHierarchy").toString())) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.productHierarchy.like("%" + param.get("productHierarchy").toString() + "%"));
        }
        //产品统计编码
        if (StrUtil.isNotBlank(param.get("productCode").toString())) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.productCode.like("%"+ param.get("productCode").toString() +"%"));
        }
        //产品名称
        if (StrUtil.isNotBlank(param.get("productName").toString())) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.productName.like("%" + param.get("productName").toString() + "%"));
        }
        //类别
        if (StrUtil.isNotBlank(param.get("classification").toString())) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.classification.like("%"+ param.get("classification").toString() +"%"));
        }
        //图代号
        if (StrUtil.isNotBlank(param.get("drawingCode").toString())) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.drawingCode.like("%"+ param.get("drawingCode").toString() +"%"));
        }
        //重要程度
        if (StrUtil.isNotBlank(param.get("importance").toString())) {
            predicate = ExpressionUtils.and(predicate, moduleAllLevelSuppliers.importance.like("%" + param.get("importance").toString() + "%"));
        }


        List<ModuleAllLevelSuppliers> exportDataList = jpaQueryFactory.selectFrom(moduleAllLevelSuppliers)
                .orderBy(moduleAllLevelSuppliers.createtime.desc())
                .where(predicate)
                .fetch();

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("全级次供应商清单");
        HSSFRow headRow = sheet.createRow(0);
        //表头数据
        List<String> headList = Lists.newArrayList("产品层级", "产品统计编码", "产品名称","类别", "重要程度", "图代号", "上一级输入要求", "产品通用规范",
                "相关专用规范", "外包方", "供应商单位名称", "企业统一社会信用代码", "风险要素", "供应商\"次\"", "特殊说明");

        for (int i = 0; i < headList.size(); i++) {
            headRow.createCell(i).setCellValue(headList.get(i));
        }
        if (exportDataList.size() > 0) {
            //表格行数据
            exportDataList.stream().forEach(suppliers -> {
                HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
                for (int i = 0; i < headList.size(); i++) {
                    HSSFCell cell = row.createCell(i);
                    sheet.autoSizeColumn(i);
                    switch (i) {
                        case 0:
                            cell.setCellValue(suppliers.getProductHierarchy());  //产品层级
                            break;
                        case 1:
                            cell.setCellValue(suppliers.getProductCode());      //产品统计编码
                            break;
                        case 2:
                            cell.setCellValue(suppliers.getProductName());      //产品名称
                            break;
                        case 3:
                            cell.setCellValue(suppliers.getClassification());       //类别
                            break;
                        case 4:
                            cell.setCellValue(suppliers.getImportance());           //重要程度
                            break;
                        case 5:
                            cell.setCellValue(suppliers.getDrawingCode());             //图代号
                            break;
                        case 6:
                            cell.setCellValue(suppliers.getRequirement());              //上一级输入要求
                            break;
                        case 7:
                            cell.setCellValue(suppliers.getProudctSpecification());     //产品通用规范
                            break;
                        case 8:
                            cell.setCellValue(suppliers.getSpecialSpecifications());       //相关专用规范
                            break;
                        case 9:
                            cell.setCellValue(suppliers.getFirstParty());           //外包方
                            break;
                        case 10:
                            cell.setCellValue(suppliers.getPartyB());               //供应商单位名称
                            break;
                        case 11:
                            cell.setCellValue(suppliers.getUnifiedCorporateSocialCreditCode());         //企业统一社会信用代码
                            break;
                        case 12:
                            cell.setCellValue(suppliers.getRiskElements());             //风险要素
                            break;
                        case 13:
                            cell.setCellValue(suppliers.getSupplier());             //供应商"次"
                            break;
                        case 14:
                            cell.setCellValue(suppliers.getSpecialInstructions());          //特殊说明
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        try {
            String fileName = URLEncoder.encode("全级次供应商清单.xls", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            response.flushBuffer();
            wb.write(bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (Exception e) {
            LOGGER.error("导出Excel表格失败，异常：" + e);
        }


    }
}
