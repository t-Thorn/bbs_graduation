package impl;

import interfaces.HotPostCache;
import interfaces.ViewCache;

public class DefaultViewCache implements ViewCache {
    @Override
    public Integer get(Integer integer) {
        return null;
    }

    @Override
    public void set(Integer integer, Integer integer2) {

    }

    @Override
    public boolean exist(Integer integer) {
        return false;
    }

    @Override
    public HotPostCache refresh() {
        return null;
    }

    @Override
    public void mark(Integer uid, Integer pid) {

    }
}
