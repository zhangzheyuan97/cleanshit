package com.meritdata.dam.datapacket.plan.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meritdata.cloud.utils.SessionUtils;
import com.meritdata.dam.datapacket.plan.client.IMeritCloudClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author： lt.liu
 * 时间：2023/3/29
 * @description:
 **/


@Service
public class MeritCloudUtil {

    @Autowired
    IMeritCloudClient iMeritCloudClient;

    public String getUserCodeByUserId(String userId) {
        JSONObject userObject = getUserObjectByUserId(userId);
        if (null == userObject) {
            return null;
        }
        return userObject.get("code").toString();
    }


    public String getUserNameByUserId(String userId) {
        JSONObject userObject = getUserObjectByUserId(userId);
        if (null == userObject) {
            return null;
        }
        return userObject.get("name").toString();
    }


    public JSONObject getUserObjectByUserId(String userId) {
        JSONObject dept = iMeritCloudClient.getListInIds(Arrays.asList(userId));
        String dataStr = dept.get("data").toString();
        List<Object> data = (List<Object>) dept.get("data");
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                return JSON.parseObject(JSON.toJSONString(data.get(i)));
            }
        }
        return null;
    }
    public Map<String,JSONObject> getUserObjectByUserIdList(List<String> userIds) {
        Map<String,JSONObject> userMap = new HashMap<>();
        if (CollectionUtil.isEmpty(userIds)){
            return userMap;
        }
        JSONObject dept = iMeritCloudClient.getListInIds(userIds);
        String dataStr = dept.get("data").toString();
        List<Object> data = (List<Object>) dept.get("data");
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                String userId = JSON.parseObject(JSON.toJSONString(data.get(i))).get("id").toString();
                userMap.put(userId,JSON.parseObject(JSON.toJSONString(data.get(i))));
            }
        }
        return userMap;
    }


    public JSONObject getUserInfoByUserName(String userName) {
        JSONObject dept = iMeritCloudClient.userinfo(userName);
        List<Object> data = (List<Object>) dept.get("data");
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                return JSON.parseObject(JSON.toJSONString(data.get(i)));
            }
        }
        return null;
    }

    /**
     * 根据用户的名称获取用户id的列表
     *
     * @param userName
     * @return
     */
    public List<String> getUserInfoListByUserName(String userName) {
        List<String> userIdList = new ArrayList<>();
        JSONObject dept = iMeritCloudClient.userinfo(userName);
        List<Object> data = (List<Object>) dept.get("data");
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                String userId = JSON.parseObject(JSON.toJSONString(data.get(i))).getString("id");
                userIdList.add(userId);
            }
        }
        return userIdList;
    }

    public List<String> getUserCodeByUserIds(List<String> userIds) {
        JSONObject dept = iMeritCloudClient.getListInIds(userIds);

        List<String> codeStr = new ArrayList<>();
        String dataStr = dept.get("data").toString();
        List<JSONObject> data = (List<JSONObject>) dept.get("data");
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                codeStr.add(data.get(i).get("code").toString());
//                data.get(i).get("name").toString()
            }
        }
        return null;
    }


    public String getUserCodeByUserName(String userId) {
        JSONObject userObject = getUserObjectByUserId(userId);
        if (null == userObject) {
            return null;
        }
        return userObject.get("name").toString();
    }


    public List<String> getyUserListByEmpId(String empIds) {
        List<String> IDS = new ArrayList<>();
        try {
            JSONObject depts = iMeritCloudClient.getDeptById(empIds);
            Map<String, String> map = new HashMap<>();
            map.put("id", empIds);
            map.put("rows", "1000000");
            map.put("page", "1");
            map.put("secert", "");
            map.put("nameOrCode", "");
            map.put("dLevelCode", depts.get("dLevelCode").toString());

            JSONObject dept = iMeritCloudClient.getEmpList(map);
            Object data1 = dept.get("data");
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(data1));
            List<JSONObject> data = (List<JSONObject>) jsonObject.get("rows");
            if (data != null && data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    IDS.add(data.get(i).get("userId").toString());
                }
            }
        } catch (Exception ex) {

        }
        return IDS;
    }

    /**
     * 根据机构ID获取人员的信息
     *
     * @param empIds
     * @param userId
     * @return
     */
    public List<String> getEmpIdsByUserId(List<String> empIds, String userId) {
        List<String> empIdList = new ArrayList<>();
        empIds.forEach(empId -> {
            List<String> strings = getyUserListByEmpId(empId);
            if (strings.contains(userId)) {
                empIdList.add(empId);
            }
        });

        List<String> collect = empIdList.stream().distinct().collect(Collectors.toList());

        return collect;

    }

    public List<String> getRoleIdsByUserId(List<String> allRoleIds, String systemId) {
        List<String> roleIdList = new ArrayList<>();
        allRoleIds.forEach(roleId -> {
            List<String> strings = getyUserListByRoleId(roleId);
            if (strings.contains(systemId)) {
                strings.forEach(model -> {
                    roleIdList.add(roleId);
                });

            }
        });
        return roleIdList;
    }

    private List<String> getyUserListByRoleId(String roleId) {
        Map<String, String> map = new HashMap<>();
        map.put("roleId", roleId);
        map.put("rows", "1000000");
        map.put("page", "1");

        JSONObject dept = iMeritCloudClient.getRoleUser(map);
        Object data1 = dept.get("data");

        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(data1));
        List<String> IDS = new ArrayList<>();
        List<JSONObject> data = (List<JSONObject>) jsonObject.get("rows");
        if (data != null && data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                IDS.add(data.get(i).get("id").toString());
            }
        }
        List<String> collect = IDS.stream().distinct().collect(Collectors.toList());
        return collect;
    }

    public String getUserCodeByUserInfo(JSONObject userObject) {
        if (null == userObject) {
            return null;
        }
        return userObject.get("code").toString();
    }
}
