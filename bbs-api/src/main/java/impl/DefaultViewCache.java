package impl;

import interfaces.ViewCache;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultViewCache implements ViewCache {
    private static final ConcurrentHashMap CACHE = new ConcurrentHashMap();

    @Override
    public Object get(Integer integer) {
        return CACHE.get(integer);
    }

    @Override
    public ViewCache refresh() {
        CACHE.clear();
        return this;
    }

    @Override
    public void remove(Integer integer) {
        CACHE.remove(integer);
    }

    @Override
    public Object putIfAbsent(Integer integer, Object o) {
        return CACHE.putIfAbsent(integer, o);
    }
}
