package com.meritdata.dam.datapacket.plan.manage.service;

import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowListRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.request.StartFlowVerifyRequest;
import com.meritdata.dam.datapacket.plan.manage.entity.response.StartFlowListResponse;
import com.meritdata.dam.datapacket.plan.manage.entity.response.VerifyFlowListResponse;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;

import java.util.List;
import java.util.Map;

/**
 * 流程创建
 */

public interface IFlowCreateInter {

    /**
     * 分页查询数据
     *
     * @param startFlowListRequest
     * @return
     */
    GridView<StartFlowListResponse> findByPage(StartFlowListRequest startFlowListRequest);

    /**
     * 找到流程实例里面最大的流程实力编号
     *
     * @return
     */
    long findMaxBussinessId();

    /**
     * 保存
     *
     * @return
     */
    FlowCreateEntity save(FlowCreateEntity entity);

    /**
     * 保存
     *
     * @return
     */
    FlowCreateEntity update(FlowCreateEntity entity);

    /**
     * 批量保存放啊
     *
     * @return
     */
    List<FlowCreateEntity> saveAllAndFlush(List<FlowCreateEntity> entityList);

    /**
     * 判断id的数据是否存在
     *
     * @param id 主键id
     * @return
     */
    boolean existsById(String id);

    /**
     * 判断bussinessId的数据是否存在
     *
     * @param bussinessId 流程实例编号
     * @return
     */
    boolean existsByBussinessId(long bussinessId);

    /**
     * 根据流程实例id删除数据
     *
     * @param bussinessId 流程实例编号
     * @return
     */
    void deleteByBussinessId(long bussinessId);

    /**
     * 判根据主机id删除数据
     *
     * @param id 主键id
     * @return
     */
    void deleteById(String id);


    /**
     * 创建流程
     * @param startFlowRequest  流程创建请求对象
     * @param userId  用户id
     * @param bussinessId  流程实例id
     */
    void save(StartFlowRequest startFlowRequest, String userId, long bussinessId);

    /**
     * 创建流程
     * @param startFlowRequest  流程创建请求对象
     * @param userId  用户id
     * @param bussinessId  流程实例id
     */
    void update(StartFlowRequest startFlowRequest, String userId, String bussinessId);

    /**
     * 设置流程手动结束
     * @param bussinessId  流程实例id
     */
    void setFlowOver(String bussinessId);

    /**
     * 设置流程草稿状态
     * @param bussinessId  流程实例id
     */
    void setFlowDraft(String bussinessId);

    /**
     * 设置流程退回状态
     * @param bussinessId  流程实例id
     */
    void setFlowBack(String bussinessId);

    /**
     * 设置流程正常结束
     * @param bussinessId  流程实例id
     */
    void setFlowPass(String bussinessId);


    /**
     * 设置流程处理中
     * @param bussinessId  流程实例id
     */
    void setFlowTrack(String bussinessId);


    /**
     * 根据流程实例id获取对象
     * @return
     */
    FlowCreateEntity findModelByBussinessId(String bussinessId);

    /**
     * 验证数据是否有权限
     * type=authority  验证权限
     *  type=data  验证数据是不是编辑状态
     *
     * @return
     */
    ResultBody<List<String>> validateAuthority(StartFlowRequest startFlowRequest,String type);


    /**
     * 查找全部未终止的流程
     * @return
     */
    List<FlowCreateEntity> findEntityNotOver();


    /**
     * 判断流程是否结束
     * @param bussinessId
     * @return
     */
       boolean flowIsOver(String bussinessId) ;

    /**
     * 根据流程id查询数据
     * @param startFlowListRequest
     * @param bpmList
     * @return
     */
    GridView<VerifyFlowListResponse> findByPage(StartFlowVerifyRequest startFlowListRequest, List<MainFormDTO>  bpmList);

    boolean findEntityNotOver(long bussinessId);


    ResultBody<List<String>> validateAuthority(Map<String,List<Map<String, Object>>> bomTemMap, String data, List<FlowBomSheetEntity> flowBomSheetEntityList, List<ModuleManageDto> templateList);

    Map<String, Boolean> findEntityNotOverResult(List<FlowBomSheetEntity> flowBomSheetEntityList);

    void saveDraftData(StartFlowRequest startFlowRequest, String userId, long bussinessId);

    void updateDraftData(StartFlowRequest startFlowRequest, long parseLong);
}
