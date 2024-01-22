package com.meritdata.dam.datapacket.plan.manage.service;

import com.alibaba.fastjson.JSONObject;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetDataEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowBatchRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.ApproveBatch;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import org.checkerframework.checker.units.qual.A;

import java.util.List;
import java.util.Map;

/**
 * @Author fanpeng
 * @Date 2023/7/5
 * @Describe 我的审批树结构
 */
public interface IFlowTreeService {

    /**
     * 获取有权限的树结构
     *
     * @param userId 人员id
     * @return
     */
    List<TreeDto> exhibitionTree(String userId);

    /**
     * 根据型号查询发次及发次下的批次
     *
     * @param classification 类型
     * @param modeNo         型号
     * @param type           分系统、模块
     * @return
     */
    List<ApproveBatch> getBatchNoByModeNo(String classification, String modeNo, String type);

    /**
     * 根据图号查询批次
     *
     * @param classification 类型
     * @param drawingNo      图号
     * @return
     */
    List<ApproveBatch> getBatchNoByDrawingNo(String classification, String drawingNo);

    /**
     * 获取实物号集合
     *
     * @param jsonObjects 右侧列表数据集合
     * @return
     */
    List<ApproveBatch> getPhysicalNo(List<JSONObject> jsonObjects);


    //暂时未使用
    List<String> validateAuthority(Map<String, List<Map<String, Object>>> bomTemMap, String type, List<FlowBomSheetEntity> flowBomSheetEntityList, List<ModuleManageDto> templateList);


    /**
     * 校验审批中数据
     *
     * @return
     */
    List<String> validateApprove();


    /**
     * 创建流程实例
     *
     * @param startFlowBatchRequest 参数对象
     * @param businessId            业务id
     * @return
     */
    void createProcess(StartFlowBatchRequest startFlowBatchRequest, String businessId);

    /**
     * 修改流程实例
     *
     * @param startFlowBatchRequest 参数对象
     * @param businessId            业务id
     * @return
     */
    void updateProcess(StartFlowBatchRequest startFlowBatchRequest, String businessId);

    /**
     * 发起流程
     *
     * @param businessId 业务id
     * @param procDefKey 流程实例id
     * @return
     */
    boolean startProcess(long businessId, String procDefKey);

    /**
     * 保存表单数据
     * @param startFlowBatchRequest  表单数据
     * @param id 业务id
     */
     void saveFormData(StartFlowBatchRequest startFlowBatchRequest, String id);

    /**
     * 初始化bom和表单的关系
     * @param startFlowBatchRequest
     * @param businessId
     * @return
     */
    List<FlowBomSheetEntity> save(StartFlowBatchRequest startFlowBatchRequest,long businessId);


    /**
     * 保存数据与流程关系
     * @param startFlowBatchRequest
     * @param businessId
     * @return
     */
    List<FlowBomSheetDataEntity> saveRelationDataAndProcess(StartFlowBatchRequest startFlowBatchRequest, long businessId);

    void saveDraftData(StartFlowBatchRequest startFlowBatchRequest, String userId, long bussinessId);

    void updateDraftData(StartFlowBatchRequest startFlowBatchRequest, long parseLong);

    /**
     * 根据选中的批次号获取要审批的数据
     * @param approveBatch 选中的批次信息
     * @param modelId 选中的模板id
     * @return
     */
    List<Map<String,Object>> getApproveDataByBatch(ApproveBatch approveBatch,String modelId);


    /**
     * 根据批次号及模型属性获取流程信息
     * @param startFlowBatchRequest 批次号及模型属性
     * @return
     */
    List<FlowBomSheetEntity> getFlowTemplate(StartFlowBatchRequest startFlowBatchRequest);

    /**
     * 查询未办结的流程
     * @return
     */
    List<FlowCreateEntity> findNotOverProcess();

    /**
     * 校验是否有未办理完成的数据，并返回校验结果
     * @param batchNoList 选中的批次集合
     * @param templateList 选中的模板信息
     * @param flowCreateEntityList 未办结的流程
     * @return
     */
    List<String> validateTrackProcess(List<ApproveBatch> batchNoList, List<ModuleManageDto> templateList,List<FlowCreateEntity> flowCreateEntityList);


    /**
     * 获取数据，根据发次/批次信息获取数据并封装至对象中
     * @param startFlowBatchRequest
     * @return
     */
    StartFlowBatchRequest formatApproveBatch(StartFlowBatchRequest startFlowBatchRequest);

    /**
     * 校验是否存在编辑中数据
     * @param batchNoList 批次信息
     * @param templateList 模板信息
     * @return
     */
    Map<String, Object> validateData(List<ApproveBatch> batchNoList, List<ModuleManageDto> templateList);


    /**
     * 修改数据状态 是否审批中为是
     * @param businessId 业务id
     * @param modelIds 模型编码集合
     */
    void changeDataStatus(Long businessId,List<String> modelIds);

    /**
     * 流程结束后，将数据状态改为生效，其他版本改为历史
     * @param businessId
     */
    void setFlowOver(String businessId);

    /**
     * 重置数据审批状态，改为“”
     * @param businessId
     */
    void recoverDataState(String businessId);

}
