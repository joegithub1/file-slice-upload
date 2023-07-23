package com.orby.cmn.upload.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * redis
 */
public class RedisComponent {

    private static Logger log = LoggerFactory.getLogger(RedisComponent.class);

    public static final int jedisIndex_0 = 0;//第0块数据库

    private static JedisSentinelPool pool = null;

    public static Jedis getSentinelInstance() {

        //使用redis环境
        String redisActive = Environment.getProperties().getProperty("redis.active");
        //#是否使用redis集群，1=使用，0未使用
        String useRedisMaster = Environment.getProperties().getProperty("use_redis_master");
        if("1".equals(useRedisMaster)){
            if(pool==null){
                // 建立连接池配置参数
                JedisPoolConfig config = new JedisPoolConfig();
                // 设置最大连接数
                config.setMaxTotal(Integer.parseInt(Environment.getProperties().getProperty(redisActive+"redis.maxActive")));
                // 设置最大阻塞时间，记住是毫秒数milliseconds
                config.setMaxWaitMillis(Integer.parseInt(Environment.getProperties().getProperty(redisActive+"redis.maxWait")));
                // 设置空间连接
                config.setMaxIdle(Integer.parseInt(Environment.getProperties().getProperty(redisActive+"redis.maxIdle")));
                // 设置空间连接
                config.setMinIdle(Integer.parseInt(Environment.getProperties().getProperty(redisActive+"redis.minIdle")));

                String masterName = Environment.getProperties().get(redisActive+"redis.master")+"";

                String[] nodes = Environment.getProperties().getProperty(redisActive+"redis.nodes").split(",");

                //数组-->Set
                Set<String> sentinels = new HashSet<>(Arrays.asList(nodes));

                pool = new JedisSentinelPool(masterName, sentinels, config,100000);
            };
            return pool.getResource();
        }else{
            String address = (String) Environment.getProperties().get(redisActive+".redis.address");
            int port = Integer.valueOf(String.valueOf(Environment.getProperties().get(redisActive+".redis.port")));
            String passWord = (String) Environment.getProperties().get(redisActive+".redis.password");
            Jedis jedis = new Jedis(address,port);
            jedis.auth(passWord);
            return jedis;
        }

    }


    public static void closeJedis(Jedis jedis){
        if(jedis != null){
            jedis.close();
        }
    }

}
