package impl;

import interfaces.HotPoint;

import java.util.concurrent.ConcurrentHashMap;

public class test {
    static ConcurrentHashMap<Integer, HotPoint> map = new ConcurrentHashMap<Integer, HotPoint>();

    public static void main(String[] args) {
        HotPoint hotPoint = new HotPoint(56);
        map.put(1, hotPoint);
        System.out.println(map.computeIfPresent(1, (k, v) -> {
            return new HotPoint();
        }));
    }

    public static void change(HotPoint hotPoint) {
        hotPoint.setTotal(1);
    }
}
