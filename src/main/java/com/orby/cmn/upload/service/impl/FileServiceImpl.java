package com.orby.cmn.upload.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.orby.cmn.upload.service.FileService;
import com.orby.cmn.upload.utils.RedisServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * description:
 *
 * @author: huangJian
 * @create: 2020-11-22
 */
@Service
public class FileServiceImpl implements FileService {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public int save(JSONObject jsonFile) {
        try {
            String fileId = jsonFile.getString("fileKey");
            String shardIndex = jsonFile.getString("shardIndex");
            //JSONArray arr = null;
            //保存当前的分片信息到 redis中 以便 查询断续下标
           /* String value = RedisServer.getValue(RedisServer.jedisIndex_1, RedisServer.FILE_KEY + fileId);
            if(null != value){
                arr = JSONObject.parseArray(value);
            }else{
                arr = new JSONArray();
            }*/
            //arr = new JSONArray();
            //JSONObject obj = new JSONObject();
            //obj.put("shardIndex",shardIndex);//1 当前分片上传成功 1成功，0失败
            //obj.put("status","1");
            //arr.add(obj);

            //RedisServer.setValue(RedisServer.jedisIndex_1,RedisServer.FILE_KEY+fileId,arr.toJSONString());
            boolean b = RedisServer.saveHashToRedis(RedisServer.FILE_KEY + fileId, shardIndex, jsonFile.toJSONString(), RedisServer.jedisIndex_1);
            log.info("保存分片信息结果：{}",b);
            return 1;
        } catch (Exception e) {

        }
        return 0;
    }

    @Override
    public JSONObject getByFileKeyByMaxShardIndex(String fileKey) {
        JSONObject resultObj = null;
        Map<String, String> fileKeyMap = RedisServer.getHashFromRedis(RedisServer.FILE_KEY+fileKey, "", RedisServer.jedisIndex_1);
        if(null != fileKeyMap){
            Set<String> set = fileKeyMap.keySet();
            if(null != set && set.size() > 0){
                resultObj = new JSONObject();
                Object[] obj = set.toArray();
                Arrays.sort(obj);

                String maxKey = obj[obj.length - 1].toString();
                String strValue = fileKeyMap.get(maxKey);
                JSONObject valueObj = JSONObject.parseObject(strValue);
                String fileName = valueObj.getString("name");

                resultObj.put("shardIndex",maxKey);
                resultObj.put("fileName",fileName);
            }
        }
        return resultObj;
    }

    @Override
    public int deleteLocalFile(Integer shardTotal,String basePath) {
        int count = 0;
        log.info("删除分片开始");
        for (int i = 0; i < shardTotal; i++) {
            String filePath = basePath + "." + (i + 1);
            File file = new File(filePath);
            boolean result = false;
            try {
                System.gc();//防止IO流没有关闭 导致无法删除
                result = file.delete();
                if(result){
                    count ++;
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            log.info("删除{}，{}", filePath, result ? "成功" : "失败");
        }
        log.info("删除分片结束");
        return count;
    }
}
