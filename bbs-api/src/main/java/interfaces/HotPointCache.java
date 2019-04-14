package interfaces;

public interface HotPointCache extends HotPostCache<Integer, HotPoint> {
    /**
     * 需要达成原子化更新
     *
     * @param key
     * @param hotPoint 增加的热度
     * @return
     */
    Long createOrUpdate(Integer key, Long hotPoint);

    Object getMap();

    HotPointCache refresh();
}
