package com.thorn.bbsmain.confugurations.shiro.cache;

import com.thorn.bbsmain.utils.JedisUtil;
import com.thorn.bbsmain.utils.shiro.JWTUtil;
import com.thorn.bbsmain.utils.shiro.SerializableUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * 重写Shiro的Cache保存读取
 *
 * @author Wang926454
 * @date 2018/9/4 17:31
 */
@Slf4j
public class CustomCache<K, V> implements Cache<K, V> {

    /**
     * redis-key-前缀-shiro:cache:
     */
    private final static String PREFIX_SHIRO_CACHE_AUTHORIZATION = "shiro:cache:";

    /**
     * 过期时间5小时+10s内随机数 jedis的单位是秒
     */
    private static final Integer EXPIRE_TIME =
            5 * 60 * 60 + ThreadLocalRandom.current().nextInt(10);

    private String midName;

    public CustomCache(String midName) {
        this.midName = midName;
    }

    /**
     * 缓存的key名称获取为shiro:cache:account
     *
     * @param key 退出时得到的不是凭据，是principle，本项目中的实现为user信息
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/4 18:33
     */
    private String getKey(Object key) {

        if (key.toString().contains("email")) {
            return PREFIX_SHIRO_CACHE_AUTHORIZATION + midName + ":" + key.toString().substring(key.toString().indexOf("email=") + 6,
                    key.toString().indexOf(","));
        }
        return PREFIX_SHIRO_CACHE_AUTHORIZATION + midName + ":" + JWTUtil.getEmail(key.toString());
    }

    /**
     * 获取缓存
     */
    @Override
    public Object get(Object key) throws CacheException {
        try {
            if (!JedisUtil.exists(this.getKey(key))) {
                return null;
            }
            return JedisUtil.getObject(this.getKey(key));
        } catch (Exception e) {
            throw new CacheException("redis get失败:" + e.getMessage());
        }

    }

    /**
     * 保存缓存
     */
    @Override
    public Object put(Object key, Object value) throws CacheException {
        // 设置Redis的Shiro缓存
        try {
            return JedisUtil.setObject(this.getKey(key), value, EXPIRE_TIME);
        } catch (Exception e) {
            throw new CacheException("redis put失败:" + e.getMessage());
        }
    }

    /**
     * 移除缓存
     */
    @Override
    public Object remove(Object key) throws CacheException {
        try {
            if (!JedisUtil.exists(this.getKey(key))) {
                return null;
            }
            JedisUtil.delKey(this.getKey(key));
        } catch (Exception e) {
            throw new CacheException("redis移除失败:" + e.getMessage());
        }

        return null;
    }

    /**
     * 清空所有缓存
     */
    @Override
    public void clear() {
        try {
            JedisUtil.getJedis().flushDB();
        } catch (Exception e) {
            log.error("清除缓存失败" + e.getMessage());
        }
    }

    /**
     * 缓存的个数
     */
    @Override
    public int size() {
        try {
            Long size = JedisUtil.getJedis().dbSize();
            return size.intValue();
        } catch (Exception e) {
            log.error("查询缓存个数失败" + e.getMessage());
            return -1;
        }

    }

    /**
     * 获取所有的key
     */
    @Override
    public Set keys() {
        Set<byte[]> keys = null;
        try {
            keys = Objects.requireNonNull(JedisUtil.getJedis()).keys("*".getBytes());
        } catch (Exception e) {
            return null;
        }
        Set<Object> set = new HashSet<Object>();
        for (byte[] bs : keys) {
            try {
                set.add(SerializableUtil.unserializable(bs));
            } catch (Exception e) {
                log.error("反序列化失败：" + e.getMessage());
                return null;
            }
        }
        return set;
    }

    /**
     * 获取所有的value
     */
    @Override
    public Collection values() {
        Set keys = this.keys();
        List<Object> values = new ArrayList<Object>();
        for (Object key : keys) {
            try {
                values.add(JedisUtil.getObject(this.getKey(key)));
            } catch (Exception e) {
                log.error("获取value失败：" + e.getMessage());
                return null;
            }
        }
        return values;
    }
}

