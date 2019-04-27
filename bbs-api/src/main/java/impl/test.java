package impl;

import domain.HotPoint;

import java.util.concurrent.ConcurrentHashMap;

public class test {
    static ConcurrentHashMap<Integer, HotPoint> map = new ConcurrentHashMap<Integer, HotPoint>();

    public static void main(String[] args) {
        HotPoint i = new HotPoint();
        map.put(1, i);
        System.out.println(i.getTotal());
        i.setTotal(1);
        System.out.println(i.getTotal());
        System.out.println(i);
        System.out.println(map.get(1));
        /*String s="4545";
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");

        System.out.println("s = " +  pattern.matcher(s).matches());*/
        /*HotPoint hotPoint = new HotPoint(56);
        map.put(1, hotPoint);
        System.out.println(map.computeIfPresent(1, (k, v) -> {
            return new HotPoint();
        }));*/
    }

    public static void change(HotPoint hotPoint) {
        System.out.println(hotPoint.hashCode());
    }
}
