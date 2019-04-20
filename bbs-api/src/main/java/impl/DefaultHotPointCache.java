package impl;

import interfaces.HotPoint;
import interfaces.HotPointCache;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultHotPointCache implements HotPointCache {

    private final ConcurrentHashMap<Integer, HotPoint> CACHE = new ConcurrentHashMap<Integer,
            HotPoint>();

    @Override
    public void delReply(int pid) {
        CACHE.computeIfPresent(pid, (k, v) -> {
            if (v.getReply() > 0) {
                v.setReply(v.getReply() - 1);
            }
            v.setReplyIncrement(v.getReplyIncrement() - 1);
            v.setChangNum(v.getChangNum() + 1);
            return v;
        });
    }

    @Override
    public void remove(Integer integer) {
        CACHE.remove(integer);
    }

    @Override
    public Long createOrUpdate(Integer key, Long hotPoint) {
        /**
         * 原子化更新,compute返回的是旧值，所以需要加上hotpoint
         */
        return CACHE.compute(key, (k, v) -> {
            if (v != null) {
                return computeHotPoint(hotPoint, v);
            }
            HotPoint tmp = new HotPoint();
            return computeHotPoint(hotPoint, tmp);
        }).getTotal();
    }

    private HotPoint computeHotPoint(Long hotPoint, HotPoint v) {
        if (hotPoint == 5l) {
            v.setReply(v.getReply() + 1);
            v.setReplyIncrement(v.getReplyIncrement() + 1);
            v.setChangNum(v.getChangNum() + 1);
            v.setTotal(v.getTotal() + 5);
        } else {
            v.setView(v.getView() + 1);
            v.setViewIncrement(v.getViewIncrement() + 1);
            v.setChangNum(v.getChangNum() + 1);
            v.setTotal(v.getTotal() + 1);
        }
        return v;
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
    public Map<Integer, HotPoint> getMap() {
        return Collections.unmodifiableMap(CACHE);
    }

    @Override
    public void reset(int pid) {
        CACHE.computeIfPresent(pid, (k, v) -> {
            v.setViewIncrement(0);
            v.setReplyIncrement(0);
            v.setChangNum(0);
            return v;
        });
    }
}
