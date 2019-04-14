package impl;

import interfaces.HotPoint;

import java.util.concurrent.ConcurrentHashMap;

public class test {
    ConcurrentHashMap<Integer, Long> map = new ConcurrentHashMap();

    public static void main(String[] args) {
        HotPoint hotPoint = new HotPoint();
        change(hotPoint);
        System.out.println(hotPoint.getTotal());

    }

    public static void change(HotPoint hotPoint) {
        hotPoint.setTotal(1);
    }
}
