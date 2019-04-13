package com.thorn.bbsmain.confugurations.shiro.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;


@Slf4j
public class CustomCacheManager extends AbstractCacheManager {
    /**
     * 必须重写这个，不然只能缓存一种缓存（缓存包括身份认证和权限认证）
     *
     * @param name 缓存名称，用来区别
     * @return
     * @throws CacheException
     */
    @Override
    protected Cache createCache(String name) throws CacheException {
        return new CustomCache(name);
    }

}
