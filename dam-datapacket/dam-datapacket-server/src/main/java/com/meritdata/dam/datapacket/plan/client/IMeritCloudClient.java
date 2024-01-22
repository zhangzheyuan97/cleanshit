package com.meritdata.dam.datapacket.plan.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.base.mvc.entity.TreeModel;
import com.meritdata.cloud.config.CloudFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@FeignClient(value = "cloud-system-manage", configuration = CloudFeignConfiguration.class, contextId = "IMeritCloudClient")
public interface IMeritCloudClient {
    /**
     * 根据字典Code获取字典列表信息
     *
     * @param types
     * @return
     */
    @RequestMapping(value = "api/basic/lookup/lookupsEnable", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<TreeModel<Object>> listLookupsEnable(@RequestParam String types);


    /**
     * 根据员工名称查询员工信息
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "api/dam/userinfo", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    JSONObject userinfo(@RequestParam String name);


    /**
     * 获取机构下的所有人员列表
     * @param empId
     * @return
     */
    @RequestMapping(value = "api/ou/emp/getEmpById", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    JSONObject getEmpById( @RequestParam String empId);


    //根据人员id查询人员列表
    @RequestMapping(value = "api/ou/emp/list-in-id", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    JSONObject getListInIds( @RequestParam List<String> ids);


    /**
     * 获取角色下的所有人员列表
     * @param id
     * @return
     */
    @RequestMapping(value = "api/ou/role/empList", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    JSONObject empList( @RequestParam String id);


    @RequestMapping(value = "api/ou/role/getRoleUser", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    JSONObject getRoleUser( @RequestParam Map<String,String> map);



    @RequestMapping(value = "api/ou/emp/list", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    JSONObject getEmpList( @RequestParam Map<String,String> map);

    /**
     * 查询组织信息-根据组织id
     * @param id
     * @return
     */
    @RequestMapping(value = "api/ou/org/getDeptById", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    JSONObject getDeptById( @RequestParam String id);

}
