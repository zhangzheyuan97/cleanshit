package com.meritdata.dam.datapacket.plan.application.service;

import cn.hutool.json.JSONObject;
import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IDataPackSuppliersService {
    /**
     * 查询全级次表单数据
     * @param map
     * @param productHierarchy
     * @param productCode
     * @param productName
     * @param classification
     * @param drawingCode
     * @return
     */
    ResultBody<GridView> querySuppliersList(Map map, String productHierarchy, String productCode, String productName, String classification, String drawingCode,String importance);

    /**
     * 导出表格数据
     */
    void exportData(HttpServletResponse response, JSONObject param) throws IOException;
}
