package impl;

import interfaces.HotPoint;
import interfaces.HotPointCache;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultHotPointCache implements HotPointCache {
    private static final ConcurrentHashMap<Integer, HotPoint> CACHE = new ConcurrentHashMap();

    @Override
    public void remove(Integer integer) {
        CACHE.remove(integer);
    }

    @Override
    public Long createOrUpdate(Integer key, Long hotPoint) {
        /**
         * 原子化更新,compute返回的是旧值，所以需要加上hotpoint
         */
        return Optional.ofNullable(CACHE.compute(key, (k, v) -> {
            if (v != null) {
                if (hotPoint == 5l) {
                    v.setReply(v.getReply() + 1);
                    v.setTotal(v.getTotal() + 5);
                } else {
                    v.setView(v.getView() + 1);
                    v.setTotal(v.getTotal() + 1);
                }
                return v;
            }
            HotPoint tmp = new HotPoint();
            if (hotPoint == 5l) {
                tmp.setReply(tmp.getReply() + 1);
                tmp.setTotal(tmp.getTotal() + 5);
            } else {
                tmp.setView(tmp.getView() + 1);
                tmp.setTotal(tmp.getTotal() + 1);
            }
            return tmp;
        })).orElse(new HotPoint()).getTotal() + hotPoint;
    }

    @Override
    public HotPoint get(Integer integer) {
        return CACHE.get(integer);
    }

    @Override
    public Object putIfAbsent(Integer integer, HotPoint hotPoint) {
        return CACHE.putIfAbsent(integer, hotPoint);
    }

    @Override
    public HotPointCache refresh() {
        CACHE.clear();
        return this;
    }

    @Override
    public ConcurrentHashMap<Integer, HotPoint> getMap() {
        return CACHE;
    }
}
