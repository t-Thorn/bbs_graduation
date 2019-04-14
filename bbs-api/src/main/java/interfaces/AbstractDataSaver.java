package interfaces;

public abstract class AbstractDataSaver {
    /**
     * 保存到数据库 fixme 未解决一致性问题   可以用锁，但可能效率底下
     *
     * @param record pid：热度 集合
     */

    protected HotPointCache hotPointCache = null;

    public void setHotPointCache(HotPointCache hotPointCache) {
        this.hotPointCache = hotPointCache;
    }

    public void saveAll() {

    }

    /**
     * 必须利用compute方法实现动态更新
     * 保存到数据库 单条
     *
     * @param id 帖子id
     */
    public abstract void save(Object id, Object hotPoint);
}
