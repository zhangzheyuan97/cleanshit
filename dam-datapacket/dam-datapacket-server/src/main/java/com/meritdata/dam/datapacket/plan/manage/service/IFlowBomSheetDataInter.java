package com.meritdata.dam.datapacket.plan.manage.service;


import com.meritdata.dam.datapacket.plan.acquistion.vo.BatchNoNodeInfo;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetDataEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 流程bom表单
 */

public interface IFlowBomSheetDataInter {


    /**
     * 保存流程bom和模板的关系
     *
     * @param entity bom 名称
     */
    void save(FlowBomSheetDataEntity entity);

    /**
     * 批量保存数据
     *
     * @param flowBomSheetDataEntityList
     */
    void saveAllAndFlush(List<FlowBomSheetDataEntity> flowBomSheetDataEntityList);

    /**
     * 根据模板的关键字key查询流程实例的编号
     *
     * @param bom
     * @param templete
     * @param businessId
     * @return 流程实例列表
     */
    public List<String> findListByBomAndTempleteAndbusinessId(String bom, String templete, Long businessId);

    List<String> findListBybatchNoAndIssueNoAndDrawingNoAndModel(String templete,Long businessId,String batchNo,String issueNo,String drawingNo,String model);

    /*8

     */


    List<FlowBomSheetDataEntity> initDate(List<String> bom, List<String> template, long bussinessId);

    public List<FlowBomSheetDataEntity> findListByBusinessId(Long businessId);

    /**
     * 下载流程表单excl
     *
     * @param response
     * @param attributes
     * @param batchNoNodeInfo
     * @return
     * @throws Exception
     */
    Boolean dataVersionDownload(HttpServletResponse response, HashMap attributes,BatchNoNodeInfo batchNoNodeInfo) throws Exception;

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
    void exportExcel(HSSFWorkbook workbook, int sheetNum, String sheetTitle, List<String> headers, List<List<String>> result);

    /**
     * 根据bom、templete、businessId查询，返回list实体（满足下载excl）
     *
     * @param bom
     * @param templete
     * @param businessId
     * @return
     */
    List<FlowBomSheetDataEntity> findEntityByBomAndTempleteAndbusinessId(String bom, String templete, Long businessId);

    List<FlowBomSheetDataEntity> findListByBusinessIdAndType(long businessId, int i);

//    /**
//     * 查询为pass的流程
//     * @param businessId
//     * @return
//     */
//    public List<FlowCreateEntity> findEntityBybusinessId(String businessId);


    /**
     * 获取审批的数据
     *
     * @param businessId
     * @param modelId
     * @param batchNoNodeInfo
     * @return
     */
    List<Map<String, Object>> getApproveDataList(String businessId, String modelId, BatchNoNodeInfo batchNoNodeInfo);
}
