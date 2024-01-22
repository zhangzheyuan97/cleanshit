package com.meritdata.dam.datapacket.plan.application.controller;


import cn.hutool.json.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.dam.datapacket.plan.application.service.IDataPackSuppliersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/datapacket/suppliers")
@Api(value = "全级次供应商清单Api接口", tags = {"全级次供应商清单Api接口"})
public class DataPackLevelSuppliersController {

    @Autowired
    IDataPackSuppliersService iDataPackSuppliersService;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "全级次供应商清单表格数据", notes = "全级次供应商清单表格数据")
    public ResultBody<GridView> allLevelSuppliersList(@ApiParam(name = "page", value = "页数", required = true, type = "string") @RequestParam String page,
                                                      @ApiParam(name = "rows", value = "行数", required = true, type = "string") @RequestParam String rows,
                                                      @ApiParam(name = "productHierarchy", value = "产品层级", required = false, type = "string") @RequestParam(required = false) String productHierarchy,
                                                      @ApiParam(name = "productCode", value = "产品统计编码", required = false, type = "string") @RequestParam(required = false) String productCode,
                                                      @ApiParam(name = "productName", value = "产品名称", required = false, type = "string") @RequestParam(required = false) String productName,
                                                      @ApiParam(name = "classification", value = "类别", required = false, type = "string") @RequestParam(required = false) String classification,
                                                      @ApiParam(name = "drawingCode", value = "图号", required = false, type = "string") @RequestParam(required = false) String drawingCode,
                                                      @ApiParam(name = "importance", value = "重要程度", required = false, type = "string") @RequestParam(required = false) String importance) {
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

        return iDataPackSuppliersService.querySuppliersList(map, productHierarchy, productCode, productName, classification, drawingCode,importance);
    }

    @RequestMapping(value = "/exportData", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "导出", notes = "导出")
    public void exportData(@RequestBody JSONObject param, HttpServletResponse response) throws IOException {
        iDataPackSuppliersService.exportData(response, param);
    }
}
