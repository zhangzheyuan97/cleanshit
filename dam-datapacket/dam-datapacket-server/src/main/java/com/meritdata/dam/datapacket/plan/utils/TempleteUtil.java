package com.meritdata.dam.datapacket.plan.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.client.IDatamationsClient;
import com.meritdata.dam.datapacket.plan.manage.entity.client.ModelDataExportParam;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.entity.datamanage.DataOperateDTO;
import com.meritdata.dam.entity.datamanage.ModelDataQueryParamVO;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author： lt.liu
 * 时间：2023/4/6
 * @description:
 **/
@Service
public class TempleteUtil {


    @Autowired
    IDataPacketClient dataPacketClient;

    @Autowired
    IMaintainService iMaintainService;

    @Autowired
    IDatamationsClient iDatamationsClient;

    @Autowired
    IModuleManageService moduleManageService;


    /**
     * @param nodeId    树节点id
     * @param tempId
     * @param code
     * @param name
     * @param tableName
     */
    public List<ModuleManageDto> getALLTempLete(String nodeId, String tempId, String code, String name, String tableName) {
        Map<String, String> map = new HashMap();
        map.put("nodeId", nodeId);
        map.put("tempId", tempId);
        map.put("code", code);
        map.put("name", name);
        map.put("tableName", tableName);
        //查询出所有模板信息
        List<ModuleManageDto> allTempleteList = dataPacketClient.moduleAllTemplete(map);

//        List<ModuleManageDto> templeteModulList = moduleManageService
//                .list("1", "1000000", "", "", "");

        return allTempleteList;
    }

    public List<ModuleManageDto> getALLNewTempLete( String moduleName, String moduleCode, String tableName) {

        List<ModuleManageDto> templeteModulList = moduleManageService
                .list("1", "1000000", "", "", "");

        return templeteModulList;
    }




    /**
     * 根据bom和templete查询所有数据
     *
     * @param bom       实物bom
     * @param modelInfo modelInfo
     * @return
     */
    public List<Map<String, Object>> getALLDataByModelInfoAndBom(String modelInfo, String bom) {
        ModelDataQueryParamVO param = new ModelDataQueryParamVO();
        param.setPage(1);
        param.setRows(100000);
        List<Map<String, Object>> aLLData = iMaintainService.dataListManage(bom, modelInfo, param,"").getData().getRows();
        return aLLData;
    }


    /**
     * 根据bom和templete和ids查询所有数据
     *
     * @param bom      实物bom
     * @param template template
     * @param idList   为空list查询的就是最高版本的数据
     * @return
     */
    public List<Map<String, Object>> getALLDataByModelInfoAndBomIds(String template, String bom, List<String> idList) {
        ModelDataQueryParamVO param = new ModelDataQueryParamVO();
        param.setPage(1);
        param.setRows(100000);
        //list传递未空，查询最高版本
        List<Map<String, Object>> aLLData = iMaintainService.dataListManageByIds(bom, template, param, idList).getData().getRows();

//        iMaintainService.dataListManageByIds(bomSheet.getBomName(), bomSheet.getTemplate(), param, new ArrayList<>()).getData().getRows();
        return aLLData;
    }


    /**
     * 根据bom和templete和ids查询最高版本数据
     *
     * @param bom      实物bom
     * @param template template
     * @return
     */
    public List<Map<String, Object>> getMaxVersion(String template, String bom) {
        List<Map<String, Object>> aLLData = getALLDataByModelInfoAndBomIds(template, bom, new ArrayList<>());
        List<Map<String, Object>> maxVersion = aLLData.stream().filter(mode -> mode.get("S_M_SYS_MAXVERSION").toString().equals("1")).collect(Collectors.toList());
        return maxVersion;
    }

    /**
     * 根据bom和templete和ids查询最高版本状态是编辑的数据
     *
     * @param bom      实物bom
     * @param template template
     * @param idList   为空list查询的就是最高版本的数据
     * @return
     */
    public List<Map<String, Object>> getMaxVersionAndEdit(String template, String bom, List<String> idList) {

        List<Map<String, Object>> maxVersion = getMaxVersion(template, bom);
        return getMaxVersionAndEdit(maxVersion);
    }


