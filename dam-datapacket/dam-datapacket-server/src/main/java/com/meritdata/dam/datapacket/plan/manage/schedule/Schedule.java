package com.meritdata.dam.datapacket.plan.manage.schedule;

import cn.hutool.core.util.StrUtil;
import com.meritdata.cloud.bpm.base.dto.MainFormDTO;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.manage.dao.IFlowCreateDao;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.entity.QFlowCreateEntity;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetInter;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowCreateInter;
import com.meritdata.dam.datapacket.plan.manage.service.impl.BpmService;
import com.meritdata.dam.datapacket.plan.utils.enumutil.FlowStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 作为后来接手的开发，发现这种定时调度的方案并不好 并没有真正的解决问题
 *  现在已废弃
 * 建议增加流程中的监听来实现业务侧流程状态的更改
 * 具体建议如下： 正常完结时添加一个线监听器  触发后可以更改为审批通过
 * 驳回的时候给第一个节点添加任务开始 监听器 但是由于发起和退回都会触发，因此还需判断状态来确定是不是驳回 可以用平台的OperationType字段
 * 以上监听能够及时的更改业务侧的流程状态
 *
 * @author： lt.liu
 * 时间：2023/3/23
 * @description:
 **/
@Slf4j
@Component
@EnableScheduling
public class Schedule {

    @Autowired
    BpmService bpmService;

     /**
     * 流程创建表
     */
    @Autowired
    IFlowCreateInter flowCreateInter;

    @Autowired
    IFlowBomSheetInter flowBomSheetInter;

    @Autowired
    IFlowCreateDao flowCreateDao;


//    5分钟
//    @Scheduled(fixedRate = 1000 * 60*1)
//    public void getBpmState() {
        //定时查询状态没有结束的流程
//        List<FlowCreateEntity> entityNotOver = flowCreateInter.findEntityNotOver();
//        log.debug("  数据未结束"+entityNotOver.size());
//        // 这块主要处理流程终止的操作
//        entityNotOver.forEach(flowCreateEntity -> {
//            log.debug("  getBussinessId="+flowCreateEntity.getBussinessId());
//            //查询未结束的流程
//            ResultBody<MainFormDTO> notOverFlows = bpmService.queryFlowInstanceIdByBussinessID(flowCreateEntity.getBussinessId());
//            log.debug("  getBussinessId="+flowCreateEntity.getBussinessId());
//            if (null!=notOverFlows&&
//                    null!=notOverFlows.getData()&&  null!=notOverFlows.getData().getProcDealStatus()){
//            switch (notOverFlows.getData().getProcDealStatus()) {
//                case "draft": { //草稿
//                    log.debug("  draft================");
////                    if (!FlowStateEnum.DRAFT.getCode().equals(flowCreateEntity.getFlowState())) {
//                    // 处理退回的状态 平台的这个字段等于1的时候表示退回的包含(退回拟稿人，退回上一节点操作) 但由于其他操作不会改这个值所以直接就用非空来判断了
//                    if (StrUtil.isNotBlank(notOverFlows.getData().getOperationType())){
////                        flowCreateEntity.setFlowState(FlowStateEnum.DRAFT.getCode());
////                        flowCreateInter.setFlowDraft(flowCreateEntity.getBussinessId());
//                        flowCreateEntity.setFlowState(FlowStateEnum.SENDBACK.getCode());
//                        flowCreateDao.save(flowCreateEntity);
////                        flowCreateInter.setFlowBack(flowCreateEntity.getBussinessId());
//                    } else {
//                        // 收回时 平台侧的值状态值也为draft  但是801侧应该为处理中
//                        flowCreateEntity.setFlowState(FlowStateEnum.COUNTERSIGNER.getCode());
//                        flowCreateDao.save(flowCreateEntity);
//                    }
//                }
//                break;
                // 平台侧已经是over了，但是801还是进行中说明 流程终止
                // 目前没有移动到监听中是因为触发不了监听事件
//                case "over": { //处理中
////                    log.debug("  over================");
////                    if (FlowStateEnum.COUNTERSIGNER.getCode().equals(flowCreateEntity.getFlowState())) {
////                    flowBomSheetInter.recoverDataState(flowCreateEntity.getBussinessId());
////                    flowCreateInter.setFlowOver(flowCreateEntity.getBussinessId() + "");
////                    }
//                }
//                break; //处理中
//                case "track": { //处理中
//                    log.debug("  track================");
//                    //异常结束
//                    flowBomSheetInter.recoverDataState(flowCreateEntity.getBussinessId());
//                    flowCreateInter.setFlowOver(flowCreateEntity.getBussinessId() + "");
//                }
//                break;
//                default: {
//                }
//                break;
//            }
//            }else {
//                log.debug( "bpm查询实例getBussinessId="+flowCreateEntity.getBussinessId()+"报错!");
//            }
//        });
//    }
}
