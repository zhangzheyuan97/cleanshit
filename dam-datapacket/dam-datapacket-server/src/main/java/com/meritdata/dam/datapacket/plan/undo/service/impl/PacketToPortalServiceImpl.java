package com.meritdata.dam.datapacket.plan.undo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.bpm.base.dto.UndoDTO;
import com.meritdata.dam.datapacket.plan.manage.entity.FlowBomSheetEntity;
import com.meritdata.dam.datapacket.plan.manage.service.IFlowBomSheetInter;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.undo.entity.ParamVO;
import com.meritdata.dam.datapacket.plan.undo.service.IPacketToPortalService;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import com.meritdata.dam.datapacket.plan.utils.DESUtils;
import com.meritdata.dam.datapacket.plan.utils.DateUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author fanpeng
 * @Date 2023/5/10
 * @Describe 质量数据包待办推送到门户接口实现类
 */
@Service
public class PacketToPortalServiceImpl implements IPacketToPortalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacketToPortalServiceImpl.class);

    @Value("${CLOUD_HOME:cloud_home}")
    private String CLOUD_HOME;

    //异构系统标识
    @Value("${datapacket.sysCode:}")
    private String sysCode;

    //流程类型名称
    @Value("${datapacket.workflowName:}")
    private String workflowName;

    //异构系统名称
    @Value("${datapacket.sysName:}")
    private String sysName;

    //供门户系统单点的地址
    @Value("${datapacket.pcurl:}")
    private String pcurl;

    //门户接收异构系统流程接口地址
    @Value("${datapacket.undoUrl:}")
    private String undoUrl;

    //门户删除异构系统指定人员待办流程接口
    @Value("${datapacket.delUrl:}")
    private String delUrl;


    @Autowired
    private IModuleManageService moduleManageService;

    @Autowired
    private IFlowBomSheetInter flowBomSheetInter;

    @Override
    public void syncUndo(List<UndoDTO> undoList, String undoType) {
        //获取所有模型信息
        List<ModuleManageDto> templeteModulList = moduleManageService
                .list(Constants.PAGE + "", Constants.ROWS + "", "", "", "");
        undoList.forEach(item -> {
            //流程业务id
            String businessId = item.getBusinessId();
            String subject = getTemplateName(businessId, templeteModulList);
            //封装参数信息
            ParamVO paramVO = new ParamVO();
            paramVO.setSyscode(sysCode);
            String secret = (StringUtils.isEmpty(item.getSecret()) || item.getSecret().equals("0")) ? "0" : "1";
            paramVO.setFlowid(secret + "-" + businessId);
            //异构系统名称+待办内容
            paramVO.setRequestname(sysName + subject);
            paramVO.setWorkflowname(workflowName);
            //节点名称
            paramVO.setNodename(item.getTaskName());
            //对用户id进行加密
            String encryptSenderId = DESUtils.getEncryptString(item.getSenderId());
            //对单点详情页地址进行拼接
            String path = pcurl + "?userId=" + encryptSenderId + "&businessId=" + businessId;
            paramVO.setAppurl(path);
            paramVO.setPcurl(path);
            //流程处理状态  用于流程流转的核心字段
            //0：待办
            //2：已办
            //4：办结
            //8：抄送（待阅）
            //dealstatus 状态 0待办 1已经办理
            paramVO.setIsremark(undoType);
            //流程查看状态
            //0：未读
            //1：已读;
            paramVO.setViewtype(item.getIsRead());
            paramVO.setCreator(item.getSenderId());
            String currDateTimeStr = DateUtils.getCurrDateTimeStr();
            paramVO.setCreatedatetime(currDateTimeStr);
            paramVO.setReceiver(item.getReceiverId());
            paramVO.setReceivedatetime(currDateTimeStr);
            //毫秒级时间戳
            paramVO.setReceivets(System.currentTimeMillis() + "");
            //调用接口
            String result = this.sendDataPost(undoUrl, JSON.toJSONString(paramVO));
            if (StringUtils.isEmpty(result)) {
                LOGGER.error("请求待办接口失败！接口地址为：[{}]", undoUrl);
            }
            LOGGER.info("给门户系统同步待办信息成功，同步待办状态为：[" + undoType + "]同步的待办标题为[" + subject + "];接口返回信息为[" + result + "]");
        });
    }

    /**
     * 根据businessId 和所有模型信息,获取待办标题(模型名称,多个用,隔开)
     *
     * @param businessId
     * @param templeteModulList
     */
    private String getTemplateName(String businessId, List<ModuleManageDto> templeteModulList) {
        //根据businessId获取template信息
        List<FlowBomSheetEntity> bomSheetList = flowBomSheetInter.getBomSheetList(businessId, "");
        List<String> modelIds = bomSheetList.stream().map(FlowBomSheetEntity::getTemplate).collect(Collectors.toList());
        //先根据模型id过滤,再根据名称组合为,隔开的
        List<String> templateName = templeteModulList.stream().filter(item -> modelIds.contains(item.getModelInfo())).map(ModuleManageDto::getName).collect(Collectors.toList());
        return StringUtils.join(templateName, ",");
    }

    @Override
    public void deleteUndo(List<UndoDTO> undoList) {
        String delUrl = "";
        String sysCode = "datapacket";
        undoList.forEach(item -> {
            JSONObject param = new JSONObject();
            param.put("syscode", sysCode);
            param.put("flowid", item.getBusinessId());
            param.put("userid", item.getReceiverId());
            //调用接口
            String result = this.sendDataPost(delUrl, param.toJSONString());
            if (StringUtils.isEmpty(result)) {
                LOGGER.error("请求删除异构系统指定人员待办流程接口失败！接口地址为：[{}]", delUrl);
            }
            LOGGER.info("删除异构系统指定人员待办流程成功;接口返回信息为[" + result + "]");
        });
    }

    @Override
    public String sendDataPost(String url, String param) {
        LOGGER.info("调用的接口参数:" + param);
        RestTemplate restTemplate = new RestTemplate();
        //创建请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        //传递json格式参数
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(param, httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        //如果200则请求成功
        if (HttpStatus.OK == responseEntity.getStatusCode()) {
            return responseEntity.getBody();
        }
        return "";
    }
}
