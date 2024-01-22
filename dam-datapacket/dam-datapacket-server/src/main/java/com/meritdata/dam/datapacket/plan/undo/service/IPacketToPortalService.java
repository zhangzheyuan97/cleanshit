package com.meritdata.dam.datapacket.plan.undo.service;

import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.bpm.base.dto.UndoDTO;

import java.util.List;

/**
 * @Author fanpeng
 * @Date 2023/5/10
 * @Describe 质量数据包待办推送到门户接口
 */
public interface IPacketToPortalService {

    /**
     * 同步门户待办已办功能
     * @param undoList
     * @param undoType 0待办 2 已办
     * @return
     */
    void syncUndo(List<UndoDTO> undoList,String undoType);

    /**
     * 删档
     * @param undoList
     * @return
     */
    void deleteUndo(List<UndoDTO> undoList);

    /**
     * 发送post请求
     * @param url 请求地址
     * @param param 请求参数 （JSON字符串）
     * @return
     */
    String sendDataPost(String url, String param);
}
