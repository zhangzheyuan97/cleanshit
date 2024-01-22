package com.meritdata.dam.datapacket.plan.manage.service;

import com.meritdata.dam.datapacket.plan.manage.entity.FlowApproveEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.request.AppriovaNode;

import java.util.List;

/**
 * 流程创建
 */

public interface IFlowApproveInter {

    /**
     * 批量保存
     * @param startFlowListRequest
     * @return
     */
    /**
     * 批量保存放啊
     *
     * @return
     */
    List<FlowApproveEntity> saveAllAndFlush(List<FlowApproveEntity> entityList);

    /**
     * 批量保存流程的审核人员
     * @param appriovaNode 审批节点对象
     * @param bussinessId  流程实例编号
     * @return
     */
    void save(AppriovaNode appriovaNode, long bussinessId);

    void update(AppriovaNode appriovaNode, Long bussinessId);
    void delete(Long bussinessId);

    /**
     * 获取表的主键
     * @return
     */
    String getUUID();

    /**
     * 根据businessid和流程node 获取审批人员code
     * @param businessId 流程实例编号
     * @param step 用户code
     * @return
     */
    List<String> getCustomUserCode(String businessId, int step);


    /**
     * 根据businessid和流程node 获取审批人员id
     * @param businessId 流程实例编号
     * @param step 用户code
     * @return
     */
    public List<String> getCustomUserId(String businessId, int step);

    /**
     *
     * @param nodeNames  流程审核节点
     * @param bussinessId  流程实例编号
     */
    String setNodeAppriovaName(String nodeNames, Long bussinessId);

    /**
     * 根据流程实例id删除数据
     *
     * @param bussinessId 流程实例编号
     * @return
     */
    void deleteByBussinessId(long bussinessId);


    /**
     * 根据id删除数据
     *
     * @param id 流程实例编号
     * @return
     */
     void  deleteById(String id);
}