    public List<Map<String, Object>> getMaxVersionAndEdit(List<Map<String, Object>> maxVersion) {
        List<Map<String, Object>> edit = maxVersion.stream().filter(mode ->
                ObjectUtils.notEqual(mode.get("S_M_SYS_VERSIONSTATUS"), null) &&
                        mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("2")
                        && (null == mode.get("F_IsApproval") || StringUtils.isEmpty(mode.get("F_IsApproval").toString())))
                .collect(Collectors.toList());
        return edit;
    }


    /**
     * 根据templete名称查询所有数据
     *
     * @param templete 模板名称
     * @return
     */
    public List<String> getALLDataByTempleteName(String templete) {
        Map<String, String> map = new HashMap();
        map.put("nodeId", "");
        map.put("tempId", "");
        map.put("code", "");
        map.put("name", templete);
        map.put("tableName", "");
        //查询出所有模板信息
        List<ModuleManageDto> moduleManageInfoList = dataPacketClient.moduleAllPage(map);
        //查询所有的modelInfo
        List<String> modelInfoList = moduleManageInfoList.stream().map(ModuleManageDto::getModelInfo).distinct().collect(Collectors.toList());
        return modelInfoList;
    }


    /**
     * 根据bom和templete查询最大版本数据
     *
     * @param bom       实物bom
     * @param modelInfo modelInfo
     * @return
     */
    public List<Map<String, Object>> getMaxVersionOfALLDataByModelInfoAndBom(String modelInfo, String bom) {
        List<Map<String, Object>> allDataByModelInfoAndBom = getALLDataByModelInfoAndBom(modelInfo, bom);
        //最高版本数据
        List<Map<String, Object>> maxVersion = allDataByModelInfoAndBom.stream().filter(mode -> mode.get("S_M_SYS_MAXVERSION").toString().equals("1")).collect(Collectors.toList());
        return maxVersion;
    }

    /**
     * 根据bom和templete查询生效的数据
     *
     * @param bom       实物bom
     * @param modelInfo modelInfo
     * @return
     */
    public List<Map<String, Object>> getEffectDataOfALLDataByModelInfoAndBom(String modelInfo, String bom) {
        List<Map<String, Object>> allDataByModelInfoAndBom = getMaxVersionOfALLDataByModelInfoAndBom(modelInfo, bom);
        //最高版本数据
        //最高版本编辑中数据
        List<Map<String, Object>> edit = allDataByModelInfoAndBom.stream().filter(mode -> mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("2")).collect(Collectors.toList());
        return edit;
    }


