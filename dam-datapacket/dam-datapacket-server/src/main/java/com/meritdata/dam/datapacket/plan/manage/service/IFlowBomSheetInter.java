package com.meritdata.dam.datapacket.plan.manage.service;

import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetDataEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.request.ApproveVersionRequrst;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.ApproveVersionResponse;
import com.meritdata.dam.datapacket.plan.manage.entity.response.StartFlowListResponse;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;

import javax.imageio.stream.IIOByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * 流程bom表单
 */

public interface IFlowBomSheetInter {


    /**
     * 保存流程bom和模板的关系
     *
     * @param bomNameList  bom 名称
     * @param templateList 模板名称id
     * @param bussinessId  流程实例id
     */
    void save(List<String> bomNameList, List<String> templateList, Map<String,String> nodIds,long bussinessId);

    /**
     * 保存流程bom和模板的关系
     * @param bomNameList  bom 名称
     * @param templateList 模板名称id
     * @param bussinessId  流程实例id
     *  @param nodNames  节点名称
     */
    List<FlowBomSheetEntity> save(List<String> bomNameList, List<String> templateList, Map<String,String> nodIds,long bussinessId, Map<String,String> nodNames);

    /**
     * 批量保存数据
     *
     * @param flowBomSheetEntityList
     */
    void saveAllAndFlush(List<FlowBomSheetEntity> flowBomSheetEntityList);

    /**
     * 根据模板的关键字key查询流程实例的编号
     *
     * @param templateKey
     * @return 流程实例列表
     */
    List<Long> findBusinessIdListByTemplate(String templateKey);



    /**
     * 根据模板的关键字key查询流程实例的编号
     *
     * @param modelInfo
     * @return 流程实例列表
     */
    List<Long> findBusinessIdListByTemplate(List<String> modelInfo);



    /**
     * 根据模板的关键字key查询流程bom和模板清单
     *
     * @param templateKey
     * @return
     */
    List<FlowBomSheetEntity> findListByTemplate(String templateKey);

    List<FlowBomSheetEntity> findListByBussinessId(String bussinessId);

    /**
     * 根据流程实力id获取实体bom列表
     * @param bussinessId
     * @return
     */
    List<String> findBomListByBussinessId(String bussinessId);
    /**
     * 根据流程实力id获取实体Bom和表单
     * @param bussinessId
     * @return
     */
    List<FlowBomSheetEntity>  getBomSheetList(String bussinessId,String bom);

    List<FlowBomSheetDataEntity>  getBomSheetDataList(String bussinessId, String bom);

    /**
     * 获取流程的实例id
     * @param request
     * @return
     */
    List<Long> getBussinessId(ApproveVersionRequrst request);

    /**
     * 构建流程返回的信息
     * @param bussinessIds
     * @param list
     * @return
     */
    List<ApproveVersionResponse> getApproveVersionResponse(List<Long> bussinessIds, List<MainFormDTO> list,List<ApproveVersionResponse> approveVersionResponseList,ApproveVersionRequrst request);

    void setBomAndTemplate(List<StartFlowListResponse> responseList);

    /**
     * 验证数据
     * @param startFlowRequest
     * @return
     */
    ResultBody<List<String>> validate(StartFlowRequest startFlowRequest,List<MainFormDTO> mainFormDTO);

    /**
     * 修改数据状态
     * @param startFlowRequest
     */
    void chageDataState(StartFlowRequest startFlowRequest,List<MainFormDTO> mainFormDTO,String  bussinessId);



    /**
     * 流程手动结束
     * @param bussinessId
     */
    void setFlowOver(String bussinessId);



    /**
     * 流程正常结束
     * @param bussinessId
     */
    void setFlowSuccessOver(String bussinessId);

    /**
     * 找流程中的最后一个
     * @param bom
     * @param bom1
     */
    FlowBomSheetEntity findLastModelByBomAndTemplete(String bom, String bom1);

    /**
     * 获取数据的列表
     * @param template  modelinfo
     *  @param bom 实物号码
     * @return
     */
    ResultBody dataListManage(String template,String bom);

    /**
     * 删除
     * @param templateId
     * @param bom
     * @param bussinessId
     */
    void deleteBomSheet(String templateId, String bom, long bussinessId);

    /**
     * 流程异常结束，数据状态恢复
     * @param bussinessId
     */
    void  recoverDataState(String bussinessId );

    /**
     * 流程正常结束，数据状态恢复
     * @param bussinessId
     */
    void  FlowOverDataState(String bussinessId );

    ResultBody<List<String>> validate(StartFlowRequest startFlowRequest,  List<FlowBomSheetEntity> flowBomSheetEntityList, List<ModuleManageDto> templeteList,Map<String,Boolean>  isNotOverMap);

    void chageDataStateNew(Map<String, List<Map<String, Object>>> bomTemMap, List<FlowBomSheetEntity> flowBomSheetEntityList);
}
