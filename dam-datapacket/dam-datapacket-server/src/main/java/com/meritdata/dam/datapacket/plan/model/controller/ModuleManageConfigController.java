package com.meritdata.dam.datapacket.plan.model.controller;


import com.meritdata.dam.datapacket.plan.model.service.IModuleInfoConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@RestController
@RequestMapping("/api/datapacket/manageConfig")
public class ModuleManageConfigController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManageConfigController.class);

    /**
     * 首页
     */
    private final static String INDEX_PATTH = "index";

    /**
     * 模块基础路径
     */
//    private final static String VIEW_PATH_PRE = "/dam/datapacket/plan/model/config/infoconfig/";

    private final static String VIEW_PATH_PRE = "/dam/datapacket/plan/model/config/moduleConfig/";
    /**
     * 视图地址前缀
     */

    private final static String VIEW = "view/";

    @Autowired
    private IModuleInfoConfigService moduleInfoConfigService;

    /**
     * 页面跳转
     */
    @RequestMapping({"/view/{id}"})
    public ModelAndView getPage(@PathVariable String id, HttpServletRequest request) {
        ModelAndView mView = new ModelAndView();
        if (INDEX_PATTH.equals(id)) {
            mView.setViewName(VIEW_PATH_PRE + id);
        } else {
            mView.setViewName(VIEW_PATH_PRE + VIEW + id);
        }
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = (String) paramNames.nextElement();
            mView.addObject(name, request.getParameter(name));
        }
        return mView;
    }

//    /**
//     * 查询模型的具体配置
//     *
//     * @return
//     */
//    @RequestMapping(value = "/column/list", method = {RequestMethod.POST})
//    @ApiOperation(value = "查询模板所有字段", notes = "查询模板所有字段")
//    @ResponseBody
//    public ResultBody getColumnList(@ApiParam(name = "模板编码", value = "模板编码", required = true, type = "string") @RequestParam String moduleCode) {
//        moduleInfoConfigService.getModuleCurate(treeNode, moduleCode);
//        return ResultBody.success();
//    }
}