    /**
     * 判断bom和templete的数据是否存在
     *
     * @param bom       实物bom
     * @param modelInfo modelInfo
     * @return
     */
    public boolean dataIsExitByBomAndTemplete(String modelInfo, String bom) {
        List<Map<String, Object>> allDataByModelInfoAndBom = getALLDataByModelInfoAndBom(modelInfo, bom);
        if (null != allDataByModelInfoAndBom && allDataByModelInfoAndBom.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 修改801自定义数据状态
     * @param F_M_SYS_ID
     * @param F_IsApproval
     * @param ModelId
     * @param S_M_SYS_VERSIONSTATUS
     * @return
     */
    public ResultBody updateModelDataState(String F_M_SYS_ID, String F_IsApproval, String ModelId, String  S_M_SYS_VERSIONSTATUS) {
        DataOperateDTO updateModelData = new DataOperateDTO();
        updateModelData.setModelId(ModelId);
        Map<String, String> F_M_SYS_IDMap = new HashMap<>();
        F_M_SYS_IDMap.put("F_M_SYS_ID", F_M_SYS_ID);
//        if(!StringUtils.isEmpty(S_M_SYS_DATAID)){
//            F_M_SYS_IDMap.put("S_M_SYS_DATAID", S_M_SYS_DATAID);
//        }


//        if (StringUtils.isNotEmpty(S_M_SYS_VERSIONSTATUS)){
//            F_M_SYS_IDMap.put("S_M_SYS_VERSIONSTATUS", S_M_SYS_VERSIONSTATUS);
//        }
        updateModelData.setPrimaryData(F_M_SYS_IDMap);
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("F_IsApproval", F_IsApproval);  //数据版本状态
        updateModelData.setData(dataMap);
        updateModelData.setEffect(false);
        String[] array = {"varchar"};
        updateModelData.setDataArray(array);
        ResultBody resultBody = iDatamationsClient.updateModelData(updateModelData);

        return  resultBody;
    }

    /**
     * 修改中台数据状态
     * @param S_M_SYS_DATAID
     * @param S_M_SYS_VERSION
     * @param ModelId
     * @param F_M_SYS_ID
     * @return
     */
    public ResultBody updateModelDataStateCenter(String S_M_SYS_DATAID, String S_M_SYS_VERSION, String ModelId, String F_M_SYS_ID) {
        //这里要修改bom的代码
        ModelDataExportParam modelDataExportParam = new ModelDataExportParam();
        modelDataExportParam.setModelId(ModelId);
        modelDataExportParam.setMaskFlag(false);
        modelDataExportParam.setOperType("dataManage");
        List<Map<String, Object>> selectGridData = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("F_M_SYS_ID", F_M_SYS_ID);
        map.put("S_M_SYS_DATAID", S_M_SYS_DATAID);
        map.put("S_M_SYS_VERSION",S_M_SYS_VERSION); //数据版本
        map.put("S_M_SYS_VERSIONSTATUS", 2); //数据版本

        //STOP("停用", 0),
        //EFFECT("生效", 1),
        //EDIT("编辑中", 2),
        //AUDIT("审核中", 3),  801 项目自定义审核中字段 F_IsApproval
        //HISTORY("历史", 4);
        selectGridData.add(map);
        modelDataExportParam.setSelectGridData(selectGridData);
        //更新数据仓库数据状态
        ResultBody resultBody = iMaintainService.effectData(modelDataExportParam);
        return resultBody;

//        {"modelId":"01ad4bba82ae43e7851fcd0cddf18b4c",
//                "maskFlag":false,
//                "operType":"dataManage",
//                "selectGridData": [{
//               "F_M_SYS_ID":"dd5b16ac2ba64c2ba8a7aeeb6076fc5c",
//                "S_M_SYS_DATAID":"dd5b16ac2ba64c2ba8a7aeeb6076fc5c",
//                "S_M_SYS_VERSIONSTATUS":2,
//                "S_M_SYS_VERSION":1
//        }
//]
//        }

    }

    /**
     *
     * @param temlateId  模板id
     * @param bomNameIdList bom集合
     */
    public Map<String,List<Map<String, Object>>> findTemplateByBomList(String temlateId, List<String> bomNameIdList,Map<String,List<Map<String, Object>>> map) {

        List<Map<String, Object>> aLLData = getALLDataByModelInfoAndBomIdList(temlateId, bomNameIdList, new ArrayList<>());
        bomNameIdList.forEach(bom ->{

            List<Map<String, Object>> editVersion = aLLData.stream()
                    .filter(mode -> mode.get("S_M_SYS_MAXVERSION").toString().equals("1")
                            && bom.equals(mode.get("F_PhysicalCode").toString()))
                    .filter(mode ->
                            ObjectUtils.notEqual(mode.get("S_M_SYS_VERSIONSTATUS"), null) &&
                                    mode.get("S_M_SYS_VERSIONSTATUS").toString().equals("2")
                                    && (null == mode.get("F_IsApproval") || StringUtils.isEmpty(mode.get("F_IsApproval").toString())))
                    .collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(editVersion)){
                map.put(temlateId+ "_" + bom, editVersion);
            }

        });
        return map;

    }

    /**
     *
     * @param temlateId  模板id
     * @param bomNameIdList bom集合
     */
    public List<Map<String, Object>> findTemplateByBomListAndDataIdList(String temlateId, List<String> bomNameIdList,List<String> dataIds) {
        ArrayList<String> tempList = new ArrayList<String>(dataIds);
        List<Map<String, Object>> aLLData = getALLDataByModelInfoAndBomIdList(temlateId, bomNameIdList, tempList);
        return aLLData;

    }

    private List<Map<String, Object>> getALLDataByModelInfoAndBomIdList(String temlateId, List<String> bomNameIdList, ArrayList<String> idList) {
        ModelDataQueryParamVO param = new ModelDataQueryParamVO();
        param.setPage(1);
        param.setRows(100000);
        //list传递未空，查询最高版本
//        iMaintainService.dataListManageByIds(bom, template, param, idList).getData().getRows();
        List<Map<String, Object>> aLLData = iMaintainService.dataListManageByIds(String.join(",",bomNameIdList), temlateId, param, idList).getData().getRows();

//        iMaintainService.dataListManageByIds(bomSheet.getBomName(), bomSheet.getTemplate(), param, new ArrayList<>()).getData().getRows();
        return aLLData;

    }

//    public static void main(String[] args) {
//        String eName = "";
//        String filename = URLEncoder.encode(eName, "UTF-8");
//    }
}
