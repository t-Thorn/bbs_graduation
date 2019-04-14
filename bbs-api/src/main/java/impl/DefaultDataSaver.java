package impl;

import interfaces.AbstractDataSaver;

import java.util.concurrent.ConcurrentHashMap;


public abstract class DefaultDataSaver extends AbstractDataSaver {
    public void saveAll() {
        if (hotPointCache == null) {
            throw new NullPointerException();
        }
        ConcurrentHashMap map = (ConcurrentHashMap) hotPointCache.getMap();
        map.forEach((k, v) -> {
            save(k, map.compute(k, null));
        });
    }

    /**
     * 必须利用compute方法实现动态更新
     * 保存到数据库 单条
     *
     * @param id 帖子id
     */
    public abstract void save(Object id, Object hotPoint);
}
