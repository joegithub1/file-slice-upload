package com.orby.cmn.upload.utils;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * description:
 *
 * @author: huangJian
 * @create: 2020-09-29
 */
public class RedisServer {

    private static Logger log = LoggerFactory.getLogger(RedisServer.class);
    private static RedisServer redisServer = null;
    //文件的ID名
    public static final String FILE_KEY = "FILE_KEYS:";//作为标记上传次数
    //上传成功后的文件唯一标识  如果文件已经上传过了，就不在上传了。
    public static final String SUCCESS_FILE_KEY = "SUCCESS-FILE-ID:";
    /**
     *
     *
     */
    //public static final int jedisIndex_0 = 0;

    /**
     *
     *
     */
    public static final int jedisIndex_1 = 1;


    private RedisServer(){}

    public static RedisServer getInstance(){
        if(redisServer == null){
            redisServer = new RedisServer();
            redisServer.initRedisServer();
        }
        return redisServer;
    }

    private void initRedisServer(){ }


    /**
     * 通过key获取value值
     * @param jedisIndex
     * @param key
     * @return
     */
    public static  String getValue(int jedisIndex,String key){
        Jedis jedis = null;
        try{
            jedis = RedisComponent.getSentinelInstance();
            jedis.select(jedisIndex);
            int s = getKeyTtl(jedisIndex,key);
            log.info(key+"过期时间：："+s);
            String value = jedis.get(key);
//			log.info("获取缓存  value:::"+value);
            return value;
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            RedisComponent.closeJedis(jedis);
        }
        return "";

    }

    /**
     *
     * 描述：过期时间检查
     * @author liuchuanqun
     * @created 2019年9月21日 上午11:01:10
     * @since
     * @param jedisIndex
     * @param key
     * @return
     */
    public static int getKeyTtl(int jedisIndex,String key){
        Jedis jedis = null;
        try{
            jedis = RedisComponent.getSentinelInstance();
            jedis.select(jedisIndex);
            long mil = jedis.ttl(key);
            int s = (int)mil;
            return s;
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }finally {
            RedisComponent.closeJedis(jedis);
        }
        return 0;
    }

    //例子  TRADE:INFO:*
    /**
     * description:
     * @author HuangJian
     * @param perfixKey 删除所有数据 key 前缀
     * @param index redis下标
     * @return void
     * @create 2020-09-04
     */
    public static void deleteKeys(String perfixKey,int index){
        long startTime = System.currentTimeMillis();
        Jedis jedis = null;
        int count = 0;
        try {
            jedis = RedisComponent.getSentinelInstance();
            jedis.select(index);
            Set<String> keys = jedis.keys(perfixKey);
            if(null != keys && keys.size() > 0){
                count = keys.size();
                log.info("删除["+perfixKey+"]中所有的kye,个数:"+keys.size());
                for(String key:keys){
                    jedis.del(key);
                }
            }
        } catch (Exception e) {
            log.error("[deleteKeys] system error ",e);
        }finally {
            RedisComponent.closeJedis(jedis);
        }
        long endTime = System.currentTimeMillis();
        log.info("批量删除redis中["+perfixKey+"]的数据耗时" + (endTime - startTime) + ":ms，删除条数：" + count);
    }
    /**
     * description:  删除单条数据
     * @author HuangJian
     * @param key
     * @param index redis下标
     * @return void
     * @create 2020-09-04
     */
    public static void deleteByOneKey(String key,int index){
        //long startTime = System.currentTimeMillis();
        Jedis jedis = null;
        try {
            jedis = RedisComponent.getSentinelInstance();
            jedis.select(index);
            Boolean exists = jedis.exists(key);
            if(exists){
                jedis.del(key);
            }
        } catch (Exception e) {
            log.error("[deleteKeys] system error ",e);
        }finally {
            RedisComponent.closeJedis(jedis);
        }
        //long endTime = System.currentTimeMillis();
    }

    /**
     * description:  批量新增数据到redis中  key:   value: json字符串
     * @author HuangJian
     * @param data 需要存的数据
     * @param index redis下标
     * @return void
     * @create 2020-09-04
     */
    public static void saveBatchToRedis(Map<String,String> data, int index){
        long startTime = System.currentTimeMillis();
        Jedis jedis = null;
        Pipeline p = null;
        int count = 0;
        try {
            jedis = RedisComponent.getSentinelInstance();
            jedis.select(index);
            p = jedis.pipelined();

            Set<Map.Entry<String, String>> entries = data.entrySet();
            count = entries.size();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String jsonValue = entry.getValue();//json字符串
                p.set(key,jsonValue);
                log.info("批量新增数据到redis中：key["+key+"] values:"+jsonValue);
            }
            p.sync();// 调用syn会关闭管道，所以在调用syn之后就不可以在使用管道了
        } catch (Exception e) {
            log.error("批量新增数据到redis中异常：", e);
        } finally {
            if(p != null){
                try {
                    p.close();
                } catch (IOException e) {
                    log.error("批量新增数据到redis中管道关闭异常：", e.getMessage());
                }
            }
            RedisComponent.closeJedis(jedis);
        }
        long endTime = System.currentTimeMillis();
        log.info("批量新增数据到redis中插入耗时" + (endTime - startTime) + ":ms，插入条数：" + count);
    }
    /**
     * 设置value 有超时
     * @param jedisIndex
     * @param key
     * @return
     */
    public static  String setValue(int jedisIndex,String key,String value){
        Jedis jedis = null;
        try{
            jedis = RedisComponent.getSentinelInstance();
            jedis.select(jedisIndex);
            String res =jedis.set(key,value);
            return res;
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }finally {
            RedisComponent.closeJedis(jedis);
        }
        return "";

    }
    /**
    * description:  
    * @author HuangJian
    * @param key
    * @param field
    * @param value
    * @param index
    * @return boolean
    * @create 2020-11-26
    */
    public static boolean saveHashToRedis(String key,String field,String value, int index){
        //log.info("saveHashToRedis ");
        Jedis jedis= RedisComponent.getSentinelInstance();
        if(jedis==null){
            log.error("获取连接失败");
            return false;
        }
        //log.info("获取连接成功");
        jedis.select(index);
        try {
            jedis.hset(key, field, value);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return false;
        }finally{
            RedisComponent.closeJedis(jedis);
        }
        return true;
    }
    /**
    * description:
    * @author HuangJian
    * @param key
    * @param field
    * @param index
    * @return java.util.Map<java.lang.String,java.lang.String>
    * @create 2020-11-26
    */
    public static Map<String,String> getHashFromRedis(String key,String field,int index){
        Map<String,String> resultMap = null;
        Jedis jedis= RedisComponent.getSentinelInstance();
        if(jedis==null){
            log.error("获取连接失败");
            return resultMap;
        }
        //log.info("获取连接成功");
        jedis.select(index);
        try {
            resultMap = jedis.hgetAll(key);
            return resultMap;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }finally{
            RedisComponent.closeJedis(jedis);
        }
        return resultMap;
    }
}
