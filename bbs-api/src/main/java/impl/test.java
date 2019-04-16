package impl;

import interfaces.HotPoint;

import java.util.concurrent.ConcurrentHashMap;

public class test {
    static ConcurrentHashMap<Integer, Long> map = new ConcurrentHashMap();

    public static void main(String[] args) {
        HotPoint hotPoint = new HotPoint();
        map.put(1, 1l);
        System.out.println(map.computeIfAbsent(1, (k) -> 2l));
        System.out.println(map.computeIfPresent(1, (k, v) -> 2l));
        System.out.println(map.computeIfPresent(1, (k, v) -> 3l));
        System.out.println(map.compute(2, (k, v) -> 2l));
        System.out.println(map.compute(2, (k, v) -> 3l));
        System.out.println(map.computeIfAbsent(3, (k) -> 2l));
        System.out.println(map.computeIfAbsent(3, (k) -> 3l));
    }

    public static void change(HotPoint hotPoint) {
        hotPoint.setTotal(1);
    }
}
