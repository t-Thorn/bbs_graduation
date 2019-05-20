package impl;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultHotPostHandlerTest {

    @Test
    public void updateTopPost() {
        ConcurrentHashMap<Integer, Integer> s = new ConcurrentHashMap();
        System.out.println("s.computeIfPresent(1, (k,v)->0) = " + s.computeIfPresent(1, (k, v) -> 0));
        System.out.println(s.computeIfPresent(1, (k, v) -> 0));
    }
}