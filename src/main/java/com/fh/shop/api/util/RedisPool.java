package com.fh.shop.api.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {

    private RedisPool(){

    }

    private static JedisPool jedisPool;

    private static void intoPool(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(1000);
        jedisPoolConfig.setMinIdle(500);
        jedisPoolConfig.setMaxIdle(500);

        jedisPool=new JedisPool(jedisPoolConfig,"192.168.183.130",6379,100,"qwer");

    }
    static {
        intoPool();
    }

    public static Jedis getResource(){
        return jedisPool.getResource();
    }

}
