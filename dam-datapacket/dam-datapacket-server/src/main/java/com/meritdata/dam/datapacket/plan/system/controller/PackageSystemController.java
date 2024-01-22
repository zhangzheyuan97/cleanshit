package com.meritdata.dam.datapacket.plan.system.controller;

import com.alibaba.fastjson.JSON;
import com.meritdata.cloud.resultmodel.ResultBody;
import com.meritdata.dam.datapacket.plan.system.entity.PackageSystemEntity;
import com.meritdata.dam.datapacket.plan.system.service.PackageSystemInter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author： lt.liu
 * 时间：2023/3/6
 * @description: 包的流程审批
 **/
@RestController
@RequestMapping("/api/system")
@Api(tags = {"系统管理"})
@Validated
public class PackageSystemController {


    @Autowired
    PackageSystemInter packageSystemInter;


//    @ApiOperation(value = "系统管理-权限管理-单个保存")
//    @RequestMapping(value = "/save", method = RequestMethod.POST)
//    public ResultBody save(@RequestBody PackageSystemEntity entity){
//        packageSystemInter.save(entity);
//        return ResultBody.success();
//    }


    @ApiOperation(value = "系统管理-权限管理-批量保存")
    @RequestMapping(value = "/saveList", method = RequestMethod.POST)
    public  ResultBody saveList(@Valid @RequestBody List<PackageSystemEntity>   entity){
        packageSystemInter.saveList(entity);
        return ResultBody.success();
    }

    @ApiOperation(value = "系统管理-权限管理-模糊搜索")
    @RequestMapping(value = "/findByEntity", method = RequestMethod.POST)
    public ResultBody<List<PackageSystemEntity>> findByEntity(@RequestBody PackageSystemEntity   entity){
        List<PackageSystemEntity> byEntity = packageSystemInter.findByEntity(entity);
        return ResultBody.success(byEntity);
    }
    @ApiOperation(value = "系统管理-权限管理-根据id删除数据")
    @RequestMapping(value = "/deleteById", method = RequestMethod.POST)
    public   ResultBody  deleteById(String id){
        packageSystemInter.deleteById(id);
        return ResultBody.success();
    }
    @ApiOperation(value = "系统管理-权限管理-根据ids删除数据")
    @RequestMapping(value = "/deleteByIds", method = RequestMethod.POST)
    public  ResultBody  deleteByIds(@RequestBody  List<String> ids){
        packageSystemInter.deleteByIds(ids);
       return ResultBody.success();
    }

    @ApiOperation(value = "系统管理-权限管理-findAuthorityDataByEntity")
    @RequestMapping(value = "/findAuthorityDataByEntity", method = RequestMethod.POST)
    public ResultBody findAuthorityDataByEntity(@RequestBody PackageSystemEntity entity) {
        List<PackageSystemEntity> authorityDataByEntity = packageSystemInter.findAuthorityDataByEntity(entity);
        System.out.println(JSON.toJSONString(authorityDataByEntity));
        return ResultBody.success();
    }







}
