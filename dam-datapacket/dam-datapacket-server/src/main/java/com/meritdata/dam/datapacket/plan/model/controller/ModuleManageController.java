package com.meritdata.dam.datapacket.plan.model.controller;

import com.meritdata.cloud.base.mvc.entity.GridView;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.cloud.resultmodel.ResultStatus;
import com.meritdata.dam.datapacket.plan.client.IDataPacketClient;
import com.meritdata.dam.datapacket.plan.model.entity.ModulePool;
import com.meritdata.dam.datapacket.plan.model.service.IModuleManageService;
import com.meritdata.dam.datapacket.plan.model.vo.BaseTypeDTO;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleManageDto;
import com.meritdata.dam.datapacket.plan.model.vo.ModuleVerFieldDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/datapacket/manage")
@Api(value = "模板管理api接口", tags = {"模板管理api接口"})
public class ModuleManageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManageController.class);
    private final static String INDEX = "index";
    private final static String PREFIX = "/view";
    @Autowired
    IModuleManageService iModuleManageService;
    /**
     * 数仓建设接口
     */
    @Autowired
    IDataPacketClient dataPacketClient;
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    @Autowired
    IModuleManageService moduleManageService;

    /**
     * 查询模型版本列表
     */
    @ApiOperation(value = "查询模板管理列表", notes = "查询模板管理列表")
    @RequestMapping(value = {"page"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<GridView> list(@ApiParam(name = "page", value = "页数", required = true, type = "string") @RequestParam String page,
                                     @ApiParam(name = "rows", value = "行数", required = true, type = "string") @RequestParam String rows,
                                     @ApiParam(name = "moduleName", value = "模版名称", required = false, type = "string") @RequestParam(required = false) String moduleName,
                                     @ApiParam(name = "moduleCode", value = "模版编码", required = false, type = "string") @RequestParam(required = false) String moduleCode,
                                     @ApiParam(name = "tableName", value = "表名称", required = false, type = "string") @RequestParam(required = false) String tableName) {
        try {
            final String regex = "\\d+";
            Map<String, String> map = new HashMap<>();
            map.put("page", page);
            map.put("rows", rows);
            map.put("name", moduleName);
            map.put("code", moduleCode);
            map.put("tableName", tableName);
            if (StringUtils.isBlank(page) && !page.matches(regex)) {
                String message = "分页参数page 传值有误，page:" + page;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            if (StringUtils.isBlank(rows) && !rows.matches(regex)) {
                String message = "分页参数rows 传值有误，rows:" + rows;
                return ResultBody.failure(ResultStatus.PARAM_IS_BLANK, message);
            }
            List<ModuleManageDto> list = moduleManageService.list(page, rows, moduleName, moduleCode, tableName);
            Long aLong = dataPacketClient.moduleCount(map);
            return ResultBody.success(new GridView<>(list, aLong));
        }catch (Exception e){
            return ResultBody.success(new GridView<>(new ArrayList<>(), 0));
        }

    }


    /**
     * 查询向上汇总的模型版本列表
     */
    @ApiOperation(value = "查询模板管理列表", notes = "查询模板管理列表")
    @RequestMapping(value = "/isup/list", method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<List<ModuleManageDto>> listAndIsUp() {
//        List<ModuleManageDto> list = moduleManageService
//                .list("1", "1000000", "", "", "")
//                .stream()
//                .filter(model -> model.getIsPool().equals("1"))
//                .collect(Collectors.toList());

        List<ModuleManageDto> list = moduleManageService
                .list("1", "1000000", "", "", "")
                .stream()
                .collect(Collectors.toList());
        return ResultBody.success(list);
    }

    /**
     * 查询向上汇总的模型版本列表
     */
    @ApiOperation(value = "查询模板", notes = "查询模板")
    @RequestMapping(value = "/get/model", method = {RequestMethod.POST})
    @ResponseBody
    @ApiImplicitParams(
            @ApiImplicitParam(name = "id", value = "模板id", required = true)
    )
    public ResultBody<ModuleManageDto> module(@RequestParam(required = true, value = "id")  String  id) {
     ModuleManageDto moduleManageDtos = dataPacketClient.moduleVerById(id);
        return ResultBody.success(moduleManageDtos);
    }



    /**
     * 根据模板编码以及属性信息模糊查询模型字段
     */
    @ApiOperation(value = "查询模板字段信息", notes = "查询模板字段信息")
    @RequestMapping(value = {"moduleVerFieldInfo"}, method = {RequestMethod.POST})
//    @ResponseBody
    public ResultBody<GridView> moduleVerFieldInfo(@ApiParam(name = "模板编码", value = "模板编码", required = false, type = "string") @RequestParam(required = false) String code,
                                                   @ApiParam(name = "属性名称", value = "属性名称", required = false, type = "string") @RequestParam(required = false) String busiName,
                                                   @ApiParam(name = "英文名称", value = "英文名称", required = false, type = "string") @RequestParam(required = false) String fieldName,
                                                   @ApiParam(name = "数据类型", value = "数据类型", required = false, type = "string") @RequestParam(required = false) String dataType,
                                                   @ApiParam(name = "长度", value = "长度", required = false, type = "string") @RequestParam(required = false) String length,
                                                   @ApiParam(name = "排序", value = "排序", required = false, type = "string") @RequestParam(required = false) String sortNumber,
                                                   @ApiParam(name = "状态", value = "状态", required = false, type = "string") @RequestParam(required = false) String status,
                                                   @ApiParam(name = "精度", value = "精度", required = false, type = "string") @RequestParam(required = false) String definition) {
        try {
            final String regex = "^[1-9]+[0-9]*$";
            final String regEx = ".*[\\s`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）_——+|{}【】‘；：”“’。，、？\\\\]+.*";
            if (StringUtils.isNotBlank(busiName)) {
                if (busiName.matches(regEx)) {
                    String message = "不能输入非法字符";
                    return ResultBody.failure(ResultStatus.PARAM_TYPE_BIND_ERROR, message);
                }
            }
            if (StringUtils.isNotBlank(fieldName)) {
                if (fieldName.matches(regEx)) {
                    String message = "不能输入非法字符";
                    return ResultBody.failure(ResultStatus.PARAM_TYPE_BIND_ERROR, message);
                }
            }
            if (StringUtils.isNotBlank(dataType)) {
                if (dataType.matches(regEx)) {
                    String message = "不能输入非法字符";
                    return ResultBody.failure(ResultStatus.PARAM_TYPE_BIND_ERROR, message);
                }
            }
            if (StringUtils.isNotBlank(length)) {
                if (!length.matches(regex)) {
                    String message = "请输入正整数";
                    return ResultBody.failure(ResultStatus.PARAM_TYPE_BIND_ERROR, message);
                }
            }
            if (StringUtils.isNotBlank(sortNumber)) {
                if (!sortNumber.matches(regex)) {
                    String message = "请输入正整数";
                    return ResultBody.failure(ResultStatus.PARAM_TYPE_BIND_ERROR, message);
                }
            }
            if (StringUtils.isNotBlank(definition)) {
                if (!definition.matches(regex)) {
                    String message = "请输入正整数";
                    return ResultBody.failure(ResultStatus.PARAM_TYPE_BIND_ERROR, message);
                }
            }
            Map<String, String> map = new HashMap();
            map.put("code", code);
            map.put("busiName", busiName);
            map.put("fieldName", fieldName);
            map.put("dataType", dataType);
            map.put("length", length == null ? "" : length);
            map.put("sortNumber", sortNumber == null ? "" : sortNumber);
            map.put("status", status == null ? "" : status);
            map.put("definition", definition == null ? "" : definition);
            List<ModuleVerFieldDto> moduleVerFieldDtos = moduleManageService.moduleVerFieldInfo(code, busiName, fieldName, dataType, length, sortNumber, status, definition);
            Long aLong = dataPacketClient.moduleVerFieldCount(map);
            return ResultBody.success(new GridView<>(moduleVerFieldDtos, aLong));
        }catch (Exception e){
            return ResultBody.success(new GridView<>(new ArrayList<>(), 0));
        }

//        return ResultBody.success(new GridView<>(moduleManageService.moduleVerFieldInfo(code,busiName,fieldName,dataType,length,sortNumber,status,definition),dataPacketClient.moduleCount(map)));
    }

    /**
     * 根据模板编码以及属性信息模糊查询模型字段
     */
    @ApiOperation(value = "数据类型下拉列表", notes = "数据类型下拉列表")
    @RequestMapping(value = {"moduleVerFieldDataType"}, method = {RequestMethod.POST})
//    @ResponseBody
    public ResultBody<List<BaseTypeDTO>> moduleVerFieldDataType(@ApiParam(name = "模板编码", value = "模板编码", required = false, type = "string") @RequestParam(required = false) String code) {
        try {
            return ResultBody.success(moduleManageService.moduleVerFieldDataType(code));
        }catch (Exception e){
            return ResultBody.success(new ArrayList<>());
        }

    }

    /**
     * 根据模板编码查询
     */
    @ApiOperation(value = "根据模板编码查询模板具体信息", notes = "根据模板编码查询模板具体信息")
    @RequestMapping(value = {"moduleVerFieldById"}, method = {RequestMethod.POST})
//    @ResponseBody
    public ResultBody<GridView> moduleVerFieldById(@RequestParam(value = "code", required = true) String code) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("code", code);
            return ResultBody.success(new GridView<>(moduleManageService.moduleVerFieldById(code), dataPacketClient.moduleCount(map)));
        }catch (Exception e){
            return ResultBody.success(new GridView<>(new ArrayList<>(),0));
        }

    }

    /**
     * 页面跳转
     */
    @RequestMapping("/view/{id}/**")
    public ModelAndView view(@PathVariable String id, HttpServletRequest request, @RequestParam(required = false) Map<String, String> params) {
        ModelAndView mView = new ModelAndView();
        if (INDEX.equals(id)) {
            mView.setViewName("/dam/datapacket/plan/model/manage/" + id);
        } else {
            String uri = request.getRequestURI();
            String suffix = uri.substring(uri.indexOf(PREFIX) + PREFIX.length());
            mView.setViewName("/dam/datapacket/plan/model/manage/view" + suffix);
        }
        mView.addAllObjects(params);
        return mView;
    }

    /**
     * 配置页面查询是否向上汇总
     */
    @ApiOperation(value = "模板管理配置页面具体信息", notes = "模板管理配置页面具体信息")
    @RequestMapping(value = {"moduleIsPoolByCode"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<GridView> moduleIsPoolByCode(@RequestParam(value = "code", required = true) String code) {
        try {
            return ResultBody.success(moduleManageService.moduleIsPoolByCode(code));
        }catch (Exception e){
            return ResultBody.failure("查询失败");
        }

    }

    /**
     * 模板管理配置
     */
    @ApiOperation(value = "模板管理配置", notes = "模板管理配置")
    @RequestMapping(value = {"moduleConfig"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResultBody<GridView> moduleConfig(@RequestParam(value = "code", required = true) String code,
                                             @RequestParam(value = "isPool", required = true) String isPool) {
        try {
            return ResultBody.success(moduleManageService.moduleConfig(code, isPool));
        }catch (Exception e){
            return ResultBody.failure("模板管理配置失败");
        }

    }

}
