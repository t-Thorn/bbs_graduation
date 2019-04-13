package impl;

import annotation.RefreshHotPost;

public class Test {

    @RefreshHotPost(1)
    public void test() {
        System.out.println("hh");
    }
}
