package com.meritdata.dam.datapacket.plan.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author fanpeng
 * @Date 2023/4/23
 * @Describe redis操作类
 */
@Component
public class RedisTemplateService {

    @Autowired
    StringRedisTemplate redisTemplate;

    private static final String USER_FLAG_FIX = "tree-";

    /**
     * 判断redis数据是否存在
     *
     * @param userId 用户id
     * @param key    页面标识
     * @return
     */
    public boolean hasKey(String userId, String key) {
        String userKey = USER_FLAG_FIX + userId;
        Boolean hasKey = redisTemplate.hasKey(USER_FLAG_FIX + userId);
        //存在该用户缓存，则获取该缓存数据
        if (null != hasKey && hasKey) {
            String treeAllData = redisTemplate.opsForValue().get(userKey);
            JSONObject jsonObject = JSONObject.parseObject(treeAllData);
            if (null == jsonObject) {
                return false;
            }
            //判断是否存在该节点标识
            return jsonObject.containsKey(key);
        }
        return false;
    }

    /**
     * 获取用户该页面节点下的树结构数据
     *
     * @param userId 用户id
     * @param key    页面标识
     * @return
     */
    public JSONArray getTreeData(String userId, String key) {
        //先获取用户所有缓存数据
        String userKey = USER_FLAG_FIX + userId;
        String treeAllData = redisTemplate.opsForValue().get(userKey);
        //解析需要的节点数据
        JSONObject jsonObject = JSONObject.parseObject(treeAllData);
        if (null == jsonObject) {
            return null;
        }
        return jsonObject.getJSONArray(key);
    }

    /**
     * 设置用户缓存
     *
     * @param userId    用户id
     * @param key       页面标识
     * @param jsonValue json格式的树结构字符串
     * @return
     */
    public void setTreeData(String userId, String key, String jsonValue) {
        String userKey = USER_FLAG_FIX + userId;
        //先判断redis的key存不存在
        Boolean hasKey = redisTemplate.hasKey(userKey);
        //存在即获取
        if (null != hasKey && hasKey) {
            String treeAllData = redisTemplate.opsForValue().get(userKey);
            //解析需要的节点数据
            JSONObject jsonObject = JSONObject.parseObject(treeAllData);
            if (null != jsonObject) {
                //存入新的值
                jsonObject.put(key, jsonValue);
                redisTemplate.opsForValue().set(userKey, jsonObject.toJSONString());
                return;
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(key, jsonValue);
        redisTemplate.opsForValue().set(userKey, jsonObject.toJSONString());
    }
}
