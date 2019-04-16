package impl;

import interfaces.ViewCache;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultViewCache implements ViewCache {
    private static final ConcurrentHashMap<String, Integer> CACHE = new ConcurrentHashMap();

    @Override
    public Integer get(String key) {
        return CACHE.get(key);
    }

    @Override
    public ViewCache refresh() {
        CACHE.clear();
        return this;
    }

    @Override
    public void remove(String key) {
        CACHE.remove(key);
    }

    /**
     * 根据帖子id模糊删除 有待优化
     *
     * @param pid
     */
    public void removeLike(int pid) {
        CACHE.forEachKey(3, k -> {
            if (k.endsWith(":" + pid)) {
                CACHE.remove(pid);
            }
        });
    }
    @Override
    public Object putIfAbsent(String key, Integer flag) {
        return CACHE.putIfAbsent(key, flag);
    }
}
